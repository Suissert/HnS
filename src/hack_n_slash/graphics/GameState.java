package hack_n_slash.graphics;

import hack_n_slash.engines.Engine1stEdition;
import hack_n_slash.map.MatrixLogic;

/**
 * Istantanea immutabile dello stato di gioco in un determinato turno.
 * La usa la view per disegnare la situazione corrente senza leggere direttamente
 * i campi privati del motore.
 */
public class GameState {

    public final Engine1stEdition.Time time;
    public final int hp0;
    public final int hp1;
    public final int bot0X, bot0Y;
    public final int bot1X, bot1Y;
    public final int powerupX, powerupY;
    public final MatrixLogic.Tiles[][] map;
    public final int currentTurn;

    public GameState(Engine1stEdition.Time time,
                     int hp0, int hp1,
                     int bot0X, int bot0Y,
                     int bot1X, int bot1Y,
                     int powerupX, int powerupY,
                     MatrixLogic.Tiles[][] map,
                     int currentTurn) {
        this.time = time;
        this.hp0 = hp0;
        this.hp1 = hp1;
        this.bot0X = bot0X;
        this.bot0Y = bot0Y;
        this.bot1X = bot1X;
        this.bot1Y = bot1Y;
        this.powerupX = powerupX;
        this.powerupY = powerupY;
        this.map = map;
        this.currentTurn = currentTurn;
    }
}