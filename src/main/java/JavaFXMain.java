import controllers.LoginController;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JavaFXMain extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        try {
            var fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent loginRoot = fxmlLoader.load();
            var loginController = fxmlLoader.<LoginController>getController();
            stage.setScene(new Scene(loginRoot));
            loginController.setStage(stage);

        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.show();

    }
}
