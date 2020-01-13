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
import javafx.application.Platform;
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

/**
 * Controller sceny pro hru - obsahuje kod tlacitek a zpracovani zprav z backendu
 */
public class GameController {

    private static final String DEALER = "dealer";

    /**
     * Grid pane s hraci
     */
    @FXML
    private GridPane gridPane;

    /**
     * Log se stavem hry - odpojeni / pripojeni hracu
     */
    @FXML
    private TextArea textArea;

    /**
     * Button pro HIT
     */
    @FXML
    private Button hitButton;

    /**
     * Button pro STAND
     */
    @FXML
    private Button standButton;

    /**
     * Button pro DOUBLE DOWN
     */
    @FXML
    private Button doubleDownButton;

    /**
     * Button pro opusteni hry
     */
    @FXML
    private Button leaveGameButton;

    /**
     * Boolean zda-li hrac muze hrat - pokud neobdrzi od serveru zpravu nemuze hrat
     */
    @Setter
    private Boolean canPlay = false;

    /**
     * Zda-li byla scena jiz postavena - pote jenom aktualizuje data
     */
    @Setter
    private Boolean sceneBuilt = false;

    /**
     * Reference na klienta
     */
    @Setter
    private Client client;

    /**
     * Mapa s jednotlivymi panely hracu
     */
    private Map<String, PlayerCellController> playerCellMap;

    /**
     * Pocet hracu
     */
    private Integer playerCount;

    /**
     * Napise do logu text
     * @param text text, ktery se ma napsat
     */
    public void showMessage(String text) {
        textArea.appendText(text + "\n");
    }

    /**
     * Postavi scenu hry
     * @param message zprava s daty hry
     * @throws IOException exception, pokud by nesly najit fxml soubory
     */
    public void buildScene(TCPData message) throws IOException {
        //Hrace pro snadny pristup ulozime do hashmapy podle jmena - je to unikatni identifikator
        this.playerCellMap = new HashMap<>();
        this.playerCount = Integer.parseInt(message.valueOf(Fields.PLAYER_COUNT));

        //Pro kazdeho hrace vytvorime jeho panel a controller
        for (var playerNo = 0; playerNo < playerCount; playerNo++) {
            var fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/player-cell.fxml"));
            Parent playerCell = fxmlLoader.load();
            var playerCellController = fxmlLoader.<PlayerCellController>getController();

            //Nastavime potrebne udaje pro zobrazeni
            var username = message.valueOf(Fields.PLAYER + playerNo);
            var bet = message.valueOf(Fields.PLAYER + playerNo + Fields.BET);

            playerCellController.getBetResult().setText(bet + " : " + "???");
            if (username.equals(client.getUsername())) {
                username += " (you)";
            }
            playerCellController.setUsername(username);

            var cards = getCards(message, playerNo);
            playerCellController.setCardList(cards);

            var totalScore = message.valueOf(Fields.PLAYER + playerNo + Fields.TOTAL_VALUE);
            String[] hardLightHands = totalScore.split(";");
            playerCellController.setTotalScore(hardLightHands[0] + " : " + hardLightHands[1]);

            playerCellMap.put(Fields.PLAYER + playerNo, playerCellController);
            gridPane.addColumn(playerNo, playerCell);
        }

        //Stejny postup pouzijeme pro dealera
        var fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/player-cell.fxml"));
        Parent dealerCell = fxmlLoader.load();
        var dealerCellController = fxmlLoader.<PlayerCellController>getController();
        dealerCellController.setUsername(DEALER);
        var cards = getDealerCards(message);
        dealerCellController.setCardList(cards);
        dealerCellController.getBetToGain().setText("");

        gridPane.addColumn(playerCount, dealerCell);
        playerCellMap.put(DEALER, dealerCellController);

        setButtonFunctions();
    }

