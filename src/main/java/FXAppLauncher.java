/**
 * Tato trida spousti samotny program - je potreba externi main, jinak maven kompilator nezkompiluje program spravne
 * a on nebude vedet jak se dostat k javafx knihovne.
 */
public class FXAppLauncher {

    public static void main(String[] args) {
        JavaFXMain.main(args);
    }
}
