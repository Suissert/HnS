package hack_n_slash.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MatrixLogic {
	
	public static enum Tiles {
		GROUND,
		WALL_ROCK,
		WALL_TREE,
		WALL_FIRECAMP
	}
	
	public static final Map<Tiles, String> SYMBOL = Map.of(
			Tiles.GROUND, ".",
			Tiles.WALL_ROCK, "X",
			Tiles.WALL_TREE, "X",
			Tiles.WALL_FIRECAMP, "X"
	    );
	
	private final Random rand;
	
	private static Tiles[][] matrix;
	private List<int[]> posValid = new ArrayList<>();
	
//=====================================================================================================================
	
	//identificatori
	private final Tiles[] wallID = {Tiles.WALL_ROCK, Tiles.WALL_TREE, Tiles.WALL_FIRECAMP};
	
//=====================================================================================================================
	
	//variabili generazione
	private double prob;
	private double deltaProb;
	private int wallNum;
	final public boolean oShape = true;
	final public boolean plusShape = true;
	
//=====================================================================================================================
	
	public MatrixLogic(int r, int c) {
		this(r, c, 1.0, 0.5, 15);
	}
	
	public MatrixLogic(int r, int c, double prob, double deltaProb, int wallNum) {
		this.rand = new Random();
		this.matrix = new Tiles[r][c];
		this.prob = prob;
		this.deltaProb = deltaProb;
		this.wallNum = wallNum;
	}
	
//=====================================================================================================================
	
	public Tiles[][] getMatrix() {
		return matrix;
	}

//=====================================================================================================================
	
	public Tiles[][] generaMatrice() {
		
		matrix = generaMatriceDiBase();
		
		inserisciMuri();

		return matrix;
	}

//=====================================================================================================================
	
	private Tiles[][] generaMatriceDiBase() {
		
		for(int i = 0; i < matrix.length; i++) {
			for(int j = 0; j < matrix[0].length; j++) {
				
				if((i == 0 || i == matrix.length - 1) || (j == 0 || j == matrix[0].length - 1)) {
					matrix[i][j] = estraiMuroCasuale();
				} else {
					matrix[i][j] = Tiles.GROUND;
				}
			}
		}	
		
		return matrix;
	}

//=====================================================================================================================
	
	private void inserisciMuri() {
		
		validPos();
		
		double tmpProb = prob;
		
		while(!posValid.isEmpty() && this.wallNum > 0) {
			
			int[] pos = posValid.remove(rand.nextInt(posValid.size()));
			matrix[pos[0]][pos[1]] = estraiMuroCasuale();
			this.wallNum--;
			
			prob = tmpProb;
			
			List<int[]> near = getNearValid(pos);
			
			while(prob > 0 && this.wallNum > 0 && !near.isEmpty()) {
				
				int[] next = near.remove(rand.nextInt(near.size()));
				posValid.removeIf(p -> p[0] == next[0] && p[1] == next[1]);
								
				matrix[next[0]][next[1]] = estraiMuroCasuale();
				wallNum--;
				prob -= this.deltaProb;
				
				pos = next;
				near = getNearValid(pos);
			}
		}
		
		/* STAMPA DEBUG
		for (int i=0; i<matrix.length; i++)
		{
			for (int j=0; j<matrix[0].length; j++)
			{
				System.out.print(matrix[i][j] + "   ");
			}
			System.out.println("\n");
		}
		*/
	}

//=====================================================================================================================
	
	private List<int[]> getNearValid(int[] valList) {
		
		List<int[]> nearValid = new ArrayList<>();
		
		int[] dx = {-1, 1, 0, 0};
		int[] dy = {0, 0, -1, 1};
		
		for(int i = 0; i < 4; i++) {
			int nx = valList[0] + dx[i];
            int ny = valList[1] + dy[i];
            if(isValid(matrix[nx][ny])) {
            	if(nx >= 0 && ny >= 0 && nx < matrix.length && ny < matrix[0].length && 
            			isValid(matrix[nx][ny]) && 
            			!caselleVietate(nx, ny)) {
            		
            		nearValid.add(new int[] {nx, ny});
            	}
            }
		}
		return nearValid;
	}
	
//=====================================================================================================================

	private void validPos() {
		posValid = new ArrayList<>();

		for(int i = 0; i < matrix.length; i++) {
			for(int j = 0; j < matrix[0].length; j++) {
				if(isValid(matrix[i][j])) {
					
					if(caselleVietate(i, j)) {
						continue;
					}
					
					posValid.add(new int[]{i, j});
				}
			}
		}
	}

//=====================================================================================================================
	
	public static boolean isValid(Tiles valoreInPosizione) {
		return valoreInPosizione == Tiles.GROUND;
	}

//=====================================================================================================================
	
	private Tiles estraiMuroCasuale() {
		return wallID[rand.nextInt(wallID.length)];
	}

//=====================================================================================================================
	
	public boolean caselleVietate(int i, int j) {
		
		if(oShape && ((i == 1 || i == matrix.length - 2) || (j == 1 || j == matrix[0].length - 2))) {
			return true;
		}
		
		if(plusShape && (i == matrix.length / 2 || j == matrix[0].length / 2)) {
			return true;
		}
		
		if((i == 2 && j == 2) || (i == matrix.length - 3 && j == 2) ||
				(i == 2 && j == matrix[0].length - 3) ||
				(i == matrix.length - 3 && j == matrix[0].length - 3)) {
			return true;
		}
		
		return false;		 
	}
	
//=====================================================================================================================

	public static boolean checkClearColumn(int col, int startY, int endY) {
		boolean ret = true;
		
		if (startY > endY)
		{
			int tmp = startY;
			startY = endY;
			endY = tmp;
		}
		
		for (int i = startY; i<=endY; i++)
		{
			if (!isValid(matrix[i][col])) {
				ret = false;
			}
		}
		
		return ret;
	}
	
	public static boolean checkClearRow(int row, int startX, int endX) {
		boolean ret = true;
		
		if (startX > endX)
		{
			int tmp = startX;
			startX = endX;
			endX = tmp;
		}
		
		for (int i = startX; i<=endX; i++)
		{
			if (!isValid(matrix[row][i])) {
				ret = false;
			}
		}
		
		return ret;
	}
	
}
