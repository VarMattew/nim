package hu.bme.nim;

import hu.bme.nim.ui.NimApplication;

/**
 * Belépési pont.
 * <p>
 * Szándékosan <b>nem</b> származik a JavaFX {@code Application} osztályból: így a program
 * classpath-ról is indítható ("JavaFX runtime components are missing" hiba nélkül), és a
 * {@code --console} kapcsolóval a grafikus felület helyett a konzolos bemutató fut.
 * <pre>
 *   mvn javafx:run                                   – grafikus felület
 *   java -cp target/classes hu.bme.nim.App --console 3 8 5 1   – konzolos bemutató
 * </pre>
 */
public final class App {

    private App() {
    }

    /**
     * Program indítása.
     *
     * @param args parancssori argumentumok; {@code --console [maxTake|0] [kupac...]} a konzolos
     *             bemutatót futtatja, egyébként a JavaFX felület indul
     */
    public static void main(String[] args) {
        if (args.length > 0 && "--console".equals(args[0])) {
            String[] rest = new String[args.length - 1];
            System.arraycopy(args, 1, rest, 0, rest.length);
            ConsoleDemo.run(rest);
            return;
        }
        NimApplication.launchApp(args);
    }
}
