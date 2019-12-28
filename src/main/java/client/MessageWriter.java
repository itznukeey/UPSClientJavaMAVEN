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

    private synchronized void sendMessage(String serializedMessage) {
        System.out.println(serializedMessage);
        output.print(serializedMessage);
        output.flush();
    }

    public synchronized void sendPing() {
        sendMessage(new TCPData(DataType.PING).serialize());
    }

    public synchronized void sendAuthenticationRequest(String username) {
        var message = new TCPData(DataType.REQUEST);
        message.add(Fields.REQUEST, Values.LOGIN);
        message.add(Fields.USERNAME, username);
        sendMessage(message.serialize());
    }

    public synchronized void sendLobbyUpdateRequest() {
        var message = new TCPData(DataType.REQUEST);
        message.add(Fields.REQUEST, Values.LOBBY_LIST);
        sendMessage(message.serialize());
    }

    public synchronized void sendJoinLobbyRequest(Lobby selected) {
        var message = new TCPData(DataType.REQUEST);
        message.add(Fields.REQUEST, Values.JOIN_LOBBY);
        message.add(Fields.LOBBY_ID, String.valueOf(selected.getId()));
        sendMessage(message.serialize());
    }

    public synchronized void sendVoteStartRequest() {
        var message = new TCPData(DataType.REQUEST);
        message.add(Fields.REQUEST, Values.VOTE_START);
        sendMessage(message.serialize());
    }

    public synchronized void sendLeaveLobbyRequest(Integer lobbyId) {
        var message = new TCPData(DataType.REQUEST);
        message.add(Fields.REQUEST, Values.LEAVE_LOBBY);
        message.add(Fields.LOBBY_ID, String.valueOf(lobbyId));
        sendMessage(message.serialize());
    }

}
