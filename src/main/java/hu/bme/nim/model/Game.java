package hu.bme.nim.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Egy játszma: az aktuális állás és a lépések története. Ez a (megváltoztatható) burkoló köti
 * össze a megváltoztathatatlan {@link GameState}-eket; a felület ezt figyeli.
 */
public final class Game {

    /** Egy megtett lépés naplóbejegyzése. */
    public record Entry(int turn, Player player, Move move, GameState before, GameState after) {
        @Override
        public String toString() {
            return turn + ". " + player.displayName() + ": " + move + " → " + after.heaps();
        }
    }

    private final GameState initialState;
    private GameState state;
    private final List<Entry> history = new ArrayList<>();

    public Game(GameState initialState) {
        this.initialState = Objects.requireNonNull(initialState, "initialState");
        this.state = initialState;
    }

    public GameState initialState() {
        return initialState;
    }

    public GameState state() {
        return state;
    }

    public List<Entry> history() {
        return Collections.unmodifiableList(history);
    }

    public boolean isOver() {
        return state.isOver();
    }

    public Player winner() {
        return state.winner();
    }

    /**
     * A soron következő játékos megteszi a lépést.
     *
     * @return az új állás
     * @throws IllegalArgumentException ha a lépés nem legális
     * @throws IllegalStateException    ha a játék már véget ért
     */
    public GameState play(Move move) {
        if (state.isOver()) {
            throw new IllegalStateException("A játék véget ért.");
        }
        GameState before = state;
        GameState after = before.apply(move);
        history.add(new Entry(history.size() + 1, before.currentPlayer(), move, before, after));
        state = after;
        return after;
    }

    /** Visszavonja az utolsó lépést (ha volt). */
    public boolean undo() {
        if (history.isEmpty()) {
            return false;
        }
        Entry last = history.remove(history.size() - 1);
        state = last.before();
        return true;
    }

    /** Ugyanezzel a kezdőállással új játszma ("visszavágó"). */
    public Game rematch() {
        return new Game(initialState);
    }
}
