package hu.bme.nim.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A játék egy állása: a kupacok mérete, a soron következő játékos és a szabályok.
 * <p>
 * Az osztály <b>megváltoztathatatlan</b> (immutable): egy lépés alkalmazása ({@link #apply(Move)})
 * új állást ad vissza. Ez teszi lehetővé, hogy az AI mellékhatás nélkül "előre nézzen".
 */
public final class GameState {

    private final List<Integer> heaps;
    private final Player currentPlayer;
    private final Rules rules;

    public GameState(List<Integer> heaps, Player currentPlayer, Rules rules) {
        Objects.requireNonNull(heaps, "heaps");
        Objects.requireNonNull(currentPlayer, "currentPlayer");
        Objects.requireNonNull(rules, "rules");
        if (heaps.isEmpty()) {
            throw new IllegalArgumentException("Legalább egy kupac szükséges.");
        }
        for (int h : heaps) {
            if (h < 0) {
                throw new IllegalArgumentException("A kupac mérete nem lehet negatív: " + h);
            }
        }
        this.heaps = Collections.unmodifiableList(new ArrayList<>(heaps));
        this.currentPlayer = currentPlayer;
        this.rules = rules;
    }

    /** Kényelmi konstruktor: kupacméretek felsorolással. */
    public static GameState of(Rules rules, Player first, int... heapSizes) {
        List<Integer> list = new ArrayList<>(heapSizes.length);
        for (int h : heapSizes) {
            list.add(h);
        }
        return new GameState(list, first, rules);
    }

    public List<Integer> heaps() {
        return heaps;
    }

    public int heap(int index) {
        return heaps.get(index);
    }

    public int heapCount() {
        return heaps.size();
    }

    public Player currentPlayer() {
        return currentPlayer;
    }

    public Rules rules() {
        return rules;
    }

    /** Összes hátralévő kavics. */
    public int totalStones() {
        int sum = 0;
        for (int h : heaps) {
            sum += h;
        }
        return sum;
    }

    /** Vége a játéknak, ha nincs több kavics. */
    public boolean isOver() {
        return totalStones() == 0;
    }

    /**
     * Normál játékban az nyer, aki az utolsó kavicsot elvette, azaz nem a soron következő.
     *
     * @return a győztes, vagy {@code null}, ha a játék még tart
     */
    public Player winner() {
        return isOver() ? currentPlayer.other() : null;
    }

    public boolean isLegal(Move move) {
        return move.heapIndex() < heaps.size()
                && rules.isLegalTake(heaps.get(move.heapIndex()), move.count());
    }

    /** Az összes legális lépés ebből az állásból. */
    public List<Move> legalMoves() {
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < heaps.size(); i++) {
            int max = rules.maxTakeFrom(heaps.get(i));
            for (int take = 1; take <= max; take++) {
                moves.add(new Move(i, take));
            }
        }
        return moves;
    }

    /**
     * Lépés alkalmazása: új állást ad vissza, amelyben a másik játékos következik.
     *
     * @throws IllegalArgumentException ha a lépés nem legális
     */
    public GameState apply(Move move) {
        if (!isLegal(move)) {
            throw new IllegalArgumentException("Nem legális lépés: " + move + " az állásban " + this);
        }
        List<Integer> next = new ArrayList<>(heaps);
        next.set(move.heapIndex(), next.get(move.heapIndex()) - move.count());
        return new GameState(next, currentPlayer.other(), rules);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameState other)) return false;
        return heaps.equals(other.heaps) && currentPlayer == other.currentPlayer && rules.equals(other.rules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(heaps, currentPlayer, rules);
    }

    @Override
    public String toString() {
        return "GameState" + heaps + ", lép: " + currentPlayer + ", szabály: " + rules;
    }
}
