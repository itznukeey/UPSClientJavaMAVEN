package serialization;

public enum Value {

    OK("ok"),
    LOBBY_LIST("lobbyList"),
    LOBBY("lobby"),
    TRUE("true"),
    FALSE("false"),
    ;

    Value(String value) {
        this.valueString = value;
    }

    private String valueString;
}
