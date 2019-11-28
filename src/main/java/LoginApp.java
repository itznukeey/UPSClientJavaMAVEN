import javafx.application.Application;
import javafx.stage.Stage;
import scenes.LoginController;

public class LoginApp extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage loginStage) {

        var loginController = LoginController.getLoginScene();

        loginStage.setTitle("Blackjack client");
        loginStage.setScene(loginController.getScene());

        var loginButton = loginController.getLoginButton();
        var loginTextField = loginController.getLoginTextField();
        var serverInfo = loginController.getServerInfo();

        loginStage.show();


    }
}
