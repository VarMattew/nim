package hu.bme.nim.ui;

import hu.bme.nim.engine.Difficulty;
import hu.bme.nim.model.GameState;
import hu.bme.nim.model.Player;
import hu.bme.nim.model.Rules;

import java.util.List;
import java.util.Random;

/**
 * A beállító képernyőn összeállított játékbeállítások.
 *
 * @param heaps      kezdő kupacméretek
 * @param rules      lépésszabály
 * @param difficulty AI nehézségi szint
 * @param starter    ki kezd; {@code null} = véletlen
 * @param tutorMode  oktató mód (Grundy-számok megjelenítése)
 */
public record GameSettings(List<Integer> heaps, Rules rules, Difficulty difficulty, Player starter, boolean tutorMode) {

    public GameSettings {
        heaps = List.copyOf(heaps);
    }

    /** A tényleges kezdő játékos: ha {@code starter == null}, véletlenszerűen dől el. */
    public Player resolveStarter(Random random) {
        return starter != null ? starter : (random.nextBoolean() ? Player.HUMAN : Player.AI);
    }

    public GameState initialState(Player first) {
        return new GameState(heaps, first, rules);
    }

    /** Alapértelmezett beállítás az első indításhoz. */
    public static GameSettings defaults() {
        return new GameSettings(List.of(7, 5, 3), Rules.standard(), Difficulty.MEDIUM, Player.HUMAN, true);
    }
}
