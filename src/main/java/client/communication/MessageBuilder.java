package client.communication;

import java.time.Duration;
import serialization.Serializer;

public class MessageBuilder {

    private static final Duration LOGIN_TIMEOUT = Duration.ofSeconds(10);

    private static final Integer MESSAGE_SIZE = 2048;

    private final Serializer serializer;

    public MessageBuilder() {
        this.serializer = new Serializer();
    }

    public String loginRequest(String username) {
        serializer.clear();
        serializer.add("request","login");
        serializer.add("username", username);
        return serializer.serialize();
    }

    public String lobbyListRequest() {
        serializer.clear();
        serializer.add("request","lobbyList");
        return serializer.serialize();
    }

    public String pingResponse() {
        serializer.clear();
        serializer.add("response","ok");
        return serializer.serialize();
    }
}
