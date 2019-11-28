import client.Client;
import javafx.application.Application;
import javafx.stage.Stage;
import scenes.LoginScene;

public class JavaFXApp extends Application {

    private static String[] ARGS;

    public static void main(String[] args) {
        ARGS = args;
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        if (ARGS.length < 2) {
            System.err.println("Error, not enough arguments provided, be sure to provide ip and port");
            System.exit(-1); //TODO ERROR CODES
        }

        int port = -1;

        try {
            port = Integer.parseInt(ARGS[1]);
        }
        catch (NumberFormatException ex) {
            System.err.println("Error provided port is not a number!");
            System.exit(-1); //TODO ERROR CODES
        }

        var client = new Client(ARGS[0], port);

        primaryStage.setTitle("Blackjack client");
        primaryStage.setScene(LoginScene.getLoginScene().getScene());
        primaryStage.show();
    }
}
