import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class JavaFXApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Blackjack client");
        primaryStage.show();
        primaryStage.setResizable(false);

        primaryStage.setScene(LoginScene.getLoginScene().getScene());
    }
}
