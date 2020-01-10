package client.game.data;

import lombok.AllArgsConstructor;
import lombok.Setter;

/**
 * Jednoducha trida pro zobrazeni karty
 */
@AllArgsConstructor
public class Card {

    /**
     * Barva karty
     */
    private Suit suit;

    /**
     * Hodnota karty
     */
    private Rank rank;

    /**
     * Zda-li se ma karta zobrazit - dealerova druha karta se nezobrazuje
     */
    @Setter
    private boolean show;

    /**
     * Override toString pro spravne zobrazeni ve hre
     *
     * @return overriden string
     */
    @Override
    public String toString() {
        if (show) {
            return Suit.unicodeString(suit) + " " + Rank.unicodeString(rank);
        } else return "???";
    }

}
