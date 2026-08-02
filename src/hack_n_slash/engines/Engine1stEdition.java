package hack_n_slash.engines;

import java.lang.Math;

import hack_n_slash.miscellaneous.*;
import hack_n_slash.bots.*;
import hack_n_slash.map.MatrixLogic;
import hack_n_slash.graphics.GameView;
import hack_n_slash.graphics.GameState;

public class Engine1stEdition extends Engine {

	public enum Time {
		DAY,
		NIGHT
	}
	
	private MatrixLogic.Tiles[][] map;
	private Bot[] bots;
	private Time time;
	private Coord[] coord;
	private int[] hp;
	private boolean[] alreadyTeleported;
	private boolean[] berserkerBuffNextTurn;
	private boolean oneMoreTurn;
	private GameView view;
	private int currentTurn;

	public Engine1stEdition (MatrixLogic.Tiles[][] map, Bot bot0, Bot bot1, GameView view) {
		this.map = map;
		this.view = view;
		this.currentTurn = 0;
		
		bots = new Bot[2];
		bots[0] = bot0;
		bots[1] = bot1;
		
		coord = new Coord[3]; // 0 -> bot0  |  1 -> bot1  |  2 -> power-up
		coord[0] = new Coord(1, 1);
		coord[1] = new Coord(map[0].length-2, map.length-2);
		coord[2] = new Coord(-1, -1);
		
		hp = new int[2];
		hp[0] = Bot.HP.get(bots[0].getRpgClass());
		hp[1] = Bot.HP.get(bots[1].getRpgClass());
		
		alreadyTeleported = new boolean[2];
		alreadyTeleported[0] = false;
		alreadyTeleported[1] = false;

		berserkerBuffNextTurn = new boolean[2];
		berserkerBuffNextTurn[0] = false;
		berserkerBuffNextTurn[1] = false;
	}
	
	public void start() {
		
		bots[0].setMap(map);
		bots[1].setMap(map);

        oneMoreTurn = false;
    	time = Time.DAY;
    	
		boolean terminated = false;
        int i = (int) (Math.random() * 2);
        int j = 0;

    	view.render(new GameState(time, hp[0], hp[1],
    			coord[0].x, coord[0].y, coord[1].x, coord[1].y,
    			coord[2].x, coord[2].y, map, currentTurn));
    	view.waitForAdvance();
    	
        while (!terminated) { //fino a fine partita
        	
        	i = (i+1)%2;
        	currentTurn = i;
        	terminated = play(i, bots[i].move(coord[i], coord[(i+1)%2], coord[2], hp[i], hp[(i+1)%2]));
        	if (oneMoreTurn) {
        		i = (i+1)%2;
        	}
        	
        	j++;
        	if (j==3) {
        		time = (time == Time.DAY)?Time.NIGHT:Time.DAY;
        		j = 0;
        		if (time == Time.DAY) {
        			do {
        				coord[2].x = (int) (Math.random() * (map[0].length - 2)) + 1;
        				coord[2].y = (int) (Math.random() * (map.length) - 2) + 1;
        			} while (!MatrixLogic.isValid(map[coord[2].y][coord[2].x]));
        		}
        	}
        	view.render(new GameState(time, hp[0], hp[1],
        			coord[0].x, coord[0].y, coord[1].x, coord[1].y,
        			coord[2].x, coord[2].y, map, currentTurn));
        	view.waitForAdvance();
}
        int winner = (hp[0] > 0) ? 0 : 1;
        System.out.println("Bot" + winner + " Vince!");
        view.showWinner(winner);
        view.render(new GameState(time, hp[0], hp[1],
    			coord[0].x, coord[0].y, coord[1].x, coord[1].y,
    			coord[2].x, coord[2].y, map, currentTurn));
    }
	
	public boolean play(int turn, Action[] as) {
		boolean terminated = false;

		boolean alreadyAttacked = false;
		int travels = 0;
		
		for (int i = 0; i < as.length && !terminated; i++)
		{
			if (as[i].getActionType() == Action.ActionType.TRAVEL
			   && ((time == Time.DAY && travels == 0)
				  ||((bots[turn].getRpgClass() != Bot.RpgClass.VAMPIRE || time == Time.NIGHT) && travels < 2))) {
				terminated = play(turn, as[i]);
				travels++;
			}
			else if (!alreadyAttacked && as[i].getActionType() == Action.ActionType.ATTACK) {
				terminated = play(turn, as[i]);
				alreadyAttacked = true;
			}
			else if (bots[turn].getRpgClass() == Bot.RpgClass.ARCHER && !alreadyTeleported[turn] && as[i].getActionType() == Action.ActionType.TELEPORT) {
				terminated = play(turn, as[i]);
				alreadyTeleported[turn] = true;
			}
			else if (bots[turn].getRpgClass() == Bot.RpgClass.GHOST && !alreadyAttacked && as[i].getActionType() == Action.ActionType.HEAL) {
				terminated = play(turn, as[i]);
				alreadyAttacked = true;
			}
		}
		
		this.berserkerBuffNextTurn[turn] = false;
		
		return terminated;
	}
	
