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
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
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
            stage.setResizable(false);
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

            Platform.runLater(this::prepareLoginAfterDC);

        } catch (IOException e) {
            System.err.println("Error while attempting to close socket");
        }

    }

    public void showUsernameNotUnique() {
        loginController.showUsernameNotUnique();
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

    }

    public void updatePlayerList(TCPData message) {
        lobbyController.updateUsersList(parseUsernames(message));
        messageWriter.sendPlayerListUpdated();
    }

    public void showPlayerConnected(TCPData message) {
        lobbyController.showPlayerConnected(message.valueOf(Values.USERNAME));
        messageWriter.sendShownPlayerConnected();
    }

    public void showPlayerDisconnected(TCPData message) {
        lobbyController.showPlayerDisconnected(message.valueOf(Values.USERNAME));
        messageWriter.sendShownPlayerDisconnected();
    }

    public void confirmParticipation() {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game will start soon");
        alert.setHeaderText("Confirm your participation");
        alert.setContentText("Press OK to confirm participation, otherwise you will be removed from the lobby");
        var result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            messageWriter.sendConfirmParticipation();
        }
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
}
