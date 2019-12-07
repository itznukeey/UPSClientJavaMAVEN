package serialization;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import lombok.Getter;

public class Deserializer {

    private static final Logger LOGGER = Logger.getLogger(Deserializer.class.getName());

    @Getter
    private Map<String, String> fields;

    public void deserialize(String message) {

        fields = new HashMap<>();

        if (!message.startsWith("{") && message.endsWith("}")) {
            LOGGER.warning("Recieved wrong message from the server.");
            throw new IllegalStateException("Recieved wrong message");
        }

        try {
            message = message.replace("{", "");
            message = message.replace("}", "");

            String[] split = message.split(",");

            Arrays.stream(split).forEach(e -> {
                String[] field = e.split(":");
                fields.put(field[0], field[1]);
            });
        } catch (Exception ex) {
            throw new IllegalStateException("Error while parsing message, message is probably corrupted");
        }
    }

    public String valueOf(String field) {
        return fields.get(field);
    }
}
