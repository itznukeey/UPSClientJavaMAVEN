package controllers;

import client.game.data.Card;
import client.game.data.Rank;
import client.game.data.Suit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import serialization.Fields;
import serialization.TCPData;

public class GameController {

    @FXML
    private GridPane gridPane;

    @FXML
    private TextArea textArea;

    private Map<String, PlayerCellController> playerCellMap;

    private Integer playerCount;

    public void buildScene(TCPData message) throws IOException {
        playerCellMap = new HashMap<>();
        playerCount = Integer.parseInt(message.valueOf(Fields.PLAYER_COUNT));

        for (var playerNo = 0; playerNo < playerCount; playerNo++) {
            var fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/player-cell.fxml"));
            Parent playerCell = fxmlLoader.load();
            var playerCellController = fxmlLoader.<PlayerCellController>getController();
            playerCellController.setUsername(message.valueOf(Fields.PLAYER + playerNo));

            var cards = getCards(message, playerNo);
            playerCellController.setCardList(cards);
            playerCellController.setTotalScore(message.valueOf(Fields.PLAYER + playerNo + Fields.TOTAL_VALUE));
            playerCellMap.put(playerCellController.getUsername().getText(), playerCellController);
            gridPane.addColumn(playerNo, playerCell);
        }

        var fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/player-cell.fxml"));
        Parent dealerCell = fxmlLoader.load();
        var dealerCellController = fxmlLoader.<PlayerCellController>getController();
        dealerCellController.setUsername("Dealer");
        var cards = getDealerCards(message);
        dealerCellController.setCardList(cards);
        gridPane.addColumn(playerCount, dealerCell);
        playerCellMap.put(dealerCellController.getUsername().getText(), dealerCellController);
    }

    private List<Card> getCards(TCPData message, int i) {
        var cardNo = 0;
        var cards = new ArrayList<Card>();
        while (true) {
            var card = message.valueOf(Fields.PLAYER + i + Fields.CARD + cardNo);
            if (card == null) {
                break;
            }

            String[] cardProperties = card.split(";");
            cards.add(new Card(Suit.valueOf(cardProperties[0]), Rank.valueOf(cardProperties[1]), true));
            cardNo++;
        }
        return cards;
    }

    private List<Card> getDealerCards(TCPData message) {
        var cards = new ArrayList<Card>();
        var hiddenCard = message.valueOf(Fields.DEALER + Fields.CARD + 0);
        String[] cardProperties = hiddenCard.split(";");
        cards.add(new Card(Suit.valueOf(cardProperties[0]), Rank.valueOf(cardProperties[1]), false));

        var cardNo = 1;
        while (true) {
            var card = message.valueOf(Fields.DEALER + Fields.CARD + cardNo);
            if (card == null) {
                break;
            }

            cardProperties = card.split(";");
            cards.add(new Card(Suit.valueOf(cardProperties[0]), Rank.valueOf(cardProperties[1]), true));
            cardNo++;
        }
        return cards;
    }

    public void updateData(TCPData message) {
        playerCount = Integer.parseInt(message.valueOf(Fields.PLAYER_COUNT));

        for (var playerNo = 0; playerNo < playerCount; playerNo++) {
            var controller = playerCellMap.get(message.valueOf(Fields.PLAYER + playerNo));
            var cards = getCards(message, playerNo);
            controller.setCardList(cards);
            controller.setTotalScore(message.valueOf(Fields.PLAYER + playerNo + Fields.TOTAL_VALUE));
        }

        var dealerController = playerCellMap.get(message.valueOf("Dealer"));
        var cards = getDealerCards(message);
        dealerController.setCardList(cards);
    }
}
