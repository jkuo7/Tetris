import java.awt.Color;
import java.awt.Graphics2D;
import java.util.*;

public class Tetrimino{
	private Color color;
	private static Color violet = new Color(128,0,128);
	private static Color[] pieces = {Color.CYAN, Color.BLUE, Color.ORANGE, Color.YELLOW, Color.GREEN, violet, Color.RED};
	private int num;

	private Tetris game;

	//Stores the starting locations of the pieces
	private static int [][] startRows = {{-1,-1,-1,-1}, {-1,-1,-1,-2}, {-1,-1,-1,-2}, {-2,-1,-2,-1}, {-1,-1,-2,-2}, {-1,-1,-2,-1}, {-1,-1,-2,-2}};
	private static int [][] startCols = {{3,4,5,6}, {5,4,3,3}, {3,4,5,5}, {4,4,5,5}, {3,4,4,5}, {3,4,4,5}, {5,4,4,3}};
	
	//Stores the current locations of the piece
	private int [] curRows;
	private int [] curCols;

	//Stores the piece's current rotation state (0, 1, 2, 3), increasing for clockwise, decreasing for counterclockwise
	private int rotationState = 0;

	private static int [] outSpaces = {21,21,21,21};

	private static final int BOARDWIDTH = 10;
	private static final int BOARDHEIGHT = 20;
	private static final int SIZE = 24;
	private static final int NEXTSIZE = SIZE/2;

	private static final int HELDSTARTX = SIZE;
	private static final int HELDWIDTH = 6*NEXTSIZE;

	private static final int GRIDSTARTX = HELDSTARTX + HELDWIDTH + SIZE;
	private static final int GRIDSTARTY = 50;
	private static final int HELDSTARTY = GRIDSTARTY;


	private static final int NEXTSTARTX = GRIDSTARTX + BOARDWIDTH*SIZE + SIZE;
	private static final int NEXTSTARTY = GRIDSTARTY;

	//Defines the test moves for wall kicks
	private static int [][][] xKick = { {{0,1,1,0,1},{0,-1,-1,0,-1}}, {{0,1,1,0,1},{0,1,1,0,1}}, {{0,-1,-1,0,-1},{0,1,1,0,1}}, {{0,-1,-1,0,-1},{0,-1,-1,0,-1}}};
	private static int [][][] yKick = { {{0,0,1,-2,-2},{0,0,1,-2,-2}}, {{0,0,-1,2,2},{0,0,-1,2,2}}, {{0,0,1,-2,-2},{0,0,1,-2,-2}}, {{0,0,-1,2,2},{0,0,-1,2,2}}};
	//                                    state0, ccw    state0, cw       state1, ccw    state1, cw
	private static int [][][] xKickI = { {{0,-1,2,-1,2},{0,-2,1,-2,1}}, {{0,2,-1,2,-1},{0,-1,2,-1,2}}, {{0,1,-2,1,-2},{0,2,-1,2,-1}}, {{0,-2,1,-2,1},{0,1,-2,1,-2}} };
	private static int [][][] yKickI = { {{0,0,0,2,-1},{0,0,0,-1,2}}, {{0,0,0,1,-2},{0,0,0,2,-1}}, {{0,0,0,-2,1},{0,0,0,1,-2}}, {{0,0,0,-1,2},{0,0,0,-2,1}} };
	//Defines the text moves for wall kicks for I pieces

	public Tetrimino(Tetris game){
		this.game = game;
		num = (int)(Math.random() * 7);
		// num = 3;
		color = pieces[num];
		curRows = Arrays.copyOf(startRows[num], 4);
		curCols = Arrays.copyOf(startCols[num], 4);
	}

	public Color getColor(){
		return color;
	}

	public int getRow(int i){
		//Returns the row the square is in
		return curRows[i];
	}

	public int getCol(int i){
		//Returns the column the square is in
		return curCols[i];
	}

