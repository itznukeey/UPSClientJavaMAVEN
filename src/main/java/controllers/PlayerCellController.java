package controllers;

import client.game.data.Card;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import lombok.Getter;

/**
 * Controller pro herni panel jednoho hrace - obsahuje jeho stav hry - skore, karty atd.
 */
public class PlayerCellController {

    public static final String TOTAL_SCORE = "Score: ";

    /**
     * Uzivatelske jmeno hrace
     */
    @FXML
    @Getter
    private Text username;

    /**
     * Seznam karet
     */
    @FXML
    @Getter
    private ListView<Card> cardList;

    /**
     * Celkove skore
     */
    @FXML
    @Getter
    private Text totalScore;

    /**
     * Vysledek hry
     */
    @FXML
    @Getter
    private Text gameResult;

    /**
     * Vsazena castka vuci vyhre
     */
    @FXML
    @Getter
    private Text betToGain;

    /**
     * Vysledek sazky
     */
    @FXML
    @Getter
    private Text betResult;

    /**
     * Nastavi uzivatelske jmeno
     *
     * @param username uzivatelske jmeno
     */
    public void setUsername(String username) {
        this.username.setText(username);
    }

    /**
     * Nastavi seznam karet
     *
     * @param cards seznam karet
     */
    public void setCardList(List<Card> cards) {
        cardList.getItems().clear();
        cardList.getItems().addAll(cards);
    }

    /**
     * Nastavi celkove skore
     *
     * @param score celkove skore
     */
    public void setTotalScore(String score) {
        totalScore.setText(TOTAL_SCORE + score);
    }

    /**
     * Nastavi vysledek hry
     *
     * @param result vysledek hry - WIN nebo LOSS
     */
    public void setGameResult(String result) {
        gameResult.setText(result);
    }
}
