package model;

import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;

public class Server {
    private final String nickname; // server nickname
    private final Integer portNumber; // server port number

    private ServerSocket server;

    private final ArrayList<SocketController> socketControllers = new ArrayList<>();

    // create server
    public Server(String nickname, Integer portNumber) {
        this.nickname = nickname;
        this.portNumber = portNumber;
        try {
            server = new ServerSocket(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // adds new connection
    // returns client ip
    public byte[] acceptNewClient() {
        try {
            SocketController client = new SocketController(nickname, server.accept());
            socketControllers.add(client);
            return client.getIP();
        } catch (IOException e) {
            return null;
        }
    }

    public String getClientNickname(byte[] ip) {
        for(SocketController item: socketControllers)
            if (Arrays.equals(item.getIP(), ip))
                return item.getInterlocutorNickname();

        return null;
    }

    public void sendData(byte[] ip, byte[] data) {
        for(SocketController item: socketControllers) {
            if (Arrays.equals(item.getIP(), ip)) {
                item.send(data);
                break;
            }
        }
    }

    public void disconnectClient(byte[] ip, boolean sayGoodbye) {
        for(SocketController item: socketControllers) {
            if (Arrays.equals(item.getIP(), ip)) {
                if (sayGoodbye) item.sayGoodbye();
                item.stop();
                socketControllers.remove(item);
                break;
            }
        }
    }
    
    public void destroyServer() {
        for(SocketController item: socketControllers) {
            if (item.isClosed()) continue;
            item.sayGoodbye();
            item.stop();
        }
        socketControllers.clear();

        try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}