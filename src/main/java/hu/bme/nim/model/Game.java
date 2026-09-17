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

    /**
     * Egy megtett lépés naplóbejegyzése.
     *
     * @param turn   a lépés sorszáma (1-től)
     * @param player aki lépett
     * @param move   a lépés
     * @param before az állás a lépés előtt
     * @param after  az állás a lépés után
     */
    public record Entry(int turn, Player player, Move move, GameState before, GameState after) {
        /**
         * Naplósor.
         *
         * @return pl. {@code "3. Gép: 1 kavics a(z) 2. kupacból → [4, 4, 1]"}
         */
        @Override
        public String toString() {
            return turn + ". " + player.displayName() + ": " + move + " → " + after.heaps();
        }
    }

    private final GameState initialState;
    private GameState state;
    private final List<Entry> history = new ArrayList<>();

    /**
     * Új játszma a megadott kezdőállásból.
     *
     * @param initialState a kezdőállás
     * @throws NullPointerException ha {@code initialState == null}
     */
    public Game(GameState initialState) {
        this.initialState = Objects.requireNonNull(initialState, "initialState");
        this.state = initialState;
    }

    /**
     * A kezdőállás (visszavágóhoz).
     *
     * @return a játszma kezdőállása
     */
    public GameState initialState() {
        return initialState;
    }

    /**
     * Az aktuális állás.
     *
     * @return a legutóbbi lépés utáni állás
     */
    public GameState state() {
        return state;
    }

    /**
     * A megtett lépések naplója, időrendben.
     *
     * @return módosíthatatlan nézet
     */
    public List<Entry> history() {
        return Collections.unmodifiableList(history);
    }

    /**
     * Véget ért-e a játszma.
     *
     * @return {@code true}, ha nincs több kavics
     */
    public boolean isOver() {
        return state.isOver();
    }

    /**
     * A győztes.
     *
     * @return a győztes, vagy {@code null}, ha a játszma még tart
     */
    public Player winner() {
        return state.winner();
    }

    /**
     * A soron következő játékos megteszi a lépést.
     *
     * @param move a lépés
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

    /**
     * Visszavonja az utolsó lépést.
     *
     * @return {@code true}, ha volt mit visszavonni
     */
    public boolean undo() {
        if (history.isEmpty()) {
            return false;
        }
        Entry last = history.remove(history.size() - 1);
        state = last.before();
        return true;
    }

    /**
     * Ugyanezzel a kezdőállással új játszma ("visszavágó").
     *
     * @return új, üres történetű játszma
     */
    public Game rematch() {
        return new Game(initialState);
    }
}
