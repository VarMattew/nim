package hu.bme.nim.model;

/**
 * A két játékos (ülőhely). AI vs. AI módban a {@link #HUMAN} helyen is gép játszik;
 * a megjelenített nevet ilyenkor a felület dönti el.
 */
public enum Player {
    /** Az emberi játékos helye ("Játékos"). */
    HUMAN("Játékos"),
    /** A gépi ellenfél helye ("Gép"). */
    AI("Gép");

    private final String displayName;

    Player(String displayName) {
        this.displayName = displayName;
    }

    /**
     * A játékos alapértelmezett megjelenített neve.
     *
     * @return {@code "Játékos"} vagy {@code "Gép"}
     */
    public String displayName() {
        return displayName;
    }

    /**
     * A másik játékos.
     *
     * @return {@link #AI}, ha ez {@link #HUMAN}, és fordítva
     */
    public Player other() {
        return this == HUMAN ? AI : HUMAN;
    }
}
