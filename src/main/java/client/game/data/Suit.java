package client.game.data;

/**
 * Trida obsahujici vsechny barvy karty a metody pro zobrazeni v klientu
 */
public enum Suit {
    CLUBS,
    DIAMONDS,
    SPADES,
    HEARTS;

    /**
     * Namapuje string ze serializovane zpravy na Suit
     *
     * @param string string ze serializovane zpravy ze serveru
     * @return Suit
     */
    public static Suit getSuit(String string) {
        switch (string) {
            case "S":
                return SPADES;

            case "D":
                return DIAMONDS;

            case "H":
                return HEARTS;
        }

        return CLUBS;
    }

    /**
     * Namapuje barvu na spravny unicode string
     *
     * @param suit barva
     * @return unicode string pro zobrazeni
     */
    public static String unicodeString(Suit suit) {
        switch (suit) {

            case CLUBS:
                return "♣ CLUB";
            case DIAMONDS:
                return "♦ DIAMOND";
            case SPADES:
                return "♠ SPADE";
            case HEARTS:
                return "♥ HEART";
        }
        return "null";
    }
}
