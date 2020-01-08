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
        System.out.print(serializedMessage);
        output.print(serializedMessage);
        output.flush();
    }

    public void sendPing() {
        sendMessage(new TCPData(DataType.PING).serialize());
    }

    public void sendAuthenticationRequest(String username) {
        var message = new TCPData(DataType.REQUEST);
        message.add(Fields.REQUEST, Values.LOGIN);
        message.add(Fields.USERNAME, username);
        sendMessage(message.serialize());
    }

    public void sendLobbyListUpdateRequest() {
        var message = new TCPData(DataType.REQUEST);
        message.add(Fields.REQUEST, Values.LOBBY_LIST);
        sendMessage(message.serialize());
    }

    public void sendJoinLobbyRequest(Lobby selected) {
        var message = new TCPData(DataType.REQUEST);
        message.add(Fields.REQUEST, Values.JOIN_LOBBY);
        message.add(Fields.LOBBY_ID, String.valueOf(selected.getId()));
        sendMessage(message.serialize());
    }

    public void sendReady() {
        var message = new TCPData(DataType.REQUEST);
        message.add(Fields.REQUEST, Values.SEND_READY);
        sendMessage(message.serialize());
    }

    public void sendLeaveLobbyRequest() {
        var message = new TCPData(DataType.REQUEST);
        message.add(Fields.REQUEST, Values.LEAVE_LOBBY);
        sendMessage(message.serialize());
    }

    public void sendPlayerListUpdated() {
        var message = new TCPData(DataType.RESPONSE);
        message.add(Fields.RESPONSE, Values.UPDATE_PLAYER_LIST);
        sendMessage(message.serialize());
    }

    public void sendShownPlayerConnected() {
        var message = new TCPData(DataType.RESPONSE);
        message.add(Fields.RESPONSE, Values.SHOW_PLAYER_CONNECTED);
        sendMessage(message.serialize());
    }

    public void sendShownPlayerDisconnected() {
        var message = new TCPData(DataType.RESPONSE);
        message.add(Fields.RESPONSE, Values.SHOW_PLAYER_DISCONNECTED);
        sendMessage(message.serialize());
    }

    public void sendConfirmParticipation(String bet) {
        var message = new TCPData(DataType.RESPONSE);
        message.add(Fields.BET, bet);
        message.add(Fields.RESPONSE, Values.CONFIRM_PARTICIPATION);
        sendMessage(message.serialize());
    }

    public void sendHit() {
        var message = new TCPData(DataType.RESPONSE);
        message.add(Fields.RESPONSE, Values.TURN);
        message.add(Fields.TURN_TYPE, Values.HIT);
        sendMessage(message.serialize());
    }

    public void sendStand() {
        var message = new TCPData(DataType.RESPONSE);
        message.add(Fields.RESPONSE, Values.TURN);
        message.add(Fields.TURN_TYPE, Values.STAND);
        sendMessage(message.serialize());
    }

    public void sendDoubleDown() {
        var message = new TCPData(DataType.RESPONSE);
        message.add(Fields.RESPONSE, Values.TURN);
        message.add(Fields.TURN_TYPE, Values.DOUBLE_DOWN);
        sendMessage(message.serialize());
    }

    public void sendDeclineParticipation() {
        var message = new TCPData(DataType.RESPONSE);
        message.add(Fields.RESPONSE, Values.DECLINE_PARTICIPATION);
        sendMessage(message.serialize());
    }
}
