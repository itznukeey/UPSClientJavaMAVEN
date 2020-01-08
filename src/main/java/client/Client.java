package client;

import controllers.GameController;
import controllers.LobbiesController;
import controllers.LobbyController;
import controllers.LoginController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import serialization.Constants;
import serialization.Fields;
import serialization.TCPData;
import serialization.Values;

public class Client {

    /**
     * JavaFX Stage
     */
    private final Stage stage;

    /**
     * IP serveru
     */
    private String ip;

    /**
     * Port serveru
     */
    private Integer port;

    /**
     * Socket
     */
    private Socket socket;

    /**
     * Stav klienta
     */
    @Getter
    @Setter
    private State state;

    @Getter
    @Setter
    private String username;

    /**
     * Controller pro login
     */
    private LoginController loginController;

    /**
     * Controller pro lobby list
     */
    private LobbiesController lobbiesController;

    /**
     * Controller pro lobby
     */
    private LobbyController lobbyController;

    /**
     * Controller pro hru
     */
    private GameController gameController;

    /**
     * Message reader objekt
     */
    @Getter
    private MessageReader messageReader;

    /**
     * Message writer objekt
     */
    @Getter
    private MessageWriter messageWriter;

    /**
     * Pingovaci vlakno pro zjisteni pripojeni
     */
    private PingService pingService;

    /**
     * Konstruktor klienta
     *
     * @param stage
     */
    public Client(Stage stage) {
        this.stage = stage;
        prepareLoginScene();
    }

    /**
     * @param ip
     * @param port
     * @return
     */
    public boolean connect(String ip, int port) {
        try {
            this.socket = new Socket(ip, port);
            this.ip = ip;
            this.port = port;
            this.messageWriter = new MessageWriter(new PrintWriter(socket.getOutputStream(), true));
            this.pingService = new PingService(this, messageWriter);
            this.messageReader = new MessageReader(new BufferedReader(new InputStreamReader(socket.getInputStream())),
                    this, pingService);
        } catch (IOException ex) {
            loginController.showServerUnreachable();
            return false;
        }

        var thread = new Thread(messageReader);
        thread.setDaemon(true);
        thread.start();

        thread = new Thread(pingService);
        thread.setDaemon(true);
        thread.start();

        return true;
    }

    public void killSocket() {
        messageReader.closeThread();
        messageWriter = null;
        try {
            socket.shutdownOutput();
            socket.shutdownInput();
            socket = null;
        } catch (IOException ex) {
            System.err.println("Error while attempting to close socket");
        }
    }

    public void disconnect() {

        try {
            //Zpusobi ze se vlakna v messageReaderu a pingService zavrou
            messageReader.closeThread();
            pingService.closeThread();

            socket.shutdownInput();
            socket.shutdownOutput();

            prepareLoginAfterDC();

        } catch (IOException e) {
            System.err.println("Error while attempting to close socket");
        }

    }

    public boolean reconnect() {
        try {
            this.socket = new Socket(ip, port);
            this.messageWriter = new MessageWriter(new PrintWriter(socket.getOutputStream(), true));
            this.messageReader = new MessageReader(new BufferedReader(new InputStreamReader(socket.getInputStream())),
                    this, pingService);

            messageWriter.sendAuthenticationRequest(username);
        } catch (IOException e) {
            System.err.println("Reconnect attempt failed");
            return false;
        }

        return true;
    }

    public void login() {
        String[] address = loginController.getAddressField().getText().split(":");
        if (connect(address[0], Integer.parseInt(address[1]))) {
            username = loginController.getLoginField().getText();
            messageWriter.sendAuthenticationRequest(username);
        }
    }

    public void prepareLoginAfterDC() {
        try {
            var fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent loginRoot = fxmlLoader.load();

            loginController = fxmlLoader.getController();
            loginController.setClient(this);
            loginController.getLoginField().setText(username);
            loginController.getAddressField().setText(ip + ":" + port);

            stage.setScene(new Scene(loginRoot));
            stage.setResizable(false);
            stage.show();

            state = State.AUTHENTICATION;
        } catch (IOException ex) {
            System.err.println("Error fxml file of login scene is corrupted");
            System.exit(-1);
        }
    }

    public void prepareLoginScene() {
        try {
            var fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent loginRoot = fxmlLoader.load();

            loginController = fxmlLoader.getController();
            loginController.setClient(this);

            stage.setScene(new Scene(loginRoot));
            stage.setResizable(true);
            stage.show();

            state = State.AUTHENTICATION;
        } catch (IOException ex) {
            System.err.println("Error fxml file of login scene is corrupted");
            System.exit(-1);
        }
    }

    public void prepareLobbyListScene() {
        try {
            var fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/lobbies.fxml"));
            Parent lobbiesRoot = fxmlLoader.load();
            lobbiesController = fxmlLoader.getController();
            messageWriter.sendLobbyListUpdateRequest();
            state = State.LOBBY_LIST;
            lobbiesController.setClient(this);
            stage.setScene(new Scene(lobbiesRoot));
        } catch (IOException ex) {
            System.err.println("Error fxml file of lobbies scene is corrupted");
            System.exit(-1);
        }
    }

    public void updateLobbyList(List<Lobby> lobbyList) {
        lobbiesController.updateListView(lobbyList);
    }

    public void prepareLobbyScene() {
        try {
            var fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/lobby.fxml"));
            Parent lobbyParent = fxmlLoader.load();
            lobbyController = fxmlLoader.getController();
            lobbyController.setClient(this);

            stage.setScene(new Scene(lobbyParent));
            state = State.LOBBY;
        } catch (IOException e) {
            System.err.println("Error fxml file of lobby scene is corrupted");
            System.exit(-1);
        }

    }

