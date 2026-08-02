package hack_n_slash.bots;

import java.util.*;

import hack_n_slash.miscellaneous.*;
import hack_n_slash.map.MatrixLogic;

public abstract class Bot {

	public static enum RpgClass {
		ARCHER,
		BERSERKER,
		GHOST,
		VAMPIRE
	}
	
	public static final Map<RpgClass, Integer> HP = Map.of(
	        RpgClass.ARCHER, 3,
	        RpgClass.BERSERKER, 4,
	        RpgClass.GHOST, 4,
	        RpgClass.VAMPIRE, 3
	    );
	
	private RpgClass myClass;
	
	protected MatrixLogic.Tiles[][] map;
	
	public Bot(RpgClass rpgClass) {
		this.myClass = rpgClass;
	}
	
	public Bot() {
		
	}
	
	public void setRpgClass(Bot.RpgClass rpgClass) {
		this.myClass = rpgClass;
	}
	
	public RpgClass getRpgClass() {
		return myClass;
	}

	public void setMap(MatrixLogic.Tiles[][] map) {
		this.map = map;
	}
	
	public abstract Action[] move(Coord self, Coord opponent, Coord powerup, int hpSelf, int hpOpponent);
	
}
