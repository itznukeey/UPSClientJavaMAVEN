package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import javafx.application.Platform;
import serialization.Fields;
import serialization.TCPData;
import serialization.Values;

public class MessageReader implements Runnable {

    private BufferedReader input;

    private final Client client;

    private Boolean close = false;

    private PingService pingService;

    public MessageReader(BufferedReader input, Client client, PingService pingService) {
        this.input = input;
        this.client = client;
        this.pingService = pingService;
    }

    public synchronized void closeThread() {
        close = true;
    }

    /**
     * Běh vlákna
     */
    @Override
    public void run() {

        while (!close) {
            try {
                var message = input.readLine();
                if (message != null) {
                    pingService.setLastResponseReceived(LocalDateTime.now());
                    parse(message);
                    System.out.println(message);
                }
            } catch (IOException ex) {
                System.err.println("Incorrect data received, disconnecting");
                client.disconnect();
            }
        }
    }

    private void parse(String messageString) {
        var message = new TCPData(messageString);

        try {
            switch (message.getDataType()) {
                case REQUEST:
                    processRequest(message);
                    break;
                case RESPONSE:
                    processResponse(message);
                    break;
                case PING:

                default:
                    break;
            }
        } catch (NullPointerException | IllegalStateException ex) {
            ex.printStackTrace(); //todo remove this
            System.err.println("Received incorrect message");
            client.disconnect();
            System.exit(-1);
        }
    }

    private void processRequest(TCPData request) {

    }

    private void processResponse(TCPData message) {
        var response = message.valueOf(Fields.RESPONSE);

        if (response.equals(Values.LOGIN)) {
            if (message.valueOf(Fields.IS_UNIQUE).equals(Values.TRUE)) {
                client.setState(State.LOBBY_LIST);
                Platform.runLater(client::prepareLobbyListScene);
            } else {
                client.showUsernameNotUnique();
            }
            return;
        }

        if (response.equals(Values.LOBBY_LIST)) {

            var lobbyList = new ArrayList<Lobby>();
            message.getFields().forEach((field, value) -> {
                if (field.equals(Fields.DATA_TYPE) || field.equals(Fields.RESPONSE)) {
                    return;
                }

                String[] lobbyInfo = value.split(";");
                lobbyList.add(new Lobby(
                        Integer.parseInt(lobbyInfo[0]), Integer.parseInt(lobbyInfo[1]), Integer.parseInt(lobbyInfo[2])));
            });

            //Aktualizuje list, který předtím ještě seřadí podle id
            client.updateLobbyList(
                    lobbyList.stream().sorted(Comparator.comparingInt(Lobby::getId)).collect(Collectors.toList()));
            return;
        }

        if (response.equals(Values.JOIN_LOBBY)) {
            if (message.valueOf(Fields.IS_JOINABLE).equals(Values.TRUE)) {
                client.setLobbyId(Integer.parseInt(message.valueOf(Fields.LOBBY_ID)));
                Platform.runLater(() -> client.prepareLobbyScene(client.parseUsernames(message)));
            } else {
                client.showLobbyNotJoinable();
            }
        }


    }
}