    public void showLobbyNotJoinable() {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error while joining lobby");
        alert.setContentText("Lobby you tried to join has either started a game or is full");
        alert.setHeaderText("Cannot join selected lobby");
        alert.show();
    }

    public List<String> parseUsernames(TCPData message) {
        var list = new ArrayList<String>();
        message.getFields().forEach((field, value) -> {
            if (!field.contains("client")) {
                return;
            }

            list.add(value);
        });
        return list;
    }

    public void restoreState(TCPData message) {
        var restoreState = message.valueOf(Fields.RESTORE_STATE);
        System.out.println("restoring state");
        switch (restoreState) {
            case Values.LOBBY_LIST:
                prepareLobbyListScene();
                break;
            case Values.LOBBY:
                prepareLobbyScene();
                break;
            case Values.GAME:
                prepareGameScene();
                break;
        }
    }

    public void updatePlayerList(TCPData message) {
        lobbyController.updateUsersList(parseUsernames(message));
        System.out.println("Updated playerlist from the server");
        messageWriter.sendPlayerListUpdated();
    }

    public void showPlayerConnected(TCPData message) {
        if (state == State.LOBBY) {
            lobbyController.showPlayerConnected(message.valueOf(Values.USERNAME));
            messageWriter.sendShownPlayerConnected();
        }
    }

    public void showPlayerDisconnected(TCPData message) {
        if (state == State.LOBBY) {
            lobbyController.showPlayerDisconnected(message.valueOf(Values.USERNAME));
            messageWriter.sendShownPlayerDisconnected();
        } else if (state == State.GAME) {
            gameController.showPlayerDisconnected(message);
            messageWriter.sendShownPlayerDisconnected();
        }
    }

    public void confirmParticipation() {
        var dialog = new TextInputDialog("1000");
        var validationButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        var input = dialog.getEditor();
        validationButton.addEventFilter(ActionEvent.ACTION, filter -> {
            if (!input.getCharacters().toString().matches("\\d+")) {
                filter.consume();
                return;
            }

            var value = Integer.parseInt(input.getCharacters().toString());
            if (!(value >= Constants.MIN_VALUE_BET && value <= Constants.MAX_VALUE_BET)) {
                filter.consume();
            }
        });

        dialog.setTitle("Game will start soon");
        dialog.setHeaderText("Confirm your participation, place a bet (minimum 100, maximum 10k)");
        dialog.setContentText("Press OK to confirm your bet");
        dialog.showAndWait().ifPresentOrElse(result ->
                        messageWriter.sendConfirmParticipation(dialog.getEditor().getText()),
                messageWriter::sendDeclineParticipation
        );

    }

    public void prepareGameScene() {
        try {
            var fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/game.fxml"));
            Parent root = fxmlLoader.load();
            gameController = fxmlLoader.getController();
            gameController.setClient(this);
            stage.setScene(new Scene(root));
            state = State.GAME;
            System.out.println("Game is prepared");
        } catch (IOException ex) {
            System.err.println("Error fxml game scene file is corrupted");
        }
    }

    public void updateBoard(TCPData message) {
        if (!gameController.isSceneBuilt()) {
            try {
                gameController.buildScene(message);
                gameController.setSceneBuilt(true);
            } catch (IOException ex) {
                System.err.println("Error fxml game scene file is corrupted");
            }
        } else {
            gameController.updateData(message);
        }
    }

    public void showPlayerReconnected(TCPData message) {
        gameController.showPlayerReconnected(message);
    }

    public void showResults(TCPData message) {
        gameController.showResults(message);
    }

    public void showPlayerTurn(TCPData message) {
        gameController.showTurn(message);
    }

    public void playerTurn() {
        gameController.setCanPlay(true);
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Your turn");
        alert.setHeaderText("You have 60s to make your next turn");
        alert.setContentText("After 60s has expired your turn will be considered as STAND");
        alert.show();
    }

    public void showReconnectedFromSomewhereElse() {
        disconnect();
        prepareLoginAfterDC();
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Disconnected from the server");
        alert.setHeaderText("You have been disconnected from the server");
        alert.setContentText("Someone else logged under your username");
        alert.show();
    }

    public void showRemovedFromLobby() {
        prepareLoginAfterDC();
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Removed from lobby");
        alert.setHeaderText("You have been removed from the lobby");
        alert.setContentText("Game you attempt to reconnect to has finished");
        alert.show();
    }

    public void showNotYourTurnDialog() {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Not your turn!");
        alert.setHeaderText("Wait for your turn");
        alert.setContentText("It is not your turn to play yet");
        alert.show();
    }

    public void showDoubleDownAfterHit() {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Attempting to double down after having already hit");
        alert.setHeaderText("You cannot double down after hitting");
        alert.setContentText("You can only double down in your first turn");
        alert.show();
    }

    public void showGameStartFailed() {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Game could not start");
        alert.setHeaderText("Not enough players confirmed");
        alert.setContentText("At least two or more players need to confirm with initial bet for game to launch");
        alert.show();
    }

    public void showReturnToLobby(TCPData message) {
        var timeSeconds = message.valueOf(Fields.TIME);
        gameController.showMessage("Game has finished, you will be returned to lobby in " + timeSeconds + " seconds");
    }

    public void showClientDidntConfirm() {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("You were removed from the lobby");
        alert.setHeaderText("You didnt bet / confirm game");
        alert.setContentText("You were removed from lobby, you may try to reconnect if game hasnt started without you");
        alert.show();
    }

}
