package hu.bme.nim.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * A JavaFX alkalmazás: egyetlen ablak, amelyben a beállító és a játék nézet váltakozik.
 */
public final class NimApplication extends Application {

    private static final String TITLE = "NIM – Grundy-számos ellenfél · BME";
    private static final double WIDTH = 980;
    private static final double HEIGHT = 660;

    private Stage stage;

    public static void launchApp(String[] args) {
        Application.launch(NimApplication.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.stage = primaryStage;
        stage.setTitle(TITLE);
        stage.setMinWidth(780);
        stage.setMinHeight(540);
        Theme.apply(stage);
        showSetup(null);
        stage.show();
    }

    /** Beállító képernyő; {@code previous} az előző beállítás, ha volt (előtöltéshez). */
    public void showSetup(GameSettings previous) throws IOException {
        FXMLLoader loader = loader("setup.fxml");
        Parent root = loader.load();
        SetupController controller = loader.getController();
        controller.init(this, previous);
        setRoot(root);
    }

    /** Játék képernyő a megadott beállításokkal. */
    public void showGame(GameSettings settings) throws IOException {
        FXMLLoader loader = loader("game.fxml");
        Parent root = loader.load();
        GameController controller = loader.getController();
        controller.init(this, settings);
        setRoot(root);
    }

    private FXMLLoader loader(String fxml) {
        URL url = Objects.requireNonNull(NimApplication.class.getResource(fxml), "Hiányzó FXML: " + fxml);
        return new FXMLLoader(url);
    }

    private void setRoot(Parent root) {
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, WIDTH, HEIGHT);
            Theme.apply(scene);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
    }
}
