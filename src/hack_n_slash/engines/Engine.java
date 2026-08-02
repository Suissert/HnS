package hack_n_slash.engines;

import hack_n_slash.miscellaneous.Action;

public abstract class Engine {
	
	public abstract void start();
	
	public abstract boolean play(int turn, Action[] as);
	
	public abstract boolean play(int turn, Action a);

	
}
