package client;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LobbyList {

    private List<Lobby> lobbies;


    public void sortById() {
        this.lobbies =
                lobbies.stream()
                        .sorted(Comparator.comparingInt(Lobby::getId))
                        .collect(Collectors.toList());
    }

    public void addLobby(Lobby lobby) {
        lobbies.add(lobby);
    }
}
