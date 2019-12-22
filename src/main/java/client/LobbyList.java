package client;

import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import lombok.Getter;

public class LobbyList {

    @Getter
    private List<Lobby> lobbies;

    public LobbyList() {
        lobbies = new Vector<>();
    }

    public synchronized void sortById() {
        this.lobbies =
                lobbies.stream()
                        .sorted(Comparator.comparingInt(Lobby::getId))
                        .collect(Collectors.toList());
    }

    public synchronized void addLobby(Lobby lobby) {
        lobbies.add(lobby);
    }
}
