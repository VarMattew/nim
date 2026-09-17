# NIM – Grundy-számok alapján döntő AI

Java 21 + JavaFX asztali játék, amelyben a gépi ellenfél a Sprague–Grundy elmélet alapján lép.

## Matematikai háttér

Egy kupac Grundy-száma a definíció szerint a rákövetkező állások Grundy-számainak `mex`-e
(a legkisebb nemnegatív egész, amely nincs köztük):

```
g(0) = 0
g(n) = mex{ g(n − r) : 1 ≤ r ≤ min(n, maxTake) }
```

Ebből adódik, hogy 1–3 kavicsos szabálynál `g(n) = n mod 4`, korlátlan elvételnél (klasszikus Nim)
`g(n) = n`. Több kupac esetén a játék Grundy-száma a kupacok Grundy-számainak XOR-ja.
A soron következő játékos pontosan akkor van vesztő (P) állásban, ha ez az XOR 0.

Az AI nyerő állásból mindig olyan lépést választ, amely után az XOR 0 lesz; vesztő állásból a
nehézségi szinttől függően véletlen legális lépést tesz (Közepes), vagy húzza az időt (Nehéz).
Könnyű szinten nyerő állásból is időnként hibázik.

## Felépítés

```
hu.bme.nim
├── model/    Rules, Move, Player, GameState (immutable), Game (játszma + napló), HeapGenerator
├── engine/   GrundyCalculator (mex + memo), NimAnalyzer (nyerő lépések), AiPlayer, Difficulty
├── ui/       JavaFX felület (FXML + CSS) – 2. ütem
└── App       belépési pont
```

## Fordítás, tesztek, futtatás

```
mvn test          # unit tesztek (Grundy-képletek, AI helyessége)
mvn javafx:run    # alkalmazás indítása
```

Amíg a JavaFX felület nem készül el, az `App` egy konzolos bemutatót futtat, amelyben az AI önmaga
ellen játszik és minden lépésnél kiírja a Grundy-számokat binárisan:

```
mvn -q compile exec:java            # vagy:
java -cp target/classes hu.bme.nim.App 3 7 5 3     # maxTake=3, kupacok: 7 5 3
java -cp target/classes hu.bme.nim.App 0 3 4 5     # 0 = korlátlan (klasszikus Nim)
```
