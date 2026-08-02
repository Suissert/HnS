# Hack_n_slash — Specifica del progetto

## Panoramica

Hack_n_slash e' un gioco di programmazione in cui due bot scritti in Java si sfidano su una griglia. I partecipanti implementano un bot sottoclassando `Bot`; il motore di gioco (`Engine1stEdition`) gestisce turno, movimento, combattimento, giorno/notte e power-up.

Il progetto originale si trova in `/home/susan/Repo/Hack_n_slash/`. La versione con GUI browser si trova in `/home/susan/Repo/Hack_n_slash_swing/` e aggiunge:
- Package `graphics/` con `GameView`, `GameState`, `WebView`
- Server HTTP integrato (JDK stdlib) che disegna il tabellone come PNG
- Interfaccia HTML/JS con bottoni Play / Pausa / Step
- `Main.java` semplificato: nessun menu, avvia diretto la GUI

---

## 1. Avvio

### Requisiti
- JDK 17+ (sviluppato con JDK 26)
- Un browser ( qualsiasi — e' arbitrato chiamato `xdg-open` o equivalente)

### Comando
```bash
./run.sh
```
Scarica le dipendenze se mancanti, compila, avvia il gioco e apre il browser su `http://localhost:8765`.

### Da VS Code
Premere F5 sulla classe `Main`. Nessuna configurazione extra richiesta.

---

## 2. La griglia

| Proprieta' | Valore |
|---|---|
| Dimensioni | 9 righe x 15 colonne |
| Bordo | Muri random (roccia/albero/fuoco — tutti bloccanti) |
| Interno | GROUND con fino a 15 muri procedurali a cluster |
| Celle vietate per muri | Anello attorno al bordo (riga/col 1 e 7/13), croce centrale (riga 4, col 7), 4 angoli interni |

Convenzione: `map[y][x]` — riga = y, colonna = x.

`MatrixLogic.isValid(tile)` ritorna `true` solo per `GROUND`.

---

## 3. Posizioni iniziali

| Entita' | Posizione |
|---|---|
| Bot 0 | (1, 1) — alto-sinistra |
| Bot 1 | (13, 7) — basso-destra |
| Power-up | assente all'inizio |

Il primo a muovere e' scelto random (poi invertito dal loop, quindi l'altro bot parte per primo).

---

## 4. Classi e HP

| Classe | HP iniziali / max | Mosse per turno (max step) | Abilita' speciali |
|---|---|---|---|
| **ARCHER** | 3 | 1 passo ortogonale (no diagonale) | Attacco a distanza (LOS), TELEPORT una volta a partita, knockback sul difensore |
| **BERSERKER** | 4 | 1 passo per asse (dia i n 8 direzioni, no corner-cut) | Rage: +1 danno se HP <= 2 |
| **GHOST** | 4 | 1 passo ortogonale | HEAL: cura 1 HP (consume slot attacco, max 4) |
| **VAMPIRE** | 3 | 1 passo per asse (8 direzioni) | Lifesteal: +1 HP ogni attacco (no cap), 1 mossa di giorno / 2 di notte |

###_step size dettagliato
- **ARCHER / GHOST**: `|dx| + |dy| < 2` → solo 1 passo ortogonale
- **BERSERKER / VAMPIRE**: `|dx| < 2 && |dy| < 2` → 1 passo in 8 direzioni (diagonale inclusa), con corner-cut check: almeno una delle due celle ortogonalmente adiacenti deve essere valida

---

## 5. Interfaccia del bot

```java
public abstract class Bot {
    public enum RpgClass { ARCHER, BERSERKER, GHOST, VAMPIRE }

    public static Map<RpgClass, Integer> HP;  // ARCHER=3, BERSERKER=4, GHOST=4, VAMPIRE=3

    public abstract Action[] move(Coord self, Coord opponent, Coord powerup,
                                  int hpSelf, int hpOpponent);
    public void setMap(Tiles[][] map);
    public RpgClass getRpgClass();
}
```

