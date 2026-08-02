package hack_n_slash.graphics;

import java.util.Scanner;

import hack_n_slash.map.MatrixLogic;

/**
 * Implementazione ASCII di {@link GameView}: stampa la griglia su console
 * e attende Invio dall'utente per avanzare un turno.
 *
 * Riutilizza lo Scanner passato dal Main per evitare di consumare input
 * buffered da System.in con Scanner multipli.
 */
public class AsciiView implements GameView {

    private final Scanner scan;

    public AsciiView(Scanner scan) {
        this.scan = scan;
    }

    @Override
    public void render(GameState s) {
        System.out.println("\n\n\n" + s.time
                + "\t\t HP bot0: " + s.hp0 + "\t\t HP bot1: " + s.hp1);
        System.out.println("-------------------------------------------------------");
        for (int i = 0; i < s.map.length; i++) {
            for (int j = 0; j < s.map[0].length; j++) {
                if (s.bot0Y == i && s.bot0X == j) {
                    System.out.print("\u2584  ");
                } else if (s.bot1Y == i && s.bot1X == j) {
                    System.out.print("\u2593  ");
                } else if (s.powerupY == i && s.powerupX == j) {
                    System.out.print("?  ");
                } else {
                    System.out.print(MatrixLogic.SYMBOL.get(s.map[i][j]) + "  ");
                }
            }
            System.out.println("\n");
        }
    }

    @Override
    public void waitForAdvance() {
        scan.nextLine();
    }

    @Override
    public void showWinner(int winnerBotIndex) {
        System.out.println("\n=== Bot" + winnerBotIndex + " Vince! ===\n");
    }
}