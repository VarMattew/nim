package hu.bme.nim.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Véletlen kezdőkupacok előállítása a beállító képernyő "Véletlen" gombjaihoz. */
public final class HeapGenerator {

    /** A kupacok legnagyobb megengedett száma. */
    public static final int MAX_HEAPS = 20;
    /** Egy kupac legnagyobb megengedett mérete. */
    public static final int MAX_HEAP_SIZE = 50;

    private final Random random;

    /** Generátor új véletlenszám-forrással. */
    public HeapGenerator() {
        this(new Random());
    }

    /**
     * Generátor megadott véletlenszám-forrással (teszteléshez determinisztikus maggal).
     *
     * @param random a használt véletlenszám-generátor
     */
    public HeapGenerator(Random random) {
        this.random = random;
    }

    /**
     * {@code heapCount} darab kupac, mindegyik {@code minSize..maxSize} közötti (zárt intervallum) mérettel.
     * Garantálja, hogy legalább egy kavics van összesen.
     *
     * @param heapCount a kupacok száma ({@code 1..MAX_HEAPS})
     * @param minSize   a legkisebb kupacméret (legalább 0)
     * @param maxSize   a legnagyobb kupacméret (legfeljebb {@link #MAX_HEAP_SIZE}, és legalább {@code minSize})
     * @return új lista a kupacméretekkel
     * @throws IllegalArgumentException érvénytelen kupacszám vagy mérettartomány esetén
     */
    public List<Integer> generate(int heapCount, int minSize, int maxSize) {
        if (heapCount < 1 || heapCount > MAX_HEAPS) {
            throw new IllegalArgumentException("A kupacok száma 1.." + MAX_HEAPS + " lehet, kapott: " + heapCount);
        }
        if (minSize < 0 || maxSize < minSize || maxSize > MAX_HEAP_SIZE) {
            throw new IllegalArgumentException("Érvénytelen mérettartomány: " + minSize + ".." + maxSize);
        }
        List<Integer> heaps;
        do {
            heaps = new ArrayList<>(heapCount);
            for (int i = 0; i < heapCount; i++) {
                heaps.add(minSize + random.nextInt(maxSize - minSize + 1));
            }
        } while (heaps.stream().mapToInt(Integer::intValue).sum() == 0);
        return heaps;
    }

    /**
     * Véletlen kupacszám ({@code 1..MAX_HEAPS}) és méretek {@code 1..maxSize} között.
     *
     * @param maxSize a legnagyobb kupacméret
     * @return új lista a kupacméretekkel
     */
    public List<Integer> generateRandomCount(int maxSize) {
        return generate(1 + random.nextInt(MAX_HEAPS), 1, maxSize);
    }
}
