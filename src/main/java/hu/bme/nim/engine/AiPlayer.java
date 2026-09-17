package hu.bme.nim.engine;

import hu.bme.nim.model.GameState;
import hu.bme.nim.model.Move;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Grundy-számok alapján döntő gépi játékos.
 * <ul>
 *     <li>Ha az állás Grundy-száma nem 0 (nyerő állás), olyan lépést választ, amely után 0 lesz.</li>
 *     <li>Ha 0 (vesztő állás), nincs jó lépés: a nehézségi szinttől függően véletlen legális
 *         lépést tesz, vagy "húzza az időt", hogy az ellenfélnek legyen alkalma hibázni.</li>
 *     <li>Könnyű szinten nyerő állásból is időnként szándékosan hibázik.</li>
 * </ul>
 */
public final class AiPlayer {

    private final NimAnalyzer analyzer;
    private final Difficulty difficulty;
    private final Random random;

    /**
     * Gépi játékos új véletlenszám-forrással.
     *
     * @param analyzer   az állás-elemző
     * @param difficulty a nehézségi szint
     */
    public AiPlayer(NimAnalyzer analyzer, Difficulty difficulty) {
        this(analyzer, difficulty, new Random());
    }

    /**
     * Gépi játékos megadott véletlenszám-forrással (teszteléshez determinisztikus maggal).
     *
     * @param analyzer   az állás-elemző
     * @param difficulty a nehézségi szint
     * @param random     a véletlenszám-generátor a lépésválasztáshoz
     * @throws NullPointerException ha bármely paraméter {@code null}
     */
    public AiPlayer(NimAnalyzer analyzer, Difficulty difficulty, Random random) {
        this.analyzer = Objects.requireNonNull(analyzer, "analyzer");
        this.difficulty = Objects.requireNonNull(difficulty, "difficulty");
        this.random = Objects.requireNonNull(random, "random");
    }

    /**
     * A játékos nehézségi szintje.
     *
     * @return a szint
     */
    public Difficulty difficulty() {
        return difficulty;
    }

    /**
     * Lépést választ az adott állásban.
     *
     * @param state az állás, amelyben a gép lép
     * @return egy legális lépés; nyerő állásból (a szándékos hibázást leszámítva) mindig 0 Grundy-számú állásba vezet
     * @throws IllegalStateException ha a játék már véget ért
     */
    public Move chooseMove(GameState state) {
        if (state.isOver()) {
            throw new IllegalStateException("A játék véget ért, nincs lépés.");
        }
        List<Move> winning = analyzer.winningMoves(state);
        boolean blunder = difficulty.blunderProbability() > 0
                && random.nextDouble() < difficulty.blunderProbability();

        if (!winning.isEmpty() && !blunder) {
            return winning.get(random.nextInt(winning.size()));
        }
        // Vesztő állás (vagy szándékos hiba): nincs 0-ba vezető lépés.
        return switch (difficulty) {
            case HARD -> stallingMove(state);
            case EASY, MEDIUM -> randomMove(state);
        };
    }

    /** Véletlen legális lépés. */
    private Move randomMove(GameState state) {
        List<Move> legal = state.legalMoves();
        return legal.get(random.nextInt(legal.size()));
    }

    /** Időhúzás: 1 kavics a legnagyobb kupacból – a lehető legtöbb lépést hagyja a játékban. */
    private Move stallingMove(GameState state) {
        int best = -1;
        int bestSize = 0;
        for (int i = 0; i < state.heapCount(); i++) {
            if (state.heap(i) > bestSize) {
                bestSize = state.heap(i);
                best = i;
            }
        }
        return new Move(best, 1);
    }
}
