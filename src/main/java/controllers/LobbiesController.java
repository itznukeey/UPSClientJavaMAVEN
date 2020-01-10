package controllers;

import client.Client;
import client.Lobby;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import lombok.Getter;
import lombok.Setter;

public class LobbiesController {

    @Getter
    @Setter
    private Client client;

    @FXML
    private ListView<Lobby> listView;

    public void updateListView(List<Lobby> lobbies) {
        Platform.runLater(() -> {
            listView.getItems().clear();
            listView.getItems().addAll(lobbies);
        });
    }

    @FXML
    private void joinLobby() {
        client.getMessageWriter().sendJoinLobbyRequest(listView.getSelectionModel().getSelectedItem());

    }

    @FXML
    private void refresh() {
        client.getMessageWriter().sendLobbyListUpdateRequest();
    }

    @FXML
    private void disconnect() {
        client.disconnect();
    }

}
