import client.Client;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import serialization.Constants;

public class JavaFXMain extends Application {


    public static final String DEFAULT_BET = "1000";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        // var client = new Client(stage);

        var dialog = new TextInputDialog(DEFAULT_BET);
        var validationButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        var input = dialog.getEditor();

        validationButton.addEventFilter(ActionEvent.ACTION, filter -> {
            if (!input.getCharacters().toString().matches("\\d+")) {
                filter.consume();
                return;
            }

            var value = Integer.parseInt(input.getCharacters().toString());

            if (!(value >= Constants.MIN_VALUE_BET && value < Constants.MAX_VALUE_BET)) {
                filter.consume();
            }
        });

        dialog.setTitle("Game will start soon");
        dialog.setHeaderText("Confirm your participation, place a bet (minimum 100, maximum 10k)");
        dialog.setContentText("Press OK to confirm your bet");
        dialog.showAndWait().ifPresent(e -> System.out.println("gsdknfdgojgfdoj"));

    }
}
