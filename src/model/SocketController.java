package model;

import controller.Controller;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class SocketController extends Timer {
    private final Socket socket;

    private String interlocutorNickname;
    private final byte[] interlocutorIP;

    private DataInputStream in;
    private DataOutputStream out;

    private final Cryptographer cryptographer;

    SocketController(String nickname, Socket client) {
        this.socket = client;
        this.interlocutorIP = client.getInetAddress().getAddress();

        try {
            in = new DataInputStream(new BufferedInputStream(client.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()));
        } catch (IOException e) {
            System.out.println("Error: failed get input/output streams (SocketController class)");
        }

        cryptographer = new Cryptographer();
        sayHello(nickname);
        this.scheduleAtFixedRate(getMessageTask, 0, 800);
    }

    // give(get) to(from) interlocutor 1) RSA public key, 2) AES secret key, 3) nickname
    private void sayHello(String nickname) {
        sendWithoutEncode(cryptographer.getPublicKeyBytes());
        cryptographer.setInterlocutorPublicKey(getMessage(true));
        delay(10);

        sendWithoutEncode(cryptographer.getEncodedSecretKey());
        cryptographer.setInterlocutorSecretKey(getMessage(true));
        delay(10);

        send(nickname.getBytes());
        interlocutorNickname = new String(cryptographer.decode(getMessage(true)));
    }

    public void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // message listener (period = 0.8 second)
    TimerTask getMessageTask = new TimerTask() {
        @Override
        public void run() {
            byte[] message = getMessage(false); // read from socket only one time
            if (message == null) return; // there is no new message
            message = cryptographer.decode(message);

            byte[] goodbyeMessage = "terminateCurrentSession".getBytes(StandardCharsets.UTF_8);
            if (Arrays.equals(message, goodbyeMessage))
                Controller.getInstance().sessionCompleted(interlocutorIP);
            else
                Controller.getInstance().newMessageFromInterlocutor(message);
        }
    };

    // receive new message from socket
    // loop == false => 1 attempt
    // loop == true => until the message arrives
    private byte[] getMessage(boolean loop) {
        byte[] data = null;
        do {
            try {
                if (in.available() > 0) {
                    loop = false;

                    data = new byte[in.available()];
                    for (int i = 0; i < data.length; i++)
                        data[i] = in.readByte();
                }
            } catch (IOException e) {
                System.out.println("Error: failed to read bytes from socket (SocketController class)");
            }
        } while (loop);

        return data;
    }

    // returns: true == success / false == fail
    public boolean sendWithoutEncode(byte[] data) {
        try {
            out.write(data);
            out.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // returns: true == success / false == fail
    public boolean send(byte[] data) {
        try {
            out.write(cryptographer.encode(data));
            out.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void sayGoodbye() {
        send("terminateCurrentSession".getBytes(StandardCharsets.UTF_8));
    }

    // destroy current connection
    public void stop() {
        this.cancel();

        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Error: failed to stop the client (SocketController class)");
        }
    }

    public byte[] getIP() { return interlocutorIP; }

    public String getInterlocutorNickname() { return interlocutorNickname; }

    public boolean isClosed() { return socket.isClosed(); }
}