### Come creare un bot
1. Creare una classe nel package `hack_n_slash.bots`
2. Estendere `Bot`
3. Chiamare `super(RpgClass.TUA_CLASSE)` nel costruttore senza argomenti
4. Implementare `move(...)` — ritorna un array di `Action` ordinato (eseguito in sequenza)

### Action
```java
public enum ActionType { TRAVEL, ATTACK, TELEPORT, HEAL }

new Action(ActionType.TRAVEL);   // usa setDX/setDY per displacement relativo
new Action(ActionType.ATTACK);
new Action(ActionType.TELEPORT); // usa setABSX/setABSY per coordinate assolute
new Action(ActionType.HEAL);
```

---

## 6. Struttura del turno

Ogni turno un bot ritorna un array di `Action`. Il motore le esegue in ordine con questi limiti:

| Tipo | Limite per turno | Note |
|---|---|---|
| TRAVEL | fino a 2 | Vedi giorno/notte per il VAMPIRE |
| ATTACK | 1 | HEAL consuma questo slot |
| TELEPORT | 1 per partita | solo ARCHER |
| HEAL | 1 (consume slot attacco) | solo GHOST, solo se HP < max |

Le Action in eccesso sono ignorate silenziosamente.

---

## 7. Ciclo giorno / notte

| Proprieta' | Valore |
|---|---|
| Inizio | DAY |
| Durata | Cambio ogni 3 turni-bot (non 3 round) |
| Effetto | VAMPIRE: 1 TRAVEL di giorno, 2 di notte. Le altre classi non sono affette. |

---

## 8. Power-up

- Spawn solo su transizione NOTTE→GIORNO (ogni 6 turni-bot)
- Posizione random su cella valida dell'interno
- Un solo power-up attivo alla volta
- Effetto random al pickup:

| Roll | Effetto |
|---|---|
| 0 | Cura a HP massimo |
| 1 | Inverte giorno/notte |
| 2 | Un turno extra per il bot che lo raccoglie (see bug noti) |

- Pickup: si attiva camminando sopra la cella (TRAVEL). Non si attiva con TELEPORT.

---

## 9. Combattimento

### Raggio d'attacco
- **ARCHER**: stessa riga o stessa colonna con LOS libera (i muri bloccano)
- **BERSERKER / GHOST / VAMPIRE**: zona 3x3 attorno al difensore (Chebyshev-1), ignora i muri

### Effetti dell'attacco (in ordine)
1. `HP difensore -= 1`
2. Se BERSERKER e HP attaccante <= 2 → `HP difensore -= 1` (rage)
3. Se VAMPIRE → `HP attaccante += 1` (lifesteal, no cap)
4. Se difensore e' ARCHER → **knockback**: spin al tile a specchio `(2*def - att)`, se valido
5. Se HP difensore < 1 → **vittoria** dell'attaccante

### HEAL (GHOST)
- `HP attaccante += 1` se HP < max (4)
- Consuma lo slot attacco

---

## 10. Condizione di vittoria

Una partita termina quando un ATTACK riduce l'HP del difensore sotto 1. L'attaccante vince. Non c'e' limite di turni, ne' pareggio.

---

## 11. GUI: controlli Play / Pausa / Step

L'interfaccia browser ha tre bottoni con questa macchina a stati:

```
                    +-----------+
    INIZIO -------->|  IN PAUSA  |<-----------+
                    +-----------+             |
                     |        |               |
                Play |        | Step          |
                     v        v               |
                    +-----------+         +-----------+
                    |  PLAYING  |  Pausa  |  IN PAUSA  |
                    +-----------+-------> +-----------+
                                          |
                                     Play | (gioca)
                                          v
                                     +-----------+
                                     | GAME OVER |
                                     +-----------+
```

### Regole
- **All'avvio**: stato = PAUSA. Play e Step abilitati, Pausa disabilitata.
- **Play**: avvia autoplay (un turno ogni 700ms). Play si disabilita, Pausa e' abilitata, Step si disabilita.
- **Pausa**: ferma l'autoplay. Torna a PAUSA. Play e Step abilitati, Pausa disabilitata.
- **Step**: avanz a di un singolo turno. Disponibile **solo in PAUSA**. Dopo lo Step, si rimane in PAUSA.
- **Game Over**: tutti i bottoni disabilitati. Il tabellone mostra l'overlay "BotX Vince!".

