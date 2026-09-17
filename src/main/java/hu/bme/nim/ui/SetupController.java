package hu.bme.nim.ui;

import hu.bme.nim.engine.Difficulty;
import hu.bme.nim.engine.GrundyCalculator;
import hu.bme.nim.engine.PositionType;
import hu.bme.nim.model.GameState;
import hu.bme.nim.model.HeapGenerator;
import hu.bme.nim.model.Player;
import hu.bme.nim.model.Rules;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** A beállító képernyő vezérlője. */
public final class SetupController {

    /** A "Ki kezd" lista elemei; {@code null} játékos = véletlen. */
    private record StarterOption(Player player, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    private static final List<StarterOption> STARTERS = List.of(
            new StarterOption(Player.HUMAN, "Én kezdek"),
            new StarterOption(Player.AI, "A gép kezd"),
            new StarterOption(null, "Véletlen"));

    @FXML private Spinner<Integer> heapCountSpinner;
    @FXML private FlowPane heapSizesPane;
    @FXML private Label totalLabel;
    @FXML private Spinner<Integer> randomMinSpinner;
    @FXML private Spinner<Integer> randomMaxSpinner;
    @FXML private Button randomButton;
    @FXML private Label errorLabel;

    @FXML private ToggleGroup rulesGroup;
    @FXML private RadioButton rulesStandard;
    @FXML private RadioButton rulesLimited;
    @FXML private RadioButton rulesClassic;
    @FXML private Spinner<Integer> maxTakeSpinner;
    @FXML private Label grundyHintLabel;

    @FXML private ComboBox<Difficulty> difficultyBox;
    @FXML private ComboBox<StarterOption> starterBox;
    @FXML private Label difficultyHintLabel;
    @FXML private CheckBox tutorCheck;
    @FXML private CheckBox aiVsAiCheck;
    @FXML private VBox aiVsAiBox;
    @FXML private ComboBox<Difficulty> difficultyBBox;
    @FXML private Label typeLabel;
    @FXML private Button startButton;

    private final List<Spinner<Integer>> heapSpinners = new ArrayList<>();
    private final HeapGenerator generator = new HeapGenerator(new Random());
    private NimApplication app;

    @FXML
    private void initialize() {
        heapCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, HeapGenerator.MAX_HEAPS, 3));
        randomMinSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, HeapGenerator.MAX_HEAP_SIZE, 1));
        randomMaxSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, HeapGenerator.MAX_HEAP_SIZE, 12));
        maxTakeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, HeapGenerator.MAX_HEAP_SIZE, 5));
        for (Spinner<Integer> s : List.of(heapCountSpinner, randomMinSpinner, randomMaxSpinner, maxTakeSpinner)) {
            commitOnFocusLost(s);
        }

        heapCountSpinner.valueProperty().addListener((obs, oldV, newV) -> rebuildHeapSpinners(newV, null));

        StringConverter<Difficulty> difficultyConverter = new StringConverter<>() {
            @Override public String toString(Difficulty d) { return d == null ? "" : d.displayName(); }
            @Override public Difficulty fromString(String s) { return null; }
        };
        difficultyBox.setItems(FXCollections.observableArrayList(Difficulty.values()));
        difficultyBox.setConverter(difficultyConverter);
        difficultyBox.valueProperty().addListener((obs, o, n) -> updateDifficultyHint());
        difficultyBBox.setItems(FXCollections.observableArrayList(Difficulty.values()));
        difficultyBBox.setConverter(difficultyConverter);

        aiVsAiBox.visibleProperty().bind(aiVsAiCheck.selectedProperty());
        aiVsAiBox.managedProperty().bind(aiVsAiCheck.selectedProperty());
        aiVsAiCheck.selectedProperty().addListener((obs, o, n) -> updateStarterLabels());

        starterBox.setItems(FXCollections.observableArrayList(STARTERS));
        starterBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(StarterOption item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : starterBox.getConverter().toString(item));
            }
        });

        rulesGroup.selectedToggleProperty().addListener((obs, o, n) -> onRulesChanged());
        maxTakeSpinner.valueProperty().addListener((obs, o, n) -> onRulesChanged());
        maxTakeSpinner.disableProperty().bind(rulesLimited.selectedProperty().not());
    }

    /**
     * Az alkalmazás hívja az FXML betöltése után.
     *
     * @param app      az alkalmazás (képernyőváltáshoz)
     * @param previous az űrlap előtöltéséhez használt korábbi beállítás, vagy {@code null}
     */
    void init(NimApplication app, GameSettings previous) {
        this.app = app;
        GameSettings s = previous != null ? previous : GameSettings.defaults();

        heapCountSpinner.getValueFactory().setValue(s.heaps().size());
        rebuildHeapSpinners(s.heaps().size(), s.heaps());

        if (s.rules().isUnlimited()) {
            rulesClassic.setSelected(true);
        } else if (s.rules().maxTake() == 3) {
            rulesStandard.setSelected(true);
        } else {
            rulesLimited.setSelected(true);
            maxTakeSpinner.getValueFactory().setValue(s.rules().maxTake());
        }
        difficultyBox.setValue(s.difficulty());
        starterBox.setValue(STARTERS.stream().filter(o -> o.player() == s.starter()).findFirst().orElse(STARTERS.get(0)));
        tutorCheck.setSelected(s.tutorMode());
        aiVsAiCheck.setSelected(s.aiVsAi());
        difficultyBBox.setValue(s.secondDifficulty() != null ? s.secondDifficulty() : Difficulty.HARD);
        onRulesChanged();
        updateDifficultyHint();
        updateStarterLabels();
    }

    /** AI vs. AI módban a "Ki kezd" lista szövegei a gépekre utalnak. */
    private void updateStarterLabels() {
        boolean ai = aiVsAiCheck.isSelected();
        starterBox.setConverter(new StringConverter<>() {
            @Override public String toString(StarterOption o) {
                if (o == null) return "";
                if (!ai) return o.label();
                return o.player() == null ? "Véletlen" : o.player() == Player.HUMAN ? "Gép A kezd" : "Gép B kezd";
            }
            @Override public StarterOption fromString(String s) { return null; }
        });
        // a gombcella frissítéséhez újra beállítjuk az értéket
        StarterOption current = starterBox.getValue();
        starterBox.setValue(null);
        starterBox.setValue(current);
    }

    // ---- kupacok ----

    private void rebuildHeapSpinners(int count, List<Integer> presetSizes) {
        List<Integer> old = currentHeaps();
        heapSizesPane.getChildren().clear();
        heapSpinners.clear();
        for (int i = 0; i < count; i++) {
            int value = presetSizes != null && i < presetSizes.size() ? presetSizes.get(i)
                    : i < old.size() ? old.get(i) : 1 + (i * 2) % 9;
            Spinner<Integer> spinner = new Spinner<>(0, HeapGenerator.MAX_HEAP_SIZE, value);
            spinner.setEditable(true);
            spinner.setPrefWidth(78);
            commitOnFocusLost(spinner);
            spinner.valueProperty().addListener((obs, o, n) -> updateSummary());
            heapSpinners.add(spinner);

            Label label = new Label((i + 1) + ". kupac");
            label.getStyleClass().add("hint");
            VBox box = new VBox(2, label, spinner);
            heapSizesPane.getChildren().add(box);
        }
        updateSummary();
    }

    private List<Integer> currentHeaps() {
        List<Integer> heaps = new ArrayList<>(heapSpinners.size());
        for (Spinner<Integer> s : heapSpinners) {
            heaps.add(s.getValue());
        }
        return heaps;
    }

    @FXML
    private void onRandomSizes() {
        int min = randomMinSpinner.getValue();
        int max = randomMaxSpinner.getValue();
        if (min > max) {
            showError("A minimális méret nem lehet nagyobb a maximálisnál.");
            return;
        }
        List<Integer> sizes = generator.generate(heapSpinners.size(), min, max);
        for (int i = 0; i < sizes.size(); i++) {
            heapSpinners.get(i).getValueFactory().setValue(sizes.get(i));
        }
        showError("");
    }

    @FXML
    private void onRandomAll() {
        int min = randomMinSpinner.getValue();
        int max = randomMaxSpinner.getValue();
        if (min > max) {
            showError("A minimális méret nem lehet nagyobb a maximálisnál.");
            return;
        }
        int count = 1 + new Random().nextInt(HeapGenerator.MAX_HEAPS);
        List<Integer> sizes = generator.generate(count, min, max);
        heapCountSpinner.getValueFactory().setValue(count); // ez újraépíti a spinnereket
        rebuildHeapSpinners(count, sizes);
        showError("");
    }

    // ---- szabályok ----

    private Rules currentRules() {
        if (rulesClassic.isSelected()) {
            return Rules.classicNim();
        }
        if (rulesLimited.isSelected()) {
            return Rules.limited(maxTakeSpinner.getValue());
        }
        return Rules.standard();
    }

    private void onRulesChanged() {
        Rules rules = currentRules();
        if (rules.isUnlimited()) {
            grundyHintLabel.setText("Kupaconkénti Grundy-szám: g(n) = n (a kupacméret maga)");
        } else {
            grundyHintLabel.setText("Kupaconkénti Grundy-szám: g(n) = n mod " + (rules.maxTake() + 1));
        }
        updateSummary();
    }

    private void updateDifficultyHint() {
        Difficulty d = difficultyBox.getValue();
        if (d == null) {
            difficultyHintLabel.setText("");
            return;
        }
        difficultyHintLabel.setText(switch (d) {
            case EASY -> "Könnyű: nyerő állásból is időnként hibázik – legyőzhető.";
            case MEDIUM -> "Közepes: mindig optimálisan lép; vesztő állásból véletlenül.";
            case HARD -> "Nehéz: mindig optimálisan lép; vesztő állásból húzza az időt, hogy hibázhass.";
        });
    }

    // ---- összegzés és indítás ----

    private void updateSummary() {
        List<Integer> heaps = currentHeaps();
        int total = heaps.stream().mapToInt(Integer::intValue).sum();
        totalLabel.setText(String.valueOf(total));
        startButton.setDisable(total == 0);
        if (total == 0 || heaps.isEmpty()) {
            typeLabel.setText("Legalább egy kavics szükséges.");
            return;
        }
        Rules rules = currentRules();
        GrundyCalculator calc = new GrundyCalculator(rules);
        GameState v0 = new GameState(heaps, Player.HUMAN, rules);
        int x = calc.grundy(v0);
        PositionType type = calc.type(v0);
        typeLabel.setText("Kezdőállás Grundy-száma: " + x + "   →   T(J) = " + type
                + (type == PositionType.I ? "  (a kezdőnek van nyerő stratégiája)" : "  (a másodiknak van nyerő stratégiája)"));
    }

    @FXML
    private void onStart() {
        List<Integer> heaps = currentHeaps();
        if (heaps.stream().mapToInt(Integer::intValue).sum() == 0) {
            showError("Legalább egy kavics szükséges.");
            return;
        }
        GameSettings settings = new GameSettings(heaps, currentRules(), difficultyBox.getValue(),
                starterBox.getValue().player(), tutorCheck.isSelected(),
                aiVsAiCheck.isSelected(), aiVsAiCheck.isSelected() ? difficultyBBox.getValue() : null);
        try {
            app.showGame(settings);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Nem sikerült betölteni a játék képernyőt:\n" + e.getMessage());
            alert.setHeaderText("Hiba");
            alert.setGraphic(null);
            Theme.apply(alert);
            alert.showAndWait();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    /**
     * Szerkeszthető Spinner: a beírt szöveg fókuszvesztésnél is érvényesüljön.
     *
     * @param spinner a beállítandó Spinner
     */
    private static void commitOnFocusLost(Spinner<Integer> spinner) {
        spinner.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                try {
                    spinner.commitValue();
                } catch (RuntimeException ignored) {
                    // érvénytelen szöveg: marad az előző érték
                }
                spinner.getEditor().setText(String.valueOf(spinner.getValue()));
            }
        });
    }
}
