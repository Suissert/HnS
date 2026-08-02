package hack_n_slash.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Random;

import javax.imageio.ImageIO;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import hack_n_slash.engines.Engine1stEdition;
import hack_n_slash.map.MatrixLogic;

public class WebView implements GameView {

    private static final int PORT = 8765;
    private static final int CELL = 38;
    private static final int COLS = 15;
    private static final int ROWS = 9;

    private final String gameId;
    private HttpServer server;
    private volatile GameState latestState;
    private final Object advanceLock = new Object();
    private volatile boolean advanceRequested = false;
    private volatile int winnerIndex = -1;

    public WebView() {
        this.gameId = String.valueOf(new Random().nextInt(1_000_000));
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/", this::handlePage);
            server.createContext("/board.png", this::handleBoardImage);
            server.createContext("/state", this::handleState);
            server.createContext("/advance", this::handleAdvance);
            server.setExecutor(null);
            server.start();
            System.out.println("Server su http://localhost:" + PORT + " (game id: " + gameId + ")");
            openBrowser();
        } catch (IOException e) {
            throw new RuntimeException("Server HTTP fallito: " + e.getMessage(), e);
        }
    }

    private void openBrowser() {
        String url = "http://localhost:" + PORT + "/";
        String os = System.getProperty("os.name", "").toLowerCase();
        try {
            if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", url});
            } else {
                Runtime.getRuntime().exec(new String[]{"xdg-open", url});
            }
            System.out.println("Browser aperto su " + url);
        } catch (Exception e) {
            System.err.println("Apri il browser su " + url);
        }
    }

    @Override
    public void render(GameState s) {
        latestState = s;
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

    @Override
    public void showWinner(int winnerBotIndex) {
        this.winnerIndex = winnerBotIndex;
    }

    // ---- HTTP handlers ----

    private void handleAdvance(HttpExchange ex) throws IOException {
        String q = ex.getRequestURI().getQuery();
        if (q == null || !q.equals("id=" + gameId)) {
            ex.sendResponseHeaders(403, -1);
            ex.close();
            return;
        }
        synchronized (advanceLock) {
            advanceRequested = true;
            advanceLock.notifyAll();
        }
        ex.sendResponseHeaders(204, -1);
        ex.close();
    }

    private void handleState(HttpExchange ex) throws IOException {
        GameState s = latestState;
        String json = (s == null) ? "{}" : String.format(
            "{\"hp0\":%d,\"hp1\":%d,\"turn\":%d,\"time\":\"%s\",\"winner\":%d,\"gameId\":%s}",
            s.hp0, s.hp1, s.currentTurn,
            s.time == Engine1stEdition.Time.DAY ? "DAY" : "NIGHT",
            winnerIndex, gameId);
        byte[] body = json.getBytes();
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(200, body.length);
        ex.getResponseBody().write(body);
        ex.close();
    }

    private void handleBoardImage(HttpExchange ex) throws IOException {
        GameState s = latestState;
        BufferedImage img = new BufferedImage(COLS * CELL, ROWS * CELL, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        drawBoard(g, s);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        byte[] body = baos.toByteArray();
        ex.getResponseHeaders().set("Content-Type", "image/png");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(200, body.length);
        ex.getResponseBody().write(body);
        ex.close();
    }

    private void handlePage(HttpExchange ex) throws IOException {
        byte[] body = HTML_PAGE.getBytes();
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(200, body.length);
        ex.getResponseBody().write(body);
        ex.close();
    }

    // ---- disegno del tabellone come immagine ----

    private void drawBoard(Graphics2D g, GameState s) {
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, COLS * CELL, ROWS * CELL);

        for (int i = 0; i < ROWS; i++) {
            if (s == null) break;
            for (int j = 0; j < COLS; j++) {
                int x = j * CELL;
                int y = i * CELL;
                if (MatrixLogic.isValid(s.map[i][j])) {
                    g.setColor(new Color(240, 238, 225));
                } else {
                    g.setColor(new Color(90, 78, 65));
                }
                g.fillRect(x, y, CELL, CELL);
                g.setColor(new Color(200, 195, 180));
                g.drawRect(x, y, CELL, CELL);
            }
        }

        if (s != null) {
            if (s.powerupX >= 0) {
                g.setColor(new Color(255, 200, 0));
                g.fillOval(s.powerupX * CELL + 9, s.powerupY * CELL + 9, CELL - 18, CELL - 18);
            }
            g.setColor(Color.BLUE);
            g.fillOval(s.bot0X * CELL + 7, s.bot0Y * CELL + 7, CELL - 14, CELL - 14);
            g.setColor(Color.RED);
            g.fillOval(s.bot1X * CELL + 7, s.bot1Y * CELL + 7, CELL - 14, CELL - 14);
        }

        if (winnerIndex >= 0) {
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(0, 0, COLS * CELL, ROWS * CELL);
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 28f));
            String msg = "Bot" + winnerIndex + " Vince!";
            int w = g.getFontMetrics().stringWidth(msg);
            g.drawString(msg, (COLS * CELL - w) / 2, ROWS * CELL / 2);
        }
    }

    private static final String HTML_PAGE = """
<!DOCTYPE html>
<html lang="it"><head><meta charset="UTF-8"><title>Hack_n_slash</title>
<style>
  *{font-family:sans-serif;box-sizing:border-box}
  body{background:#1a1a2e;color:#eee;display:flex;flex-direction:column;align-items:center;margin:0;padding:20px}
  h1{margin:0 0 12px;font-size:22px;letter-spacing:1px}
  #panel{display:flex;gap:16px}
  #sidebar{background:#16213e;border-radius:12px;padding:18px;min-width:180px}
  .row{margin-bottom:12px}
  .label{color:#8899aa;font-size:11px;text-transform:uppercase;letter-spacing:1px}
  .val{font-size:17px;font-weight:bold;margin-top:2px}
  .hpbar{height:8px;border-radius:4px;background:#2a2a4a;margin-top:5px;overflow:hidden}
  .hpfill{height:100%;border-radius:4px;transition:width .3s}
  #controls{display:flex;gap:8px;margin-top:16px}
  button{padding:10px 18px;border:none;border-radius:8px;font-size:15px;font-weight:bold;cursor:pointer;transition:.15s}
  #btnPlay{background:#4ecca3;color:#1a1a2e}#btnPause{background:#e07a5f;color:#fff}#btnStep{background:#81b29a;color:#1a1a2e}
  button:disabled{opacity:.35;cursor:default;filter:none}
  button:not(:disabled):hover{filter:brightness(1.15)}
  #board{border-radius:8px;box-shadow:0 4px 20px rgba(0,0,0,.5)}
  #winner{font-size:20px;font-weight:bold;color:#4ecca3;margin-top:12px;text-align:center}
  #status{font-size:12px;color:#8899aa;margin-top:10px;text-transform:uppercase;letter-spacing:1px}
</style></head><body>
  <h1>Hack_n_slash</h1>
  <div id="panel">
    <img id="board" width="570" height="342" />
    <div id="sidebar">
      <div class="row"><div class="label">Fase</div><div id="t" class="val">-</div></div>
      <div class="row"><div class="label">Bot 0</div><div id="h0" class="val">-</div><div class="hpbar"><div id="b0" class="hpfill" style="background:#4f8ef7;width:100%"></div></div></div>
      <div class="row"><div class="label">Bot 1</div><div id="h1" class="val">-</div><div class="hpbar"><div id="b1" class="hpfill" style="background:#f76f6f;width:100%"></div></div></div>
      <div class="row"><div class="label">Turno</div><div id="tn" class="val">-</div></div>
      <div id="controls">
        <button id="btnPlay">Play</button>
        <button id="btnPause" disabled>Pausa</button>
        <button id="btnStep">Step</button>
      </div>
      <div id="status">In pausa</div>
      <div id="winner"></div>
    </div>
  </div>
<script>
const SPEED=700,MAX_HP=4;
let playing=false,timer=null,gameOver=false,myGameId=null;
const board=document.getElementById('board');
const btnPlay=document.getElementById('btnPlay');
const btnPause=document.getElementById('btnPause');
const btnStep=document.getElementById('btnStep');
const statusEl=document.getElementById('status');
function refresh(){board.src='/board.png?t='+Date.now();}
async function advance(){
  if(!myGameId)return;
  try{
    const r=await fetch('/advance?id='+myGameId,{method:'POST'});
    if(!r.ok){stopPlay();}
  }catch(e){stopPlay();}
}
async function pollState(){
  try{
    const r=await fetch('/state');
    const s=await r.json();
    if(s.hp0===undefined)return;
    if(s.gameId!==undefined){
      if(myGameId===null){myGameId=s.gameId;}
      if(s.gameId!==myGameId){window.location.reload();return;}
    }
    document.getElementById('t').textContent=s.time;
    document.getElementById('h0').textContent=s.hp0+' HP';
    document.getElementById('h1').textContent=s.hp1+' HP';
    document.getElementById('tn').textContent='Bot'+s.turn;
    document.getElementById('b0').style.width=Math.max(0,s.hp0)/MAX_HP*100+'%';
    document.getElementById('b1').style.width=Math.max(0,s.hp1)/MAX_HP*100+'%';
    if(s.winner>=0){
      document.getElementById('winner').textContent='Bot'+s.winner+' Vince!';
      gameOver=true;stopPlay();
    }
  }catch(e){}
}
function startPlay(){
  if(gameOver)return;
  playing=true;
  timer=setInterval(()=>advance(),SPEED);
  btnPlay.disabled=true;btnPause.disabled=false;btnStep.disabled=true;
  statusEl.textContent='In riproduzione';
}
function stopPlay(){
  playing=false;
  if(timer)clearInterval(timer);
  timer=null;
  btnPlay.disabled=gameOver;btnPause.disabled=true;btnStep.disabled=gameOver;
  statusEl.textContent=gameOver?'Partita terminata':'In pausa';
}
btnPlay.onclick=startPlay;
btnPause.onclick=stopPlay;
btnStep.onclick=()=>{if(!playing&&!gameOver)advance();};
refresh();pollState();
setInterval(()=>{refresh();pollState();},250);
</script>
</body></html>
""";
}