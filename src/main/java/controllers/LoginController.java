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

    /**
     * Pattern pro zjisteni zda-li je string formatu ip adresa:port
     * Muze byt i localhost:port
     */
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^"
            + "(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}"
            + "|"
            + "localhost"
            + "|"
            + "(([0-9]{1,3}\\.){3})[0-9]{1,3})"
            + ":"
            + "[0-9]{1,5}$");

    private static final Pattern LOGIN_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

    @FXML
    protected void loginEvent() {
        if (loginField.getText() == null || loginField.getText().isEmpty()) {
            errorText.setText("Please enter username");
            return;
        }

        if (loginField.getText().length() > MAX_USERNAME_LENGTH) {
            errorText.setText("Please use shorter username (max " + MAX_USERNAME_LENGTH + ") letters");
            return;
        }

        if (loginField.getText().equals("Dealer")) {
            errorText.setText("Error name \"Dealer\" is forbidden, please select another one");
            return;
        }

        if (!LOGIN_PATTERN.matcher(loginField.getText()).matches()) {
            errorText.setText("Error forbidden symbol, please be sure to only use alphabetic and/or numeric symbols");
            return;
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
}
