package hu.bme.nim.model;

/**
 * A játék szabályai: egy lépésben egy kupacból legfeljebb hány kavics vehető el.
 * <p>
 * {@code maxTake = UNLIMITED} esetén klasszikus Nim (egy kupacból bármennyi elvehető),
 * egyébként az 1..maxTake közötti elvétel engedélyezett (a klasszikus "1-2-3" változat: maxTake = 3).
 * Normál játék: aki az utolsó kavicsot elveszi, nyer.
 *
 * @param maxTake egy lépésben legfeljebb ennyi kavics vehető el (>= 1), vagy {@link #UNLIMITED}
 */
public record Rules(int maxTake) {

    /** Jelzőérték a korlátlan elvételhez (klasszikus Nim). */
    public static final int UNLIMITED = Integer.MAX_VALUE;

    public Rules {
        if (maxTake < 1) {
            throw new IllegalArgumentException("maxTake legalább 1 kell legyen, kapott: " + maxTake);
        }
    }

    /** A "1-2-3" változat: 1, 2 vagy 3 kavics vehető el. */
    public static Rules standard() {
        return new Rules(3);
    }

    /** Klasszikus Nim: egy kupacból tetszőleges (>= 1) számú kavics vehető el. */
    public static Rules classicNim() {
        return new Rules(UNLIMITED);
    }

    /** Egyéni felső korlát: 1..k kavics vehető el. */
    public static Rules limited(int k) {
        return new Rules(k);
    }

    public boolean isUnlimited() {
        return maxTake == UNLIMITED;
    }

    /** Egy adott méretű kupacból legfeljebb hány kavics vehető el ebben a lépésben. */
    public int maxTakeFrom(int heapSize) {
        return Math.min(heapSize, maxTake);
    }

    /** Legális-e {@code take} kavics elvétele egy {@code heapSize} méretű kupacból. */
    public boolean isLegalTake(int heapSize, int take) {
        return take >= 1 && take <= maxTakeFrom(heapSize);
    }

    @Override
    public String toString() {
        return isUnlimited() ? "klasszikus Nim (korlátlan)" : "1–" + maxTake + " kavics";
    }
}
