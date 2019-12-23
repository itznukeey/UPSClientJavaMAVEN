package controllers.concurrency;

import client.Client;
import client.MessageWriter;

public class LobbyListUpdater implements Runnable {

    private static final long UPDATE_RATE_MS = 2000;

    private final MessageWriter messageWriter;

    private long lastUpdate;

    private Boolean stop = false;

    public LobbyListUpdater(Client client) {
        this.messageWriter = client.getMessageWriter();
        messageWriter.sendLobbyUpdateRequest();
    }

    @Override
    public void run() {

        lastUpdate = System.currentTimeMillis();
        while (!stop) {
            if (System.currentTimeMillis() - lastUpdate > UPDATE_RATE_MS) {
                messageWriter.sendLobbyUpdateRequest();
            }
        }

    }

    private synchronized void updateLobbyList() {

    }

    public synchronized void stop() {
        this.stop = true;
    }
}
