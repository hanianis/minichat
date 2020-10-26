package dz.univ_bejaia.student.chat_consol;

import java.io.IOException;
import java.util.Scanner;


public class ChatMain {


    public static void main(String[] args) throws IOException {

        Chat chat = new Chat("localhost", 9096);

        statusListner(chat);

        if (chat.connect()){
            Scanner scanner = new Scanner(System.in);

            while (true){
                System.out.println("SERVER ONLINE");

                System.out.println("sing up ? : yes / no");
                String answer = scanner.nextLine();
                if (answer.equalsIgnoreCase("yes")){

                    System.out.println("please entre a login : ");
                    String login = scanner.nextLine();

                    System.out.println("type your password");
                    String password = scanner.nextLine();
                    chat.singUP(login, password);
                }
                System.out.println("SERVER ONLINE");
                System.out.println("please entre a login : ");
                String login = scanner.nextLine();

                System.out.println("type your password");
                String password = scanner.nextLine();
                while (!chat.login(login , password)){
                    System.out.println("please entre a login : ");
                     login = scanner.nextLine();

                    System.out.println("type your password");
                     password = scanner.nextLine();
                }

                menu(chat);

            }

        }else {
            System.out.println("Filed TO JOIN SERVER");
        }


    }

    private static void statusListner(Chat chat) {
        chat.addactiveStatus(new UserListener() {
            @Override
            public void online(String login) {
                System.out.println("OFFLINE : " + login);
            }

            @Override
            public void offline(String logoff) {
                System.out.println("ONLINE :" + logoff);
            }
            @Override
            public void messaging(String fromLogin, String str){
                System.out.println("message from" + fromLogin + " ---> : " + str);
            }
        });
    }

    public static void menu(Chat chat) throws IOException {
        Scanner scanner = new Scanner(System.in);
        int select = 0;
        switch (select) {
            case 0:
                System.out.println("TAPE 1 - to SEND message \nTAPE 2 - to logoff");
                scanner.next();
                break;
            case 1:
                System.out.println("Send message");
                System.out.println("entre user login to send message :");
                String sendTO = scanner.nextLine();
                System.out.println("TAPE MESSAGE BODY");
                String message = scanner.nextLine();
                chat.msg(sendTO, message);
                break;
            case 2:
                System.out.println("logoff");
                chat.logoff();
                break;
        }
    }
}
