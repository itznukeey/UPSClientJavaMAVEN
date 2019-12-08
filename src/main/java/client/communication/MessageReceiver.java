package client.communication;

import client.Lobby;
import client.LobbyList;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import serialization.TCPData;

public class MessageReceiver {

    private static final Duration LOGIN_TIMEOUT = Duration.ofMillis(500);

    private static final Integer MAX_RESPONSE_LENGTH = 2048;

    @Getter
    @Setter
    private BufferedReader input;

    public MessageReceiver(BufferedReader input) {
        this.input = input;
    }

    private String getResponse() throws IOException {
        String message;
        var start = Instant.now();

        do {
            message = input.readLine();

            if (message.length() > MAX_RESPONSE_LENGTH) {
                throw new IllegalStateException("Server response too long to parse");
            }

            if (Duration.between(start, Instant.now()).compareTo(LOGIN_TIMEOUT) > 0) {
                throw new IllegalStateException("Server did not respond");
            }

        } while (!(message.startsWith("{") && message.endsWith("}")));

        return message;
    }

    public boolean getLoginResponse() throws IOException {
        var data = new TCPData(getResponse());
        return data.valueOf("response").equals("true");
    }

    public LobbyList getLobbyListResponse() throws IOException, NumberFormatException {
        var data = new TCPData(getResponse());
        var lobbyList = new LobbyList();

        data.getFields().forEach((id, values) -> {
            String[] fields = values.split(";");

            lobbyList.addLobby(
                    new Lobby(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]), Integer.parseInt(fields[2])));
        });

        lobbyList.sortById();
        return lobbyList;
    }
}
