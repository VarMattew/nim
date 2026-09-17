package hu.bme.nim.model;

/**
 * Egy lépés: a {@code heapIndex} indexű kupacból {@code count} kavics elvétele.
 *
 * @param heapIndex a kupac 0-tól számozott indexe
 * @param count     az elvett kavicsok száma (legalább 1)
 */
public record Move(int heapIndex, int count) {

    /**
     * Ellenőrzi a paramétereket.
     *
     * @throws IllegalArgumentException ha az index negatív vagy a darabszám kisebb 1-nél
     */
    public Move {
        if (heapIndex < 0) {
            throw new IllegalArgumentException("A kupac indexe nem lehet negatív: " + heapIndex);
        }
        if (count < 1) {
            throw new IllegalArgumentException("Legalább 1 kavicsot el kell venni, kapott: " + count);
        }
    }

    /**
     * Magyar nyelvű leírás a naplóhoz.
     *
     * @return pl. {@code "2 kavics a(z) 1. kupacból"} (a kupacot 1-től számozva)
     */
    @Override
    public String toString() {
        return count + " kavics a(z) " + (heapIndex + 1) + ". kupacból";
    }
}
