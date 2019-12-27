package client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO reprezentujici info o lobby
 */
@AllArgsConstructor
public class Lobby {

    @Getter
    private final int id;

    @Getter
    @Setter
    private int playerCount;

    @Getter
    @Setter
    private int playerLimit;

    @Override
    public String toString() {
        return "Lobby " + id + " " + playerCount + " / " + playerLimit;
    }
}
