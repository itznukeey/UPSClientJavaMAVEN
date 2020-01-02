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
import serialization.Fields;
import serialization.TCPData;
import serialization.Values;

public class Client {

    private final Stage stage;

    private String ip;

    private Integer port;

    private Socket socket;

    @Getter
    @Setter
    private State state;

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private Integer lobbyId;

    private LoginController loginController;

    private LobbiesController lobbiesController;

    private LobbyController lobbyController;

    private GameController gameController;

    @Getter
    private MessageReader messageReader;

    @Getter
    private MessageWriter messageWriter;

    private PingService pingService;

    public Client(Stage stage) {
        this.stage = stage;
        prepareLoginScene();
    }

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

    public void login() {
        //format je vzdy ip:port, takze staci split, pokud uzivatel neco jineho uz  to zachytil frontend
        String[] address = loginController.getAddressField().getText().split(":");
        //connect vrati bool
        if (connect(address[0], Integer.parseInt(address[1]))) {
            username = loginController.getLoginField().getText();
            messageWriter.sendAuthenticationRequest(username);
        }
    }

    private void prepareLoginAfterDC() {
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
            messageWriter.sendLobbyUpdateRequest();
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

    public void disconnect() {

        try {
            socket.shutdownInput();
            socket.shutdownOutput();

            //Zpusobi ze se vlakna v messageReaderu a pingService zavrou
            messageReader.closeThread();
            pingService.closeThread();

            state = State.AUTHENTICATION;
            prepareLoginAfterDC();

        } catch (IOException e) {
            System.err.println("Error while attempting to close socket");
        }

    }

    public void prepareLobbyScene(List<String> users) {
        try {
            var fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/lobby.fxml"));
            Parent lobbyParent = fxmlLoader.load();
            lobbyController = fxmlLoader.getController();
            lobbyController.setClient(this);

            lobbyController.updateUsersList(users);

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
        if (message.valueOf(Fields.IN_GAME).equals(Values.FALSE)) {
            prepareLobbyListScene();
        }
    }

    public void updatePlayerList(TCPData message) {
        lobbyController.updateUsersList(parseUsernames(message));
        messageWriter.sendPlayerListUpdated();
    }

    public void showPlayerConnected(TCPData message) {
        if (state == State.LOBBY) {
            lobbyController.showPlayerConnected(message.valueOf(Values.USERNAME));
            messageWriter.sendShownPlayerConnected();
        }
    }

    public void showPlayerDisconnected(TCPData message) {
        lobbyController.showPlayerDisconnected(message.valueOf(Values.USERNAME));
        messageWriter.sendShownPlayerDisconnected();
    }

    public void confirmParticipation() {
        var dialog = new TextInputDialog("200");
        var validationButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        var input = dialog.getEditor().getText();
        validationButton.addEventFilter(ActionEvent.ACTION, filter -> {
            if (!input.matches("\\d+") || Integer.parseInt(input) > 10000) {
                filter.consume();
            }
        });

        dialog.setTitle("Game will start soon");
        dialog.setHeaderText("Confirm your participation, place a bet (minimum 100, maximum 10k)");
        dialog.setContentText("Press OK to confirm your bet");
        dialog.showAndWait().ifPresent(action -> messageWriter.sendConfirmParticipation(dialog.getEditor().getText()));

    }

    public void showGameStartFailed() {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game could not start");
        alert.setHeaderText("Not enough players confirmed participation");
        alert.setContentText("Vote to start to make lobby start faster");
        alert.show();
    }

    public void updateBoard(TCPData message) {
        if (state == State.LOBBY) {
            state = State.GAME;
            prepareGameScene(message);
        } else {
            updateGame(message);
        }
    }

    private void prepareGameScene(TCPData message) {
        try {
            var fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/game.fxml"));
            Parent root = fxmlLoader.load();
            gameController = fxmlLoader.getController();
            gameController.setClient(this);
            gameController.buildScene(message);
            stage.setScene(new Scene(root));
        } catch (IOException ex) {
            System.err.println("Error fxml game scene file is corrupted");
        }
    }

    private void updateGame(TCPData message) {
        gameController.updateData(message);
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
        alert.setHeaderText("You have 60s to make your move");
        alert.setContentText("After 60s your move will be automatically taken as stand");
        alert.show();
    }

    public void showReconnectedFromSomewhereElse() {
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

    public void showGameCouldNotStart() {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Game could not start");
        alert.setHeaderText("Not enough players confirmed");
        alert.setContentText("At least two or more players need to confirm with initial bet for game to launch");
        alert.show();
    }
}
