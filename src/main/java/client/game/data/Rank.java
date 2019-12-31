package client.game.data;

public enum Rank {

    ACE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    TEN,
    JACK,
    QUEEN,
    KING;


    public static Rank getRank(String string) {
        switch (string) {
            case "ace":
                return ACE;

            case "two":
                return TWO;

            case "three":
                return THREE;

            case "four":
                return FOUR;

            case "five":
                return FIVE;

            case "six":
                return SIX;

            case "seven":
                return SEVEN;

            case "eight":
                return EIGHT;

            case "nine":
                return NINE;

            case "ten":
                return TEN;

            case "jack":
                return JACK;

            case "queen":
                return QUEEN;
        }
        return KING;
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
