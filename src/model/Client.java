package model;

import javax.swing.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
    private SocketController socketController;
	public boolean successfulConnection = true;

    // create client
    public Client(String nickname, String serverIP, int serverPortNumber) {
    	try {
    		Socket socket = new Socket();
    		socket.connect(new InetSocketAddress(serverIP, serverPortNumber), 3000);
    		socketController = new SocketController(nickname, socket);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Connection error");
			successfulConnection = false;
		}
    }

    public boolean isConnected() { return successfulConnection; }

	public String getServerNickname() { return socketController.getInterlocutorNickname(); }

	public void sendData(byte[] data) { socketController.send(data); }

    public void disconnect(boolean sayGoodbye) {
		if (sayGoodbye) socketController.sayGoodbye();
		socketController.stop();
    }
}