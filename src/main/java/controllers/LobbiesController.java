package controllers;

import client.Client;
import client.Lobby;
import client.LobbyList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import lombok.Setter;

public class LobbiesController {

    @Setter
    private Client client;

    @FXML
    private ListView<Lobby> listView;

    @FXML
    private Button joinLobbyButton;

    @Setter
    private Stage stage;

    protected void initialize() {
    }

    private void mapLobbies(LobbyList lobbyList) {
        listView.getItems().clear();
        listView.getItems().addAll(lobbyList.getLobbies());
    }

    @FXML
    private void joinLobby() {
        var selected = listView.getSelectionModel().getSelectedItem();

    }

}