    /**
     * Nastavi funkce vsech tlacitek
     */
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
        doubleDownButton.setOnAction(actionEvent -> {
            if (canPlay) {
                client.getMessageWriter().sendDoubleDown();
                setCanPlay(false);
            }
        });
        leaveGameButton.setOnAction(actionEvent -> {
            client.getMessageWriter().sendLeaveLobbyRequest();
            Platform.runLater(client::prepareLobbyListScene);
        });
    }

    /**
     * Zpracuje zpravu a najde vsechny karty daneho hrace
     *
     * @param message zprava od serveru s daty o hre
     * @param i       index hrace
     * @return seznam vsech karet daneho hrace
     */
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

    /**
     * Najde a vrati karty dealera
     *
     * @param message zprava s daty o hre
     * @return seznam s kartami dealera
     */
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

    /**
     * Aktualizuje data - lze pouzit pouze pro "built" scenu - tzn. inicializovanou metodou {@code buildScene()}
     *
     * @param message
     */
    public void updateData(TCPData message) {
        //Aktualizace se od postaveni sceny lisi pouze tim ze ziska dane hrace a uz nevytvari panely hracu znovu
        for (var playerNo = 0; playerNo < playerCount; playerNo++) {
            var controller = playerCellMap.get(Fields.PLAYER + playerNo);
            var cards = getCards(message, playerNo);
            var bet = message.valueOf(Fields.PLAYER + playerNo + Fields.BET);
            controller.setCardList(cards);
            controller.getBetResult().setText(bet + " : " + "???");

            var totalScore = message.valueOf(Fields.PLAYER + playerNo + Fields.TOTAL_VALUE);
            String[] hardLightHands = totalScore.split(";");
            controller.setTotalScore(hardLightHands[0] + " : " + hardLightHands[1]);
        }

        var dealerController = playerCellMap.get(DEALER);
        var cards = getDealerCards(message);
        dealerController.setCardList(cards);
    }

    /**
     * Zobrazi vysledky
     *
     * @param message zprava s vysledky hry
     */
    public void showResults(TCPData message) {
        for (var playerNo = 0; playerNo < playerCount; playerNo++) {
            var controller = playerCellMap.get(Fields.PLAYER + playerNo);

            var betToGained = message.valueOf(Fields.PLAYER + playerNo + Fields.BET);
            String[] bets = betToGained.split(";");
            controller.getBetResult().setText(bets[0] + " : " + bets[1]);
            controller.setGameResult(message.valueOf(Fields.PLAYER + playerNo).toUpperCase());
        }
        var dealerController = playerCellMap.get(DEALER);
        var cards = new ArrayList<>(dealerController.getCardList().getItems());

        cards.forEach(card -> card.setShow(true));
        dealerController.setCardList(cards);

        var totalScore = message.valueOf(Fields.TOTAL_VALUE);
        String[] hardLightHands = totalScore.split(";");
        dealerController.setTotalScore(hardLightHands[0] + " : " + hardLightHands[1]);
    }

    /**
     * Zobrazi vysledek tahu hrace
     *
     * @param message zprava s vysledkem tahu
     */
    public void showTurn(TCPData message) {
        var player = message.valueOf(Fields.USERNAME);
        switch (message.valueOf(Fields.TURN_TYPE)) {
            case Values.STAND:
                showMessage("Player " + player + " stood");
                return;
            case Values.HIT: {
                String[] cardProperties = message.valueOf(Fields.CARD).split(";");
                var card = new Card(Suit.getSuit(cardProperties[0]), Rank.getRank(cardProperties[1]), true);
                var isBusted = message.valueOf(Fields.RESULT).equals(Values.BUSTED);

                showMessage(isBusted ?
                        "Player " + player + " hit, got " + card.toString() + " and is busted"
                        : "Player" + player + " hit and got " + card.toString());
                break;
            }
            case Values.DOUBLE_DOWN: {
                String[] cardProperties = message.valueOf(Fields.CARD).split(";");
                var card = new Card(Suit.getSuit(cardProperties[0]), Rank.getRank(cardProperties[1]), true);
                var isBusted = message.valueOf(Fields.RESULT).equals(Values.BUSTED);

                showMessage(isBusted ?
                        "Player " + player + " doubled down, got " + card.toString() + " and is busted"
                        : "Player " + player + " doubled down and got " + card.toString());

                break;
            }
        }
    }

    /**
     * Pro zjisteni, zda-li je scena postavena
     *
     * @return true, pokud scena byla postavena, jinak false
     */
    public Boolean isSceneBuilt() {
        return sceneBuilt;
    }

    /**
     * Zobrazi znovu pripojeneho hrace
     *
     * @param message zprava s informacemi o hraci
     */
    public void showPlayerReconnected(TCPData message) {
        var player = message.valueOf(Fields.USERNAME);
        showMessage("Player " + player + " has reconnected");
    }

    /**
     * Zobrazi zpravu ze se hrac odpojil ze hry
     *
     * @param message zprava s informacemi o hraci
     */
    public void showPlayerDisconnected(TCPData message) {
        var player = message.valueOf(Fields.USERNAME);
        showMessage("Player " + player + " has disconnected");
    }
}
