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

Egy `v` állás típusa: `T(v) = I`, ha a soron következő (1.) játékosnak van nyerő stratégiája
(`g(v) ≠ 0`), és `T(v) = II`, ha a 2. játékosnak (`g(v) = 0`). A kezdőállás típusa a játék típusa,
`T(J)`. Az egykupacos, 1–3-as változatban tehát `T(J) = I ⇔ n` nem osztható 4-gyel.

Az AI nyerő állásból mindig olyan lépést választ, amely után az XOR 0 lesz; vesztő állásból a
nehézségi szinttől függően véletlen legális lépést tesz (Közepes), vagy húzza az időt (Nehéz).
Könnyű szinten nyerő állásból is időnként hibázik.

## Felépítés

```
hu.bme.nim
├── model/    Rules, Move, Player, GameState (immutable), Game (játszma + napló), HeapGenerator
├── engine/   GrundyCalculator (mex + memo), NimAnalyzer (nyerő lépések), AiPlayer, Difficulty, PositionType
├── ui/       JavaFX felület: NimApplication, SetupController + setup.fxml, GameController + game.fxml,
│             HeapView (kavicsok), GameSettings; stílus: style.css
└── App       belépési pont (launcher; --console kapcsolóval konzolos bemutató)
```

## Felület

- **Beállító képernyő:** kupacok száma (1–20) és mérete (0–50) kupaconként, véletlen méretek adott
  tartományból vagy teljesen véletlen felállás; lépésszabály (1–3 / egyéni 1–k / korlátlan);
  nehézség (Könnyű, Közepes, Nehéz); ki kezd; oktató mód. Alul élőben látszik a kezdőállás
  Grundy-száma és `T(J)`.
- **Játék képernyő:** a kupacok kavicsokként jelennek meg; kattintás a kupacra, majd az elvenni kívánt
  darabszám gombja (nagy korlátnál Spinner). Az egérrel a gomb fölé állva narancssárgán látszik, mely
  kavicsok tűnnek el; az előző lépésben elvett kavicsok helye szaggatott körvonal. A gép kis
  késleltetéssel lép. Visszavonás (a gép válaszával együtt), Visszavágó, Új beállítás.
- **Oktató panel** (kapcsolható): kupaconként a méret és a Grundy-szám binárisan – a jegyszám a
  kezdőállás legnagyobb kupacához igazodik –, az XOR, `T(vᵢ)` és egy rövid magyarázat; a
  „Tipp" gomb megjelöli az egyik nyerő lépést.

## Fordítás, tesztek, futtatás

```
mvn test          # unit tesztek (Grundy-képletek, AI helyessége)
mvn javafx:run    # alkalmazás indítása
```

A `--console` kapcsolóval grafikus felület nélkül egy konzolos bemutató fut, amelyben az AI önmaga
ellen játszik és minden `vᵢ` állásnál kiírja a kupacméreteket és a Grundy-számokat binárisan (a
jegyszám a legnagyobb kezdő kupacmérethez igazodik, pl. 8 → 4 jegy), az XOR-t és `T(vᵢ)`-t:

```
mvn -q compile
java -cp target/classes hu.bme.nim.App --console 3 8 5 1   # maxTake=3, kupacok: 8 5 1
java -cp target/classes hu.bme.nim.App --console 0 3 4 5   # 0 = korlátlan (klasszikus Nim)
```
(Windows-konzolon az ékezetekhez előbb: `chcp 65001`.)
