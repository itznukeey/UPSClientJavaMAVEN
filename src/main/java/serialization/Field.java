package serialization;

public enum Field {

    REQUEST("request"),
    DATA_TYPE("dataType"),
    USERNAME("username"),
    LOBBY("lobby")
    ;


    String fieldName;

    Field(String string) {
        this.fieldName = string;
    }
}
