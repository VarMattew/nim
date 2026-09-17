package hu.bme.nim;

import hu.bme.nim.engine.AiPlayer;
import hu.bme.nim.engine.Difficulty;
import hu.bme.nim.engine.GrundyCalculator;
import hu.bme.nim.engine.NimAnalyzer;
import hu.bme.nim.model.Game;
import hu.bme.nim.model.GameState;
import hu.bme.nim.model.Move;
import hu.bme.nim.model.Player;
import hu.bme.nim.model.Rules;

import java.util.List;

/**
 * Konzolos bemutató: az AI önmaga ellen játszik, minden lépésnél kiírva a kupaconkénti
 * Grundy-számokat binárisan és az XOR-t. Ellenőrzésre szolgál, amíg a JavaFX felület elkészül.
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

        System.out.println("Szabály: " + rules);
        describe(calc, game.state());
        while (!game.isOver()) {
            Move move = ai.chooseMove(game.state());
            Player mover = game.state().currentPlayer();
            game.play(move);
            System.out.println(mover.displayName() + " lép: " + move);
            describe(calc, game.state());
        }
        System.out.println("Győztes: " + game.winner().displayName());
    }

    private static void describe(GrundyCalculator calc, GameState state) {
        List<Integer> g = calc.heapGrundies(state);
        int width = Math.max(1, Integer.toBinaryString(g.stream().mapToInt(Integer::intValue).max().orElse(0)).length());
        StringBuilder sb = new StringBuilder("  kupacok " + state.heaps() + "  Grundy:");
        for (int i = 0; i < g.size(); i++) {
            sb.append(' ').append(bin(g.get(i), width));
        }
        int x = calc.grundy(state);
        sb.append("  XOR = ").append(bin(x, width)).append(" (").append(x).append(") → ")
          .append(x == 0 ? "P-állás (soron következő veszít)" : "N-állás (soron következő nyer)");
        System.out.println(sb);
    }

    private static String bin(int value, int width) {
        String s = Integer.toBinaryString(value);
        return "0".repeat(Math.max(0, width - s.length())) + s;
    }
}
