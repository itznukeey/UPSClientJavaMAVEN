package client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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


}
