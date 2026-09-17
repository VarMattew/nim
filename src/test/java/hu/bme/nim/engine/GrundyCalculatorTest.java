package hu.bme.nim.engine;

import hu.bme.nim.model.GameState;
import hu.bme.nim.model.Player;
import hu.bme.nim.model.Rules;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrundyCalculatorTest {

    @Test
    @DisplayName("Végállapot Grundy-száma 0 minden szabálynál")
    void terminalIsZero() {
        assertEquals(0, new GrundyCalculator(Rules.standard()).grundy(0));
        assertEquals(0, new GrundyCalculator(Rules.classicNim()).grundy(0));
        assertEquals(0, new GrundyCalculator(Rules.limited(5)).grundy(0));
    }

    @Test
    @DisplayName("1–3 kavicsos szabály: g(n) = n mod 4")
    void standardRulesIsNMod4() {
        GrundyCalculator calc = new GrundyCalculator(Rules.standard());
        for (int n = 0; n <= 200; n++) {
            assertEquals(n % 4, calc.grundy(n), "n=" + n);
        }
    }

    @ParameterizedTest(name = "1–{0} kavics: g(n) = n mod {0}+1")
    @ValueSource(ints = {1, 2, 3, 4, 5, 7, 10})
    void limitedRulesIsNModKPlus1(int k) {
        GrundyCalculator calc = new GrundyCalculator(Rules.limited(k));
        for (int n = 0; n <= 150; n++) {
            assertEquals(n % (k + 1), calc.grundy(n), "k=" + k + " n=" + n);
        }
    }

    @Test
    @DisplayName("Klasszikus Nim: g(n) = n")
    void classicNimIsIdentity() {
        GrundyCalculator calc = new GrundyCalculator(Rules.classicNim());
        for (int n = 0; n <= 300; n++) {
            assertEquals(n, calc.grundy(n), "n=" + n);
        }
    }

    @Test
    @DisplayName("Memoizálás: nagy értéket kérve a kisebbek is helyesek maradnak, sorrendtől függetlenül")
    void memoIsOrderIndependent() {
        GrundyCalculator calc = new GrundyCalculator(Rules.standard());
        assertEquals(1000 % 4, calc.grundy(1000));
        assertEquals(3, calc.grundy(7));
        assertEquals(0, calc.grundy(4));
    }

    @Test
    @DisplayName("Több kupac: a játék Grundy-száma a kupacok Grundy-számainak XOR-ja")
    void multiHeapIsXor() {
        GrundyCalculator classic = new GrundyCalculator(Rules.classicNim());
        // 3 ^ 4 ^ 5 = 2
        assertEquals(3 ^ 4 ^ 5, classic.grundy(GameState.of(Rules.classicNim(), Player.HUMAN, 3, 4, 5)));
        // 1 ^ 2 ^ 3 = 0 → P-állás
        assertEquals(0, classic.grundy(GameState.of(Rules.classicNim(), Player.HUMAN, 1, 2, 3)));

        GrundyCalculator standard = new GrundyCalculator(Rules.standard());
        // 7,5,3 → (7%4=3) ^ (5%4=1) ^ (3%4=3) = 1
        GameState s = GameState.of(Rules.standard(), Player.HUMAN, 7, 5, 3);
        assertEquals(List.of(3, 1, 3), standard.heapGrundies(s));
        assertEquals(1, standard.grundy(s));
        // 8,4,12 → 0 ^ 0 ^ 0 = 0 → P-állás, pedig nyers XOR-ral 8^4^12 = 0 is lenne;
        // 5,6 → 1 ^ 2 = 3 → N-állás, pedig nyers XOR-ral 5^6 = 3 – itt egyezik;
        // 4,8 → 0 ^ 0 = 0 → P; nyers: 4^8 = 12 ≠ 0 – itt LÁTSZIK, hogy a nyers XOR hibás lenne.
        assertEquals(0, standard.grundy(GameState.of(Rules.standard(), Player.HUMAN, 4, 8)));
    }

    @Test
    @DisplayName("Egy kupac, 1–3 szabály: a kezdő pontosan akkor nyer, ha n nem osztható 4-gyel")
    void singleHeapFirstPlayerWinsIffNotDivisibleBy4() {
        GrundyCalculator calc = new GrundyCalculator(Rules.standard());
        for (int n = 1; n <= 100; n++) {
            GameState s = GameState.of(Rules.standard(), Player.HUMAN, n);
            assertEquals(n % 4 != 0, calc.isNPosition(s), "n=" + n);
            assertEquals(n % 4 == 0, calc.isPPosition(s), "n=" + n);
        }
    }

    @Test
    @DisplayName("Eltérő szabályú állással hívva kivételt dob")
    void rulesMismatchThrows() {
        GrundyCalculator calc = new GrundyCalculator(Rules.standard());
        GameState classic = GameState.of(Rules.classicNim(), Player.HUMAN, 3);
        assertThrows(IllegalArgumentException.class, () -> calc.grundy(classic));
    }

    @Test
    @DisplayName("P-állásból minden lépés N-állásba vezet; N-állásból van P-állásba vezető lépés")
    void pAndNPositionsAreConsistentWithDefinition() {
        for (Rules rules : List.of(Rules.standard(), Rules.limited(2), Rules.classicNim())) {
            GrundyCalculator calc = new GrundyCalculator(rules);
            for (int a = 0; a <= 9; a++) {
                for (int b = 0; b <= 9; b++) {
                    for (int c = 0; c <= 9; c++) {
                        GameState s = GameState.of(rules, Player.HUMAN, a, b, c);
                        if (s.isOver()) continue;
                        boolean existsMoveToP = s.legalMoves().stream()
                                .anyMatch(m -> calc.isPPosition(s.apply(m)));
                        if (calc.isPPosition(s)) {
                            assertFalse(existsMoveToP, "P-állásból nem lehet P-be lépni: " + s);
                        } else {
                            assertTrue(existsMoveToP, "N-állásból kell legyen P-be vezető lépés: " + s);
                        }
                    }
                }
            }
        }
    }
}
