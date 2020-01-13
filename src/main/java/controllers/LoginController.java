package controllers;

import client.Client;

import java.util.regex.Pattern;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.Setter;

/**
 * Trida, slouzici k ovladani Login sceny
 */
public class LoginController {

    /**
     * Maximalni delka uzivatelskeho jmena
     */
    private static final int MAX_USERNAME_LENGTH = 10;
    /**
     * Pattern pro zjisteni, zda-li je uzivatelske jmeno pouze cisla a pismena - aby uzivatel nepouzival zakazane znaky
     * jako ",", ";" atd.
     */
    private static final Pattern LOGIN_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    /**
     * Pole s nazvem uzivatele
     */
    @FXML
    @Getter
    private TextField loginField;
    /**
     * Pole s ip a portem
     */
    @FXML
    @Getter
    private TextField addressField;
    /**
     * Chybovy text
     */
    @FXML
    @Getter
    private Text errorText;

    /**
     * Pattern pro zjisteni zda-li je string formatu ip adresa:port
     * Muze byt i localhost:port
     */
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^"
            + "(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}"
            + "|"
            + "localhost"
            + "|"
            + "(([0-9]{1,3}\\.){3})[0-9]{1,3})"
            + ":"
            + "[0-9]{1,5}$");
    /**
     * Reference na klienta
     */
    @Setter
    private Client client;

    /**
     * Akce pri stisknuti login buttonu
     */
    @FXML
    protected void loginEvent() {
        if (loginField.getText() == null || loginField.getText().isEmpty()) {
            errorText.setText("Please enter username");
            return;
        }

        //Presah maximalniho poctu znaku
        if (loginField.getText().length() > MAX_USERNAME_LENGTH) {
            errorText.setText("Please use shorter username (max " + MAX_USERNAME_LENGTH + ") letters");
            return;
        }

        //Ilegalni jmeno - Dealer se pouziva ve zpravach ve hre
        if (loginField.getText().equals("Dealer")) {
            errorText.setText("Error name \"Dealer\" is forbidden, please select another one");
            return;
        }

        //Ilegalni znaky
        if (!LOGIN_PATTERN.matcher(loginField.getText()).matches()) {
            errorText.setText("Error forbidden symbol, please be sure to only use alphabetic and/or numeric symbols");
            return;
        }

        //Spatny format adresy
        if (!ADDRESS_PATTERN.matcher(addressField.getText()).matches()) {
            errorText.setText("Error, check address format");
            return;
        }

        client.login();
    }

    /**
     * Zobrazi, ze se nepodarilo pripojit k serveru
     */
    public void showServerUnreachable() {
        errorText.setText("Server is unreachable, please try again");
    }

    /**
     * Zobrazi, ze spojeni se serverem bylo uzavreno
     */
    public void showConnectionClosed() {
        errorText.setText("Previous connection was closed, either because server was unreachable " +
                "or due to incorrect message received");
    }
}
