package serialization;


import java.util.HashMap;
import java.util.Map;

public class Serializer {

    private Map<String, String> fields;

    public Serializer() {
        this.fields = new HashMap<>();
    }

    public void add(String field, String value) {
        fields.put(field, value);
    }

    public void clear() {
        fields = new HashMap<>();
    }

    public String serialize() {
        var stringBuilder = new StringBuilder("{");
        fields.forEach((field, value) -> {
            stringBuilder.append(field).append(":").append(value).append(",");
        });

        return stringBuilder.deleteCharAt(stringBuilder.length() - 1).append("}").toString();
    }
}
