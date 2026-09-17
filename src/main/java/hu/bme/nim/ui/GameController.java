package hu.bme.nim.ui;

import hu.bme.nim.engine.AiPlayer;
import hu.bme.nim.engine.GrundyCalculator;
import hu.bme.nim.engine.NimAnalyzer;
import hu.bme.nim.engine.PositionType;
import hu.bme.nim.model.Game;
import hu.bme.nim.model.GameState;
import hu.bme.nim.model.Move;
import hu.bme.nim.model.Player;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * A játék képernyő vezérlője: emberi lépések, AI lépések, oktató panel, napló.
 * <p>
 * AI vs. AI (bemutató) módban mindkét helyen gép játszik; az ember Szünet/Folytatás és
 * "Egy lépés" gombokkal követi a játszmát.
 */
public final class GameController {

    /** Ennyi elvétel-gombig gombokat mutatunk, fölötte Spinnert. */
    private static final int MAX_TAKE_BUTTONS = 6;
    private static final Duration AI_DELAY = Duration.millis(900);
    private static final Duration AI_VS_AI_DELAY = Duration.millis(1400);

    @FXML private Label rulesLabel;
    @FXML private Label statusLabel;
    @FXML private Button undoButton;
    @FXML private SplitPane splitPane;
    @FXML private FlowPane heapsBox;
    @FXML private Label movePromptLabel;
    @FXML private HBox autoBox;
    @FXML private Button pauseButton;
    @FXML private Button stepButton;
    @FXML private Label autoInfoLabel;
    @FXML private HBox humanBox;
    @FXML private HBox takeButtonsBox;
    @FXML private Spinner<Integer> takeSpinner;
    @FXML private Button takeButton;
    @FXML private Button hintButton;
    @FXML private VBox tutorBox;
    @FXML private GridPane tutorGrid;
    @FXML private Label tutorXorLabel;
    @FXML private Label tutorTypeLabel;
    @FXML private Label tutorExplainLabel;
    @FXML private ListView<String> logList;

    private NimApplication app;
    private GameSettings settings;
    private Game game;
    private GrundyCalculator calc;
    private NimAnalyzer analyzer;
    /** Melyik helyen melyik gép játszik. Ember elleni játékban csak az {@link Player#AI} kulcs él. */
    private final Map<Player, AiPlayer> ais = new EnumMap<>(Player.class);
    private int binaryWidth;

    private final List<HeapView> heapViews = new ArrayList<>();
    private int selectedHeap = -1;
    private int markedCount = 0;
    private Move lastMove;
    private final PauseTransition aiTimer = new PauseTransition(AI_DELAY);
    private boolean aiThinking;
    /** AI vs. AI módban: szünetel-e az automatikus lejátszás. */
    private boolean paused;

