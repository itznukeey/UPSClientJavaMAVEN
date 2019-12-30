package client.game.data;

import lombok.AllArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
public class Card {

    private Suit suit;

    private Rank rank;

    @Setter
    private boolean show;

    @Override
    public String toString() {
        if (show) {
            return Suit.unicodeString(suit) + " " + Rank.unicodeString(rank);
        } else return "???";
    }

}
