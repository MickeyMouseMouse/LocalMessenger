package controller;

import model.Client;
import model.Server;
import view.PanelController;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

public class Controller { // singleton
    private static Controller instance;
    public static synchronized Controller getInstance() {
        if (instance == null) instance = new Controller();
        return instance;
    }
    
    private static Server server;
    private static Client client;
    private static boolean nodeType; // true = server, false = client
    private static byte[] interlocutorIP;

    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture<?> getNewClientTask;

    private boolean soundOn = true;

    private String pathToDownloads;

    private final int FILE_NAME_LENGTH = 100; // sending files

    public void createServer(String nickname, String downloads,
                             String serverPortNumber) {
        if (verifyInputData(true, nickname, downloads, null, serverPortNumber)) {
            nodeType = true;
            this.pathToDownloads = downloads;

            server = new Server(nickname, Integer.parseInt(serverPortNumber));
            getNewClientTask = timer.schedule(getNewClient, 0, TimeUnit.SECONDS);
        }
    }

    Runnable getNewClient = new Runnable() {
        @Override
        public void run() {
            interlocutorIP = server.acceptNewClient();
            if (interlocutorIP != null)
                PanelController
                        .getInstance()
                        .setMessagingPanel(server.getClientNickname(interlocutorIP) +
                                " (192.168.0." + interlocutorIP[3] + ")");
        }
    };

    public void destroyServer() { server.destroyServer(); }

    public void createClient(String nickname, String downloads,
                                String serverIP, String serverPortNumber) {
        if (verifyInputData(false, nickname, downloads, serverIP, serverPortNumber)) {
            nodeType = false;
            this.pathToDownloads = downloads;

            client = new Client(nickname, "192.168.0." + serverIP, Integer.parseInt(serverPortNumber));

            if (client.isConnected())
                PanelController.getInstance().setMessagingPanel(client.getServerNickname());
            else
                client = null;
        }
    }
    
