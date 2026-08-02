package hack_n_slash.bots;

import hack_n_slash.miscellaneous.Action;
import hack_n_slash.miscellaneous.Coord;
import hack_n_slash.map.MatrixLogic;
import java.util.ArrayList;

public class SimpleArcherBot extends Bot{

	Action moveRight = new Action(Action.ActionType.TRAVEL);
	Action moveLeft = new Action(Action.ActionType.TRAVEL);
	Action moveUp = new Action(Action.ActionType.TRAVEL);
	Action moveDown = new Action(Action.ActionType.TRAVEL);
	Action attack = new Action(Action.ActionType.ATTACK);
	Action tp = new Action(Action.ActionType.TELEPORT);
	
	public SimpleArcherBot() {
		super(Bot.RpgClass.ARCHER);
		moveRight.setDX(1);
		moveRight.setDY(0);
		moveLeft.setDX(-1);
		moveLeft.setDY(0);
		moveUp.setDX(0);
		moveUp.setDY(-1);
		moveDown.setDX(0);
		moveDown.setDY(1);
	}
	
	public Action[] move(Coord self, Coord opponent, Coord powerup, int hpSelf, int hpOpponent) {
		ArrayList<Action> ret = new ArrayList<Action>();
		
		Action tmpAction;
		int tmpx, tmpy;
		
		int dy = opponent.y - self.y;
		int dx = opponent.x - self.x;
		
		//attacca poi allontanati
		if (self.y == opponent.y && MatrixLogic.checkClearRow(self.y, self.x, opponent.x)) {
			ret.add(attack);
			tmpAction = (dx>0)?moveLeft:moveRight;
		}
		else if (self.x == opponent.x && MatrixLogic.checkClearColumn(self.x, self.y, opponent.y)) {
			ret.add(attack);
			tmpAction = (dy>0)?moveUp:moveDown;
		}
		//muovi poi attacca
		else if (Math.abs(dy) < 3 && MatrixLogic.checkClearRow(opponent.y, self.x, opponent.x)) {
			tmpAction = (dy>0)?moveDown:moveUp;
			for (int i=0; i<Math.abs(dy); i++)
			{
				ret.add(tmpAction);
			}
			ret.add(attack);
		}
		else if (Math.abs(dx) < 3 && MatrixLogic.checkClearColumn(opponent.x, self.y, opponent.y)) {
			tmpAction = (dx>0)?moveRight:moveLeft;
			for (int i=0; i<Math.abs(dy); i++)
			{
				ret.add(tmpAction);
			}
			ret.add(attack);
		}
		// altrimenti, allontanati
		else {
			tmpAction = (dy>0)?moveLeft:moveRight;
			ret.add(tmpAction);
			ret.add(tmpAction);
		}
		
		//tp difensivo
		if (Math.abs(dx) + Math.abs(dy) < 4
		   && Math.abs(self.x - map[0].length / 2) > map[0].length / 2 - 2
		   && Math.abs(self.y - map.length / 2) > map.length / 2 - 2) {
			do {
				tmpx = (int) (Math.random() * (map[0].length - 2)) + 1;
				tmpy = (int) (Math.random() * (map.length) - 2) + 1;
			} while (!MatrixLogic.isValid(map[tmpy][tmpx]));
			tp.setABSX(tmpx);
			tp.setABSY(tmpy);
			ret.add(tp);
		}
		
		ret.add(moveLeft);
		ret.add(moveUp);
		
		return ret.toArray(new Action[0]);
	}
	
}
