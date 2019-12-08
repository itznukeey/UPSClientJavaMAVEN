package client;

import lombok.Getter;
import lombok.Setter;

public class ClientData {

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private LobbyList lobbyList;
}
