package hu.bme.nim.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

/**
 * Egy kupac grafikus megjelenítése: cím, kavicsszám és a kavicsok körökként.
 * <p>
 * A kavicsok fölülről lefelé töltődnek; a "megjelölt" kavicsok (amelyeket a játékos éppen
 * elvenni készül, vagy amelyeket a tipp javasol) narancssárgák, az előző lépésben elvett
 * kavicsok helye szaggatott körvonallal látszik.
 */
final class HeapView extends VBox {

    private static final double RADIUS = 9;
    private static final int PER_ROW = 5;

    private final int index;
    private final Label countLabel = new Label();
    private final FlowPane pebbles = new FlowPane();

    HeapView(int index) {
        this.index = index;
        getStyleClass().add("heap");
        setMinWidth(PER_ROW * (RADIUS * 2 + 6) + 24);
        setPrefWidth(PER_ROW * (RADIUS * 2 + 6) + 24);

        Label title = new Label((index + 1) + ". kupac");
        title.getStyleClass().add("heap-title");
        countLabel.getStyleClass().add("heap-count");

        pebbles.setHgap(6);
        pebbles.setVgap(6);
        pebbles.setAlignment(Pos.TOP_CENTER);
        pebbles.setPrefWrapLength(PER_ROW * (RADIUS * 2 + 6));

        getChildren().addAll(title, countLabel, pebbles);
    }

    int index() {
        return index;
    }

    /**
     * Újrarajzolás.
     *
     * @param count   jelenlegi kavicsszám
     * @param marked  hány kavics legyen megjelölve (a kupac tetejéről számolva)
     * @param removed hány kavics helye látszódjon "szellemként" (az előző lépésben elvettek)
     */
    void render(int count, int marked, int removed) {
        countLabel.setText(String.valueOf(count));
        pebbles.getChildren().clear();
        for (int i = 0; i < count; i++) {
            Circle c = new Circle(RADIUS);
            c.getStyleClass().add("pebble");
            if (i < marked) {
                c.getStyleClass().add("marked");
            }
            pebbles.getChildren().add(c);
        }
        for (int i = 0; i < removed; i++) {
            Circle c = new Circle(RADIUS);
            c.getStyleClass().addAll("pebble", "removed");
            pebbles.getChildren().add(c);
        }
        getStyleClass().remove("empty");
        if (count == 0) {
            getStyleClass().add("empty");
        }
    }

    void setSelected(boolean selected) {
        getStyleClass().remove("selected");
        if (selected) {
            getStyleClass().add("selected");
        }
    }

    void setInteractive(boolean interactive) {
        getStyleClass().remove("disabled");
        if (!interactive) {
            getStyleClass().add("disabled");
        }
    }
}
