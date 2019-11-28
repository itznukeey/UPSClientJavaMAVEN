package scenes;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class LoginSceneWrapper {

    @Getter
    private Scene scene;

    @Getter
    private Button loginButton;

    @Getter
    private TextField textField;

    private Label response;

    public static LoginSceneWrapper getLoginScene() {
        return new LoginSceneWrapper();
    }

    private LoginSceneWrapper() {
        var root = new VBox();
        var welcomeMessage = new Label("Please enter your desired username");
        this.loginButton = new Button("Login");
        this.textField = new TextField();
        this.response = new Label();

        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(welcomeMessage, textField, loginButton, response);
        scene = new Scene(root);
        scene.getStylesheets().add("scenes/stylesheets/fonts.css");
    }

    public void showCouldNotConnect() {
        response.setText("Could not connect");
    }

    public void showUsernameIsEmpty() {
        response.setText("Please enter valid username");
    }

    public void showUserAlreadyConnected() {
        response.setText("Error such user has already joined the server");
    }
}
