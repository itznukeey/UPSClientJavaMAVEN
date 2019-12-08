package client;

import java.time.Duration;

public class LobbyHandler implements Runnable {

    private static final Duration UPDATE_DURATION = Duration.ofSeconds(2);

    private LobbyList lobbyList;

    private Boolean update;

    public LobbyHandler(LobbyList lobbyList) {
        this.lobbyList = lobbyList;
    }

    @Override
    public void run() {

    }
}
