package serialization;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.Getter;

public class TCPData {

    /**
     * Typ dat - Request, response nebo ping od serveru
     */
    @Getter
    private DataType dataType;

    /**
     * Identifikator
     */
    @Getter
    private UUID guid;

    /**
     * Obsahuje mapping poli a jejich hodnot
     */
    @Getter
    private Map<String, String> fields;

    /**
     * Zda-li jsou data editovatelna
     */
    @Getter
    private Boolean isEditable = true;

    /**
     * Konstruktor pro deserializaci
     * @param message string precteny z printwriteru
     */
    public TCPData(String message) {
        this.fields = new HashMap<>();
        deserialize(message);
    }

    public TCPData(DataType dataType) {
        this.dataType = dataType;
        this.guid = UUID.randomUUID();
    }

    public String valueOf(String field) {
        return fields.get(field);
    }

    public void add(String field, String value) {
        if (!isEditable) {
            throw new IllegalStateException("Error, data are marked as not editable");
        }

        fields.put(field,value);
    }

    public boolean isResponse(TCPData data) {
        return guid.equals(data.guid) && data.dataType.equals(DataType.RESPONSE);
    }

    public String serialize() {
        var stringBuilder = new StringBuilder("{");
        fields.forEach((field, value) -> {
            stringBuilder.append(field).append(":").append(value).append(",");
        });
        stringBuilder.append("dataType").append(":").append(dataType.toString()).append(",");
        stringBuilder.append("guid").append(":").append(guid.toString()).append(",");
        stringBuilder.append("}");
        isEditable = false;
        return stringBuilder.toString();
    }

    private void deserialize(String message) {
        if (!message.startsWith("{") && !message.endsWith("}")) {
            throw new IllegalStateException("Error, incorrect message format");
        }

        try {
            message = message.replace("{", "").replace("}","");

            String[] doubleColonSplit = message.split(",");
            Arrays.stream(doubleColonSplit).forEach(e -> {
                String[] field = e.split(":");
                fields.put(field[0], field[1]);
            });

            //Nastavi typ dat, pokud vstup spatny metoda getType hodi IllegalStateException
            dataType = DataType.getType(fields.get("dataType"));
        }

        catch (Exception ex) {
            throw new IllegalStateException("Error, incorrect message format");
        }

        isEditable = false;
    }
}
