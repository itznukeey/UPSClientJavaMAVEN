package controllers.concurrency;

import client.Client;
import controllers.LobbiesController;
import javafx.application.Platform;

public class LobbyListUpdater implements Runnable {

    private static final long UPDATE_RATE_MS = 2000;
    private final LobbiesController lobbiesController;

    private final Client client;
    private long lastUpdate;
    private Boolean stop = false;

    public LobbyListUpdater(LobbiesController lobbiesController, Client client) {
        this.lobbiesController = lobbiesController;
        this.client = client;
    }

    @Override
    public void run() {

        lastUpdate = System.currentTimeMillis();
        while (!stop) {
            if (System.currentTimeMillis() - lastUpdate > UPDATE_RATE_MS) {
                Platform.runLater(() -> lobbiesController.updateListView(client.getLobbyList()));
            }
        }

    }

    private synchronized void updateLobbyList() {

    }

    public synchronized void stop() {
        this.stop = true;
    }
}
