package controllers;

import client.Client;
import client.Lobby;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import lombok.Getter;
import lombok.Setter;

/**
 * Controller trida pro scenu s hernimi mistnostmi
 */
public class LobbiesController {

    /**
     * Reference na klienta
     */
    @Getter
    @Setter
    private Client client;

    /**
     * ListView s hernimi mistnostmi
     */
    @FXML
    private ListView<Lobby> listView;

    /**
     * Aktualizuje listView - potreba aby bylo synchronizovano s vlaknem JavaFX sceny
     *
     * @param lobbies seznam vsech hernich mistnosti
     */
    public void updateListView(List<Lobby> lobbies) {
        listView.getItems().clear();
        listView.getItems().addAll(lobbies);
    }

    /**
     * Odesle pozadavek pro pripojeni se do lobby
     */
    @FXML
    private void joinLobby() {
        client.getMessageWriter().sendJoinLobbyRequest(listView.getSelectionModel().getSelectedItem());
    }

    /**
     * Odesle pozadavek pro aktualizaci hernich mistnosti
     */
    @FXML
    private void refresh() {
        client.getMessageWriter().sendLobbyListUpdateRequest();
    }

    /**
     * Odpoji se ze serveru
     */
    @FXML
    private void disconnect() {
        client.disconnect();
    }

}
