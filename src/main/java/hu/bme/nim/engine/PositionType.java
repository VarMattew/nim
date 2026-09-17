package hu.bme.nim.engine;

import hu.bme.nim.model.GameState;

/**
 * Egy állás (vagy a játék) típusa.
 * <ul>
 *     <li>{@link #I}: az állásban soron következő (1.) játékosnak van nyerő stratégiája – Grundy-szám ≠ 0.</li>
 *     <li>{@link #II}: a másik (2.) játékosnak van nyerő stratégiája – Grundy-szám = 0.</li>
 * </ul>
 * A kezdőállás típusa a játék típusa: T(J) = T(v₀).
 */
public enum PositionType {
    I("T=I – a soron következő (1.) játékosnak van nyerő stratégiája"),
    II("T=II – a 2. játékosnak van nyerő stratégiája");

    private final String description;

    PositionType(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }

    /** Grundy-szám alapján: 0 → II, egyébként I. */
    public static PositionType ofGrundy(int grundy) {
        return grundy == 0 ? II : I;
    }

    public static PositionType of(GrundyCalculator calculator, GameState state) {
        return ofGrundy(calculator.grundy(state));
    }
}
