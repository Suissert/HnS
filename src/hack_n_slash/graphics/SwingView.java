package hack_n_slash.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import hack_n_slash.engines.Engine1stEdition;
import hack_n_slash.map.MatrixLogic;

/**
 * Vista grafica Swing: finestra con griglia colorata, pannello stato e bottoni.
 *
 * Il thread del motore chiama render() per disegnare lo stato corrente e
 * waitForAdvance() per sospendersi finche' l'utente (o il timer) non chiede
 * il turno successivo. I bottoni Play/Pausa/Step e il timer agiscono sul
 * anticipo tramite notify su advanceLock.
 */
public class SwingView implements GameView {

    private static final int CELL_SIZE = 36;
    private static final int AUTO_DELAY_MS = 700;

    private GameState state;
    private String winnerText = null;

    private final Object advanceLock = new Object();
    private boolean advanceRequested = false;

    private JFrame frame;
    private JPanel gridPanel;
    private JLabel phaseLabel;
    private JLabel hpLabel;
    private JLabel turnLabel;

    private javax.swing.Timer autoTimer;

    public SwingView() {
        try {
            javax.swing.SwingUtilities.invokeAndWait(() -> buildGui());
        } catch (java.awt.HeadlessException he) {
            throw new IllegalStateException(
                "Impossibile aprire la GUI in ambiente senza display. Usa la modalita' 1 (ASCII).", he);
        } catch (Exception e) {
            throw new RuntimeException("Costruzione GUI fallita.", e);
        }
    }

    private void buildGui() {
        frame = new JFrame("Hack_n_slash");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));

        gridPanel = new JPanel() {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
            }
        };
        gridPanel.setPreferredSize(new Dimension(15 * CELL_SIZE, 9 * CELL_SIZE));
        frame.add(gridPanel, BorderLayout.CENTER);

        JPanel side = new JPanel();
        side.setLayout(new java.awt.GridLayout(0, 1, 4, 4));

        phaseLabel = new JLabel(" ");
        hpLabel = new JLabel(" ");
        turnLabel = new JLabel(" ");
        Font f = phaseLabel.getFont().deriveFont(Font.BOLD, 14f);
        phaseLabel.setFont(f);
        hpLabel.setFont(f);
        turnLabel.setFont(f);

        side.add(new JLabel("STATO"));
        side.add(phaseLabel);
        side.add(hpLabel);
        side.add(turnLabel);
        side.add(new JLabel(" "));

        JButton btnPlay = new JButton("Play");
        JButton btnPause = new JButton("Pausa");
        JButton btnStep = new JButton("Step");
        btnPlay.addActionListener(e -> startAuto());
        btnPause.addActionListener(e -> stopAuto());
        btnStep.addActionListener(e -> requestAdvance());
        side.add(btnPlay);
        side.add(btnPause);
        side.add(btnStep);
        side.add(new JLabel(" "));

        frame.add(side, BorderLayout.EAST);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        startAuto();
    }

    private void startAuto() {
        if (autoTimer == null) {
            autoTimer = new javax.swing.Timer(AUTO_DELAY_MS, e -> requestAdvance());
        }
        autoTimer.start();
    }

    private void stopAuto() {
        if (autoTimer != null) {
            autoTimer.stop();
        }
    }

    private void requestAdvance() {
        synchronized (advanceLock) {
            advanceRequested = true;
            advanceLock.notifyAll();
        }
    }

    @Override
    public void render(GameState s) {
        final GameState snapshot = s;
        javax.swing.SwingUtilities.invokeLater(() -> {
            this.state = snapshot;
            phaseLabel.setText("Fase: " + (snapshot.time == Engine1stEdition.Time.DAY ? "DAY" : "NIGHT"));
            hpLabel.setText("<html>Bot0: " + snapshot.hp0 + " HP<br>Bot1: " + snapshot.hp1 + " HP</html>");
            turnLabel.setText("Turno di: Bot" + snapshot.currentTurn);
            gridPanel.repaint();
        });
    }

    @Override
    public void waitForAdvance() {
        synchronized (advanceLock) {
            while (!advanceRequested) {
                try {
                    advanceLock.wait();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            advanceRequested = false;
        }
    }

    private void drawBoard(Graphics g) {
        if (state == null) {
            g.setColor(new Color(245, 245, 230));
            g.fillRect(0, 0, gridPanel.getWidth(), gridPanel.getHeight());
            return;
        }
        for (int i = 0; i < state.map.length; i++) {
            for (int j = 0; j < state.map[0].length; j++) {
                int x = j * CELL_SIZE;
                int y = i * CELL_SIZE;

                if (MatrixLogic.isValid(state.map[i][j])) {
                    g.setColor(new Color(245, 245, 230));
                } else {
                    g.setColor(new Color(90, 80, 70));
                }
                g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                g.setColor(new Color(200, 200, 185));
                g.drawRect(x, y, CELL_SIZE, CELL_SIZE);

                if (state.bot0Y == i && state.bot0X == j) {
                    g.setColor(Color.BLUE);
                    g.fillOval(x + 7, y + 7, CELL_SIZE - 14, CELL_SIZE - 14);
                } else if (state.bot1Y == i && state.bot1X == j) {
                    g.setColor(Color.RED);
                    g.fillOval(x + 7, y + 7, CELL_SIZE - 14, CELL_SIZE - 14);
                } else if (state.powerupY == i && state.powerupX == j) {
                    g.setColor(new Color(255, 215, 0));
                    g.fillOval(x + 9, y + 9, CELL_SIZE - 18, CELL_SIZE - 18);
                }
            }
        }

        if (winnerText != null) {
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(0, 0, gridPanel.getWidth(), gridPanel.getHeight());
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 28f));
            java.awt.FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(winnerText);
            g.drawString(winnerText, (gridPanel.getWidth() - w) / 2, gridPanel.getHeight() / 2);
        }
    }

    @Override
    public void showWinner(int winnerBotIndex) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            stopAuto();
            winnerText = "Bot" + winnerBotIndex + " Vince!";
            gridPanel.repaint();
        });
    }
}