package controllers;

import client.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import lombok.Setter;

public class LobbyController {

    @FXML
    private ListView<String> listView;

    @FXML
    private Button leaveButton;

    @FXML
    private Button voteStartButton;

    @FXML
    private Text playerCountText;

    @FXML
    private TextFlow textFlow;

    @Setter
    private Stage stage;

    @Setter
    private Client client;


}
