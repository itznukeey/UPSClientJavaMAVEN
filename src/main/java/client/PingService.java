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

    private static final Duration MAX_DURATION_BEFORE_ALERT = Duration.ofSeconds(1000);

    private static final Duration PING_PERIOD = Duration.ofSeconds(7);

    private MessageWriter messageWriter;

    @Getter
    @Setter
    private LocalDateTime lastResponseReceived;

    private LocalDateTime lastPingSent;

    private Client client;

    private Boolean stop = false;

    private Boolean alertSent = false;

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
            /*
            Zkontroluje, jestli za poslednich nekolik sekund obdrzel klient od serveru zpravu, pokud ne uzivatel bude
            upozorneny na ztraceni spojeni se serverem
             */
            if (!alertSent &&
                    Duration.between(lastResponseReceived, LocalDateTime.now()).compareTo(MAX_DURATION_BEFORE_ALERT) > 0) {
                closeConnection();
            }

            //Sluzba standardne posila pouze ping, aby se zajistilo ze server bude na ping alespon 1 za 100 ms odpovidat
            if (sendPingMessages &&
                    Duration.between(lastPingSent, LocalDateTime.now()).compareTo(PING_PERIOD) > 0) {
                messageWriter.sendPing();
                lastPingSent = LocalDateTime.now();
            }
            try {
                Thread.sleep(10);
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
            client.disconnect();
        });

    }

    public synchronized void closeThread() {
        stop = true;
    }
}
