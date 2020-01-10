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
    private static final Duration DC_TIMEOUT = Duration.ofSeconds(5);

    private static final Duration PING_PERIOD = Duration.ofSeconds(2);

    @Setter
    private volatile MessageWriter messageWriter;

    @Getter
    @Setter
    private LocalDateTime lastResponseReceived;

    private LocalDateTime lastPingSent;

    private Client client;

    private volatile Boolean stop = false;

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

            //Pokud nastal maximalni pocet pokusu pro reconnect, client se nebude dale pokouset pripojit
            if (Duration.between(lastPingSent, LocalDateTime.now()).compareTo(DC_TIMEOUT) > 0) {
                stop = true;
                client.showConnectionLostDialog();
                break;
            }

            //
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

    public void closeThread() {
        sendPingMessages = true;
        stop = true;
    }

}
