package serialization;

import java.util.Arrays;

/**
 * Typ dat zpravy
 */
public enum DataType {

    REQUEST("request"), PING("ping"), RESPONSE("response");

    /**
     * Retezec typu zpravy
     */
    private String string;

    /**
     * Konstruktor pro vytvoreni datoveho typu zpravy
     *
     * @param string typ zpravy v retezci - "request", "ping" nebo "response"
     */
    DataType(String string) {
        this.string = string;
    }

    /**
     * Vrati datovy typ ze stringu, pokud neobsahuje datovy typ vyhodi exception
     *
     * @param dataType string s datovym typem
     * @return instanci DataType
     */
    public static DataType getType(String dataType) {
        return Arrays.stream(values())
                .filter(type -> type.string.equalsIgnoreCase(dataType))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    @Override
    public String toString() {
        return string;
    }
}
