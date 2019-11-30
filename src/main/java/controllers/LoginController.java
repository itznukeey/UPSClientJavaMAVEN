package controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import lombok.Getter;

public class LoginController {

    @Getter
    @FXML
    private JFXButton loginButton;

    @FXML
    private Text errorText;

    @Getter
    @FXML
    private JFXTextField usernameTextField;

    @Getter
    @FXML
    private JFXTextField ipTextField;

    @Getter
    private Scene scene;

    public LoginController() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/loginscene.fxml"));
        this.scene = new Scene(root);
    }

    public void errorLoginNotUnique() {
        errorText.setText("Error, user is already present, please choose different username");
    }
}
