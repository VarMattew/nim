/**
 * A játék modellje, a felülettől és a mesterséges intelligenciától függetlenül.
 * <p>
 * Központi eleme a megváltoztathatatlan {@link hu.bme.nim.model.GameState} (kupacok, soron következő
 * játékos, szabály), amelyen a {@link hu.bme.nim.model.Move} lépések új állást hoznak létre. A
 * {@link hu.bme.nim.model.Game} egy játszmát és annak lépésnaplóját tartja nyilván, a
 * {@link hu.bme.nim.model.Rules} a lépésszabályt írja le, a {@link hu.bme.nim.model.HeapGenerator}
 * véletlen kezdőkupacokat állít elő.
 */
package hu.bme.nim.model;
