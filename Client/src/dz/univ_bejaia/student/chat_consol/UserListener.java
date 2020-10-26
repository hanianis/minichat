package dz.univ_bejaia.student.chat_consol;

public interface UserListener {

    public void online(String login);
    public void offline(String login);
    public void messaging(String fromLogin, String str);
}
