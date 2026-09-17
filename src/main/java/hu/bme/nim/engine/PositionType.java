package hu.bme.nim.engine;

import hu.bme.nim.model.GameState;

/**
 * Egy állás (vagy a játék) típusa.
 * <ul>
 *     <li>{@link #I}: az állásban soron következő (1.) játékosnak van nyerő stratégiája – Grundy-szám ≠ 0.</li>
 *     <li>{@link #II}: a másik (2.) játékosnak van nyerő stratégiája – Grundy-szám = 0.</li>
 * </ul>
 * A kezdőállás típusa a játék típusa: {@code T(J) = T(v0)}.
 */
public enum PositionType {
    /** A soron következő (1.) játékosnak van nyerő stratégiája. */
    I("T=I – a soron következő (1.) játékosnak van nyerő stratégiája"),
    /** A 2. játékosnak van nyerő stratégiája. */
    II("T=II – a 2. játékosnak van nyerő stratégiája");

    private final String description;

    PositionType(String description) {
        this.description = description;
    }

    /**
     * Magyar nyelvű magyarázat.
     *
     * @return a típus szöveges leírása
     */
    public String description() {
        return description;
    }

    /**
     * Típus a Grundy-szám alapján.
     *
     * @param grundy az állás Grundy-száma
     * @return {@link #II}, ha {@code grundy == 0}, egyébként {@link #I}
     */
    public static PositionType ofGrundy(int grundy) {
        return grundy == 0 ? II : I;
    }

    /**
     * Egy állás típusa a megadott kalkulátorral számolva.
     *
     * @param calculator a Grundy-kalkulátor
     * @param state      az állás
     * @return a típus
     */
    public static PositionType of(GrundyCalculator calculator, GameState state) {
        return ofGrundy(calculator.grundy(state));
    }
}
