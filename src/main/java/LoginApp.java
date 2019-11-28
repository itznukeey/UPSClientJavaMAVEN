import client.Client;
import java.io.IOException;
import javafx.application.Application;
import javafx.stage.Stage;
import scenes.LoginSceneWrapper;

public class LoginApp extends Application {

    private static String[] ARGS;

    public static void main(String[] args) {
        ARGS = args;
        launch(args);
    }

    @Override
    public void start(Stage loginStage) {

        if (ARGS.length < 2) {
            System.err.println("Error, not enough arguments provided, be sure to provide ip and port");
            System.exit(-1); //TODO ERROR CODES
        }

        int port = -1;

        try {
            port = Integer.parseInt(ARGS[1]);
        } catch (NumberFormatException ex) {
            System.err.println("Error provided port is not a number!");
            System.exit(-1); //TODO ERROR CODES
        }

        var client = new Client(ARGS[0], port);
        var loginSceneWrapper = LoginSceneWrapper.getLoginScene();

        loginStage.setTitle("Blackjack client");
        loginStage.setScene(loginSceneWrapper.getScene());

        var loginButton = loginSceneWrapper.getLoginButton();
        var loginTextField = loginSceneWrapper.getTextField();

        loginButton.setOnAction(event -> {
            if (!loginTextField.getText().isEmpty()) {
                try {
                    client.connect();
                } catch (IOException e) {
                    loginSceneWrapper.showCouldNotConnect();
                    return;
                }

                if (client.validate()) {
                    new MainStage(client).run();
                }

                loginSceneWrapper.showUserAlreadyConnected();
            } else {
                loginSceneWrapper.showUsernameIsEmpty();
            }
        });

        loginStage.show();


    }
}
