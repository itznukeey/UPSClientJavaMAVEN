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

    /**
     * Konstruktor pro serializaci
     *
     * @param dataType typ zpravy
     */
    public TCPData(DataType dataType) {
        this.fields = new HashMap<>();
        this.dataType = dataType;
    }

    /**
     * Vrati hodnotu daneho pole
     *
     * @param field nazev pole
     * @return hodnotu daneho pole nebo null, pokud hodnota nebyla nalezena
     */
    public String valueOf(String field) {
        return fields.get(field);
    }

    /**
     * Prida par pole hodnota do zpravy
     *
     * @param field nazev pole
     * @param value nazev hodnoty
     */
    public void add(String field, String value) {
        if (!isEditable) {
            throw new IllegalStateException("Error, data are marked as not editable");
        }

        fields.put(field, value);
    }

    /**
     * Serializuje zpravu
     *
     * @return vrati serializovany retezec
     */
    public String serialize() {
        var stringBuilder = new StringBuilder("{");
        fields.forEach((field, value) -> {
            stringBuilder.append(field).append(":").append(value).append(",");
        });
        stringBuilder.append("dataType").append(":").append(dataType.toString()).append("}\n");
        isEditable = false;
        return stringBuilder.toString();
    }

    /**
     * Deserializuje zpravu
     *
     * @param message prijata zprava ze serveru
     */
    private void deserialize(String message) {
        if (!message.startsWith("{") && !message.endsWith("}")) {
            throw new IllegalStateException("Error, incorrect message format");
        }

        try {
            System.out.println("Serialized message read: " + message);
            message = message.replace("{", "").replace("}", "");

            String[] commaSplit = message.split(",");
            Arrays.stream(commaSplit).forEach(e -> {
                String[] field = e.split(":");
                fields.put(field[0], field[1]);
            });

            //Nastavi typ dat, pokud vstup spatny metoda getType hodi IllegalStateException
            dataType = DataType.getType(fields.get("dataType"));
        }

        /**
         * Pokud server poslal spatny format vyhodi chybu, ktera se zachyti ve vlakne a klient se odpoji
         */ catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalStateException("Error, incorrect message format");
        }

        isEditable = false;
    }
}
