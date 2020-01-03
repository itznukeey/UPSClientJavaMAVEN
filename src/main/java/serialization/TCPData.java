package serialization;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public class TCPData {

    /**
     * Typ dat - Request, response nebo ping od serveru
     */
    @Getter
    private DataType dataType;

    /**
     * Obsahuje mapping poli a jejich hodnot
     */
    @Getter
    private final Map<String, String> fields;

    /**
     * Zda-li jsou data editovatelna
     */
    @Getter
    private Boolean isEditable = true;

    /**
     * Konstruktor pro deserializaci
     *
     * @param message string precteny z printwriteru
     */
    public TCPData(String message) throws IllegalStateException {
        this.fields = new HashMap<>();
        deserialize(message);
    }

    public TCPData(DataType dataType) {
        this.fields = new HashMap<>();
        this.dataType = dataType;
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

    public String serialize() throws IllegalStateException {
        var stringBuilder = new StringBuilder("{");
        fields.forEach((field, value) -> {
            stringBuilder.append(field).append(":").append(value).append(",");
        });
        stringBuilder.append("dataType").append(":").append(dataType.toString()).append("}\n");
        isEditable = false;
        return stringBuilder.toString();
    }

    private void deserialize(String message) {
        if (!message.startsWith("{") && !message.endsWith("}")) {
            throw new IllegalStateException("Error, incorrect message format");
        }

        try {
            System.out.println(message);
            message = message.replace("{", "").replace("}", "");

            String[] commaSplit = message.split(",");
            Arrays.stream(commaSplit).forEach(e -> {
                String[] field = e.split(":");
                fields.put(field[0], field[1]);
            });

            //Nastavi typ dat, pokud vstup spatny metoda getType hodi IllegalStateException
            dataType = DataType.getType(fields.get("dataType"));
        }

        catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalStateException("Error, incorrect message format");
        }

        isEditable = false;
    }
}
