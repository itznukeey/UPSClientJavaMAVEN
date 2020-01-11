package client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO reprezentujici info o lobby
 */
@AllArgsConstructor
public class Lobby {

    /**
     * Id lobby
     */
    @Getter
    private final int id;

    /**
     * Celkovy pocet hracu v lobby
     */
    @Getter
    @Setter
    private int playerCount;

    /**
     * Maximalni limit hracu
     */
    @Getter
    @Setter
    private int playerLimit;

    /**
     * Override toString pro zobrazeni lobby v lobbylistu
     * @return string s informacemi o lobby
     */
    @Override
    public String toString() {
        return "Lobby " + id + " " + playerCount + " / " + playerLimit;
    }
}
