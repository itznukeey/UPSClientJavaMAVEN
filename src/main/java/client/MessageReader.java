package client;

import javafx.application.Platform;
import serialization.Fields;
import serialization.TCPData;
import serialization.Values;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;

public class MessageReader implements Runnable {

    /**
     * Reference na klienta
     */
    private final Client client;
    /**
     * Vstup ze socketu
     */
    private BufferedReader input;
    /**
     * Flag pro vypnuti vlakna
     */
    private Boolean stop = false;

    /**
     * Reference na pingovaci sluzbu pro pripojeni
     */
    private PingService pingService;

    /**
     * @param input       input stream ze socketu
     * @param client      reference na klienta
     * @param pingService reference na ping service
     */
    public MessageReader(BufferedReader input, Client client, PingService pingService) {
        this.input = input;
        this.client = client;
        this.pingService = pingService;
    }

    /**
     * Nastavi stop na true, aby se ukoncil while ve vlaknu a vlakno se zavrelo
     */
    public synchronized void closeThread() {
        this.stop = true;
    }

    /**
     * Run metoda, ktera bezi ve vlakne
     */
    @Override
    public void run() {

        while (!stop) {
            try {
                String message = null;
                if (input.ready()) {
                    //Server na konec kazde zpravy pridava newline tzn zprava jde vzdy precist pres readline
                    message = input.readLine();
                }
                if (message != null) {
                    pingService.setLastResponseReceived(LocalDateTime.now());
                    parse(message);
                }
            } catch (IOException ex) {
                //Pokud klient dostane nespravna data, nejedna se o spravny server -> disconnect
                System.err.println("Incorrect data received, disconnecting");
                Platform.runLater(client::disconnect);
            }
        }
    }

    /**
     * Zpracuje precteny string ze serveru
     *
     * @param messageString string ziskany ctenim ze serveru
     */
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
                //Server ping nikdy neposila, posila pouze response a request, odpoved na ping je vzdy response
                case PING:

                default:
                    break;
            }
        } catch (NullPointerException | IllegalStateException ex) {
            System.err.println("Received incorrect message");
            Platform.runLater(client::disconnect);
            System.exit(-1);
        }
    }

    /**
     * Zpracuje dany pozadavek
     *
     * @param message zprava s pozadavkem
     */
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
                Platform.runLater(client::prepareLobbyScene);
                break;

            case Values.SHOW_PLAYER_TURN:
                Platform.runLater(() -> client.showPlayerTurn(message));
                break;

            case Values.SHOW_PLAYER_RECONNECTED:
                Platform.runLater(() -> client.showPlayerReconnected(message));
                break;

            case Values.TURN:
                Platform.runLater(client::playerTurn);
                break;

            case Values.SHOW_RETURN_TO_LOBBY:
                Platform.runLater(() -> client.showReturnToLobby(message));
                break;
        }
    }

    /**
     * Zpracuje odpoved od serveru
     *
     * @param message zprava s odpovedi
     */
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
                Platform.runLater(() ->
                        client.parseLobbyList(message));
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
}