	public boolean play(int turn, Action a) {
		if (a.getActionType() == Action.ActionType.ATTACK &&
				(
						(bots[turn].getRpgClass() == Bot.RpgClass.ARCHER &&
							((coord[0].x == coord[1].x && MatrixLogic.checkClearColumn(coord[0].x, coord[0].y, coord[1].y))
							|| (coord[0].y == coord[1].y && MatrixLogic.checkClearRow(coord[0].y, coord[0].x, coord[1].x))))
				||		(bots[turn].getRpgClass() == Bot.RpgClass.BERSERKER &&
							(coord[0].x - coord[1].x < 2 && coord[0].x - coord[1].x > -2
							&& coord[0].y - coord[1].y < 2 && coord[0].y - coord[1].y > -2)) //QUADRATO 3x3, ignora ostacoli
				||		(bots[turn].getRpgClass() == Bot.RpgClass.GHOST &&
							(coord[0].x - coord[1].x < 2 && coord[0].x - coord[1].x > -2
							&& coord[0].y - coord[1].y < 2 && coord[0].y - coord[1].y > -2)) //QUADRATO 3x3, ignora ostacoli
				||		(bots[turn].getRpgClass() == Bot.RpgClass.VAMPIRE &&
						(coord[0].x - coord[1].x < 2 && coord[0].x - coord[1].x > -2
						&& coord[0].y - coord[1].y < 2 && coord[0].y - coord[1].y > -2)) //QUADRATO 3x3, ignora ostacoli
				)
		   )
		{
			hp[(turn+1)%2]--;
			if (this.berserkerBuffNextTurn[turn]) {
				hp[(turn+1)%2]--;
			}
			if (bots[turn].getRpgClass() == Bot.RpgClass.BERSERKER) {
				if (hp[turn] <= 2) {
					hp[(turn+1)%2]--;
				}
				berserkerBuffNextTurn[turn] = true;
			}
			if (bots[turn].getRpgClass() == Bot.RpgClass.VAMPIRE) {
				hp[turn]++;
			}
			if (coord[(turn+1)%2].x == coord[2].x && coord[(turn+1)%2].y == coord[2].y) {
				activatePowerUp(turn+1%2);
			}
			if (bots[(turn+1)%2].getRpgClass()==Bot.RpgClass.ARCHER) {
				int tmpx = 2 * coord[(turn+1)%2].x - coord[turn].x;
				int tmpy = 2 * coord[(turn+1)%2].y - coord[turn].y;
				if (MatrixLogic.isValid(map[tmpy][tmpx])) {
					coord[(turn+1)%2].x = tmpx;
					coord[(turn+1)%2].y = tmpy;
				}
			}
			if (hp[(turn+1)%2] < 1) { // CHECK VITTORIA
				return true;
			}
		}
		else if (a.getActionType() == Action.ActionType.TRAVEL &&
				MatrixLogic.isValid(map[coord[turn].y + a.getDY()][coord[turn].x + a.getDX()]) &&
				(
					(bots[turn].getRpgClass() == Bot.RpgClass.ARCHER
					&& Math.abs(a.getDX()) + Math.abs(a.getDY()) < 2)
				||  (bots[turn].getRpgClass() == Bot.RpgClass.BERSERKER
					&& Math.abs(a.getDX()) < 2 && Math.abs(a.getDY()) < 2)
				||  (bots[turn].getRpgClass() == Bot.RpgClass.GHOST
					&& Math.abs(a.getDX()) + Math.abs(a.getDY()) < 2)
				||  (bots[turn].getRpgClass() == Bot.RpgClass.VAMPIRE
					&& Math.abs(a.getDX()) < 2 && Math.abs(a.getDY()) < 2)
				)
			)
		{
			// CHECK ATTRAVERSAMENTO ANGOLO IN CASO DI abs(dx) + abs(dy) = 2
			if (MatrixLogic.isValid(map[coord[turn].y][coord[turn].x + a.getDX()])
				|| MatrixLogic.isValid(map[coord[turn].y + a.getDY()][coord[turn].x])) {
				coord[turn].x += a.getDX();
				coord[turn].y += a.getDY();
				//POWER-UP
				if (coord[turn].x == coord[2].x && coord[turn].y == coord[2].y) {
					activatePowerUp(turn);
				}
			}
		}
		else if (a.getActionType() == Action.ActionType.TELEPORT
				&& !alreadyTeleported[turn]
				&& bots[turn].getRpgClass() == Bot.RpgClass.ARCHER
			    && MatrixLogic.isValid(map[a.getABSY()][a.getABSX()])
			)
		{
			coord[turn].x = a.getABSX();
			coord[turn].y = a.getABSY();
		}
		else if (a.getActionType() == Action.ActionType.HEAL
				&& bots[turn].getRpgClass() == Bot.RpgClass.GHOST
				&& hp[turn] < Bot.HP.get(bots[turn].getRpgClass())
			)
		{
			hp[turn]++;
		}
		return false;
	}
	
	private void activatePowerUp(int turn) {
		int rand = (int) (Math.random()*3);
		switch(rand) {
			case 0: hp[turn] = Bot.HP.get(bots[turn].getRpgClass()); break; // MAX HP
			case 1: time = (time == Time.DAY)?Time.NIGHT:Time.DAY; break; // CAMBIO giorno/notte
			case 2: oneMoreTurn = true; break; // UN TURNO IN PIU'
		}
		coord[2].x = -1;
		coord[2].y = -1;
	}
	
}
