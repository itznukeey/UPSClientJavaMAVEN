package client;

import controllers.LobbiesController;
import controllers.LobbyController;
import controllers.LoginController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import serialization.TCPData;

public class Client {

    private final Stage stage;
    private String ip;
    private int port;
    private Socket socket;
    @Setter
    private State state;

    @Setter
    private String username;

    private LoginController loginController;

    private LobbiesController lobbiesController;

    private LobbyController lobbyController;

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
            this.messageReader =
                    new MessageReader(new BufferedReader(new InputStreamReader(socket.getInputStream())), this);
            this.messageWriter = new MessageWriter(new PrintWriter(socket.getOutputStream()));
        } catch (IOException ex) {
            loginController.setServerUnreachable();
            return false;
        }

        messageReader.setOutput(messageWriter);
        //Spuštění vlákna s čtením zpráv
        new Thread(messageReader).start();
        return true;
    }

    private void prepareLoginScene() {
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
        }
    }

    public void prepareLobbyListScene() {
        username = loginController.getUsername();
        try {
            var fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/lobbies.fxml"));
            Parent lobbiesRoot = fxmlLoader.load();
            lobbiesController = fxmlLoader.getController();

            stage.setScene(new Scene(lobbiesRoot));

            state = State.LOBBY_LIST;
            lobbiesController.setClient(this);
            lobbiesController.start();
        } catch (IOException ex) {
            System.err.println("Error fxml file of lobbies scene is corrupted");
        }
    }

    public void updateLobbyList(List<Lobby> lobbyList) {
        lobbiesController.updateListView(lobbyList);
    }

    public void handleLobbyConnection(TCPData message) {
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error while closing socket");
        }

    }

    public void showUsernameNotUnique() {
        loginController.showUsernameNotUnique();
    }

    public void prepareLobbyScene() {
        try {
            var fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/lobby.fxml"));
            Parent lobbyParent = fxmlLoader.load();
            lobbyController = fxmlLoader.getController();

            //Zavre vlakno pro aktualizaci seznamu mistnosti protoze jiz neni potreba
            lobbiesController.closeLobbyListUpdater();

            stage.setScene(new Scene(lobbyParent));
            state = State.LOBBY;
        } catch (IOException e) {
            System.err.println("Error fxml file of lobby scene is corrupted");
        }

    }

    public void showLobbyNotJoinable() {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error while joining lobby");
        alert.setContentText("Lobby you tried to join has either started a game or is full");
        alert.setHeaderText("Cannot join selected lobby");
        alert.show();
    }
}
