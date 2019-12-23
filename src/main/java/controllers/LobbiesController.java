package controllers;

import client.Client;
import client.Lobby;
import controllers.concurrency.LobbyListUpdater;
import java.util.List;
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

    @Setter
    private Stage stage;

    @Getter
    private LobbyListUpdater lobbyListUpdater;

    protected void initialize() {
    }

    public synchronized void updateListView(List<Lobby> lobbies) {
        listView.getItems().clear();
        listView.getItems().addAll(lobbies);
    }

    public void start() {
        lobbyListUpdater = new LobbyListUpdater(client);
        new Thread(lobbyListUpdater).start();
    }

    public void closeLobbyListUpdater() {
        lobbyListUpdater.stop();
    }

    @FXML
    private void joinLobby() {
        var selected = listView.getSelectionModel().getSelectedItem();

    }

}
