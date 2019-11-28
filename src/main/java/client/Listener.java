package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class Listener implements Runnable {

    private final static Logger LOGGER = Logger.getLogger(Listener.class.getName());

    private BufferedReader input;

    private BlockingQueue<Response> responses;

    @Override
    public void run() {

        try {
            var response = readResponse();
        }
        catch (IOException ex) {
            LOGGER.severe("Error while reading from server");
            System.exit(-1); //TODO ERROR CODE
        }
    }

    /**
     * Precte JSON ze streamu
     *
     * @return
     */
    private String readResponse() throws IOException {
        String string;
        return (string = input.readLine()) != null ? string : "";
    }
}
