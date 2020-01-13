package controllers;

import client.Client;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import lombok.Setter;

/**
 * Trida, ktera slouzi k ovladani herni mistnosti
 */
public class LobbyController {

    /**
     * ListView vsech hracu - jejich prezdivek a statusu READY / NOT READY
     */
    @FXML
    private ListView<String> listView;

    /**
     * Text s poctem hracu
     */
    @FXML
    private Text playerCountText;

    /**
     * Log se stavy pripojenych / odpojenych hracu
     */
    @FXML
    private TextArea textArea;

    /**
     * Reference na klienta
     */
    @Setter
    private Client client;

    /**
     * Odpojeni se z lobby - klient posle pozadavek o opusteni lobby a pripravi si scenu se seznamem mistnosti
     */
    @FXML
    private void leave() {
        client.getMessageWriter().sendLeaveLobbyRequest();
        Platform.runLater(client::prepareLobbyListScene);
    }

    /**
     * Odeslani pozadavku, ze je klient pripraven na start hry
     */
    @FXML
    private void sendReady() {
        client.getMessageWriter().sendReady();
    }

    /**
     * Aktualizuje seznam hracu - potreba synchronizovat s vlaknem JavaFX sceny
     *
     * @param users seznam s hraci
     */
    public void updateUsersList(List<String> users) {
        listView.getItems().clear();
        listView.getItems().addAll(users.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList()));
        playerCountText.setText("Players in lobby: " + listView.getItems().size());
    }

    /**
     * Zobrazi, ze se hrac pripojil do lobby
     *
     * @param player hrac, ktery se pripojil
     */
    public void showPlayerConnected(String player) {
        textArea.appendText("Player " + player + " has connected \n");
    }

    /**
     * Zobrazi, ze hrac odesel z lobby
     *
     * @param player hrac, ktery odesel z lobby
     */
    public void showPlayerDisconnected(String player) {
        textArea.appendText("Player " + player + " has disconnected \n");
    }
}
