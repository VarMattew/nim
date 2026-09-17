/**
 * A játékelméleti "motor": Grundy-számozás és a rá épülő gépi játékos.
 * <p>
 * A {@link hu.bme.nim.engine.GrundyCalculator} a 1.19. definíció szerint, mex-szel és memoizálással
 * számolja egy kupac Grundy-számát, több kupacra pedig a Sprague–Grundy tétel alapján XOR-olja
 * őket. A {@link hu.bme.nim.engine.PositionType} az állás típusát ({@code T(v) ∈ {I, II}}) adja meg,
 * a {@link hu.bme.nim.engine.NimAnalyzer} a 0 Grundy-számú állásba vezető (nyerő) lépéseket keresi
 * meg, az {@link hu.bme.nim.engine.AiPlayer} pedig ezekből választ a
 * {@link hu.bme.nim.engine.Difficulty} szerint.
 * <p>
 * A csomag nem függ a JavaFX-től, ezért önállóan, egységtesztekkel ellenőrizhető.
 */
package hu.bme.nim.engine;
