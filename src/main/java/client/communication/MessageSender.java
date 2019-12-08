package client.communication;

import java.io.PrintWriter;
import java.time.Duration;
import serialization.DataType;
import serialization.TCPData;

public class MessageSender {

    private static final Duration LOGIN_TIMEOUT = Duration.ofSeconds(10);

    private static final Integer MESSAGE_SIZE = 2048;

    private PrintWriter output;

    public MessageSender(PrintWriter output) {
        this.output = output;
    }

    private void sendData(String data) {
        output.write(data);
        output.flush();
    }

    public void sendLoginRequest(String username) {
        var data = new TCPData(DataType.REQUEST);
        data.add("username", username);
        sendData(data.serialize());
    }

    public String sendLobbyListRequest() {
        var data = new TCPData(DataType.REQUEST);
        data.add("request", "lobbyList");
        return data.serialize();
    }

    public String sendPingResponse() {
        var data = new TCPData(DataType.PING);
        return data.serialize();
    }
}
