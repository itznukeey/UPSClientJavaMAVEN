import client.Client;
import javafx.application.Application;
import javafx.stage.Stage;

public class JavaFXMain extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        //Novy konstruktor Client spusti cely program
        new Client(stage);
    }
}