	public void paint(Graphics2D g2d){
		//Paints the piece in the game
		for(int i=0; i<4; i++){
			if(curRows[i] >= 0){
				g2d.setColor(Color.GRAY);
				g2d.drawRect(SIZE*curCols[i] + GRIDSTARTX, SIZE*curRows[i] + GRIDSTARTY, SIZE - 1, SIZE - 1);
				g2d.setColor(color);
				g2d.fillRect(SIZE*curCols[i] + GRIDSTARTX + 1, SIZE*curRows[i] + GRIDSTARTY + 1, SIZE - 2, SIZE - 2);	
			}
		}	
	}

	public void paintNext(Graphics2D g2d, int next){
		//Paints a next piece
		for(int i=0; i<4; i++){
			g2d.setColor(Color.BLACK);
			g2d.drawRect(NEXTSIZE*(curCols[i]-3) + NEXTSTARTX + NEXTSIZE, NEXTSIZE*(curRows[i]+2) + NEXTSTARTY + NEXTSIZE + 3*NEXTSIZE*next, NEXTSIZE - 1, NEXTSIZE - 1);
			g2d.setColor(color);
			g2d.fillRect(NEXTSIZE*(curCols[i]-3) + NEXTSTARTX + NEXTSIZE + 1, NEXTSIZE*(curRows[i]+2) + NEXTSTARTY + NEXTSIZE + 3*NEXTSIZE*next + 1, NEXTSIZE - 2, NEXTSIZE - 2);	
		}	
	}

	public void paintHeld(Graphics2D g2d){
		//Paints the piece being held
		for(int i=0; i<4; i++){
			g2d.setColor(Color.BLACK);
			g2d.drawRect(NEXTSIZE*(curCols[i]-3) + HELDSTARTX + NEXTSIZE, NEXTSIZE*(curRows[i]+2) + HELDSTARTY + 2*NEXTSIZE, NEXTSIZE - 1, NEXTSIZE - 1);
			g2d.setColor(color);
			g2d.fillRect(NEXTSIZE*(curCols[i]-3) + HELDSTARTX + NEXTSIZE + 1, NEXTSIZE*(curRows[i]+2) + HELDSTARTY + 2*NEXTSIZE + 1, NEXTSIZE - 2, NEXTSIZE - 2);	
		}	
	}

	public void hold(){
		//Removes the piece to be held 
		for(int i=0; i<4; i++){
			game.repaint(SIZE*curCols[i] + GRIDSTARTX - 1, SIZE*curRows[i] + GRIDSTARTY - 1, SIZE + 1, SIZE + 1);	
		}
		curRows = Arrays.copyOf(startRows[num], 4);
		curCols = Arrays.copyOf(startCols[num], 4);
	}

	public boolean moveSide(boolean right){
		/*Moves piece to the side 1 column, if possible
		If right is true, moves to the right.
		Else, moves to the left*/
		int colShift = -1;
		if(right){
			colShift= 1;
		}
		for(int i=0; i<4; i++){
			if(curCols[i] + colShift >= BOARDWIDTH || curCols[i] + colShift < 0  || (curRows[i] >= 0 && !game.emptySquare(curRows[i], curCols[i] + colShift))){
				return false;
			}
		}
		for(int i=0; i<4; i++){
			curCols[i] += colShift;
		}
		for(int i=0; i<4; i++){
			game.repaint(SIZE*Math.min(curCols[i], curCols[i] - colShift) + GRIDSTARTX - 1, SIZE*curRows[i] + GRIDSTARTY - 1, 2*SIZE + 1, SIZE + 1);	
		}
		return true;
	}

	public boolean moveDown(){
		//Moves piece down 1 row, if possible
		for(int i=0; i<4; i++){
			if(curRows[i] + 1 >= BOARDHEIGHT || (curRows[i] + 1 >= 0 && !game.emptySquare(curRows[i]+1, curCols[i]))){
				return false;
			}
		}
		for(int i=0; i<4; i++){
			curRows[i] += 1;
		}
		for(int i=0; i<4; i++){
			game.repaint(SIZE*curCols[i] + GRIDSTARTX - 1, SIZE*(curRows[i]-1) + GRIDSTARTY - 1, SIZE + 1, 2*SIZE + 1);	
		}
		return true;
	}

