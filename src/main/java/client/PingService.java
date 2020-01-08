package client;

import java.time.Duration;
import java.time.LocalDateTime;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import lombok.Getter;
import lombok.Setter;

/**
 * Sluzba, ktera po nekolika vterinach pinguje server
 */
public class PingService implements Runnable {

    //todo change
    private static final Duration MAX_DURATION_BEFORE_RECONNECT = Duration.ofSeconds(500);

    private static final Duration PING_PERIOD = Duration.ofSeconds(10);

    private static final Integer RECONNECT_ATTEMPTS_LIMIT = 3;

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

    private Boolean alertSent = false;

    private Boolean socketKilled = false;

    @Setter
    private Boolean sendPingMessages = false;

    public PingService(Client client, MessageWriter messageWriter) {
        this.lastResponseReceived = LocalDateTime.now();
        this.client = client;
        this.messageWriter = messageWriter;
        lastPingSent = LocalDateTime.now();
    }

    @Override
    public void run() {
        while (!stop) {
            if (reconnectAttempts >= RECONNECT_ATTEMPTS_LIMIT) {
                closeConnection();
                break;
            }

            /*
            Zkontroluje, jestli za poslednich nekolik sekund obdrzel klient od serveru zpravu, pokud ne uzivatel bude
            upozorneny na ztraceni spojeni se serverem
             */
            if (Duration.between(lastResponseReceived, LocalDateTime.now())
                    .compareTo(MAX_DURATION_BEFORE_RECONNECT) > 0) {
                sendPingMessages = false;

                if (!socketKilled) {
                    client.killSocket();
                    socketKilled = true;
                }

                if (lastReconnectAttempt == null) {
                    if (client.reconnect()) {
                        reconnectAttempts = 0;
                        socketKilled = false;

                    } else {
                        lastReconnectAttempt = LocalDateTime.now();
                        reconnectAttempts++;
                    }
                } else if (Duration.between(lastReconnectAttempt, LocalDateTime.now())
                        .compareTo(MAX_DURATION_BEFORE_RECONNECT) > 0) {
                    if (client.reconnect()) {
                        reconnectAttempts = 0;
                        lastReconnectAttempt = null;
                        socketKilled = false;
                    } else {
                        reconnectAttempts++;
                    }
                }
            }

            if (sendPingMessages &&
                    Duration.between(lastPingSent, LocalDateTime.now()).compareTo(PING_PERIOD) > 0) {
                messageWriter.sendPing();
                lastPingSent = LocalDateTime.now();
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                System.err.println("Error ping service got interrupted");
            }
        }
    }

    private void closeConnection() {
        alertSent = true;
        Platform.runLater(() -> {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Client was disconnected from the server");
            alert.setHeaderText("Connection was interrupted");
            alert.setContentText("Please retry to login with same login, your state should be restored");
            alert.show();

            //Zavre pripojeni a nastavi login screen na posledni zadanou ip adresu a username

            client.prepareLoginAfterDC();
        });
    }

    public synchronized void closeThread() {
        stop = true;
    }
}
