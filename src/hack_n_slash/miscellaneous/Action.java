package hack_n_slash.miscellaneous;

public class Action {

	public static enum ActionType {
		TRAVEL,
		ATTACK,
		TELEPORT,
		HEAL
	}
	
	private ActionType myType;
	
	private int dx;
	private int dy;
	private int absx;
	private int absy;
	
	public Action(ActionType actionType) {
		this.myType = actionType;
		if (actionType == ActionType.TRAVEL) {
			dx = 0;
			dy = 0;
		}
		if (actionType == ActionType.TELEPORT) {
			absx = 0;
			absy = 0;
		}
	}
	
	public void setDX(int x)
	{
		dx = x;
	}
	
	public void setDY(int y)
	{
		dy = y;
	}
	
	public int getDX()
	{
		return dx;
	}
	
	public int getDY()
	{
		return dy;
	}
	
	public void setABSX(int x)
	{
		absx = x;
	}
	
	public void setABSY(int y)
	{
		absy = y;
	}
	
	public int getABSX()
	{
		return absx;
	}
	
	public int getABSY()
	{
		return absy;
	}
	
	public ActionType getActionType() {
		return myType;
	}
	
}
