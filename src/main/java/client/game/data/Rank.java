package client.game.data;

public enum Rank {

    ACE("ace"),
    TWO("two"),
    THREE("three"),
    FOUR("four"),
    FIVE("five"),
    SIX("six"),
    SEVEN("seven"),
    EIGHT("eight"),
    NINE("nine"),
    TEN("ten"),
    JACK("jack"),
    QUEEN("queen"),
    KING("king");

    String value;

    Rank(String value) {
        this.value = value;
    }

    public static String unicodeString(Rank rank) {
        switch (rank) {
            case ACE:
                return "A";

            case TWO:
                return "2";

            case THREE:
                return "3";

            case FOUR:
                return "4";

            case FIVE:
                return "5";

            case SIX:
                return "6";

            case SEVEN:
                return "7";

            case EIGHT:
                return "8";

            case NINE:
                return "9";
            case TEN:
                return "10";
            case JACK:
                return "J";
            case QUEEN:
                return "Q";
            case KING:
                return "K";
        }
        return "null";
    }
}
