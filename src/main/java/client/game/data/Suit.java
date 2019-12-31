package client.game.data;

public enum Suit {
    CLUBS,
    DIAMONDS,
    SPADES,
    HEARTS;


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