	public boolean rotate(boolean clockwise){
		/*Rotates the current piece 90 degrees, if possible
    	If clockwise is true, rotates clockwise.
    	Else, rotates counterclockwise*/
		if(color.equals(Color.YELLOW)){
			return true;
		}

		int sin = clockwise ? -1 : 1;

		double pivotX = getPivotX();
		double pivotY = getPivotY();
		double newPivotX, newPivotY;

		int[] rotRows = new int[4];
		int[] rotCols = new int[4];
		int[] kickRows = new int[4];
		int[] kickCols = new int[4];

		for(int i=0;i<4;i++){
			double centerX = curCols[i] + 0.5;
			double centerY = curRows[i] + 0.5;

			double rotCenterX = sin*(centerY - pivotY) + pivotX;
			double rotCenterY = -sin*(centerX - pivotX) + pivotY;

			rotRows[i]= (int)(rotCenterY - 0.5);
			rotCols[i] = (int)(rotCenterX - 0.5);
		}

		for(int j=0; j<5; j++){
			for(int i=0; i<4; i++){
				if(color.equals(Color.CYAN)){
					kickCols[i] = rotCols[i] + xKickI[rotationState][(1-sin)/2][j];
					kickRows[i] = rotRows[i] + yKickI[rotationState][(1-sin)/2][j];
				}else{
					kickCols[i] = rotCols[i] + xKick[rotationState][(1-sin)/2][j];
					kickRows[i] = rotRows[i] + yKick[rotationState][(1-sin)/2][j];
				}
			}

			if (squaresAvailable(kickCols, kickRows)){
				curCols = kickCols;
				curRows = kickRows;
				newPivotX = getPivotX();
				newPivotY = getPivotY();
				rotationState = (rotationState - sin + 4) % 4;
				game.repaint(SIZE*(int)(Math.min(pivotX, newPivotX)-1.5) + GRIDSTARTX - 1, SIZE*(int)(Math.min(pivotY, newPivotY)-1.5) + GRIDSTARTY - 1, 4*SIZE + SIZE*(int)Math.abs(pivotX-newPivotX) + 1, 5*SIZE + SIZE*(int)Math.abs(pivotX-newPivotX) + 1);
				return true;
			}
		}
		return false;
	}

	public boolean squaresAvailable(int[] cols, int[]rows){
		//Checks whether the attempted rotation is valid, i.e. that the squares are empty
		for(int i=0; i<4; i++){
			if(cols[i] < 0 || cols[i]>= BOARDWIDTH || rows[i] < 0 || rows[i]>= BOARDHEIGHT || !game.emptySquare(rows[i], cols[i])){
				return false;
			}
		}
		return true;
	}

	public void moveOut(){
		//Moves the piece out of the gameboard
		curCols = outSpaces;
		curRows = outSpaces;
		color = new Color(220, 243, 255);
	}

	public boolean isOutsideGrid(){
		//Checks whether any parts of the piece are left above the game board
		for(int i=0; i<4;i++){
			if(curRows[i]<0){
				return true;
			}
		}
		return false;
	}

	private double getPivotX(){
		//Finds the x coordinate of the piece's pivot point
		if(color.equals(Color.CYAN)){
			if(rotationState == 3){
				return (curCols[1] + curCols[2] + 1 )/2 + 1;
			}
			return (curCols[1] + curCols[2] + 1 )/2;
		}else{
			return curCols[1] + 0.5;
		}
	}

	private double getPivotY(){
		//Finds the y coordinate of the piece's pivot point
		if(color.equals(Color.CYAN)){
			if(rotationState == 0){
				return (curRows[1] + curRows[2] + 1 )/2 + 1;
			}
			return (curRows[1] + curRows[2] + 1 )/2;
		}else{
			return curRows[1] + 0.5;	
		}
	}
}
