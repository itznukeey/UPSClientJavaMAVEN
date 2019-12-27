package controllers;

import client.Client;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.Setter;

public class LobbyController {

    @FXML
    private ListView<String> listView;

    @FXML
    private Text playerCountText;

    @FXML
    private TextFlow textFlow;

    @Setter
    private Client client;

    @FXML
    private void leave() {
        client.getMessageWriter().sendLeaveLobbyRequest(client.getLobbyId());
        Platform.runLater(client::prepareLobbyListScene);
    }

    @FXML
    private void voteStart() {
        client.getMessageWriter().sendVoteStartRequest();
    }

    public void updateUsersList(List<String> users) {
        listView.getItems().clear();
        listView.getItems().addAll(users.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList()));
        playerCountText.setText("Players in lobby: " + listView.getItems().size());
    }
}
