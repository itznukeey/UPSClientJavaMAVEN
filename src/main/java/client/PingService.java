package client;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Vlakno pro pingovani serveru. Pingovani serveru nam zaruci ze server si periodicky aktualizuje keepalive timer
 * pro klienta tzn. kdyz by klient nic nedelal server ho nesmaze a nevykopne.
 * Vlakno musi bezet po celou dobu od prvniho pripojeni do finalniho odpojeni - obsahuje informace o pokusech k opetovnemu
 * pripojeni.
 */
public class PingService implements Runnable {

    //todo change
    private static final Duration MAX_DURATION_BEFORE_RECONNECT = Duration.ofSeconds(10);

    private static final Duration PING_PERIOD = Duration.ofSeconds(5);

    private static final Integer MAX_RECONNECT_ATTEMPTS = 3;

    private Integer reconnectAttempts = 0;

    @Setter
    private MessageWriter messageWriter;

    @Getter
    @Setter
    private LocalDateTime lastResponseReceived;

    private LocalDateTime lastPingSent;

    private LocalDateTime lastReconnectAttempt;

    private Client client;

    private Boolean stop = false;

    private Boolean disconnected = false;

    @Setter
    private volatile Boolean sendPingMessages = false;

    public PingService(Client client, MessageWriter messageWriter) {
        this.lastResponseReceived = LocalDateTime.now();
        this.client = client;
        this.messageWriter = messageWriter;
        lastPingSent = LocalDateTime.now();
    }

    @Override
    public void run() {
        while (!stop) {

            if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                stop = true;
                client.showConnectionLostDialog();
                break;
            }

            //Pokud maximalni doba od ziskani zpravy prekrocila dobu pro opetovne pripojeni zkusime znovu pripojit
            if (Duration.between(lastResponseReceived, LocalDateTime.now()).compareTo(MAX_DURATION_BEFORE_RECONNECT) > 0
                    && !disconnected) {
                client.killSocket();
                if (!client.reconnect()) {
                    disconnected = true;
                    reconnectAttempts = 1;
                    lastReconnectAttempt = LocalDateTime.now();
                }
            }

            if (disconnected && Duration.between(lastReconnectAttempt, LocalDateTime.now())
                    .compareTo(MAX_DURATION_BEFORE_RECONNECT) > 0) {
                if (client.reconnect()) {
                    disconnected = false;
                    reconnectAttempts = 0;
                } else {
                    reconnectAttempts++;
                    lastReconnectAttempt = LocalDateTime.now();
                }
            }

            if (sendPingMessages &&
                    Duration.between(lastPingSent, LocalDateTime.now()).compareTo(PING_PERIOD) > 0) {
                messageWriter.sendPing();
                lastPingSent = LocalDateTime.now();
            }
        }

        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            System.err.println("Thread of ping service was interrupted");
        }
    }

}
