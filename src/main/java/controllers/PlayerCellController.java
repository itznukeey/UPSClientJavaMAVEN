package controllers;

import client.game.data.Card;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import lombok.Getter;

public class PlayerCellController {

    public static final String TOTAL_SCORE = "Total score: ";

    @FXML
    @Getter
    private Text username;

    @FXML
    @Getter
    private ListView<Card> cardList;

    @FXML
    @Getter
    private Text totalScore;

    @FXML
    @Getter
    private Text gameResult;

    @FXML
    @Getter
    private Text betToGained;

    @FXML
    @Getter
    private Text betResult;

    public void setUsername(String username) {
        this.username.setText(username);
    }

    public void setCardList(List<Card> cards) {
        cardList.getItems().clear();
        cardList.getItems().addAll(cards);
    }

    public void setTotalScore(String score) {
        totalScore.setText(TOTAL_SCORE + score);
    }

    public void setGameResult(String result) {
        gameResult.setText(result);
    }
}
