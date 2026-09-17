package hu.bme.nim.engine;

import hu.bme.nim.model.Game;
import hu.bme.nim.model.GameState;
import hu.bme.nim.model.Move;
import hu.bme.nim.model.Player;
import hu.bme.nim.model.Rules;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NimAnalyzerAndAiTest {

    @Test
    @DisplayName("Klasszikus Nim 3,4,5: a nyerő lépések pontosan a nᵢ → nᵢ XOR X csökkentések")
    void classicNimWinningMovesMatchXorFormula() {
        Rules rules = Rules.classicNim();
        NimAnalyzer analyzer = new NimAnalyzer(new GrundyCalculator(rules));
        GameState s = GameState.of(rules, Player.HUMAN, 3, 4, 5);
        // X = 2. 3^2=1<3 (vegyünk 2-t), 4^2=6>4 (nem), 5^2=7>5 (nem) → egyetlen nyerő lépés.
        assertEquals(List.of(new Move(0, 2)), analyzer.winningMoves(s));
    }

    @Test
    @DisplayName("1–3 szabály: a nyerő lépés a Grundy-számokon (n mod 4), nem a nyers méreteken alapul")
    void standardRulesWinningMoveUsesGrundyNotRawSize() {
        Rules rules = Rules.standard();
        NimAnalyzer analyzer = new NimAnalyzer(new GrundyCalculator(rules));
        // 5 → g=1. Nyerő: 1 kavics (5→4, g=0).
        assertEquals(List.of(new Move(0, 1)), analyzer.winningMoves(GameState.of(rules, Player.HUMAN, 5)));
        // 4,8 → P-állás, nincs nyerő lépés (nyers XOR 12 alapján tévesen lenne).
        assertTrue(analyzer.winningMoves(GameState.of(rules, Player.HUMAN, 4, 8)).isEmpty());
        // 6,9 → g: 2 ^ 1 = 3. Nyerő: 6→ g 1 (vegyünk 1: 5), vagy 9→ g 2 (vegyünk 3: 6).
        assertEquals(List.of(new Move(0, 1), new Move(1, 3)),
                analyzer.winningMoves(GameState.of(rules, Player.HUMAN, 6, 9)));
    }

    @Test
    @DisplayName("Minden nyerő lépés után az állás Grundy-száma 0")
    void everyWinningMoveLeadsToZero() {
        for (Rules rules : List.of(Rules.standard(), Rules.limited(4), Rules.classicNim())) {
            GrundyCalculator calc = new GrundyCalculator(rules);
            NimAnalyzer analyzer = new NimAnalyzer(calc);
            for (int a = 0; a <= 8; a++) {
                for (int b = 0; b <= 8; b++) {
                    for (int c = 0; c <= 8; c++) {
                        GameState s = GameState.of(rules, Player.AI, a, b, c);
                        for (Move m : analyzer.winningMoves(s)) {
                            assertTrue(s.isLegal(m), "legális kell legyen: " + m + " @ " + s);
                            assertEquals(0, calc.grundy(s.apply(m)), m + " @ " + s);
                        }
                        // És fordítva: minden 0-ba vezető legális lépés benne van a listában.
                        long zeroMoves = s.legalMoves().stream().filter(m -> calc.grundy(s.apply(m)) == 0).count();
                        assertEquals(zeroMoves, analyzer.winningMoves(s).size(), "teljesség @ " + s);
                    }
                }
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = Difficulty.class, names = {"MEDIUM", "HARD"})
    @DisplayName("Közepes/Nehéz AI nyerő állásból mindig 0 Grundy-számú állásba lép")
    void optimalAiAlwaysMovesToZeroFromNPosition(Difficulty difficulty) {
        for (Rules rules : List.of(Rules.standard(), Rules.classicNim())) {
            GrundyCalculator calc = new GrundyCalculator(rules);
            AiPlayer ai = new AiPlayer(new NimAnalyzer(calc), difficulty, new Random(42));
            for (int a = 0; a <= 7; a++) {
                for (int b = 0; b <= 7; b++) {
                    GameState s = GameState.of(rules, Player.AI, a, b);
                    if (s.isOver() || calc.isPPosition(s)) continue;
                    Move m = ai.chooseMove(s);
                    assertTrue(s.isLegal(m));
                    assertEquals(0, calc.grundy(s.apply(m)), difficulty + " " + m + " @ " + s);
                }
            }
        }
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    @DisplayName("Vesztő állásból is legális lépést ad minden szinten")
    void aiAlwaysReturnsLegalMoveFromPPosition(Difficulty difficulty) {
        Rules rules = Rules.standard();
        AiPlayer ai = new AiPlayer(new NimAnalyzer(new GrundyCalculator(rules)), difficulty, new Random(7));
        GameState s = GameState.of(rules, Player.AI, 4, 8, 12); // Grundy 0
        for (int i = 0; i < 50; i++) {
            assertTrue(s.isLegal(ai.chooseMove(s)));
        }
    }

    @Test
    @DisplayName("Nehéz AI vesztő állásból 1 kavicsot vesz a legnagyobb kupacból")
    void hardAiStallsFromPPosition() {
        Rules rules = Rules.standard();
        AiPlayer ai = new AiPlayer(new NimAnalyzer(new GrundyCalculator(rules)), Difficulty.HARD, new Random(1));
        assertEquals(new Move(2, 1), ai.chooseMove(GameState.of(rules, Player.AI, 4, 8, 12)));
    }

    @Test
    @DisplayName("Két optimális AI egymás ellen: N-állásból induló kezdő mindig nyer")
    void optimalVsOptimalStarterWinsFromNPosition() {
        for (Rules rules : List.of(Rules.standard(), Rules.classicNim())) {
            GrundyCalculator calc = new GrundyCalculator(rules);
            AiPlayer ai = new AiPlayer(new NimAnalyzer(calc), Difficulty.HARD, new Random(3));
            for (int a = 1; a <= 10; a++) {
                for (int b = 0; b <= 10; b++) {
                    for (int c = 0; c <= 10; c += 3) {
                        GameState start = GameState.of(rules, Player.HUMAN, a, b, c);
                        boolean starterShouldWin = calc.isNPosition(start);
                        Game game = new Game(start);
                        while (!game.isOver()) {
                            game.play(ai.chooseMove(game.state()));
                        }
                        Player expected = starterShouldWin ? Player.HUMAN : Player.AI;
                        assertEquals(expected, game.winner(), "kezdőállás " + start);
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("Könnyű AI ténylegesen hibázik néha nyerő állásból")
    void easyAiBlundersSometimes() {
        Rules rules = Rules.classicNim();
        GrundyCalculator calc = new GrundyCalculator(rules);
        AiPlayer ai = new AiPlayer(new NimAnalyzer(calc), Difficulty.EASY, new Random(11));
        GameState s = GameState.of(rules, Player.AI, 3, 4, 5); // N-állás
        boolean blundered = false;
        for (int i = 0; i < 200 && !blundered; i++) {
            blundered = calc.grundy(s.apply(ai.chooseMove(s))) != 0;
        }
        assertTrue(blundered);
        assertFalse(ai.difficulty().blunderProbability() == 0);
    }
}
