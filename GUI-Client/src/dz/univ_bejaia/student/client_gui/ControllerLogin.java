package dz.univ_bejaia.student.client_gui;

import dz.univ_bejaia.student.chat_consol.Chat;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class ControllerLogin {

    @FXML
    AnchorPane rootAnchorPane;
    @FXML
    Button login_button;
    @FXML
    TextField login_field;
    @FXML
    TextField password_field;
    @FXML
    Text server_state;
    @FXML
    Label info_label;
        Chat chat = new Chat("localhost", 9096);
    @FXML
    public void initialize()  {
//        login_button.setDisable(true);
        System.out.println(chat.toString());
        try {
            if (chat.connect()){
                server_state.setText("Server Ok");
//                login_button.setDisable(false);
            }else {
                server_state.setFill(Color.RED);
                server_state.setText("Server down");
            }
        } catch (IOException e) {

        }
    }

    @FXML
    public void setLogin_button(ActionEvent event) throws IOException {

        if (chat.login(login_field.getText(), password_field.getText())){
            FXMLLoader loader = new FXMLLoader(getClass().getResource("user_interface.fxml"));
            Scene scene = new Scene(loader.load(), 950,900);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            ControllerUI controllerUI = loader.getController();
            controllerUI.chat = this.chat;

            stage.hide();
            stage.setScene(scene);
            stage.show();
        }else {
            info_label.setTextFill(Color.RED);
            info_label.setText("WRONG USER NAME OR PASSWORD");
        }
    }

    @FXML
    public void setSingUp_button(ActionEvent event) throws IOException {
        if (chat.singUP(login_field.getText(), password_field.getText())) {
            info_label.setTextFill(Color.GREEN);
            info_label.setText("User Created");
        } else {
            info_label.setText("can't add user try later");
        }

    }
}
