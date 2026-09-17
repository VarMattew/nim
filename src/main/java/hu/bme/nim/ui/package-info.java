/**
 * JavaFX felhasználói felület (FXML + CSS, magyar nyelvű, BME-arculat).
 * <p>
 * A {@link hu.bme.nim.ui.NimApplication} egyetlen ablakban váltogatja a beállító
 * ({@link hu.bme.nim.ui.SetupController}, {@code setup.fxml}) és a játék
 * ({@link hu.bme.nim.ui.GameController}, {@code game.fxml}) nézetet. A kettő között a
 * {@link hu.bme.nim.ui.GameSettings} rekord viszi át a beállításokat. A kupacokat a
 * {@code HeapView} rajzolja kavicsokként, a {@code Theme} a {@code style.css}-t és az ablakikont
 * alkalmazza minden ablakra és dialógusra.
 * <p>
 * A csomag csak megjelenít és eseményeket továbbít; a játéklogika a {@link hu.bme.nim.model} és
 * {@link hu.bme.nim.engine} csomagokban van.
 */
package hu.bme.nim.ui;
