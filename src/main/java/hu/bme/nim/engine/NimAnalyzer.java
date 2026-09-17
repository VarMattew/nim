package hu.bme.nim.engine;

import hu.bme.nim.model.GameState;
import hu.bme.nim.model.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Állás-elemzés a Grundy-számok alapján: megkeresi azokat a lépéseket, amelyek 0 Grundy-számú
 * ("mag", P-állás) állásba vezetnek. Ezek a nyerő lépések.
 * <p>
 * Ha az aktuális állás Grundy-száma {@code X ≠ 0}, a Sprague–Grundy tétel garantálja, hogy van
 * olyan kupac és elvétel, amely után az új XOR 0. Klasszikus Nimnél ez éppen az
 * {@code nᵢ → nᵢ XOR X} csökkentés valamely {@code nᵢ XOR X < nᵢ} kupacon; az általános
 * (korlátozott elvételű) esetre a lépéseken végigmenő keresés minden szabályra helyes marad.
 */
public final class NimAnalyzer {

    private final GrundyCalculator calculator;

    public NimAnalyzer(GrundyCalculator calculator) {
        this.calculator = Objects.requireNonNull(calculator, "calculator");
    }

    public GrundyCalculator calculator() {
        return calculator;
    }

    /**
     * Az összes olyan legális lépés, amely után az állás Grundy-száma 0.
     * Üres lista, ha az aktuális állás már P-állás (nincs nyerő lépés).
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

    /** Van-e nyerő lépése a soron következőnek (N-állás). */
    public boolean hasWinningMove(GameState state) {
        return !state.isOver() && calculator.isNPosition(state);
    }
}
