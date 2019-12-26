package controllers;

import client.Client;
import client.Lobby;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

public class LobbiesController {

    @Getter
    @Setter
    private Client client;

    @FXML
    private ListView<Lobby> listView;

    @FXML
    private Button joinLobbyButton;

    @FXML
    private Button refreshButton;

    @Setter
    private Stage stage;


    protected void initialize() {
    }

    public void updateListView(List<Lobby> lobbies) {
        Platform.runLater(() -> {
            listView.getItems().clear();
            listView.getItems().addAll(lobbies);
        });
    }


    @FXML
    private void joinLobby() {
        var selected = listView.getSelectionModel().getSelectedItem();
        client.getMessageWriter().sendJoinLobbyRequest(selected);
    }

    @FXML
    private void refresh() {
        client.getMessageWriter().sendLobbyUpdateRequest();
    }

}
