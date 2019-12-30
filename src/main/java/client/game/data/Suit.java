package client.game.data;

public enum Suit {
    CLUBS("C"),
    DIAMONDS("D"),
    SPADES("S"),
    HEARTS("H");


    Suit(String string) {
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
