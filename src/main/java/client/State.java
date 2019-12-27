package client;

import java.util.Arrays;

public enum State {
    AUTHENTICATION("authentication"),
    LOBBY_LIST("lobbyList"),
    LOBBY("lobby"),
    DISCONNECTED("disconnected"),
    GAME("game");

    private String string;

    State(String state) {
        this.string = state;
    }

    public static State getState(String state) {
        return Arrays.stream(values())
                .filter(type -> type.string.equals(state))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }
}
