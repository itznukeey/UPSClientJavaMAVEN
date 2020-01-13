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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
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

/**
 * Trida klienta, ktera obsahuje metody pro ovladani frontendu. Jakakoliv metoda ovlivnujici stage musi
 * byt volana pres {@code Platform.runLater()}, aby byla synchronizovana s vlaknem, ve kterem je stage spustena
 */
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

    /**
     * Uzivatelske jmeno
     */
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
     * @param stage stage z JavaFX mainu
     */
    public Client(Stage stage) {
        this.stage = stage;
        stage.setTitle("Blackjack client UPS 2019/2020");
        prepareLoginScene();
    }

    /**
     * @param ip   ip adresa serveru z klienta
     * @param port port serveru
     * @return true, pokud java socket api nevyhodi zadny exception, jinak false - spojeni pravdepodobne neprobehlo
     * spravne
     */
    public boolean connect(String ip, int port) {
        try {
            System.out.println("Attempting to connect to " + ip + ":" + port);
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

    /**
     * Odpoji klienta - zavre message reader vlakno a socket - volano z
     */
    public void disconnect() {

        try {
            //Zpusobi ze se vlakna v messageReaderu a pingService zavrou
            messageReader.closeThread();
            pingService.closeThread();

            if (socket != null) {
                socket.shutdownInput();
                socket.shutdownOutput();
                socket = null;
            }

            prepareLoginAfterDC();

        } catch (IOException e) {
            System.err.println("Error while attempting to close socket");
        }

    }

    /**
     * Pokusi se pripojit k serveru
     */
    public void login() {
        String[] address = loginController.getAddressField().getText().split(":");
        if (connect(address[0], Integer.parseInt(address[1]))) {
            username = loginController.getLoginField().getText();
            messageWriter.sendAuthenticationRequest(username);
        }
    }

    /**
     * Ukaze dialog se ztracenym spojenim
     */
    public void showConnectionLostDialog() {
        Platform.runLater(() -> {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Client was disconnected from the server");
            alert.setHeaderText("Connection lost");
            alert.setContentText("Server appears to be unreachable, please try to log in later");
            alert.show();

            //Zavre pripojeni a nastavi login screen na posledni zadanou ip adresu a username
            prepareLoginAfterDC();
        });
    }

    /**
     * Pripravi scenu s loginem po odpojeni od serveru
     */
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

    /**
     * Pripravi scenu s loginem
     */
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

    /**
     * Pripravi scenu se seznamem hernich mistnosti
     */
    public void prepareLobbyListScene() {
        try {
            var fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/lobbies.fxml"));
            Parent lobbiesRoot = fxmlLoader.load();
            lobbiesController = fxmlLoader.getController();
            messageWriter.sendLobbyListUpdateRequest();
            state = State.LOBBY_LIST;
            lobbiesController.setClient(this);
            stage.setScene(new Scene(lobbiesRoot));
            stage.setResizable(true);
        } catch (IOException ex) {
            System.err.println("Error fxml file of lobbies scene is corrupted");
            System.exit(-1);
        }
    }

    /**
     * Aktualizuje stav vsech hernich mistnosti
     *
     * @param lobbyList seznam hernich mistnosti
     */
    public void updateLobbyList(List<Lobby> lobbyList) {
        lobbiesController.updateListView(lobbyList);
    }

    /**
     * Pripravi scenu s lobby
     */
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

    /**
     * Zpracuje zpravu se seznamem hernich mistnosti a aktualizuje lobby list v lobby scene
     *
     * @param message
     */
    public void parseLobbyList(TCPData message) {
        var lobbyList = new ArrayList<Lobby>();
        message.getFields().forEach((field, value) -> {
            if (field.equals(Fields.DATA_TYPE) || field.equals(Fields.RESPONSE)) {
                return;
            }

            String[] lobbyInfo = value.split(";");
            lobbyList.add(new Lobby(
                    Integer.parseInt(lobbyInfo[0]), Integer.parseInt(lobbyInfo[1]), Integer.parseInt(lobbyInfo[2])));
        });
        updateLobbyList(lobbyList.stream().sorted(Comparator.comparingInt(Lobby::getId)).collect(Collectors.toList()));
    }

    /**
     * Alert s lobby not joinable - uzivatel se zkusil pripojit do lobby, kde je rozehrana hra nebo maximum hracu
     */
    public void showLobbyNotJoinable() {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error while joining lobby");
        alert.setContentText("Lobby you tried to join has either started a game or is full");
        alert.setHeaderText("Cannot join selected lobby");
        alert.show();
    }

    /**
     * Zpracuje vsechny uzivatelska jmena ze zpravy
     *
     * @param message tcp data se zpravou
     * @return seznam vsech hracu
     */
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

    /**
     * Obnovi stav, ve kterem klient s danym uzivatelskym jmenem zustal
     *
     * @param message tcp data
     */
    public void restoreState(TCPData message) {
        var restoreState = message.valueOf(Fields.RESTORE_STATE);
        System.out.println("This username was already on the server, restoring its state...");
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

    /**
     * Aktualizuje seznam vsech hracu
     *
     * @param message zprava od serveru
     */
    public void updatePlayerList(TCPData message) {
        lobbyController.updateUsersList(parseUsernames(message));
        messageWriter.sendPlayerListUpdated();
    }

    /**
     * Zobrazi pripojeni noveho hrace
     *
     * @param message zprava o pripojeni noveho hrace do lobby
     */
    public void showPlayerConnected(TCPData message) {
        if (state == State.LOBBY) {
            lobbyController.showPlayerConnected(message.valueOf(Values.USERNAME));
            messageWriter.sendShownPlayerConnected();
        }
    }

    /**
     * Zobrazi zpravu o odpojeni hrace z lobby nebo ze hry
     *
     * @param message zprava o odpojeni hrace
     */
    public void showPlayerDisconnected(TCPData message) {
        if (state == State.LOBBY) {
            lobbyController.showPlayerDisconnected(message.valueOf(Values.USERNAME));
            messageWriter.sendShownPlayerDisconnected();
        } else if (state == State.GAME) {
            gameController.showPlayerDisconnected(message);
            messageWriter.sendShownPlayerDisconnected();
        }
    }

    /**
     * Spusti dialog s potvrzenim ucasti - uzivateli se zobrazi kolik chce vsadit, pokud okno vypne
     * dialog automaticky posle ze se zucastnit nechce a server ho odstrani z lobby
     */
    public void confirmParticipation() {
        var dialog = new TextInputDialog("1000");
        var validationButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        var input = dialog.getEditor();
        //Validace zda-li je v textovem poli cislo a je v danem rozmezi
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
        //pokud je stisknuty OK a je validni odesle confirmParticipation jinak declineParticipation
        dialog.showAndWait().ifPresentOrElse(result ->
                        messageWriter.sendConfirmParticipation(dialog.getEditor().getText()),
                messageWriter::sendDeclineParticipation
        );

    }

    /**
     * Pripravi scenu s hrou
     */
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

    /**
     * Aktualizuje stav hry
     * @param message zprava s daty hry
     */
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

    /**
     * Zobrazi zpravu o opetovnem pripojeni hrace
     * @param message zprava o opetovnem pripojeni hrace
     */
    public void showPlayerReconnected(TCPData message) {
        gameController.showPlayerReconnected(message);
    }

    /**
     * Zobrazi vysledky
     * @param message zprava s vysledky
     */
    public void showResults(TCPData message) {
        gameController.showResults(message);
    }

    /**
     * Zobrazi hracuv tah
     * @param message zprava o tahu hrace
     */
    public void showPlayerTurn(TCPData message) {
        gameController.showTurn(message);
    }

    /**
     * Oznami hraci ze je jeho tah
     */
    public void playerTurn() {
        gameController.setCanPlay(true);
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Your turn");
        alert.setHeaderText("You have 60s to make your next turn");
        alert.setContentText("After 60s has expired your turn will be considered as STAND");
        alert.show();
    }

    /**
     * Zobrazi, ze se uzivatelske jmeno pripojilo odjinud a odpoji se od serveru
     */
    public void showReconnectedFromSomewhereElse() {
        disconnect();
        prepareLoginAfterDC();
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Disconnected from the server");
        alert.setHeaderText("You have been disconnected from the server");
        alert.setContentText("Someone else logged under your username");
        alert.show();
    }

    /**
     * Zobrazi hraci, ze byl odstranen z lobby - odmitnutim sazky nebo neaktivite pri sazce
     */
    public void showRemovedFromLobby() {
        prepareLobbyListScene();
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Removed from lobby");
        alert.setHeaderText("You have been removed from the lobby");
        alert.setContentText("You declined the initial bet");
        alert.show();
        messageWriter.sendLobbyListUpdateRequest();
    }

    /**
     * Zobrazi uzivateli, ze neni jeho tah
     */
    public void showNotYourTurnDialog() {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Not your turn!");
        alert.setHeaderText("Wait for your turn");
        alert.setContentText("It is not your turn to play yet");
        alert.show();
    }

    /**
     * Zobrazi uzivateli, ze se pokusil zahrat double down po tom co uz zahral hit
     */
    public void showDoubleDownAfterHit() {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Attempting to double down after having already hit");
        alert.setHeaderText("You cannot double down after hitting");
        alert.setContentText("You can only double down in your first turn");
        alert.show();
    }

    /**
     * Zobrazi ze hru se nepodarilo spustit
     */
    public void showGameStartFailed() {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Game could not start");
        alert.setHeaderText("Not enough players confirmed");
        alert.setContentText("At least two or more players need to confirm with initial bet for game to launch");
        alert.show();
    }

    /**
     * Zobrazi oznameni o vraceni se do lobby
     * @param message zprava s casem
     */
    public void showReturnToLobby(TCPData message) {
        var timeSeconds = message.valueOf(Fields.TIME);
        gameController.showMessage("Game has finished, you will be returned to lobby in " + timeSeconds + " seconds");
    }

    /**
     * Zobrazi uzivateli, ze nepotvrdil sazku a byl odstranen z lobby
     */
    public void showClientDidntConfirm() {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("You were removed from the lobby");
        alert.setHeaderText("You didnt bet / confirm game");
        alert.setContentText("You were removed from lobby, you may try to reconnect if game hasnt started without you");
        alert.show();
    }

    /**
     * Zobrazi aktualniho hrace, ktery ma hrat
     *
     * @param message zprava s jmenem aktualniho hrace
     */
    public void showCurrentPlayer(TCPData message) {
        var player = message.valueOf(Fields.USERNAME);
        gameController.showMessage(player + " now has 60s to play");
    }

    /**
     * Zobrazi hrace, ktery byl preskocen kvuli neaktivite
     *
     * @param message zprava s jmenem preskoceneho hrace
     */
    public void showPlayerSkipped(TCPData message) {
        var player = message.valueOf(Fields.USERNAME);
        gameController.showMessage("Player " + player + " was skipped due to inactivity");
    }
}
