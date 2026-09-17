package hu.bme.nim.model;

/**
 * Egy lépés: a {@code heapIndex} indexű kupacból {@code count} kavics elvétele.
 */
public record Move(int heapIndex, int count) {

    public Move {
        if (heapIndex < 0) {
            throw new IllegalArgumentException("A kupac indexe nem lehet negatív: " + heapIndex);
        }
        if (count < 1) {
            throw new IllegalArgumentException("Legalább 1 kavicsot el kell venni, kapott: " + count);
        }
    }

    @Override
    public String toString() {
        return count + " kavics a(z) " + (heapIndex + 1) + ". kupacból";
    }
}
