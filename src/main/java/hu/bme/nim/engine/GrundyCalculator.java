package hu.bme.nim.engine;

import hu.bme.nim.model.GameState;
import hu.bme.nim.model.Rules;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

/**
 * Grundy-számozás a 1.19. definíció szerint:
 * <pre>
 *     g(p) = mex{ g(q) : pq ∈ E }
 * </pre>
 * ahol {@code mex} (minimal excludant) a legkisebb nemnegatív egész, amely nincs a halmazban.
 * <p>
 * Egyetlen kupacra a lehetséges átmenetek: {@code n → n − r}, ahol {@code r} a szabály szerint
 * megengedett elvétel. A végállapot ({@code n = 0}) Grundy-száma 0.
 * <p>
 * Ismert speciális esetek (a tesztek ellenőrzik):
 * <ul>
 *     <li>{@code 1..k} kavics vehető el: {@code g(n) = n mod (k+1)} – pl. 1–3 kavicsnál {@code n mod 4}</li>
 *     <li>korlátlan elvétel (klasszikus Nim): {@code g(n) = n}</li>
 * </ul>
 * Több kupac esetén a játék Grundy-száma a Sprague–Grundy tétel szerint a kupacok
 * Grundy-számainak XOR-ja ({@link #grundy(GameState)}).
 * <p>
 * Az osztály a kiszámolt értékeket memoizálja; egy példány egy adott {@link Rules}-hoz tartozik.
 */
public final class GrundyCalculator {

    private final Rules rules;
    /** {@code memo.get(n) == g(n)}; mindig 0-tól folytonosan feltöltve. */
    private final List<Integer> memo = new ArrayList<>();

    /**
     * Kalkulátor a megadott szabályhoz.
     *
     * @param rules a lépésszabály, amelyre a Grundy-számok vonatkoznak
     * @throws NullPointerException ha {@code rules == null}
     */
    public GrundyCalculator(Rules rules) {
        this.rules = Objects.requireNonNull(rules, "rules");
        memo.add(0); // g(0) = 0: végállapot
    }

    /**
     * A kalkulátorhoz tartozó szabály.
     *
     * @return a szabály
     */
    public Rules rules() {
        return rules;
    }

    /**
     * Egy {@code heapSize} méretű kupac Grundy-száma.
     *
     * @param heapSize a kupac mérete (nemnegatív)
     * @return {@code g(heapSize)}
     * @throws IllegalArgumentException ha a méret negatív
     */
    public int grundy(int heapSize) {
        if (heapSize < 0) {
            throw new IllegalArgumentException("A kupac mérete nem lehet negatív: " + heapSize);
        }
        ensureComputed(heapSize);
        return memo.get(heapSize);
    }

    /**
     * A teljes állás Grundy-száma: a kupacok Grundy-számainak XOR-ja (Sprague–Grundy tétel).
     *
     * @param state az állás; szabálya meg kell egyezzen a kalkulátorével
     * @return a Nim-összeg, azaz {@code g(n1) ⊕ g(n2) ⊕ …}
     * @throws IllegalArgumentException ha az állás szabálya eltér a kalkulátorétól
     */
    public int grundy(GameState state) {
        checkRules(state);
        int xor = 0;
        for (int h : state.heaps()) {
            xor ^= grundy(h);
        }
        return xor;
    }

    /**
     * Kupaconkénti Grundy-számok az állás sorrendjében (az oktató módhoz).
     *
     * @param state az állás
     * @return új lista: az {@code i}. elem az {@code i}. kupac Grundy-száma
     * @throws IllegalArgumentException ha az állás szabálya eltér a kalkulátorétól
     */
    public List<Integer> heapGrundies(GameState state) {
        checkRules(state);
        List<Integer> result = new ArrayList<>(state.heapCount());
        for (int h : state.heaps()) {
            result.add(grundy(h));
        }
        return result;
    }

    /**
     * Az állás típusa: {@code T(v) = II}, ha {@code g(v) = 0} (a 2. játékos nyer);
     * {@code T(v) = I}, ha {@code g(v) > 0} (a soron következő nyer).
     *
     * @param state az állás
     * @return a típus
     */
    public PositionType type(GameState state) {
        return PositionType.ofGrundy(grundy(state));
    }

    /**
     * {@code T(v) = II}: a soron következő játékos veszít optimális játék mellett, azaz a Grundy-szám 0.
     * A definíció szerint {@code P = { p : g(p) = 0 }}.
     *
     * @param state az állás
     * @return {@code true}, ha a Grundy-szám 0
     */
    public boolean isPPosition(GameState state) {
        return grundy(state) == 0;
    }

    /**
     * {@code T(v) = I}: a soron következő játékosnak van nyerő stratégiája ({@code N = { p : g(p) > 0 }}).
     *
     * @param state az állás
     * @return {@code true}, ha a Grundy-szám nem 0
     */
    public boolean isNPosition(GameState state) {
        return !isPPosition(state);
    }

    /**
     * Bináris kiíráshoz szükséges jegyszám: a legnagyobb kupacméret bináris hossza
     * (pl. 8 → 4 jegy, 7 → 3 jegy). Legalább 1.
     *
     * @param state az állás (tipikusan a kezdőállás, hogy a szélesség a játék végéig rögzített legyen)
     * @return a jegyszám
     */
    public static int binaryWidth(GameState state) {
        int max = 0;
        for (int h : state.heaps()) {
            max = Math.max(max, h);
        }
        return Math.max(1, Integer.toBinaryString(max).length());
    }

    /**
     * Fix szélességű bináris alak, balról nullákkal kitöltve (pl. {@code toBinary(5, 4) = "0101"}).
     * Ha az érték hosszabb a szélességnél, nem vág le.
     *
     * @param value a nemnegatív érték
     * @param width a kívánt legkisebb jegyszám
     * @return a bináris karakterlánc
     */
    public static String toBinary(int value, int width) {
        String s = Integer.toBinaryString(value);
        return "0".repeat(Math.max(0, width - s.length())) + s;
    }

    // ---- belső ----

    private void checkRules(GameState state) {
        if (!state.rules().equals(rules)) {
            throw new IllegalArgumentException(
                    "Az állás szabálya (" + state.rules() + ") nem egyezik a kalkulátorral (" + rules + ")");
        }
    }

    /** Iteratívan feltölti a memo táblát {@code n}-ig, hogy nagy kupacnál se legyen mély rekurzió. */
    private void ensureComputed(int n) {
        for (int size = memo.size(); size <= n; size++) {
            int maxTake = rules.maxTakeFrom(size);
            // A rákövetkezők Grundy-számai: g(size − r), r = 1..maxTake
            BitSet seen = new BitSet(maxTake + 1);
            for (int r = 1; r <= maxTake; r++) {
                seen.set(memo.get(size - r));
            }
            memo.add(mex(seen));
        }
    }

    /**
     * Minimal excludant: a legkisebb nemnegatív egész, amely nincs a halmazban.
     *
     * @param set a halmaz bitkészletként
     * @return a mex
     */
    static int mex(BitSet set) {
        return set.nextClearBit(0);
    }
}
