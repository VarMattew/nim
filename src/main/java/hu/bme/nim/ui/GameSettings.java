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
 * @param heaps            kezdő kupacméretek
 * @param rules            lépésszabály
 * @param difficulty       a gép nehézségi szintje (AI vs. AI módban az "A" gépé)
 * @param starter          ki kezd; {@code null} = véletlen
 * @param tutorMode        oktató mód (Grundy-számok megjelenítése)
 * @param aiVsAi           bemutató mód: a gép önmaga ellen játszik, az ember csak néz
 * @param secondDifficulty AI vs. AI módban a "B" gép nehézsége ({@code null} = ugyanaz, mint {@code difficulty})
 */
public record GameSettings(List<Integer> heaps, Rules rules, Difficulty difficulty, Player starter,
                           boolean tutorMode, boolean aiVsAi, Difficulty secondDifficulty) {

    public GameSettings {
        heaps = List.copyOf(heaps);
        if (secondDifficulty == null) {
            secondDifficulty = difficulty;
        }
    }

    /** A tényleges kezdő játékos: ha {@code starter == null}, véletlenszerűen dől el. */
    public Player resolveStarter(Random random) {
        return starter != null ? starter : (random.nextBoolean() ? Player.HUMAN : Player.AI);
    }

    public GameState initialState(Player first) {
        return new GameState(heaps, first, rules);
    }

    /**
     * A játékos megjelenített neve. AI vs. AI módban a {@link Player#HUMAN} helyet is gép tölti be:
     * "Gép A" (az emberi hely) és "Gép B".
     */
    public String nameOf(Player player) {
        if (!aiVsAi) {
            return player.displayName();
        }
        return player == Player.HUMAN ? "Gép A" : "Gép B";
    }

    /** Az adott helyen játszó gép nehézsége (AI vs. AI módban). */
    public Difficulty difficultyOf(Player player) {
        return player == Player.HUMAN ? difficulty : secondDifficulty;
    }

    /** Alapértelmezett beállítás az első indításhoz. */
    public static GameSettings defaults() {
        return new GameSettings(List.of(7, 5, 3), Rules.classicNim(), Difficulty.MEDIUM, Player.HUMAN, true, false, null);
    }
}
