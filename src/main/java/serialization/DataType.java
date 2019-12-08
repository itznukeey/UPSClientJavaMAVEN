package serialization;

import java.util.Arrays;

public enum DataType {

    REQUEST("request"), PING("ping"), RESPONSE("response");

    private String string;

    DataType(String string) {
        this.string = string;
    }

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
