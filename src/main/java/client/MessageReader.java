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

    @Override
    public void run() {

        while (!close) {
            try {
                var message = input.readLine();
                if (message != null) {
                    pingService.setLastResponseReceived(LocalDateTime.now());
                    parse(message);
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

    private void processRequest(TCPData message) {
        var request = message.valueOf(Fields.REQUEST);

        switch (request) {

            case Values.UPDATE_PLAYER_LIST:
                Platform.runLater(() -> client.updatePlayerList(message));
                break;

            case Values.CONNECTION_CLOSED:
                Platform.runLater(client::showReconnectedFromSomewhereElse);
                break;

            case Values.REMOVED_FROM_LOBBY:
                Platform.runLater(client::showRemovedFromLobby);
                break;

            case Values.SHOW_PLAYER_CONNECTED:
                Platform.runLater(() -> client.showPlayerConnected(message));
                break;

            case Values.SHOW_PLAYER_DISCONNECTED:
                Platform.runLater(() -> client.showPlayerDisconnected(message));
                break;

            case Values.CONFIRM_PARTICIPATION:
                Platform.runLater(client::confirmParticipation);
                break;

            case Values.CLIENT_DIDNT_CONFIRM:
                Platform.runLater(client::prepareLobbyListScene);
                Platform.runLater(client::showClientDidntConfirm);
                Platform.runLater(() -> client.getMessageWriter().sendLobbyListUpdateRequest());
                break;

            case Values.SHOW_GAME_START_FAILED:
                Platform.runLater(client::showGameStartFailed);
                break;

            case Values.GAME:
                Platform.runLater(client::prepareGameScene);
                break;

            case Values.UPDATE_BOARD:
                Platform.runLater(() -> client.updateBoard(message));
                break;

            case Values.SHOW_RESULTS:
                Platform.runLater(() -> client.showResults(message));
                break;

            case Values.JOIN_LOBBY:
                Platform.runLater(() -> {
                    client.setLobbyId(Integer.parseInt(message.valueOf(Fields.LOBBY_ID)));
                    Platform.runLater(client::prepareLobbyScene);
                });
                break;

            case Values.SHOW_PLAYER_TURN:
                Platform.runLater(() -> client.showPlayerTurn(message));
                break;

            case Values.TURN:
                Platform.runLater(client::playerTurn);
                break;

            case Values.SHOW_RETURN_TO_LOBBY:
                Platform.runLater(() -> client.showReturnToLobby(message));
                break;
        }
    }

    private void processResponse(TCPData message) {
        var response = message.valueOf(Fields.RESPONSE);

        switch (response) {
            case Values.LOGIN:
                pingService.setSendPingMessages(true);
                if (message.valueOf(Fields.RESTORE_STATE).equals(Values.FALSE)) {
                    Platform.runLater(client::prepareLobbyListScene);
                } else {
                    Platform.runLater(() -> client.restoreState(message));
                }
                break;

            case Values.LOBBY_LIST:
                var lobbyList = parseLobbyList(message);
                Platform.runLater(() ->
                        client.updateLobbyList(
                                lobbyList.stream().sorted(Comparator.comparingInt(Lobby::getId)).collect(Collectors.toList())));
                break;

            case Values.JOIN_LOBBY:
                if (message.valueOf(Fields.IS_JOINABLE).equals(Values.TRUE)) {
                    Platform.runLater(client::prepareLobbyScene);
                } else {
                    Platform.runLater(client::showLobbyNotJoinable);
                }
                break;

            case Values.NOT_YOUR_TURN:
                Platform.runLater(client::showNotYourTurnDialog);
                break;

            case Values.DOUBLE_AFTER_HIT:
                Platform.runLater(client::showDoubleDownAfterHit);
                break;
        }
    }

    private ArrayList<Lobby> parseLobbyList(TCPData message) {
        var lobbyList = new ArrayList<Lobby>();
        message.getFields().forEach((field, value) -> {
            if (field.equals(Fields.DATA_TYPE) || field.equals(Fields.RESPONSE)) {
                return;
            }

            String[] lobbyInfo = value.split(";");
            lobbyList.add(new Lobby(
                    Integer.parseInt(lobbyInfo[0]), Integer.parseInt(lobbyInfo[1]), Integer.parseInt(lobbyInfo[2])));
        });
        return lobbyList;
    }
}
