package client;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Vlakno pro pingovani serveru. Pingovani serveru nam zaruci ze server si periodicky aktualizuje keepalive timer
 * pro klienta tzn. kdyz by klient nic nedelal server ho nesmaze a nevykopne.
 * Vlakno musi bezet po celou dobu od prvniho pripojeni do finalniho odpojeni - obsahuje informace o pokusech k opetovnemu
 * pripojeni.
 */
public class PingService implements Runnable {

    /**
     * Timeout po kterem se klient odpoji od serveru
     */
    private static final Duration DC_TIMEOUT = Duration.ofSeconds(14);

    /**
     * Perioda pro poslani keepalive zpravy serveru
     */
    private static final Duration PING_PERIOD = Duration.ofSeconds(2);

    /**
     * Reference na MessageWriter pro poslani pingu
     */
    @Setter
    private MessageWriter messageWriter;

    /**
     * Cas obdrzeni posledni zpravy
     */
    @Getter
    @Setter
    private LocalDateTime lastResponseReceived;

    /**
     * Cas odeslani posledni keepalive zpravy
     */
    private LocalDateTime lastPingSent;

    /**
     * Reference na klienta
     */
    private Client client;

    /**
     * Flag pro vypnuti vlakna
     */
    private Boolean stop = false;

    /**
     * Flag pro zasilani pingu - ping ma smysl posilat az kdyz od serveru klient obdrzi login response
     */
    @Setter
    private volatile Boolean sendPingMessages = false;

    /**
     * Konstruktor pro vytvoreni pingovaci sluzby
     * @param client reference na klienta
     * @param messageWriter reference na message writer
     */
    public PingService(Client client, MessageWriter messageWriter) {
        this.lastResponseReceived = LocalDateTime.now();
        this.client = client;
        this.messageWriter = messageWriter;
        lastPingSent = LocalDateTime.now();
    }

    /**
     * Kod ve vlakne
     */
    @Override
    public void run() {
        while (!stop) {

            //Pokud nastal maximalni pocet pokusu pro reconnect, client se nebude dale pokouset pripojit
            if (Duration.between(lastPingSent, LocalDateTime.now()).compareTo(DC_TIMEOUT) > 0) {
                stop = true;
                client.showConnectionLostDialog();
                break;
            }

            //Pokud se maji posilat pingovaci zpravy a je cas poslat keepalive zpravu
            if (sendPingMessages &&
                    Duration.between(lastPingSent, LocalDateTime.now()).compareTo(PING_PERIOD) > 0) {
                messageWriter.sendPing();
                lastPingSent = LocalDateTime.now();
            }
        }

        try {
            //Thread sleep pro mensi zasah na vykon - nepotrebujeme presne pocitani casu
            Thread.sleep(1);
        } catch (InterruptedException e) {
            System.err.println("Thread of ping service was interrupted");
        }
    }

    /**
     * Zavre vlakno
     */
    public void closeThread() {
        stop = true;
    }

}
