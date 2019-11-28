package scenes;

import client.Client;
import java.io.IOException;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class LoginController {

    @Getter
    private final Scene scene;

    private final Label response;

    @Getter
    private final Button loginButton;

    @Getter
    private final TextField loginTextField;

    @Getter
    private final TextField serverInfo;

    public static LoginController getLoginScene() {
        return new LoginController();
    }

    private LoginController() {
        var root = new VBox();
        var welcomeMessage = new Label("Please enter your desired username");
        this.loginButton = new Button("Login");
        this.loginTextField = new TextField();
        this.response = new Label();

        var ipSettings = new Label("Please enter ip address and port e.g 192.0.0.1:443");
        this.serverInfo = new TextField("127.0.0.1:443");

        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(welcomeMessage, loginTextField, ipSettings, serverInfo, loginButton, response);
        scene = new Scene(root);
        scene.getStylesheets().add("scenes/stylesheets/fonts.css");

        loginButton.setOnAction(event -> {
            if (!loginTextField.getText().isEmpty()) {

                String[] tokens = serverInfo.getText().split(":");
                if (tokens.length != 2 || !tokens[0].matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")
                        || !tokens[1].matches("^[0-9]*$")) {
                    showCouldNotParseIp();
                    return;
                }

                var client = new Client(tokens[0], Integer.parseInt(tokens[1]));

                try {
                    client.connect();
                } catch (IOException e) {
                   showCouldNotConnect();
                    return;
                }

                if (client.validate()) {
                    //TODO
                }

                showUserAlreadyConnected();
            } else {
                showUsernameIsEmpty();
            }
        });
    }

    /**
     * Ukaze zpravu, ze se klient nemohl pripojit
     */
    public void showCouldNotConnect() {
        response.setText("Could not connect");
    }

    public void showUsernameIsEmpty() {
        response.setText("Please enter valid username");
    }

    public void showUserAlreadyConnected() {
        response.setText("Error such user has already joined the server");
    }

    public void showCouldNotParseIp() {
        response.setText("Could not parse ip address of the server,\n " +
                "be sure to use standard ipv4 format with port. E.g 192.0.0.1:3333");
    }
}
