package client.communication;

import java.time.Duration;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import serialization.TCPData;

/**
 * Slouzi jako vlakno ktere posila zpravy, ktere dostane z requestu
 */
public class ListenerThread implements Runnable{

    public static final String REQUEST_FIELD_NAME = "request";

    private static final Logger LOGGER = Logger.getLogger(ListenerThread.class.getName());

    /**
     * Vlakno je po vykonani kontroly pritomnosti requestu uspano na 10 ms
     */
    private static final Duration THREAD_SLEEP_DURATION = Duration.ofMillis(10);

    private List<TCPData> pendingRequests;

    public ListenerThread() {
        this.pendingRequests = new Vector<>();
    }


    @Override
    public void run() {
        while (true) {
            pendingRequests.forEach(request -> {


            });
        }
    }
}
