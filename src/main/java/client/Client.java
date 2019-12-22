package client;

import client.communication.MessageReceiver;
import client.communication.MessageSender;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;

public class Client {

    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private static final Duration DURATION_BEFORE_TIMEOUT = Duration.ofSeconds(5);

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

    @Getter
    private ClientData data;

    private MessageSender messageSender;

    private MessageReceiver messageReceiver;

    public Client(String ip, int port) {
        this.serverIp = ip;
        this.serverPort = port;
        this.data = new ClientData();
    }

    public void connect() throws IOException {
        serverSocket = new Socket(serverIp, serverPort);
     //   serverSocket.setSoTimeout((int) DURATION_BEFORE_TIMEOUT.toMillis());
        LOGGER.info("Successfully connected to the server.");
        this.messageReceiver = new MessageReceiver(new BufferedReader(new InputStreamReader(serverSocket.getInputStream())));
        this.messageSender = new MessageSender(new PrintWriter(serverSocket.getOutputStream(), true));
    }

    public boolean validate(String username) throws IOException {
        LOGGER.info("Attempting to join to server using username: " + username);
        messageSender.sendLoginRequest(username);

        if (messageReceiver.getLoginResponse()) {
            data.setUsername(username);
            return true;
        }

        return false;
    }

    public void getLobbyList() throws IOException {
        data.setLobbyList(messageReceiver.getLobbyListResponse());
    }

    public void closeConnection() throws IOException {
        serverSocket.close();
    }


}
