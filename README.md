# Hack_n_slash

Un gioco di programmazione in Java: due bot si sfidano su una griglia a turni.
Tu scrivi l'IA del tuo bot, il motore di gioco gestisce il resto.

---

## Indice

1. [Installazione](#installazione)
2. [Avvio](#avvio)
3. [Come si gioca](#come-si-gioca)
4. [Le classi](#le-classi)
5. [La griglia](#la-griglia)
6. [Creare un bot](#creare-un-bot)
7. [API di riferimento](#api-di-riferimento)
8. [Struttura del progetto](#struttura-del-progetto)

---

## Installazione

### Prerequisiti

- **Java JDK 17 o superiore** (sviluppato con JDK 26)
- **Git** (per clonare la repository)
- **Un browser** (qualunque: Firefox, Chrome, Safari, Edge...)

### Linux

```bash
# Verifica Java
java -version
# Se non e' installato:
sudo apt install default-jdk      # Debian/Ubuntu
sudo dnf install java-17-openjdk  # Fedora
sudo pacman -S jdk-openjdk         # Arch

# Clona la repository
git clone <url-repository>
cd Hack_n_slash_swing

# Avvia
./run.sh
```

### Windows

```powershell
# Verifica Java
java -version
# Se non e' installato: scarica da https://adoptium.net/

# Clona la repository
git clone <url-repository>
cd Hack_n_slash_swing

# Avvia
.\run.bat
```

> Windows richiede Git Bash o WSL per eseguire `./run.sh`.

### macOS

```bash
# Verifica Java
java -version
# Se non e' installato:
brew install openjdk

# Clona la repository
git clone <url-repository>
cd Hack_n_slash_swing

# Avvia
./run.sh
```

### VS Code

1. Apri la cartella del progetto in VS Code
2. Installa l'estensione **Extension Pack for Java** (Microsoft)
3. Premi F5 su `Main.java`

Non serve nessuna configurazione manuale: `.vscode/settings.json` e `.vscode/launch.json` sono gia' inclusi.

---

## Avvio

### Da terminale

```bash
./run.sh        # Linux / macOS
.\run.bat       # Windows
```

Lo script:
1. Scarica le dipendenze in `lib/` (solo la prima volta)
2. Compila tutti i sorgenti in `bin/`
3. Avvia il gioco e apre il browser automaticamente

Se il browser non si apre, vai su **http://localhost:8765**.

### Da VS Code

Apri `src/hack_n_slash/Main.java` e premi **F5**.

---

## Come si gioca

All'avvio la partita parte **in pausa**. Vedi la griglia con i due bot e il pannello laterale.

### Controlli

| Bottoni | Quando sono attivi |
|---|---|
| **Play** | Sempre, tranne durante il gioco o a partita finita |
| **Pausa** | Solo durante il Play |
| **Step** | Solo in pausa (non durante il Play) |

- **Play**: avanza automaticamente un turno ogni 700ms
- **Pausa**: ferma il gioco
- **Step**: avanza di un singolo turno

Alla fine, il tabellone mostra l'overlay "BotX Vince!".

### Pannello laterale

- **Fase**: DAY o NIGHT (ciclo giorno/notte)
- **Bot 0 / Bot 1**: HP corrente con barra colorata
- **Turno**: di quale bot e' il turno
- **Status**: "In pausa", "In riproduzione", "Partita terminata"

---

## Le classi

Puoi scegliere tra 4 classi per il tuo bot. Ognuna ha HP, movimento e abilita' diverse.

### ARCHER (Arciere)

| Stat | Valore |
|---|---|
| HP | 3 |
| Movimento | 1 passo ortogonale (su/giu'/sinistra/destra, niente diagonali) |
| Attacco | A distanza: stessa riga o colonna con linea di vista libera (i muri bloccano) |
| Abilita' | **Knockback**: quando viene attaccato, l' insults scatta avversario l'attaccante spinge l'arciere alla posizione a specchio |
| Abilita' | **Teletrasporto**: una volta per partita, si teletrasporta in una cella valida a scelta |

### BERSERKER

| Stat | Valore |
|---|---|
| HP | 4 |
| Movimento | 1 passo per asse (8 direzioni, diagonali incluse, con corner-cut check) |
| Attacco | Corpo a corpo: 3x3 attorno a se' (Chebyshev-1), ignora i muri |
| Abilita' | **Rage**: se HP <= 2, infligge +1 danno |

### GHOST (Fantasma)

| Stat | Valore |
|---|---|
| HP | 4 |
| Movimento | 1 passo ortogonale |
| Attacco | Corpo a corpo: 3x3 attorno a se', ignora i muri |
| Abilita' | **Heal**: invece di attaccare, cura 1 HP (massimo 4). Consuma lo slot attacco |

### VAMPIRE (Vampiro)

| Stat | Valore |
|---|---|
| HP | 3 |
| Movimento | 1 passo per asse (8 direzioni, con corner-cut check) |
| Attacco | Corpo a corpo: 3x3 attorno a se', ignora i muri |
| Abilita' | **Lifesteal**: ogni attacco cura 1 HP all'attaccante |
| Abilita' | **Giorno/notte**: 1 TRAVEL di giorno, 2 TRAVEL di notte |

### Tabella riassuntiva

| Classe | HP | Mosse/turno | Tipo attacco | Speciale |
|---|---|---|---|---|
| ARCHER | 3 | 1 ortogonale | Raggio (LOS) | Knockback + Teletrasporto (1x/partita) |
| BERSERKER | 4 | 1 per asse (8 dir.) | Melee 3x3 | Rage (+1 danno se HP<=2) |
| GHOST | 4 | 1 ortogonale | Melee 3x3 | Heal (+1 HP, consume slot attacco) |
| VAMPIRE | 3 | 1 per asse (8 dir.) | Melee 3x3 | Lifesteal (+1 HP per hit) |

---

## La griglia

```
9 righe x 15 colonne
Bordo = muri (impercorribili)
Interno = terreno con muri procedurali a cluster (fino a 15)
```

| Simbolo | Significato |
|---|---|
| `.` (terreno chiaro) | Camminabile |
| `X` (muro scuro) | Bloccante |
| Cerchio giallo | Power-up |
| Cerchio blu | Bot 0 |
| Cerchio rosso | Bot 1 |

### Posizioni iniziali

- Bot 0: `(1, 1)` angolo in alto a sinistra
- Bot 1: `(13, 7)` angolo in basso a destra
- Power-up: appare dopo 6 turni, poi ogni 6 turni

### Ciclo giorno/notte

- Inizia in DAY
- Flip ogni 3 turni-bot (DAY->NIGHT->DAY...)
- Il VAMPIRE si muove 1 passo di giorno, 2 di notte

### Power-up

Appare su transizione NIGHT->DAY (ogni 6 turni) in una posizione casuale valida.

| Effetto | Probabilita' |
|---|---|
| Cura a HP massimo | 33% |
| Inverte giorno/notte | 33% |
| Un turno extra | 33% |

Si attiva quando un bot cammina sulla cella (TRAVEL).

---

## Creare un bot

### Passo 1: creare la classe

Crea un file in `src/hack_n_slash/bots/` chiamato `MioBot.java`:

```java
package hack_n_slash.bots;

import hack_n_slash.miscellaneous.Action;
import hack_n_slash.miscellaneous.Coord;
import hack_n_slash.map.MatrixLogic;
import java.util.ArrayList;

public class MioBot extends Bot {

    public MioBot() {
        super(Bot.RpgClass.BERSERKER);
    }

    @Override
    public Action[] move(Coord self, Coord opponent, Coord powerup,
                         int hpSelf, int hpOpponent) {
        ArrayList<Action> ret = new ArrayList<>();

        int dx = opponent.x - self.x;
        int dy = opponent.y - self.y;

        // Muoviti verso l'avversario
        Action moveX = new Action(Action.ActionType.TRAVEL);
        moveX.setDX(dx > 0 ? 1 : -1);
        moveX.setDY(0);
        ret.add(moveX);

        Action moveY = new Action(Action.ActionType.TRAVEL);
        moveY.setDX(0);
        moveY.setDY(dy > 0 ? 1 : -1);
        ret.add(moveY);

        // Attacca
        ret.add(new Action(Action.ActionType.ATTACK));

        return ret.toArray(new Action[0]);
    }
}
```

### Passo 2: scegliere la classe

Nel costruttore, chiama `super()` con la classe che vuoi:

```java
public MioBot() {
    super(Bot.RpgClass.BERSERKER);  // oppure ARCHER, GHOST, VAMPIRE
}
```

### Passo 3: implementare `move()`

Il motore chiama `move()` ad ogni turno passandoti:

| Parametro | Tipo | Significato |
|---|---|---|
| `self` | `Coord` | La tua posizione `(x, y)` |
| `opponent` | `Coord` | Posizione dell'avversario |
| `powerup` | `Coord` | Posizione del powerup (`(-1, -1)` se assente) |
| `hpSelf` | `int` | I tuoi HP |
| `hpOpponent` | `int` | HP dell'avversario |

Devi ritornare un `Action[]` — un array ordinato di azioni da eseguire.

### Passo 4: compilare e giocare

```bash
./run.sh
```

Il motore scopre automaticamente i bot nel package `hack_n_slash.bots` via reflection.

### Regole per le azioni

Ogni turno puoi eseguire:

| Azione | Limite |
|---|---|
| TRAVEL | Fino a 2 per turno (1 di giorno per il VAMPIRE) |
| ATTACK | 1 per turno |
| TELEPORT | 1 per partita (solo ARCHER) |
| HEAL | 1 per turno (solo GHOST, consume slot attacco) |

Le azioni in eccesso sono ignorate.

### Creare le azioni

```java
// TRAVEL: spostamento relativo
Action Move = new Action(Action.ActionType.TRAVEL);
move.setDX(1);   // +1 colonna (destra)
move.setDY(0);   // 0 righe

// ATTACK: attacca l'avversario
Action attack = new Action(Action.ActionType.ATTACK);

// TELEPORT: teletrasporto (ARCHER) a coordinate assolute
Action tp = new Action(Action.ActionType.TELEPORT);
tp.setABSX(5);   // colonna 5
tp.setABSY(3);   // riga 3

// HEAL: cura 1 HP (GHOST)
Action heal = new Action(Action.ActionType.HEAL);
```

### Limiti di movimento per TRAVEL

- **ARCHER / GHOST**: solo passi ortogonali (`|dx| + |dy| < 2`), niente diagonali
- **BERSERKER / VAMPIRE**: passi in 8 direzioni (`|dx| < 2 && |dy| < 2`), con corner-cut check (non puoi attraversare un angolo strettto fra due muri)

### Usare la mappa

Il bot riceve la mappa tramite `setMap()`. Puoi leggerla come `map[y][x]`:

```java
MatrixLogic.Tiles[][] map = this.map;  // ereditato da Bot

// Verifica se una cella e' percorribile
if (MatrixLogic.isValid(map[3][7])) {
    // map[3][7] e' GROUND, puoi muoverti lì
}

// Verifica linea di vista (utile per ARCHER)
boolean sameRow = MatrixLogic.checkClearRow(self.y, self.x, opponent.x);
if (sameRow) {
    // riga libera, puoi sparare lungo questa riga
}
```

### Esempi di bot inclusi

Il progetto include due bot di esempio:

- **`SimpleBerserkerBot.java`** — un BERSERKER che insegue l'avversario e attacca
- **`SimpleArcherBot.java`** — un ARCHER che spara a distanza e fugge

Usali come punto di partenza.

---

## API di riferimento

### `Bot` (classe base)

```java
public abstract class Bot {
    public enum RpgClass { ARCHER, BERSERKER, GHOST, VAMPIRE }

    public static final Map<RpgClass, Integer> HP;
    // ARCHER=3, BERSERKER=4, GHOST=4, VAMPIRE=3

    protected MatrixLogic.Tiles[][] map;

    public Bot(RpgClass rpgClass);

    public void setMap(Tiles[][] map);
    public RpgClass getRpgClass();

    public abstract Action[] move(Coord self, Coord opponent, Coord powerup,
                                  int hpSelf, int hpOpponent);
}
```

### `Action`

```java
public class Action {
    public enum ActionType { TRAVEL, ATTACK, TELEPORT, HEAL }

    public Action(ActionType type);

    public void setDX(int dx);
    public void setDY(int dy);
    public int getDX();
    public int getDY();

    public void setABSX(int x);
    public void setABSY(int y);
    public int getABSX();
    public int getABSY();

    public ActionType getActionType();
}
```

### `Coord`

```java
public class Coord {
    public int x;
    public int y;
    public Coord(int x, int y);
}
```

### `MatrixLogic` (metodi statici utili)

```java
// Verifica se una cella e' camminabile
public static boolean isValid(Tiles tile);

// Verifica se una colonna e' libera fra due righe
public static boolean checkClearColumn(int col, int startY, int endY);

// Verifica se una riga e' libera fra due colonne
public static boolean checkClearRow(int row, int startX, int endX);
```

### `MatrixLogic.Tiles`

```java
public enum Tiles { GROUND, WALL_ROCK, WALL_TREE, WALL_FIRECAMP }
```

Solo `GROUND` e' percorribile (`isValid` ritorna `true`).

---

## Struttura del progetto

```
Hack_n_slash_swing/
├── run.sh                   # Script di avvio (Linux/macOS)
├── run.bat                  # Script di avvio (Windows)
├── SPEC.md                  # Specifica tecnica completa
├── README.md                # Questo file
├── .vscode/
│   ├── settings.json        # Configurazione VS Code
│   └── launch.json          # Configurazione debug
├── lib/                     # Librerie (scaricate da run.sh)
└── src/hack_n_slash/
    ├── Main.java            # Entry point
    ├── bots/
    │   ├── Bot.java         # Classe base + enum RpgClass
    │   ├── SimpleBerserkerBot.java
    │   └── SimpleArcherBot.java
    ├── engines/
    │   ├── Engine.java      # Interfaccia
    │   └── Engine1stEdition.java  # Motore di gioco
    ├── graphics/
    │   ├── GameView.java    # Interfaccia vista
    │   ├── GameState.java   # Stato serializzato
    │   └── WebView.java     # Server HTTP + GUI browser
    ├── map/
    │   └── MatrixLogic.java # Griglia, generazione, LOS
    └── miscellaneous/
        ├── Action.java      # Tipo azione + dati
        └── Coord.java       # Coppia (x, y)
```

---

## Dependencias

| Libreria | Versione | Scopo |
|---|---|---|
| `org.reflections:reflections` | 0.10.2 | Discovery automatico dei bot via reflection |
| `org.javassist:javassist` | 3.28.0-GA | Dipendenza di reflections |
| `com.google.guava:guava` | 31.1-jre | Dipendenza di reflections |
| `org.slf4j:slf4j-api` | 2.0.13 | Logging |
| `org.slf4j:slf4j-simple` | 2.0.13 | Backend di logging |

La GUI non richiede librerie esterne: il server HTTP (`com.sun.net.httpserver`) e il rendering (`java.awt`, `javax.imageio`) sono nello JDK standard.

---

## FAQ

**Il browser non si apre**

Vai su http://localhost:8765 nel tuo browser.

**La port 8765 e' occupata**

Cambia `PORT` in `src/hack_n_slash/graphics/WebView.java`.

**Il gioco parte da solo senza cliccare**

Hai una vecchia pagina browser aperta con il timer attivo. Chiudi il tab e riapri http://localhost:8765. Ogni partita ha un game ID unico, quindi paggine vecchie non interferiscono piu'.

**Come aggiungo il mio bot alla partita**

Metti il tuo file `.java` in `src/hack_n_slash/bots/`. Compila con `./run.sh`. Il motore lo trovera' automaticamente. Lo prende i primi due che trova.

**Posso avere piu' di due bot**

Il motore attualmente carica i primi due bot che trova. Per cambiare abbinamenti, vedi `Main.java` riga 26-27.
