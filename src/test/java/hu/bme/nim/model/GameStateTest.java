package hu.bme.nim.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameStateTest {

    @Test
    @DisplayName("Lépés alkalmazása új állást ad, az eredeti nem változik, a játékos váltakozik")
    void applyIsImmutableAndSwitchesPlayer() {
        GameState s = GameState.of(Rules.standard(), Player.HUMAN, 5, 3);
        GameState next = s.apply(new Move(0, 2));
        assertEquals(List.of(5, 3), s.heaps());
        assertEquals(List.of(3, 3), next.heaps());
        assertEquals(Player.AI, next.currentPlayer());
        assertNull(next.winner());
    }

    @Test
    @DisplayName("Legális lépések: 1..min(kupac, maxTake) minden kupacra")
    void legalMovesRespectRules() {
        GameState s = GameState.of(Rules.standard(), Player.HUMAN, 2, 0, 5);
        assertEquals(List.of(new Move(0, 1), new Move(0, 2), new Move(2, 1), new Move(2, 2), new Move(2, 3)),
                s.legalMoves());
        GameState classic = GameState.of(Rules.classicNim(), Player.HUMAN, 4);
        assertEquals(4, classic.legalMoves().size());
    }

    @Test
    @DisplayName("Illegális lépés kivételt dob")
    void illegalMoveThrows() {
        GameState s = GameState.of(Rules.standard(), Player.HUMAN, 2);
        assertThrows(IllegalArgumentException.class, () -> s.apply(new Move(0, 3)));
        assertThrows(IllegalArgumentException.class, () -> s.apply(new Move(1, 1)));
        assertThrows(IllegalArgumentException.class, () -> new Move(0, 0));
    }

    @Test
    @DisplayName("Aki az utolsó kavicsot elveszi, nyer")
    void lastStoneWins() {
        GameState s = GameState.of(Rules.standard(), Player.AI, 1);
        GameState end = s.apply(new Move(0, 1));
        assertTrue(end.isOver());
        assertEquals(Player.AI, end.winner());
    }

    @Test
    @DisplayName("Game: napló, undo, visszavágó")
    void gameHistoryUndoRematch() {
        Game game = new Game(GameState.of(Rules.standard(), Player.HUMAN, 3));
        game.play(new Move(0, 2));
        assertEquals(1, game.history().size());
        assertEquals(Player.HUMAN, game.history().get(0).player());
        assertTrue(game.undo());
        assertEquals(List.of(3), game.state().heaps());
        assertFalse(game.undo());
        game.play(new Move(0, 3));
        assertTrue(game.isOver());
        assertThrows(IllegalStateException.class, () -> game.play(new Move(0, 1)));
        Game rematch = game.rematch();
        assertEquals(game.initialState(), rematch.state());
    }

    @Test
    @DisplayName("HeapGenerator: tartományon belüli méretek, soha nem üres játék")
    void heapGeneratorWithinBounds() {
        HeapGenerator gen = new HeapGenerator(new Random(5));
        for (int i = 0; i < 200; i++) {
            List<Integer> heaps = gen.generate(4, 0, 10);
            assertEquals(4, heaps.size());
            assertTrue(heaps.stream().allMatch(h -> h >= 0 && h <= 10));
            assertTrue(heaps.stream().mapToInt(Integer::intValue).sum() > 0);
        }
        assertThrows(IllegalArgumentException.class, () -> gen.generate(0, 1, 5));
        assertThrows(IllegalArgumentException.class, () -> gen.generate(3, 6, 5));
    }
}
