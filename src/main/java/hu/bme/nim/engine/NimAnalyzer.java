package hu.bme.nim.engine;

import hu.bme.nim.model.GameState;
import hu.bme.nim.model.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Állás-elemzés a Grundy-számok alapján: megkeresi azokat a lépéseket, amelyek 0 Grundy-számú
 * ("mag", {@code T(v) = II}) állásba vezetnek. Ezek a nyerő lépések.
 * <p>
 * Ha az aktuális állás Grundy-száma {@code X ≠ 0}, a Sprague–Grundy tétel garantálja, hogy van
 * olyan kupac és elvétel, amely után az új XOR 0. Klasszikus Nimnél ez éppen az
 * {@code nᵢ → nᵢ XOR X} csökkentés valamely {@code nᵢ XOR X < nᵢ} kupacon; az általános
 * (korlátozott elvételű) esetre a lépéseken végigmenő keresés minden szabályra helyes marad.
 */
public final class NimAnalyzer {

    private final GrundyCalculator calculator;

    /**
     * Elemző a megadott kalkulátorral.
     *
     * @param calculator a Grundy-kalkulátor, amelynek szabálya az elemzett állásokéval egyezik
     * @throws NullPointerException ha {@code calculator == null}
     */
    public NimAnalyzer(GrundyCalculator calculator) {
        this.calculator = Objects.requireNonNull(calculator, "calculator");
    }

    /**
     * A használt kalkulátor.
     *
     * @return a kalkulátor
     */
    public GrundyCalculator calculator() {
        return calculator;
    }

    /**
     * Az összes olyan legális lépés, amely után az állás Grundy-száma 0.
     *
     * @param state az állás
     * @return új lista a nyerő lépésekkel kupaconként növekvő elvétellel; üres, ha az állás már
     *         {@code T(v) = II} (nincs nyerő lépés) vagy a játék véget ért
     */
    public List<Move> winningMoves(GameState state) {
        int x = calculator.grundy(state);
        List<Move> result = new ArrayList<>();
        if (x == 0 || state.isOver()) {
            return result;
        }
        for (int i = 0; i < state.heapCount(); i++) {
            int heap = state.heap(i);
            int gHeap = calculator.grundy(heap);
            // A többi kupac XOR-ja: X kizárva ebből a kupacból.
            int others = x ^ gHeap;
            // Olyan új méret kell, amelynek Grundy-száma == others (így az új XOR 0 lesz).
            int maxTake = state.rules().maxTakeFrom(heap);
            for (int take = 1; take <= maxTake; take++) {
                if (calculator.grundy(heap - take) == others) {
                    result.add(new Move(i, take));
                }
            }
        }
        return result;
    }

    /**
     * Van-e nyerő lépése a soron következőnek ({@code T(v) = I}).
     *
     * @param state az állás
     * @return {@code true}, ha a játék tart és a Grundy-szám nem 0
     */
    public boolean hasWinningMove(GameState state) {
        return !state.isOver() && calculator.isNPosition(state);
    }
}
