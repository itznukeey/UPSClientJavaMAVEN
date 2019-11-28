import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class LoginScene {

    @Getter
    private Scene scene;

    public static LoginScene getLoginScene() {
        return new LoginScene();
    }

    private LoginScene() {
        var root = new VBox();
        var welcomeMessage = new Label("Please enter your desired username");
        var loginButton = new Button("Login");

        root.setAlignment(Pos.CENTER);
        root.getChildren().add(welcomeMessage);
        root.getChildren().add(loginButton);

        this.scene = new Scene(root);
    }
}
