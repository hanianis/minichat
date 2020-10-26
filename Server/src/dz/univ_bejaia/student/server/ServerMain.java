package dz.univ_bejaia.student.server;


public class ServerMain {
    public static void main(String[] args) {
        int port = 9096;
        Server server = new Server(port);
        server.start();
    }


}
