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

public class LobbyController {

    @FXML
    private ListView<String> listView;

    @FXML
    private Text playerCountText;

    @FXML
    private TextArea textArea;

    @Setter
    private Client client;

    @FXML
    private void leave() {
            client.getMessageWriter().sendLeaveLobbyRequest();
            Platform.runLater(client::prepareLobbyListScene);
    }

    @FXML
    private void sendReady() {
            client.getMessageWriter().sendReady();
    }

    public void updateUsersList(List<String> users) {
        listView.getItems().clear();
        listView.getItems().addAll(users.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList()));
        playerCountText.setText("Players in lobby: " + listView.getItems().size());
    }

    public void showPlayerConnected(String player) {
        textArea.appendText("Player " + player + " has connected \n");
    }

    public void showPlayerDisconnected(String player) {
        textArea.appendText("Player " + player + " has disconnected \n");
    }
}
