import client.Client;
import javafx.application.Application;
import javafx.stage.Stage;

public class JavaFXMain extends Application {


    public static final String DEFAULT_BET = "1000";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        var client = new Client(stage);
    }
}
