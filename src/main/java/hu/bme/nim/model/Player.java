package hu.bme.nim.model;

/** A két játékos. */
public enum Player {
    HUMAN("Játékos"),
    AI("Gép");

    private final String displayName;

    Player(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public Player other() {
        return this == HUMAN ? AI : HUMAN;
    }
}
