package hack_n_slash.graphics;

/**
 * Contratto della "vista" del gioco.
 *
 * Il motore, ad ogni turno del loop principale, chiama {@link #render(GameState)}
 * per mostrare la situazione corrente e poi {@link #waitForAdvance()} per
 * sospendere il proprio thread finche' l'utente non decide di procedere al
 * turno successivo (premendo Invio nella modalita' ASCII, oppure il bottone
 * "Step" o il timer in modalita' GUI).
 *
 * Questo permette di avere piu' implementazioni (ASCII su console, finestra
 * Swing, futura GUI JavaFX...) senza toccare la logica del motore.
 */
public interface GameView {

    /** Disegna/aggiorna la vista con lo stato passato. */
    void render(GameState s);

    /** Blocca il thread chiamante finche' l'utente non chiede il turno successivo. */
    void waitForAdvance();

    /** Mostra il messaggio di vittoria. Default: non fa nulla. */
    default void showWinner(int winnerBotIndex) {}
}