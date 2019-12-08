package client;

import controllers.LobbiesController;
import controllers.LoginController;
import java.io.IOException;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class LobbiesScene {

    private static final Logger LOGGER = Logger.getLogger(LobbiesScene.class.getName());

    public void start(Stage mainStage, Client client) {
        try {
            var fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/lobbies.fxml"));
            Parent loginRoot = fxmlLoader.load();
            var lobbiesController = fxmlLoader.<LobbiesController>getController();

            client.getLobbyList();
            lobbiesController.mapLobbies(client.getData().getLobbyList());
        }
        catch (IOException ex) {
            LOGGER.severe("Could not load lobbies menu, exiting");
            System.exit(-1);
        }
    }
}
