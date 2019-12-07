package client;

import client.communication.MessageBuilder;
import client.communication.MessageParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

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

    private ClientInfo clientInfo;

    private MessageBuilder messageBuilder;

    private MessageParser messageParser;


    public Client(String ip, int port) {
        this.serverIp = ip;
        this.serverPort = port;
        this.clientInfo = new ClientInfo();
        this.messageBuilder = new MessageBuilder();
    }

    public void connect() throws IOException {
        serverSocket = new Socket(serverIp, serverPort);
        input = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        output = new PrintWriter(serverSocket.getOutputStream(), true);
        LOGGER.info("Successfully connected to the server.");
        this.messageParser = new MessageParser(input);
    }

    public boolean validate(String username) throws IOException {
        LOGGER.info("Attempting to join to server using username: " + username);
        output.write(messageBuilder.loginRequest(username));

        if (messageParser.getLoginResponse()) {
            clientInfo.setUsername(username);

            return true;
        }

        return false;
    }


}
