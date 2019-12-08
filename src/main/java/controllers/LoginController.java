package controllers;

import client.Client;
import java.io.IOException;
import java.util.Objects;
import java.util.regex.Pattern;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import lombok.Getter;

public class LoginController {

    private static final int MAX_USERNAME_LENGTH = 10;

    @FXML
    private TextField loginField;

    @FXML
    private TextField addressField;

    @FXML
    private Text errorText;

    @FXML
    private Button loginButton;

    private boolean isClientJoined = false;

    @Getter
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

        try {
            String[] address = addressField.getText().split(":");
            this.client = new Client(address[0], Integer.parseInt(address[1]));
            client.connect();
            errorText.setText("Trying to validate user...");

            if (client.validate(loginField.getText())) {
                isClientJoined = true;
            }

        } catch (Exception ex) {
            errorText.setText("Server is unreachable, please try again later.");
            try {
                client.closeConnection();
            } catch (IOException ioex) {
                errorText.setText("Could not close connection");
            }
        }
    }
}
