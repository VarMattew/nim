package hu.bme.nim.engine;

/** Az AI nehézségi szintje. */
public enum Difficulty {
    /** Nyerő állásból is időnként (kb. 40%) véletlen lépést tesz – legyőzhető. */
    EASY("Könnyű", 0.4),
    /** Mindig optimális; vesztő állásból véletlen legális lépés. */
    MEDIUM("Közepes", 0.0),
    /** Mindig optimális; vesztő állásból húzza az időt (1 kavics a legnagyobb kupacból). */
    HARD("Nehéz", 0.0);

    private final String displayName;
    private final double blunderProbability;

    Difficulty(String displayName, double blunderProbability) {
        this.displayName = displayName;
        this.blunderProbability = blunderProbability;
    }

    /**
     * Magyar nyelvű megnevezés a felülethez.
     *
     * @return {@code "Könnyű"}, {@code "Közepes"} vagy {@code "Nehéz"}
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Annak valószínűsége, hogy nyerő állásban az AI szándékosan nem az optimális lépést választja.
     *
     * @return valószínűség a {@code [0, 1]} intervallumban; 0 a mindig optimális szinteknél
     */
    public double blunderProbability() {
        return blunderProbability;
    }

    /**
     * A megjelenített név, hogy a felület listáiban is olvasható legyen.
     *
     * @return ugyanaz, mint {@link #displayName()}
     */
    @Override
    public String toString() {
        return displayName;
    }
}
