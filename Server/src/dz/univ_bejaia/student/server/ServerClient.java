package dz.univ_bejaia.student.server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;

public class ServerClient extends Thread {




    private final Socket clientSocket;
    private final Server server;

    private OutputStream outputToClient;
    private InputStream inputFromClient;

    private String login = null;

    public ServerClient(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    public String getLogin() {
        return login;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String str) throws IOException{
        try {
            this.outputToClient.write((str + "\n").getBytes());
        }catch (SocketException ignored){

        }

    }

    private void handleClientSocket() throws IOException {
         inputFromClient = clientSocket.getInputStream();
         outputToClient = clientSocket.getOutputStream();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputFromClient));
        String inputData;
        while ((inputData = bufferedReader.readLine()) != null) {
            String[] keys = inputData.split(" ");
            String _key = keys[0];
            if (_key.equals("quit")) {
                handleLogeOff();
                break;
            } else if (_key.equalsIgnoreCase("login")) {
                handleLogin(outputToClient, keys);
            } else if(_key.equalsIgnoreCase("msg")) {
                handleMessage(keys);
            }else if(_key.equalsIgnoreCase("new")) {
                handleNewUser(keys);
            }else {
                send(Server.ERR_COMMAND);
            }
        }
        closeSocket();
    }

    private void closeSocket() {
        try {
            clientSocket.close();
        } catch (IOException ignore) {

        }
    }

    private void handleLogin(OutputStream outputToClient, String[] keys) throws IOException {
        if (keys.length == 3) {
            String login = keys[1];
            String password = keys[2];

            if (ServerClient.DataBase.login(login, password)) {
                this.login = login;
                System.out.println("logged successfully " + getLogin());
                send(Server.OK_LOGGED_IN);
                onlineCmd();
            } else {
                send(Server.ERR_WRONG_USER_PASS);
                System.out.println("ERROR USERNAME OR PASSWORD\n");

            }
        }
    }

    void onlineCmd() throws IOException {
        List<ServerClient> clientList = server.getClientsList();
        for (ServerClient client : clientList) {
            if (!getLogin().equals(client.getLogin()) && client.getLogin() != null){
                send(Server.ONLINE + " " + client.getLogin());
                client.send(Server.ONLINE + " " + getLogin());
            }
        }
    }

    private void handleLogeOff() throws IOException {
        send(Server.OK_LOGGED_OUT);
        List<ServerClient> clientList = server.getClientsList();
//        server.removeClient(this);
        clientList.remove(this);
        for (ServerClient client : clientList) {
            if (!getLogin().equals(client.getLogin()) && client.getLogin() != null){
                client.send(Server.OFFLINE + " " + getLogin());
                closeSocket();
            }
        }


    }

    private void handleMessage(String[] keys) throws IOException {
        if (keys.length > 1 && keys[0].equalsIgnoreCase("msg")) {
            String sendTo = keys[1];
            StringBuilder text = new StringBuilder(keys[2]);
            for (int i = 3 ; i< keys.length ;i++) {
                text.append(" ");
                text.append(keys[i]);
            }
            List<ServerClient> clientList = server.getClientsList();
            for (ServerClient client : clientList) {
                if (client.getLogin().equals(sendTo)) {
                    String output = Server.MSG + " " + getLogin() + " " + text;
                    client.send(output);
                }
            }
        }
    }

    private void handleNewUser(String[] keys) throws IOException{
        if ((keys.length == 3) && (keys[0].equalsIgnoreCase("new"))){
            String login = keys[1];
            String password = keys[2];
            if (ServerClient.DataBase.addNewUser(login, password)){
                send(Server.OK_NEW_USER);
                return;
            };
        }
        send(Server.ERR_NEW_USER);
    }

    /****************************************************************/

    private static class DataBase {

        private static final String DB_NAME = "USER_DB.db";
        private static final String DB_TABLE = "users";
        public static final int LOGIN_COLUMN = 1;
        public static final int PASSWORD_COLUMN = 2;
        public static final String LOGIN_COLUMN_STRING = "login";
        public static final String PASSWORD_COLUMN_STRING = "password";

        private static final Path path = FileSystems.getDefault().getPath(DB_NAME).toAbsolutePath();
        private static final String CONNECTION = "jdbc:sqlite:" + path;

        private static final String Q_CREATE_TABLE = "CREATE TABLE " + DB_TABLE +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "login VARCHAR(20) NOT NULL, " +
                "password TEXT NOT NULL)";

//    private static String Q ="SELECT * from " + DB_TABLE;

        private static final String Q_INSERT_USER = "INSERT INTO " + DB_TABLE +" (login,password) VALUES (? ,?)";

        private static final String Q_SELECT_USER = "SELECT login FROM " + DB_TABLE + " WHERE login = ? " ;

        public static final String Q_CHECK_LOGIN = "SELECT * FROM users WHERE login = ? AND password = ? ";

        public static final String Q_DELETE_USER = "DELETE FROM users WHERE login = ? AND password = ? ";


        private static Connection connection;
        private static PreparedStatement preparedInsertUser;
        private static PreparedStatement preparedSelectFromUsers;
        private static PreparedStatement preparedLogin;
        private static PreparedStatement preparedDellUser;
//
//        public static void main(String[] args) {
////        System.out.println(newUser("lorvfi", "loféri"));
////        System.out.println(dellUser("lorvfi", "loféri"));
//            System.out.println(login("guest", "guest"));
//
////        System.out.println(getUserQuery("lia"));
////        System.out.println(login("bob", "bob"));
//        }
//
//
//
        private static boolean open() {
            try {
                connection = DriverManager.getConnection(CONNECTION);
                preparedInsertUser = connection.prepareStatement(Q_INSERT_USER);
                preparedSelectFromUsers = connection.prepareStatement(Q_SELECT_USER);
                preparedLogin = connection.prepareStatement(Q_CHECK_LOGIN, Statement.RETURN_GENERATED_KEYS);
                preparedDellUser = connection.prepareStatement(Q_DELETE_USER);

                return true;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                return false;
            }
        }

        private static void close(){
            try {
                if (preparedLogin != null){
                    preparedLogin.close();
                }
                if (preparedInsertUser != null) {
                    preparedInsertUser.close();
                }
                if (preparedSelectFromUsers != null){
                    preparedSelectFromUsers.close();
                }
                if (connection != null){
                    connection.close();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        public static boolean login(String login, String password){
            if (open()){
                try {
                    preparedLogin.setString(1, login);
                    preparedLogin.setString(2, password);
                    ResultSet resultSet = preparedLogin.executeQuery();
                    int i = 0;
                    while (resultSet.next()){
                        i++;
                    }
                    return i == 1 ;
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }finally {
                    close();
                }
            }
            return false;
        }

        public static void createNewDatabase() throws SQLException{
            if (open()){
                Statement statement = connection.createStatement();
                statement.execute(Q_CREATE_TABLE);
                close();
            }

        }

        public static boolean getUserQuery(String login){
            if (open()){
                try {
                    preparedSelectFromUsers.setString(1, login);
                    ResultSet resultSet = preparedSelectFromUsers.executeQuery();
                    if (resultSet.next()){
                        return true;
                    }
                }catch (SQLException e){
                    e.printStackTrace();
                }finally {
                    close();
                }
            }
            return false;
        }

        public static boolean addNewUser(String login, String password) {

            if (getUserQuery(login)){
                System.out.println("user her");
                return false;
            }

            if (open()){
                try {
                    preparedInsertUser.setString(1, login);
                    preparedInsertUser.setString(2, password);
                    int rowsAffected = preparedInsertUser.executeUpdate();
                    return rowsAffected != 0;
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    close();
                }
            }
            return false;
        }
        public static boolean dellUser(String login, String password) {

            if (open()){
                try {
                    preparedDellUser.setString(1, login);
                    preparedDellUser.setString(2, password);
                    int rowsAffected = preparedDellUser.executeUpdate();
                    return rowsAffected != 0;
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    close();
                }
            }
            return false;
        }
    }


}
