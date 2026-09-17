package hu.bme.nim;

import hu.bme.nim.engine.AiPlayer;
import hu.bme.nim.engine.Difficulty;
import hu.bme.nim.engine.GrundyCalculator;
import hu.bme.nim.engine.NimAnalyzer;
import hu.bme.nim.engine.PositionType;
import hu.bme.nim.model.Game;
import hu.bme.nim.model.GameState;
import hu.bme.nim.model.Move;
import hu.bme.nim.model.Player;
import hu.bme.nim.model.Rules;

import java.util.List;
import java.util.StringJoiner;

/**
 * Konzolos bemutató: az AI önmaga ellen játszik, minden lépésnél kiírva a kupacméreteket és a
 * kupaconkénti Grundy-számokat binárisan, az XOR-t és az állás típusát T(v) ∈ {I, II}.
 * <p>
 * A bináris jegyszám a kezdőállás legnagyobb kupacméretéhez igazodik (8 → 4 jegy), és a játék
 * végéig rögzített, így a számok oszlopba rendezve olvashatók.
 * <p>
 * Használat: {@code java ... hu.bme.nim.App [maxTake|0=korlátlan] [kupac1 kupac2 ...]}
 */
final class ConsoleDemo {

    private ConsoleDemo() {
    }

    static void run(String[] args) {
        Rules rules = Rules.standard();
        int[] heaps = {7, 5, 3};
        if (args.length >= 1) {
            int k = Integer.parseInt(args[0]);
            rules = k <= 0 ? Rules.classicNim() : Rules.limited(k);
        }
        if (args.length >= 2) {
            heaps = new int[args.length - 1];
            for (int i = 1; i < args.length; i++) {
                heaps[i - 1] = Integer.parseInt(args[i]);
            }
        }

        GrundyCalculator calc = new GrundyCalculator(rules);
        NimAnalyzer analyzer = new NimAnalyzer(calc);
        AiPlayer ai = new AiPlayer(analyzer, Difficulty.HARD);
        Game game = new Game(GameState.of(rules, Player.HUMAN, heaps));
        int width = GrundyCalculator.binaryWidth(game.initialState());

        System.out.println("Szabály: " + rules);
        System.out.println("Kezdőállás v0 = " + game.state().heaps()
                + "  →  a játék típusa T(J) = " + calc.type(game.state()));
        System.out.println();
        describe(calc, game.state(), 0, width);
        int step = 1;
        while (!game.isOver()) {
            Move move = ai.chooseMove(game.state());
            Player mover = game.state().currentPlayer();
            game.play(move);
            System.out.println(mover.displayName() + " lép: " + move);
            describe(calc, game.state(), step++, width);
        }
        System.out.println();
        System.out.println("Győztes: " + game.winner().displayName());
    }

    private static void describe(GrundyCalculator calc, GameState state, int index, int width) {
        List<Integer> g = calc.heapGrundies(state);
        StringJoiner sizes = new StringJoiner(", ");
        StringJoiner grundies = new StringJoiner(", ");
        for (int i = 0; i < g.size(); i++) {
            sizes.add(GrundyCalculator.toBinary(state.heap(i), width));
            grundies.add(GrundyCalculator.toBinary(g.get(i), width));
        }
        int x = calc.grundy(state);
        PositionType type = calc.type(state);
        System.out.printf("  v%-2d %-14s kupacok: %s   Grundy: %s   XOR = %s (%d)   T(v%d) = %s%n",
                index, state.heaps(), sizes, grundies, GrundyCalculator.toBinary(x, width), x, index, type);
    }
}