    // nodeType: true = server, false = client
    private boolean verifyInputData(boolean nodeType,
                                    String nickname, String downloads,
                                    String serverIP, String serverPortNumber) {
        if (nickname.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Enter nickname");
            return false;
        }

        if (!new File(downloads).isDirectory()) {
            JOptionPane.showMessageDialog(null, "Enter download folder");
            return false;
        }

        try {
            int port = Integer.parseInt(serverPortNumber);
            if (port < 1024) {
                JOptionPane.showMessageDialog(null, "Wrong port");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Wrong port");
            return false;
        }

        if (!nodeType) {
            try {
                int ip = Integer.parseInt(serverIP);
                if (ip < 0 || ip > 255) {
                    JOptionPane.showMessageDialog(null, "Wrong server ip");
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Wrong server ip");
                return false;
            }
        }

    	return true;
    }

    public void terminateSession() {
        if (nodeType) { // true = server
            server.disconnectClient(interlocutorIP, true);
            server.destroyServer();
            server = null;
        } else {
            client.disconnect(true);
            client = null;
        }

        PanelController.getInstance().setStartPanel();
    }
    
    public void sessionCompleted(byte[] ip) {
        if (nodeType) { // server
            server.disconnectClient(ip, false);
            server.destroyServer();
            server = null;
        } else { // client
            client.disconnect(false);
            client = null;
        }

        PanelController.getInstance().setStartPanel();
    }

    // returns status
    public boolean sendString(String str) {
        if (str.matches("^\\s*$")) return false; // empty line check
        byte[] stringBytes = str.getBytes(StandardCharsets.UTF_8);

        byte[] data = new byte[1 + stringBytes.length];
        data[0] = 0; // type: string
        for(int i = 0; i < stringBytes.length; i++)
            data[i + 1] = stringBytes[i];

        send(data);
        return true;
    }

    public void sendSmile(byte id) {
        byte[] data = new byte[2];
        data[0] = 1; // type: smile
        data[1] = id;

        send(data);
    }

    public void sendFile(File file) {
        byte[] fileNameBytes = getSuitableFileName(new StringBuilder(file.getName()));

        try(FileInputStream fis = new FileInputStream(file)) {
            int length = fis.available();

            byte[] data = new byte[1 + FILE_NAME_LENGTH + fis.available()];
            data[0] = 2; // type: file

            // file name
            for(int i = 0; i < FILE_NAME_LENGTH; i++)
                if (i < fileNameBytes.length)
                    data[i + 1] = fileNameBytes[i];
                else
                    data[i + 1] = 0;

            // file content
            for(int i = 0; i < length; i++)
                data[i + FILE_NAME_LENGTH + 1] = (byte) fis.read();

            send(data);
            PanelController.getInstance().addNewMessage("File '" + file.getName() + "' is sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // the file name must be <= FILE_NAME_LENGTH (100) characters
    private byte[] getSuitableFileName(StringBuilder fileName) {
        if (fileName.length() <= FILE_NAME_LENGTH)
            return fileName.toString().getBytes(StandardCharsets.UTF_8);

        StringBuilder fileExtension = new StringBuilder();
        for(int i = fileName.length() - 1; i >= 0; i--) {
            fileExtension.append(fileName.charAt(i));
            if (fileName.charAt(i) == '.' ||
                    fileExtension.length() == 10) break; // 10 = max file extension length
        }

        for(int i = 0; i < fileExtension.length(); i++)
            fileName.setCharAt(FILE_NAME_LENGTH - i - 1,
                    fileExtension.charAt(i));

        return fileName.substring(0, FILE_NAME_LENGTH).getBytes(StandardCharsets.UTF_8);
    }

    private void send(byte[] data) {
        if (nodeType) // server
            server.sendData(interlocutorIP, data);
        else // client
            client.sendData(data);
    }
    
    public void newMessageFromInterlocutor(byte[] message) {
        byte dataType = message[0];
        byte[] data = new byte[message.length - 1];
        for(int i = 1; i < message.length; i++)
            data[i - 1] = message[i];

        switch (dataType) {
            // string
            case (byte) 0 -> PanelController
                    .getInstance()
                    .addNewMessageFromInterlocutor(new String(data, StandardCharsets.UTF_8));

            // smile
            case (byte) 1 -> PanelController
                    .getInstance()
                    .addNewSmile(data[0]);

            // file
            case (byte) 2 -> {
                String fileName = saveFileToDownloads(data);
                PanelController
                        .getInstance()
                        .addNewMessageFromInterlocutor("File: " + fileName);
            }
        }

        if (soundOn) playSound();
    }

    // returns true => On / false => Off
    public boolean soundSwitcher() {
        soundOn = !soundOn;
        return soundOn;
    }

    public void playSound() {
        try {
            AudioInputStream ais = AudioSystem
                    .getAudioInputStream(getClass()
                            .getResource("/resources/messageSound.wav"));
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.setFramePosition(0);
            clip.start();
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // returns name of received file
    public String saveFileToDownloads(byte[] data) {
    	int fileNameLength = 0;
        for (int i = 0; i < FILE_NAME_LENGTH; i++)
            if (data[i] != 0) fileNameLength++; else break;

        byte[] fileNameBytes = new byte[fileNameLength];
        for (int i = 0; i < fileNameLength; i++)
            fileNameBytes[i] = data[i];
        String fileName = getFilePath(new String(fileNameBytes, StandardCharsets.UTF_8));

        File file = new File(fileName);
    	try (FileOutputStream fos = new FileOutputStream(file)) {
    	    for (int i = FILE_NAME_LENGTH; i < data.length; i++)
    	        fos.write(data[i]);
        } catch(IOException e) {
    	    e.printStackTrace();
        }

    	return fileName;
    }

    private String getFilePath(String fileName) {
        StringBuilder result = new StringBuilder(pathToDownloads + File.separator + fileName);

        while (new File(result.toString()).exists())
            result.append("_1");

        return result.toString();
    }
    
    public void appClosing() {
        if (server != null) {
            server.disconnectClient(interlocutorIP, true);
            server.destroyServer();
            return;
        }

        if (client != null) client.disconnect(true);
    }

}