### Endpoint HTTP del server
| Endpoint | Metodo | Risposta |
|---|---|---|
| `/` | GET | Pagina HTML con tabellone + bottoni |
| `/state` | GET | JSON: `{hp0, hp1, turn, time, winner}` |
| `/board.png` | GET | Immagine PNG del tabellone (muri + bot + powerup + overlay vincitore) |
| `/advance` | POST | Avanza di un turno (sveglio il thread del motore) |

Il browser fa polling di `/state` e `/board.png` ogni 250ms.

---

## 12. Bug noti (non fixati in questa iterazione)

1. **`berserkerBuffNextTurn` azzerato**: il flag viene resettato a fine turno, prima che possa effettivamente Dare l'extra damage al turno successivo.
2. **`oneMoreTurn` non resettato**: una volta attivato, rimane `true` per sempre — lo stesso bot riceve tutti i turn successivi.
3. **`activatePowerUp(turn+1%2)`**: precedenza operatori → `turn + 1` invece di `(turn+1) % 2`. Causa index-out-of-bounds quando `turn == 1`.
4. **TRAVEL di giorno per non-Vampire**: la clausola OR rende il limite di 1 mossa inefficace per non-Vampire.
5. **SimpleArcherBot**: diversi bug di copy-paste (tmpAction non aggiunto, loop con contatore sbagliato, direzione ritirata basata su `dy` invece di `dx`).
6. **Lifesteal Vampire senza cap**: puo' superare l'HP massimo.
7. **Tile visivamente indistinguibili**: roccia/albero/fuoco renderizzano tutti come `X`.

---

## 13. Struttura del progetto (versione GUI)

```
Hack_n_slash_swing/
├── run.sh                          # avvio: dipendenze + compila + browser
├── .vscode/
│   ├── settings.json               # classpath jar
│   └── launch.json                 # F5 su Main
├── lib/                            # jar (reflections, javassist, guava, slf4j)
└── src/hack_n_slash/
    ├── Main.java                   # entry point, istanzia WebView, avvia motore
    ├── bots/
    │   ├── Bot.java                # classe base + enum RpgClass + tabella HP
    │   ├── SimpleBerserkerBot.java
    │   └── SimpleArcherBot.java
    ├── engines/
    │   ├── Engine.java            # interfaccia
    │   └── Engine1stEdition.java  # motore: loop, combat, giorno/notte, power-up
    ├── graphics/
    │   ├── GameView.java           # interfaccia: render, waitForAdvance, showWinner
    │   ├── GameState.java          # DTO immutabile
    │   └── WebView.java            # server HTTP + rendering PNG + pagina HTML/JS
    ├── map/
    │   └── MatrixLogic.java        # griglia, generazione, LOS, isValid
    └── miscellaneous/
        ├── Action.java             # ActionType + dati azione
        └── Coord.java              # coppia (x, y)
```

---

## 14. Dipendenze

| Libreria | Versione | Scopo |
|---|---|---|
| `org.reflections:reflections` | 0.10.2 | Discovery delle classi Bot via reflection |
| `org.javassist:javassist` | 3.28.0-GA | Dipendenza di reflections |
| `com.google.guava:guava` | 31.1-jre | Dipendenza di reflections |
| `org.slf4j:slf4j-api` | 2.0.13 | Logging reflections |
| `org.slf4j:slf4j-simple` | 2.0.13 | Backend logging |

Nessuna dipendenza aggiuntiva per la GUI: il server HTTP (`com.sun.net.httpserver.HttpServer`) e il rendering immagine (`javax.imageio.ImageIO`, `java.awt.Graphics2D`) sono nello JDK standard.

---

## 15. Formato JSON dello stato

`GET /state` ritorna:
```json
{
  "hp0": 4,       // HP bot 0
  "hp1": 3,       // HP bot 1
  "turn": 0,      // indice del bot del turno corrente (0 o 1)
  "time": "DAY",  // "DAY" o "NIGHT"
  "winner": -1    // -1 = in corso, 0 o 1 = vincitore
}
```