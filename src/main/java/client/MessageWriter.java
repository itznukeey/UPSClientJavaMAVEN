package client;

import java.io.PrintWriter;
import serialization.DataType;
import serialization.Fields;
import serialization.TCPData;
import serialization.Values;

public class MessageWriter {

    private final PrintWriter output;

    public MessageWriter(PrintWriter output) {
        this.output = output;
    }

    private void sendMessage(String serializedMessage) {
        output.print(serializedMessage);
        output.flush();
    }

    public void sendAuthenticationRequest(String username) {
        var message = new TCPData(DataType.REQUEST);
        message.add(Fields.REQUEST, Values.LOGIN);
        message.add(Fields.USERNAME, username);
        sendMessage(message.serialize());
    }

    public void sendLobbyUpdateRequest() {
        var message = new TCPData(DataType.REQUEST);
        message.add(Fields.REQUEST, Values.LOBBY_LIST);
        sendMessage(message.serialize());
    }

    public void sendJoinLobbyRequest(Lobby selected) {
        var message = new TCPData(DataType.REQUEST);
        message.add(Fields.REQUEST, Values.JOIN_LOBBY);
        message.add(Fields.LOBBY_ID, String.valueOf(selected.getId()));
    }
}
