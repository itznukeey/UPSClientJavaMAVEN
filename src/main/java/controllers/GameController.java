package controllers;

import client.Client;
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
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import lombok.Setter;
import serialization.Fields;
import serialization.TCPData;
import serialization.Values;

public class GameController {

    private static final String DEALER = "dealer";

    @FXML
    private GridPane gridPane;

    @FXML
    private TextArea textArea;

    @FXML
    private Button hitButton;

    @FXML
    private Button standButton;

    @Setter
    private Boolean canPlay = false;

    @Setter
    private Client client;

    private Map<String, PlayerCellController> playerCellMap;

    private Integer playerCount;


    private void appendText(String text) {
        textArea.appendText(text + "\n");
    }

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
            playerCellMap.put(Fields.PLAYER + playerNo, playerCellController);
            gridPane.addColumn(playerNo, playerCell);
        }

        var fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/player-cell.fxml"));
        Parent dealerCell = fxmlLoader.load();
        var dealerCellController = fxmlLoader.<PlayerCellController>getController();
        dealerCellController.setUsername(DEALER);
        var cards = getDealerCards(message);
        dealerCellController.setCardList(cards);
        gridPane.addColumn(playerCount, dealerCell);
        playerCellMap.put(DEALER, dealerCellController);

        setButtonFunctions();
    }

    private void setButtonFunctions() {
        hitButton.setOnAction(actionEvent -> {
            if (canPlay) {
                client.getMessageWriter().sendHit();
                setCanPlay(false);
            }
        });
        standButton.setOnAction(actionEvent -> {
            if (canPlay) {
                client.getMessageWriter().sendStand();
                setCanPlay(false);
            }
        });
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
            cards.add(new Card(Suit.getSuit(cardProperties[0]), Rank.getRank(cardProperties[1]), true));
            cardNo++;
        }
        return cards;
    }

    private List<Card> getDealerCards(TCPData message) {
        var cards = new ArrayList<Card>();
        var hiddenCard = message.valueOf(Fields.DEALER + Fields.CARD + 0);
        String[] cardProperties = hiddenCard.split(";");
        cards.add(new Card(Suit.getSuit(cardProperties[0]), Rank.getRank(cardProperties[1]), false));

        var cardNo = 1;
        while (true) {
            var card = message.valueOf(Fields.DEALER + Fields.CARD + cardNo);
            if (card == null) {
                break;
            }

            cardProperties = card.split(";");
            cards.add(new Card(Suit.getSuit(cardProperties[0]), Rank.getRank(cardProperties[1]), true));
            cardNo++;
        }
        return cards;
    }

    public void updateData(TCPData message) {
        for (var playerNo = 0; playerNo < playerCount; playerNo++) {
            var controller = playerCellMap.get(Fields.PLAYER + playerNo);
            var cards = getCards(message, playerNo);
            controller.setCardList(cards);
            controller.setTotalScore(message.valueOf(Fields.PLAYER + playerNo + Fields.TOTAL_VALUE));
        }

        var dealerController = playerCellMap.get(DEALER);
        var cards = getDealerCards(message);
        dealerController.setCardList(cards);
    }

    public void showResults(TCPData message) {
        for (var playerNo = 0; playerNo < playerCount; playerNo++) {
            var controller = playerCellMap.get(Fields.PLAYER + playerNo);
            controller.setGameResult(message.valueOf(Fields.PLAYER + playerNo));
        }
        var dealerController = playerCellMap.get(DEALER);
        var cards = new ArrayList<>(dealerController.getCardList().getItems());

        cards.forEach(card -> card.setShow(true));
        dealerController.setCardList(cards);
        dealerController.setTotalScore(message.valueOf(Fields.TOTAL_VALUE));
    }

    public void showTurn(TCPData message) {
        var player = message.valueOf(Fields.USERNAME);
        if (message.valueOf(Fields.TURN_TYPE).equals(Values.STAND)) {
            appendText("Player " + player + " stood.");
        } else {
            String[] cardProperties = message.valueOf(Fields.CARD).split(";");
            var card = new Card(Suit.getSuit(cardProperties[0]), Rank.getRank(cardProperties[1]), true);
            appendText("Player " + player + " hit and got " + card.toString());
        }
    }

}
