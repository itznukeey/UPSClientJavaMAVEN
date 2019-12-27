package client;

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
            this.messageWriter = new MessageWriter(new PrintWriter(socket.getOutputStream()));
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

            prepareLoginAfterDC();

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

    public void restoreState(State state) {
        if (state.equals(State.LOBBY_LIST)) {
            prepareLobbyListScene();
            return;
        }

        //todo fill
        if (state.equals(State.LOBBY)) {
            prepareLobbyScene(null);
        }

        //todo fill
        if (state.equals(State.GAME)) {

        }
    }

}
