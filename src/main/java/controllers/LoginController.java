package controllers;

import client.Client;
import java.util.regex.Pattern;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.Setter;

public class LoginController {

    private static final int MAX_USERNAME_LENGTH = 10;

    @FXML
    @Getter
    private TextField loginField;

    @FXML
    @Getter
    private TextField addressField;

    @FXML
    @Getter
    private Text errorText;

    @Setter
    private Client client;

    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^"
            + "(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}"
            + "|"
            + "localhost"
            + "|"
            + "(([0-9]{1,3}\\.){3})[0-9]{1,3})"
            + ":"
            + "[0-9]{1,5}$");


    @FXML
    protected void loginEvent() {
        if (loginField.getText() == null || loginField.getText().isEmpty()) {
            errorText.setText("Please enter username");
            return;
        }

        if (loginField.getText().length() > MAX_USERNAME_LENGTH) {
            errorText.setText("Please use shorter username (max " + MAX_USERNAME_LENGTH + ") letters");
        }

        if (!ADDRESS_PATTERN.matcher(addressField.getText()).matches()) {
            errorText.setText("Error, check address format");
            return;
        }

        client.login();
    }

    public void showServerUnreachable() {
        errorText.setText("Server is unreachable, please try again");
    }

    public void showUsernameNotUnique() {
        errorText.setText("Username already taken");
    }
}
