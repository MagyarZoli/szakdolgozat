# Hívási gráfok graflet-alapú vizsgálata

Szakdolgozati kutatási repó: Java programok statikus hívási gráfjainak egységes előállítása
több gráfépítési stratégiával, metódus szintű graflet-jellemzők számítása, és ezek felhasználása
hibaelőrejelzésben.

| Kutatási kérdés | Hol készül |
|---|---|
| K1: graflet-eloszlás DECLARED_TYPE vs. CHA mellett | `cg build` + `cg-graphlet` + `scripts/` |
| K2: rekurziót kezelő graflet-fogalom | `cg-graphlet` |
| K3: javul-e a hibaelőrejelzés | `scripts/` (Python) |
| K4: hatékony metódus szintű számlálás | `cg-graphlet` (párhuzamos Java) |

## Felépítés

```
callgraph-graphlets/
├── pom.xml              szülő POM 
├── cg-core/             eszközfüggetlen mag: modell, builder-interfész, azonosítók, CSV I/O, statisztika
├── cg-builder-soot/     Soot: bájtkód, CHA
├── cg-builder-osa/      OpenStaticAnalyzer: forráskód, deklarált típus (külső program)
├── test-programs/       nyelvi elemekben gazdag mintaprogram + EXPECTED.md
├── scripts/             Python elemzés, gépi tanulás 
├── config/              eszközbeállítások
├── data/                nyers bemenetek (nincs verziókövetve, csak a leírás)
├── results/             előállított gráfok, mérések (nincs verziókövetve)
└── docs/
    ├── reports/         havi összefoglalók
    └── decisions/       döntésnapló
```

### Hogyan illeszkednek a részek

- Minden gráfépítő a `CallGraphBuilder` interfészt valósítja meg (közös alaposztály:
  `AbstractCallGraphBuilder`, ellenőrzés + időmérés), és `ServiceLoader`-rel töltődik be.
  A CLI egyik eszközt sem ismeri név szerint, új eszközhöz elég egy új modul.
- A builder egy `BuildRequest`-et kap (bemenet fajtája és útvonala, classpath, stratégia,
  hatókör, azonosítóstílus), és egységes `CallGraph`-ot ad vissza.
- A csúcsok azonosítója a `SignatureStyle` szerint készül; az alapértelmezett az OSA
  `LongName` alakja (`pkg.Osztaly$Belso.metodus(ILjava/lang/String;)V`), mert a metódus szintű
  adathalmaz is ezt használja.

## Fordítás

Java 17+ és Maven 3.9+ kell.

```bash
mvn package
mvn package -DskipTests
```

### Kódokumentáció (Javadoc)

Minden osztály, metódus és paraméter magyarul dokumentált; minden csomagnak van
`package-info.java` leírása (a csomag szerepe, az adatfolyam, a beállítások).

Jó kiindulópontok olvasáshoz: `hu.thesis.cg.core.api` (egy futás menete ábrával),
`AbstractCallGraphBuilder` (új builder írása példával), `CsvGraphFormat` (kimeneti formátum).


## Kimeneti formátum

| Fájl | Oszlopok |
|---|---|
| `nodes.csv` | `id,class,method,params,return,file,line,flags` (params `;`-vel, flags `\|`-vel elválasztva) |
| `edges.csv` | `source,target,kind,line` |
| `meta.json` | eszköz, verzió, stratégia, bemenet, hatókör, projekt, időbélyeg, futásidő, méretek |

Jelzők: `CONSTRUCTOR, STATIC_INIT, STATIC, ABSTRACT, SYNTHETIC, LAMBDA, ANONYMOUS_CLASS,
INNER_CLASS, LIBRARY, PHANTOM`.  
Él fajták: `STATIC, VIRTUAL, INTERFACE, SPECIAL, DYNAMIC,
STATIC_INIT, OTHER, UNKNOWN`.