    @FXML
    private void initialize() {
        takeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1));
        takeSpinner.valueProperty().addListener((obs, o, n) -> {
            if (n != null && selectedHeap >= 0) {
                markedCount = n;
                renderHeaps();
            }
        });
        aiTimer.setOnFinished(e -> performAiMove());
    }

    void init(NimApplication app, GameSettings settings) {
        this.app = app;
        this.settings = settings;
        this.calc = new GrundyCalculator(settings.rules());
        this.analyzer = new NimAnalyzer(calc);
        ais.put(Player.AI, new AiPlayer(analyzer, settings.difficultyOf(Player.AI)));
        if (settings.aiVsAi()) {
            ais.put(Player.HUMAN, new AiPlayer(analyzer, settings.difficultyOf(Player.HUMAN)));
            aiTimer.setDuration(AI_VS_AI_DELAY);
        }

        rulesLabel.setText("Szabály: " + settings.rules() + "   ·   "
                + (settings.aiVsAi()
                ? "Gép A: " + settings.difficultyOf(Player.HUMAN).displayName()
                + " vs. Gép B: " + settings.difficultyOf(Player.AI).displayName()
                : "Ellenfél: " + settings.difficulty().displayName()));
        tutorBox.setVisible(settings.tutorMode());
        tutorBox.setManaged(settings.tutorMode());
        hintButton.setVisible(settings.tutorMode() && !settings.aiVsAi());
        hintButton.setManaged(settings.tutorMode() && !settings.aiVsAi());
        autoBox.setVisible(settings.aiVsAi());
        autoBox.setManaged(settings.aiVsAi());
        humanBox.setVisible(!settings.aiVsAi());
        humanBox.setManaged(!settings.aiVsAi());

        startGame(settings.initialState(settings.resolveStarter(new Random())));
    }

    private void startGame(GameState initial) {
        aiTimer.stop();
        aiThinking = false;
        paused = false;
        game = new Game(initial);
        binaryWidth = GrundyCalculator.binaryWidth(initial);
        selectedHeap = -1;
        markedCount = 0;
        lastMove = null;

        heapsBox.getChildren().clear();
        heapViews.clear();
        for (int i = 0; i < initial.heapCount(); i++) {
            HeapView view = new HeapView(i);
            final int index = i;
            view.setOnMouseClicked(e -> onHeapClicked(index));
            heapViews.add(view);
            heapsBox.getChildren().add(view);
        }

        logList.getItems().clear();
        log("v0  Kezdőállás " + initial.heaps() + "   T(J) = " + calc.type(initial)
                + "   ·   Kezd: " + name(initial.currentPlayer()));

        refresh();
        if (!game.isOver() && isAi(game.state().currentPlayer())) {
            scheduleAiMove();
        }
    }

    private boolean isAi(Player player) {
        return ais.containsKey(player);
    }

    private String name(Player player) {
        return settings.nameOf(player);
    }

    // ---- emberi lépés ----

    private void onHeapClicked(int index) {
        if (!humanCanMove() || game.state().heap(index) == 0) {
            return;
        }
        selectedHeap = index;
        markedCount = 1;
        buildTakeControls();
        renderHeaps();
    }

    private boolean humanCanMove() {
        return !game.isOver() && !aiThinking && !isAi(game.state().currentPlayer());
    }

    private void buildTakeControls() {
        if (settings.aiVsAi()) {
            movePromptLabel.setText(game.isOver() ? "A bemutató véget ért."
                    : paused ? "Szünet – lépj tovább az „Egy lépés” gombbal, vagy folytasd."
                    : "A gépek egymás ellen játszanak.");
            pauseButton.setText(paused ? "Folytatás" : "Szünet");
            pauseButton.setDisable(game.isOver());
            stepButton.setDisable(game.isOver() || !paused || aiThinking);
            autoInfoLabel.setText(game.isOver() ? "" : "Következik: " + name(game.state().currentPlayer()));
            return;
        }
        takeButtonsBox.getChildren().clear();
        boolean hasSelection = selectedHeap >= 0 && humanCanMove();
        int max = hasSelection ? game.state().rules().maxTakeFrom(game.state().heap(selectedHeap)) : 0;

        boolean useSpinner = hasSelection && max > MAX_TAKE_BUTTONS;
        takeSpinner.setVisible(useSpinner);
        takeSpinner.setManaged(useSpinner);
        takeButton.setVisible(useSpinner);
        takeButton.setManaged(useSpinner);

        if (!hasSelection) {
            movePromptLabel.setText(humanCanMove() ? "Válassz egy kupacot!" : " ");
            return;
        }
        movePromptLabel.setText((selectedHeap + 1) + ". kupac kijelölve (" + game.state().heap(selectedHeap)
                + " kavics). Hányat veszel el?");
        if (useSpinner) {
            takeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, max, 1));
            markedCount = 1;
        } else {
            for (int k = 1; k <= max; k++) {
                final int count = k;
                Button b = new Button(String.valueOf(k));
                b.getStyleClass().add("take");
                b.setOnAction(e -> humanMove(new Move(selectedHeap, count)));
                b.setOnMouseEntered(e -> { markedCount = count; renderHeaps(); });
                b.setOnMouseExited(e -> { markedCount = 1; renderHeaps(); });
                takeButtonsBox.getChildren().add(b);
            }
        }
    }

    @FXML
    private void onTakeSpinner() {
        if (selectedHeap < 0 || !humanCanMove()) {
            return;
        }
        try {
            takeSpinner.commitValue();
        } catch (RuntimeException ignored) {
            // érvénytelen szöveg: az előző érték marad
        }
        Integer count = takeSpinner.getValue();
        if (count != null && game.state().isLegal(new Move(selectedHeap, count))) {
            humanMove(new Move(selectedHeap, count));
        }
    }

    private void humanMove(Move move) {
        if (!humanCanMove() || !game.state().isLegal(move)) {
            return;
        }
        applyMove(Player.HUMAN, move);
        if (!game.isOver()) {
            scheduleAiMove();
        }
    }

    // ---- AI lépés ----

    private void scheduleAiMove() {
        if (game.isOver() || !isAi(game.state().currentPlayer())) {
            return;
        }
        if (settings.aiVsAi() && paused) {
            refresh();
            return;
        }
        aiThinking = true;
        selectedHeap = -1;
        markedCount = 0;
        refresh();
        aiTimer.playFromStart();
    }

    private void performAiMove() {
        aiThinking = false;
        Player current = game.state().currentPlayer();
        if (game.isOver() || !isAi(current)) {
            refresh();
            return;
        }
        Move move = ais.get(current).chooseMove(game.state());
        applyMove(current, move);
        if (!game.isOver() && isAi(game.state().currentPlayer())) {
            scheduleAiMove(); // AI vs. AI: jön a következő gép (szünetben megáll)
        }
    }

    private void applyMove(Player player, Move move) {
        GameState after = game.play(move);
        lastMove = move;
        selectedHeap = -1;
        markedCount = 0;
        log("v" + game.history().size() + "  " + name(player) + ": " + move + "  →  " + after.heaps()
                + (settings.tutorMode() ? "   g = " + calc.grundy(after) + ", T = " + calc.type(after) : ""));
        refresh();
        if (game.isOver()) {
            // Animációs visszahívásból (PauseTransition) nem szabad showAndWait-et hívni,
            // ezért a dialógus a következő eseményciklusban nyílik.
            Platform.runLater(this::showEndDialog);
        }
    }

    // ---- AI vs. AI vezérlés ----

    @FXML
    private void onPauseResume() {
        paused = !paused;
        if (paused) {
            aiTimer.stop();
            aiThinking = false;
            refresh();
        } else {
            scheduleAiMove();
        }
    }

    @FXML
    private void onStep() {
        if (!settings.aiVsAi() || game.isOver() || aiThinking) {
            return;
        }
        aiTimer.stop();
        Player current = game.state().currentPlayer();
        if (isAi(current)) {
            applyMove(current, ais.get(current).chooseMove(game.state()));
        }
    }

    // ---- műveletek ----

    @FXML
    private void onUndo() {
        aiTimer.stop();
        aiThinking = false;
        boolean any = false;
        if (settings.aiVsAi()) {
            // Bemutatóban egy lépést vonunk vissza, és megállunk.
            any = game.undo();
            paused = true;
        } else {
            // Visszavonás addig, amíg újra az ember következik (az AI válaszát is visszavesszük).
            do {
                if (!game.undo()) {
                    break;
                }
                any = true;
            } while (game.state().currentPlayer() != Player.HUMAN);
        }
        if (any) {
            lastMove = null;
            selectedHeap = -1;
            markedCount = 0;
            log("↶  Visszavonás  →  " + game.state().heaps());
            refresh();
        }
    }

    @FXML
    private void onRematch() {
        startGame(game.initialState());
    }

    @FXML
    private void onNewSetup() {
        aiTimer.stop();
        try {
            app.showSetup(settings);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Nem sikerült betölteni a beállító képernyőt:\n" + e.getMessage());
            alert.setHeaderText("Hiba");
            alert.setGraphic(null);
            Theme.apply(alert);
            alert.showAndWait();
        }
    }

    @FXML
    private void onHint() {
        if (!humanCanMove()) {
            return;
        }
        List<Move> winning = analyzer.winningMoves(game.state());
        if (winning.isEmpty()) {
            tutorExplainLabel.setText("Ebből az állásból nincs nyerő lépés: T(v) = II, a Grundy-szám 0. "
                    + "Bármit lépsz, a gép helyes játékkal nyer – de ha hibázik, azonnal fordul a kocka.");
            return;
        }
        Move m = winning.get(0);
        selectedHeap = m.heapIndex();
        markedCount = m.count();
        buildTakeControls();
        if (takeSpinner.isVisible()) {
            takeSpinner.getValueFactory().setValue(m.count());
        }
        markedCount = m.count();
        renderHeaps();
        GameState after = game.state().apply(m);
        tutorExplainLabel.setText("Nyerő lépés: " + m + " → " + after.heaps()
                + ", ekkor a Grundy-számok XOR-ja 0 lesz (T = II az ellenfél számára)."
                + (winning.size() > 1 ? " Összesen " + winning.size() + " nyerő lépés van." : ""));
    }

    // ---- megjelenítés ----

    private void refresh() {
        buildTakeControls();
        renderHeaps();
        renderStatus();
        renderTutor();
        undoButton.setDisable(game.history().isEmpty());
    }

    private void renderHeaps() {
        GameState s = game.state();
        boolean interactive = humanCanMove();
        for (HeapView view : heapViews) {
            int i = view.index();
            int marked = i == selectedHeap ? Math.min(markedCount, s.heap(i)) : 0;
            int removed = lastMove != null && lastMove.heapIndex() == i ? lastMove.count() : 0;
            view.render(s.heap(i), marked, removed);
            view.setSelected(i == selectedHeap);
            view.setInteractive(interactive && s.heap(i) > 0);
        }
    }

    private void renderStatus() {
        statusLabel.getStyleClass().removeAll("human-turn", "ai-turn", "win", "lose");
        Player current = game.state().currentPlayer();
        if (game.isOver()) {
            Player winner = game.winner();
            if (settings.aiVsAi()) {
                statusLabel.setText(name(winner) + " nyert – ő vette el az utolsó kavicsot"
                        + (winner == game.initialState().currentPlayer() ? " (a kezdő)." : " (a második)."));
                statusLabel.getStyleClass().add("win");
            } else {
                boolean humanWon = winner == Player.HUMAN;
                statusLabel.setText(humanWon ? "Nyertél! Te vetted el az utolsó kavicsot." : "A gép nyert – ő vette el az utolsó kavicsot.");
                statusLabel.getStyleClass().add(humanWon ? "win" : "lose");
            }
        } else if (settings.aiVsAi()) {
            statusLabel.setText(paused ? "Szünet. Következik: " + name(current) : name(current) + " gondolkodik…");
            statusLabel.getStyleClass().add("ai-turn");
        } else if (aiThinking) {
            statusLabel.setText("A gép gondolkodik…");
            statusLabel.getStyleClass().add("ai-turn");
        } else {
            statusLabel.setText("Te lépsz. Kattints egy kupacra, majd válaszd ki, hány kavicsot veszel el.");
            statusLabel.getStyleClass().add("human-turn");
        }
    }

    private void renderTutor() {
        if (!settings.tutorMode()) {
            return;
        }
        GameState s = game.state();
        tutorGrid.getChildren().clear();
        addTutorRow(0, "Kupac", "Méret", "Grundy", "g", true);
        List<Integer> g = calc.heapGrundies(s);
        for (int i = 0; i < s.heapCount(); i++) {
            addTutorRow(i + 1, (i + 1) + ".",
                    GrundyCalculator.toBinary(s.heap(i), binaryWidth),
                    GrundyCalculator.toBinary(g.get(i), binaryWidth),
                    String.valueOf(g.get(i)), false);
        }
        int x = calc.grundy(s);
        PositionType type = calc.type(s);
        tutorXorLabel.setText("XOR  = " + GrundyCalculator.toBinary(x, binaryWidth) + "  (" + x + ")");
        tutorTypeLabel.setText("T(v" + game.history().size() + ") = " + type);
        tutorTypeLabel.getStyleClass().removeAll("tutor-type-I", "tutor-type-II");
        tutorTypeLabel.getStyleClass().add(type == PositionType.I ? "tutor-type-I" : "tutor-type-II");

        String mover = name(s.currentPlayer());
        if (game.isOver()) {
            tutorExplainLabel.setText("Végállapot: minden Grundy-szám 0. Aki ide lépett, nyert.");
        } else if (settings.aiVsAi()) {
            tutorExplainLabel.setText(type == PositionType.I
                    ? mover + " nyerő állásban van: olyan lépést keres, amely után az XOR 0."
                    : mover + " vesztő állásban van (XOR = 0): bármit lép, az ellenfélnek lesz nyerő lépése.");
        } else if (s.currentPlayer() == Player.HUMAN) {
            tutorExplainLabel.setText(type == PositionType.I
                    ? "Neked van nyerő stratégiád: lépj úgy, hogy a Grundy-számok XOR-ja 0 legyen (a \"Tipp\" megmutatja)."
                    : "Vesztő állásban vagy (XOR = 0): bármely lépés elrontja a 0-t. Húzd az időt, és várd a gép hibáját.");
        } else {
            tutorExplainLabel.setText(type == PositionType.I
                    ? "A gép nyerő állásban van, és 0-ra fogja hozni az XOR-t."
                    : "A gép vesztő állásban van (XOR = 0): bármit lép, neked lesz nyerő lépésed.");
        }
    }

    private void addTutorRow(int row, String a, String b, String c, String d, boolean header) {
        Label[] labels = {new Label(a), new Label(b), new Label(c), new Label(d)};
        for (int col = 0; col < labels.length; col++) {
            if (header) {
                labels[col].getStyleClass().add("tutor-header");
            }
            tutorGrid.add(labels[col], col, row);
        }
    }

    private void log(String line) {
        logList.getItems().add(line);
        logList.scrollTo(logList.getItems().size() - 1);
    }

    private void showEndDialog() {
        Player winner = game.winner();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Játék vége");
        alert.setGraphic(null);
        Theme.apply(alert);
        String typeInfo = "\nA kezdőállás típusa T(J) = " + calc.type(game.initialState()) + " volt"
                + " (kezdett: " + name(game.initialState().currentPlayer()) + ").";
        if (settings.aiVsAi()) {
            alert.setHeaderText(name(winner) + " nyert.");
            alert.setContentText(name(winner) + " vette el az utolsó kavicsot."
                    + "\nLépések száma: " + game.history().size() + typeInfo);
        } else {
            boolean humanWon = winner == Player.HUMAN;
            alert.setHeaderText(humanWon ? "Nyertél!" : "A gép nyert.");
            alert.setContentText((humanWon ? "Te vetted el az utolsó kavicsot." : "A gép vette el az utolsó kavicsot.")
                    + "\nLépések száma: " + game.history().size() + typeInfo);
        }
        ButtonType rematch = new ButtonType("Visszavágó");
        ButtonType setup = new ButtonType("Új beállítás");
        ButtonType close = new ButtonType("Bezár", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(rematch, setup, close);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == rematch) {
                onRematch();
            } else if (result.get() == setup) {
                onNewSetup();
            }
        }
    }
}
