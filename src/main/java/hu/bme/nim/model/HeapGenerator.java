package hu.bme.nim.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Véletlen kezdőkupacok előállítása a beállító képernyő "Véletlen" gombjához. */
public final class HeapGenerator {

    public static final int MAX_HEAPS = 20;
    public static final int MAX_HEAP_SIZE = 50;

    private final Random random;

    public HeapGenerator() {
        this(new Random());
    }

    public HeapGenerator(Random random) {
        this.random = random;
    }

    /**
     * {@code heapCount} darab kupac, mindegyik {@code minSize..maxSize} közötti (zárt intervallum) mérettel.
     * Garantálja, hogy legalább egy kavics van összesen.
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

    /** Véletlen kupacszám (1..MAX_HEAPS) és méretek 1..maxSize között. */
    public List<Integer> generateRandomCount(int maxSize) {
        return generate(1 + random.nextInt(MAX_HEAPS), 1, maxSize);
    }
}
