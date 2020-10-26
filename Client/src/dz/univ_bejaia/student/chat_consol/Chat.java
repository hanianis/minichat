package dz.univ_bejaia.student.chat_consol;

import dz.univ_bejaia.student.server.Server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Chat {
    private final String host;
    private final int port;
    private Socket socket;
    private OutputStream outputServer;
    private InputStream inputServer;
    private  BufferedReader bufferedReader;

    private List<UserListener> activeStatus = new ArrayList<>();


    public Chat(String host, int port) {
        this.host = host;
        this.port = port;
    }


    public boolean connect()  throws IOException{
        try {
             this.socket = new Socket(host, port);
             this.outputServer = socket.getOutputStream();
             this.inputServer = socket.getInputStream();
             this.bufferedReader = new BufferedReader(new InputStreamReader(inputServer));
            return true;
        }catch (IOException e){
//            e.printStackTrace();
            return false;
        }
    }

    public boolean singUP(String login, String password) throws IOException{
        String key = "new " + login + " " + password +"\n";
        outputServer.flush();
        outputServer.write(key.getBytes());
        String response = bufferedReader.readLine();
        if (response.equals(Server.OK_NEW_USER)){
            return true;
        }
        return false;
    }

    public boolean login(String login, String password) throws IOException {
        String key = "login " + login + " " + password +"\n";

        outputServer.flush();
        outputServer.write(key.getBytes());
        String response = bufferedReader.readLine();
        if (response.equals(Server.OK_LOGGED_IN)){
            stratMessageReader();
            return true;
        }
        if (response.equals(Server.ERR_WRONG_USER_PASS)){
            System.err.println("WRONG USERNAME OR PASSWORD");
        }
        return false;
    }



    public boolean logoff() throws IOException {
        String key = "quit";

        outputServer.flush();
        outputServer.write(key.getBytes());
        String response = bufferedReader.readLine();
        if (response.equals(Server.OK_LOGGED_OUT)){
            System.out.println("logoff");
            stratMessageReader();
            return true;
        }
        if (response.equals(Server.ERR_WRONG_USER_PASS)){
            System.err.println("WRONG USERNAME OR PASSWORD");
        }
        return false;
    }

    private void stratMessageReader() {
        Thread thread = new Thread(){
            @Override
            public void run() {
                getEvent();
            }
        };
        thread.start();
    }


    public void addactiveStatus(UserListener listener){
        activeStatus.add(listener);
    }


    public void removeactiveStatus(UserListener listener){
        activeStatus.remove(listener);
    }


    public void getEvent() {
        try {
            String inputData;
            while ((inputData = bufferedReader.readLine()) != null) {
//                System.out.println(inputData);
                String[] keys = inputData.split(" ");
                if (keys.length > 0 ){
                    String key = keys[0];
                    if (key.equals(Server.ONLINE)){
                        handleOffline(keys);
                    } else if (key.equals(Server.OFFLINE)){
                        handleOnline(keys);
                    } else if (key.equals(Server.MSG)){
                            handleMessage(keys);
                    }
                }
            }
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException ignored) {

            }
        }

    }

    private void handleMessage(String[] keys) {
        if (keys.length > 2){
            String login = keys[1];
            StringBuilder bodyMsg = new StringBuilder(keys[2]);
            for (int i = 3; i < keys.length; i++) {
                bodyMsg.append(" ");
                bodyMsg.append(keys[i]);
            }
            for (UserListener listener: activeStatus) {
                listener.messaging(login, bodyMsg.toString());
            }
        }

    }

    private void handleOnline(String[] keys) {
        String login = keys[1];
        for (UserListener listener : activeStatus){
            listener.online(login);
        }
    }

    private void handleOffline(String[] keys) {
        String login = keys[1];
        for (UserListener listener : activeStatus){
            listener.offline(login);
        }
    }


    public String read() throws IOException{
        String line = bufferedReader.readLine() ;
        if (bufferedReader.readLine() == null){
            return null;
        }
        return line;
    }
    public void msg(String sendTo, String str) {
        try {
            outputServer.write(("msg " + sendTo + " " + str + "\n").getBytes());
        }catch (IOException ignored){

        }

    }
}
