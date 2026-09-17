package hu.bme.nim.ui;

import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;

/**
 * A BME-arculat (bordó–fehér) alkalmazása: stíluslap, ablakikon, dialógusok.
 * <p>
 * Minden ablak és dialógus ezen keresztül kapja meg a {@code style.css}-t, így a JavaFX
 * alapértelmezett (Modena) megjelenése sehol nem látszik.
 */
final class Theme {

    private static final String STYLESHEET = "style.css";
    private static final String ICON = "images/favicon.jfif";

    private static Image icon;

    private Theme() {
    }

    /**
     * A stíluslap URL-je.
     *
     * @return a {@code style.css} külső formájú URL-je
     * @throws IllegalStateException ha a stíluslap nem található az erőforrások között
     */
    static String stylesheet() {
        URL css = Theme.class.getResource(STYLESHEET);
        if (css == null) {
            throw new IllegalStateException("Hiányzó stíluslap: " + STYLESHEET);
        }
        return css.toExternalForm();
    }

    /**
     * Az ablakikon (favicon), első híváskor betöltve.
     *
     * @return az ikon, vagy {@code null}, ha a kép hiányzik – ekkor a program ikon nélkül fut
     */
    static Image icon() {
        if (icon == null) {
            URL url = Theme.class.getResource(ICON);
            if (url != null) {
                icon = new Image(url.toExternalForm());
            }
        }
        return icon;
    }

    /**
     * Stíluslap hozzáadása a színtérhez (csak egyszer).
     *
     * @param scene a színtér
     */
    static void apply(Scene scene) {
        if (!scene.getStylesheets().contains(stylesheet())) {
            scene.getStylesheets().add(stylesheet());
        }
    }

    /**
     * Ablakikon beállítása.
     *
     * @param stage az ablak
     */
    static void apply(Stage stage) {
        Image img = icon();
        if (img != null && !img.isError()) {
            stage.getIcons().setAll(img);
        }
    }

    /**
     * Stíluslap és ikon egy dialógusra (Alert stb.) – a megjelenítés előtt hívandó.
     *
     * @param dialog a dialógus
     */
    static void apply(Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().add(stylesheet());
        Scene scene = dialog.getDialogPane().getScene();
        if (scene != null && scene.getWindow() instanceof Stage stage) {
            apply(stage);
        }
    }
}
