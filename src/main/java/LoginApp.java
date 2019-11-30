import controllers.LoginController;
import java.io.IOException;
import javafx.application.Application;
import javafx.stage.Stage;

public class LoginApp extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage loginStage) {

        LoginController loginController = null;

        try {
            loginController = new LoginController();
        }
        catch (IOException ex) {
            System.err.println("Could not get login page, please try again later");
            System.exit(-1);
        }

        loginStage.setScene(loginController.getScene());

        loginStage.show();

    }
}
