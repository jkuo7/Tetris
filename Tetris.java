import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Tetris extends JPanel{
// 10 boxes wide, 20 boxes high, each box is 16 pixels
// Start grid at 100,100, 160 wide, 320 high
	// Grid[(y-100)/16][(x-100)/16];

	public Tetrimino currentPiece, nextPiece, nextPiece2, nextPiece3, nextPiece4, nextPiece5, nextPiece6, heldPiece;

	//Stores whether a piece is currently being held
	private boolean holding=false;

	private static final int BOARDWIDTH = 10;
	private static final int BOARDHEIGHT = 20;

	private static final int PIXELSIZE = 24;
	private static final int NEXTSIZE = PIXELSIZE/2;

	private static final int HELDSTARTX = PIXELSIZE;
	private static final int HELDWIDTH = 6*NEXTSIZE;
	private static final int HELDHEIGHT = HELDWIDTH;
	private static final int HELDSTARTY = 50;

	private static final int GRIDSTARTX = HELDSTARTX + HELDWIDTH + PIXELSIZE;
	private static final int GRIDSTARTY = HELDSTARTY;

	private static final int NEXTSTARTX = GRIDSTARTX + BOARDWIDTH*PIXELSIZE + PIXELSIZE;
	private static final int NEXTSTARTY = GRIDSTARTY;
	private static final int NEXTWIDTH = HELDWIDTH;
	private static final int NEXTHEIGHT = 19*NEXTSIZE;

	private static final int POINTSX = NEXTSTARTX;
	private static final int POINTSY = NEXTSTARTY + 19*NEXTSIZE + PIXELSIZE;
	private static final int POINTSWIDTH = NEXTWIDTH;
	private static final int POINTSHEIGHT = 112;

	private static final int GAMEWIDTH = NEXTSTARTX + NEXTWIDTH + PIXELSIZE;
	private static final int GAMEHEIGHT = 600;

	private int score = 0;
	private int linesCleared = 0;
	private int level = 1;
	private int exp = 0;
	private int delay = 1000;

	Color [][] grid = new Color[BOARDHEIGHT][BOARDWIDTH];
	private Timer timer;

	public Tetris(){
		this.setBackground(new Color(220, 243, 255));
		bindKeyStrokes();

		//Initiates the game grid
		for (int col=0; col<BOARDWIDTH; col++){
			for (int row=0; row<BOARDHEIGHT; row++){
				grid[row][col] = Color.BLACK;
			}
		}

		//Initiates the pieces
		currentPiece = new Tetrimino(this);
		nextPiece = new Tetrimino(this);
		nextPiece2 = new Tetrimino(this);
		nextPiece3 = new Tetrimino(this);
		nextPiece4 = new Tetrimino(this);
		nextPiece5 = new Tetrimino(this);
		nextPiece6 = new Tetrimino(this);

		timer = new Timer(delay, new ActionListener(){
			@Override
  			public void actionPerformed(ActionEvent e) {
	        		moveCurrentDown();
  			}
		});
		timer.start();
	}

	private void bindKeyStrokes(){
		//Binds keys to game actions
		bindKeyStrokeTo("down.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), downAc());
		bindKeyStrokeTo("right.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), sideAc(true));
		bindKeyStrokeTo("left.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), sideAc(false));
		bindKeyStrokeTo("up.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), rotateAc(true));
		bindKeyStrokeTo("x.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), rotateAc(true));
		bindKeyStrokeTo("ctrl.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, InputEvent.CTRL_DOWN_MASK), rotateAc(false));
		bindKeyStrokeTo("Z.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0), rotateAc(false));
		bindKeyStrokeTo("C.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), holdAc());
		bindKeyStrokeTo("shift.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK), holdAc());
	}

    private void bindKeyStrokeTo(String name, KeyStroke keyStroke, Action action) {
    	//Binds keyStroke to action
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        im.put(keyStroke, name);
        am.put(name, action);
    }

    private Action downAc(){
    	//When called, moves current piece down 1 row
    	return new AbstractAction(){
        	@Override
        	public void actionPerformed(ActionEvent ae){
        		moveCurrentDown();
        	}
        };
    }

    private Action sideAc(boolean right){
    	/*When called, moves current piece to the side 1 column
		If right is true, moves to the right.
		Else, moves to the left*/
    	return new AbstractAction(){
        	@Override
        	public void actionPerformed(ActionEvent ae){
        		currentPiece.moveSide(right);
        	}
        };
    }

    private Action rotateAc(boolean clockwise){
    	/*When called, rotates the current piece 90 degrees
    	If clockwise is true, rotates clockwise.
    	Else, rotates counterclockwise*/
    	return new AbstractAction(){
        	@Override
        	public void actionPerformed(ActionEvent ae){
        		currentPiece.rotate(clockwise);
        	}
        };
    }

    private Action holdAc(){
    	/*When called, stores current piece in hold, but only if there isn't already a piece being held
    	The next piece becomes the current piece
    	After the next piece is placed, the held piece will become the current piece and another piece can be held*/
    	return new AbstractAction(){
    		@Override
    		public void actionPerformed(ActionEvent ae){
    			if(!holding){
    				heldPiece = currentPiece;
    				heldPiece.hold();
    				makeNewPiece();
    				holding=true;
    			}
    		}
    	};
    }

    private void moveCurrentDown(){
    	/*Moves the current piece down, if possible
    	If it cannot, checks to see if the game is over
    	If the game isn't over, stores the piece in the grid, checks for cleared rows, and starts the next piece*/
		if(!currentPiece.moveDown()){
			if (currentPiece.isOutsideGrid()){
				gameOver();
			}else{
				addToGrid();
				clearRows();
				levelUp();
				makeNewPiece();
				repaint(GRIDSTARTX, GRIDSTARTY, BOARDWIDTH*PIXELSIZE, BOARDHEIGHT*PIXELSIZE);
			}
		}
    }

	private void addToGrid(){
		/*Stores the placed piece's color in the grid spaces corresponding to its location
		Then moves the piece itself out of the grid*/
		Color color = currentPiece.getColor();
		for(int i=0; i<4; i++){
			grid[currentPiece.getRow(i)][currentPiece.getCol(i)] = color;
		}
		currentPiece.moveOut();
	}

	private void clearRows(){
		/*Checks if rows are empty. If so, clears it and moves the rows above down
		Then awards exp and points based on how many rows were cleared simultaneously*/
		int rowsCleared = 0;
		int expGained = 0;
		for (int row=0; row<BOARDHEIGHT; row++){
			if(rowFull(row)){
				rowsCleared++;
				moveRowsDown(row);	
			}
		}
		linesCleared += rowsCleared;
		if(rowsCleared == 1) expGained = 1;
		if(rowsCleared == 2) expGained = 3;
		if(rowsCleared == 3) expGained = 5;
		if(rowsCleared == 4) expGained = 8;
		exp += expGained;
		score += expGained*100*level;
	}

	private boolean rowFull(int row){
		//Returns whether the given row is full
		for (int col=0; col<BOARDWIDTH; col++){
			if(emptySquare(row,col)){
				return false;
			}
		}
		return true;
	}

	private void moveRowsDown(int clearedRow){
		//Moves all the rows above the cleared row down 1 row
		for (int row=clearedRow; row>0; row--){
			for (int col=0; col<BOARDWIDTH; col++){
				grid[row][col] = grid[row-1][col];
			}
		}
		for(int col=0; col<BOARDWIDTH; col++){
			grid[0][col] = Color.BLACK;
		}
	}	

	private void levelUp(){
		//If the player has reached 5 times the level in exp, increases the level and speed, up to level 15
		if(exp >= 5*level && level < 15){
			exp = 0;
			// exp -= 5*level;
			level = level + 1;
			delay = (int)(Math.pow(0.8-((level - 1)*0.007), level - 1)*1000);
			timer.setDelay(delay);
		}
	}

	private void makeNewPiece(){
		/*After a piece has been placed, if a piece is being held, makes it the current piece
		Otherwise, makes the next piece the current piece, shifts the following next pieces, and makes a new next piece*/
		if(!holding){
			currentPiece = nextPiece;
			currentPiece.makeCurrent();
			nextPiece = nextPiece2;
			nextPiece2 = nextPiece3;
			nextPiece3 = nextPiece4;
			nextPiece4 = nextPiece5;
			nextPiece5 = nextPiece6;
			nextPiece6 = new Tetrimino(this);
			repaint(NEXTSTARTX, NEXTSTARTY, 6*NEXTSIZE, 22*NEXTSIZE);
		}else{
			currentPiece = heldPiece;
			currentPiece.makeCurrent();
			holding = false;
			repaint(HELDSTARTX, HELDSTARTY, HELDWIDTH - 1, HELDHEIGHT - 1);
		}
	}

	private void paintNextPieces(Graphics2D g2d){
		//Paints the next pieces in the box
		nextPiece.paintNext(g2d, 0);
		nextPiece2.paintNext(g2d, 1);
		nextPiece3.paintNext(g2d, 2);
		nextPiece4.paintNext(g2d, 3);
		nextPiece5.paintNext(g2d, 4);
		nextPiece6.paintNext(g2d, 5);
	}

	private void paintHeldPiece(Graphics2D g2d){
		//Paints the held piece, if there is one
		if(holding){
			heldPiece.paintHeld(g2d);
		}
	}

    public Dimension getPreferredSize() {
    	//Returns the dimensions of the game board
        return new Dimension(GAMEWIDTH, GAMEHEIGHT);
    }

	public boolean emptySquare(int row, int col){
		//Returns whether the given square is empty
		return grid[row][col].equals(Color.BLACK);
	}


	public void paintGrid(Graphics2D g2d){
		//Paints the game grid (the pieces that have already been place)
		for (int col=0; col<BOARDWIDTH; col++){
			for (int row=0; row<BOARDHEIGHT; row++){
				if(!grid[row][col].equals(Color.BLACK)){
					g2d.setColor(Color.GRAY);
					g2d.drawRect(PIXELSIZE*col + GRIDSTARTX, PIXELSIZE*row + GRIDSTARTY, PIXELSIZE - 1, PIXELSIZE - 1);
					g2d.setColor(grid[row][col]);
					g2d.fillRect(PIXELSIZE*col + GRIDSTARTX + 1, PIXELSIZE*row + GRIDSTARTY + 1, PIXELSIZE - 2, PIXELSIZE - 2);
				}else{
					g2d.setColor(Color.BLACK);
					g2d.fillRect(PIXELSIZE*col + GRIDSTARTX, PIXELSIZE*row + GRIDSTARTY, PIXELSIZE, PIXELSIZE);
				}
			}
		}
	}

	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.BLACK);
		g2d.setFont(new Font("Consolas", Font.BOLD, 30));
		FontMetrics fm = g2d.getFontMetrics();
		g2d.drawString("NEXT", NEXTSTARTX + (NEXTWIDTH - fm.stringWidth("NEXT"))/2, NEXTSTARTY - fm.getHeight() + fm.getAscent());
		g2d.drawRect(NEXTSTARTX, NEXTSTARTY, NEXTWIDTH - 1, NEXTHEIGHT - 1);

		g2d.drawString("HELD", HELDSTARTX + (HELDWIDTH - fm.stringWidth("HELD"))/2, HELDSTARTY - fm.getHeight() + fm.getAscent());
		g2d.drawRect(HELDSTARTX, HELDSTARTY, HELDWIDTH - 1, HELDHEIGHT - 1);		

		g2d.drawRect(POINTSX, POINTSY, POINTSWIDTH - 1, POINTSHEIGHT);
		g2d.setFont(new Font("Consolas", Font.PLAIN, 15));
		fm = g2d.getFontMetrics();
		int textHeight = fm.getHeight();
		int textAscent = fm.getAscent();
		g2d.drawString("POINTS:", POINTSX + 2, POINTSY + 1 + textHeight - textAscent/2);
		g2d.drawString("" + score, POINTSX + POINTSWIDTH - fm.stringWidth("" + score) - 2, POINTSY + 1 + 2*textHeight - textAscent/2);
		g2d.drawString("CLEARED:", POINTSX + 2, POINTSY + 1 + 3*textHeight - textAscent/2);
		g2d.drawString("" + linesCleared, POINTSX + POINTSWIDTH - fm.stringWidth("" + linesCleared) - 2, POINTSY + 1 + 4*textHeight - textAscent/2);
		g2d.drawString("LEVEL:", POINTSX + 2, POINTSY + 1 + 5*textHeight - textAscent/2);
		g2d.drawString("" + level, POINTSX + POINTSWIDTH - fm.stringWidth("" + level) - 2, POINTSY + 1 + 6*textHeight - textAscent/2);

		paintGrid(g2d);
		currentPiece.paint(g2d);
		paintNextPieces(g2d);
		paintHeldPiece(g2d);
	}

	public static void main(String[] args){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				createAndShowGUI();
			}
		});
	}

	private static void createAndShowGUI(){
		JFrame frame = new JFrame("Tetris");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Tetris tetris = new Tetris();
		frame.getContentPane().add(tetris);
		frame.pack();
		frame.setVisible(true);
	}

	private void gameOver(){
		//Displays game over message
		JOptionPane.showMessageDialog(this, "Lines Cleared: " + score, "Game Over", JOptionPane.YES_NO_OPTION);
		System.exit(ABORT);
	}
}