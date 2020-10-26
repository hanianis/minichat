package dz.univ_bejaia.student.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server  extends Thread{
    //CMD
     public static final String MSG = "MSG";
    //MY ERROR CODES
    public static final String ERR = "ERR-";
    public static final String OK = "OK-";
    public static final String ERR_CONNECTION_SERVER = ERR + "S00";
    public static final String ERR_COMMAND = ERR + "C400";
    public static final String ERR_WRONG_USER_PASS = ERR + "C402";
    public static final String ONLINE =  "ONLINE";
    public static final String OFFLINE = "OFFLINE";
    public static final String OK_LOGGED_IN = OK + "1-LOG";
    public static final String OK_LOGGED_OUT = OK + "0-LOG";
    public static final String OK_NEW_USER = OK + "NU";
    public static final String ERR_NEW_USER = ERR + "NU";

    private int port;
    ServerSocket serverSocket ;
    private List<ServerClient> clientsList = new ArrayList<>();

    public List<ServerClient> getClientsList() {
        return clientsList;
    }

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
             serverSocket = new ServerSocket(port);
             System.out.println("Waite For Connextion ...");
            while (true){
                Socket clientSocket = serverSocket.accept();
                ServerClient serverClient = new ServerClient(clientSocket, this);
                clientsList.add(serverClient);
                serverClient.start();
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void removeClient(ServerClient client){
        clientsList.remove(client);
    }
    
}
