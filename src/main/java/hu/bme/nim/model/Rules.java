package hu.bme.nim.model;

/**
 * A játék szabályai: egy lépésben egy kupacból legfeljebb hány kavics vehető el.
 * <p>
 * {@code maxTake == UNLIMITED} esetén klasszikus Nim (egy kupacból bármennyi elvehető),
 * egyébként az {@code 1..maxTake} közötti elvétel engedélyezett (a "1-2-3" változat: {@code maxTake = 3}).
 * Normál játék: aki az utolsó kavicsot elveszi, nyer.
 *
 * @param maxTake egy lépésben legfeljebb ennyi kavics vehető el (legalább 1), vagy {@link #UNLIMITED}
 */
public record Rules(int maxTake) {

    /** Jelzőérték a korlátlan elvételhez (klasszikus Nim). */
    public static final int UNLIMITED = Integer.MAX_VALUE;

    /**
     * Ellenőrzi a felső korlátot.
     *
     * @throws IllegalArgumentException ha {@code maxTake < 1}
     */
    public Rules {
        if (maxTake < 1) {
            throw new IllegalArgumentException("maxTake legalább 1 kell legyen, kapott: " + maxTake);
        }
    }

    /**
     * A "1-2-3" változat: 1, 2 vagy 3 kavics vehető el.
     *
     * @return szabály {@code maxTake = 3} értékkel
     */
    public static Rules standard() {
        return new Rules(3);
    }

    /**
     * Klasszikus Nim: egy kupacból tetszőleges (legalább 1) számú kavics vehető el.
     *
     * @return szabály {@link #UNLIMITED} korláttal
     */
    public static Rules classicNim() {
        return new Rules(UNLIMITED);
    }

    /**
     * Egyéni felső korlát: {@code 1..k} kavics vehető el.
     *
     * @param k a legnagyobb megengedett elvétel (legalább 1)
     * @return szabály {@code maxTake = k} értékkel
     */
    public static Rules limited(int k) {
        return new Rules(k);
    }

    /**
     * Korlátlan-e az elvétel (klasszikus Nim).
     *
     * @return {@code true}, ha {@code maxTake == UNLIMITED}
     */
    public boolean isUnlimited() {
        return maxTake == UNLIMITED;
    }

    /**
     * Egy adott méretű kupacból legfeljebb hány kavics vehető el egy lépésben.
     *
     * @param heapSize a kupac mérete
     * @return {@code min(heapSize, maxTake)}
     */
    public int maxTakeFrom(int heapSize) {
        return Math.min(heapSize, maxTake);
    }

    /**
     * Legális-e {@code take} kavics elvétele egy {@code heapSize} méretű kupacból.
     *
     * @param heapSize a kupac mérete
     * @param take     az elvenni kívánt kavicsok száma
     * @return {@code true}, ha {@code 1 <= take <= maxTakeFrom(heapSize)}
     */
    public boolean isLegalTake(int heapSize, int take) {
        return take >= 1 && take <= maxTakeFrom(heapSize);
    }

    /**
     * Magyar nyelvű, felületen megjeleníthető leírás.
     *
     * @return pl. {@code "klasszikus Nim (korlátlan)"} vagy {@code "1–3 kavics"}
     */
    @Override
    public String toString() {
        return isUnlimited() ? "klasszikus Nim (korlátlan)" : "1–" + maxTake + " kavics";
    }
}
