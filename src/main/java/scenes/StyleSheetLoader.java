package scenes;

import javafx.scene.Scene;

public class StyleSheetLoader {


    /**
     * Jednoducha metoda pro nacteni stylu fontu pro scenu
     * @param scene scena, pro kterou nacitame fonty
     */
    public static void loadStyleSheet(Scene scene) {
        scene.getStylesheets().add("/fontstyle.css");
    }
}
