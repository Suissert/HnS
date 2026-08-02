package hack_n_slash.bots;

import hack_n_slash.miscellaneous.Action;
import hack_n_slash.miscellaneous.Coord;

import java.util.ArrayList;

import hack_n_slash.map.MatrixLogic;

public class SimpleBerserkerBot extends Bot{

	Action moveRight = new Action(Action.ActionType.TRAVEL);
	Action moveLeft = new Action(Action.ActionType.TRAVEL);
	Action moveUp = new Action(Action.ActionType.TRAVEL);
	Action moveDown = new Action(Action.ActionType.TRAVEL);
	Action attack = new Action(Action.ActionType.ATTACK);
	
	public SimpleBerserkerBot() {
		super(Bot.RpgClass.BERSERKER);
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
		
		int dy = opponent.y - self.y;
		int dx = opponent.x - self.x;
		
		ret.add(dx>0?moveRight:moveLeft);
		System.out.println((dx>0?moveRight:moveLeft).getActionType() + " " + (dx>0?moveRight:moveLeft).getDX() + " " + (dx>0?moveRight:moveLeft).getDY());
		ret.add(dy>0?moveDown:moveUp);
		System.out.println((dy>0?moveDown:moveUp).getActionType() + " " + (dy>0?moveDown:moveUp).getDX() + " " + (dy>0?moveDown:moveUp).getDY());
		ret.add(attack);
		
		return ret.toArray(new Action[0]);
	}
	
}
