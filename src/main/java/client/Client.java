package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    private final static Logger LOGGER = Logger.getLogger(Client.class.getName());

    static {
        LOGGER.setLevel(Level.ALL);
    }

    /**
     * Ip serveru
     */
    private String serverIp;

    /**
     * Port serveru
     */
    private Integer serverPort;

    /**
     * Socket k serveru
     */
    private Socket serverSocket;

    /**
     * Input stream ze serveru
     */
    private BufferedReader input;

    /**
     * Output stream do serveru
     */
    private PrintWriter output;


    public Client(String ip, int port) {
        this.serverIp = ip;
        this.serverPort = port;
    }

    public void connect() throws IOException {
        serverSocket = new Socket(serverIp, serverPort);
        input = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        output = new PrintWriter(serverSocket.getOutputStream(), true);
        LOGGER.info("Successfully connected to the server.");



    }


}
