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

    public String displayName() {
        return displayName;
    }

    /** Annak valószínűsége, hogy nyerő állásban az AI szándékosan nem az optimális lépést választja. */
    public double blunderProbability() {
        return blunderProbability;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
