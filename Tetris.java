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

	private int countdown = 0;
	private boolean gameStarted = false;
	private boolean paused = false;
	private boolean flicker = true;

	private int score = 0;
	private int linesCleared = 0;
	private int combo = -1;
	private int b2b = 0;

	private int level = 1;
	private int exp = 0;
	private int delay = 1000;

	private int rowsDropped = 0;

	private boolean inHardDrop = false;
	private boolean inSoftDrop = false;

	Color [][] grid = new Color[BOARDHEIGHT][BOARDWIDTH];
	private Timer timer;

	public Tetris(){
		this.setBackground(new Color(220, 243, 255));


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

		bindKeyStrokeTo("enter.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), countDownAc());

		timer = new Timer(750, new ActionListener(){
			@Override
  			public void actionPerformed(ActionEvent e) {
	        		repaint();
  			}
		});
		timer.start();
	}

	private Action countDownAc(){
		//Starts countdown before game beings
		return new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent ae){
				if(!gameStarted){					
					getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), null);
					getActionMap().put("enter.pressed", null);
					countDown();
				}
			}
		};
	}

	private void countDown(){
		// Counts down to game starting
		countdown = 4;
		timer.stop();
		timer = new Timer(1000, new ActionListener(){
			@Override
  			public void actionPerformed(ActionEvent e) {
		       	repaint();
				countdown--;
				if(countdown == 0){
					startGame();
				}
  			}
		});
		timer.setInitialDelay(0);
		timer.start();
	}

	private void startGame(){
		//Starts the game
		gameStarted = true;
		bindKeyStrokes();
		timer.stop();
		timer = new Timer(delay, new ActionListener(){
			@Override
  			public void actionPerformed(ActionEvent e) {
		       		moveCurrentDown();
  			}
		});
		timer.setInitialDelay(500);
		timer.start();
	}

	private Action pauseUnpause(){
		//Pauses or unpauses the game
		return new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent ae){
				paused = !paused;
				if(paused){
					pauseGame();
				}else{
					countDown();
				}
			}
		};
	}

	private void pauseGame(){
		//Pauses the game
		timer.stop();
		timer = new Timer(750, new ActionListener(){
			@Override
  			public void actionPerformed(ActionEvent e) {
	        		repaint();
  			}
		});
		timer.start();
	}

	private void bindKeyStrokes(){
		//Binds keys to game actions
		bindKeyStrokeTo("down.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), softDrop());
		bindKeyStrokeTo("down.released", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), stopSoftDrop());
		bindKeyStrokeTo("space.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), hardDrop());

		bindKeyStrokeTo("right.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), sideAc(true));
		bindKeyStrokeTo("left.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), sideAc(false));
		bindKeyStrokeTo("up.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), rotateAc(true));
		bindKeyStrokeTo("x.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), rotateAc(true));
		bindKeyStrokeTo("ctrl.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, InputEvent.CTRL_DOWN_MASK), rotateAc(false));
		bindKeyStrokeTo("Z.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0), rotateAc(false));
		bindKeyStrokeTo("C.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), holdAc());
		bindKeyStrokeTo("shift.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK), holdAc());

		bindKeyStrokeTo("Esc.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), pauseUnpause());
		bindKeyStrokeTo("F1.pressed", KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), pauseUnpause());
	}

    private void bindKeyStrokeTo(String name, KeyStroke keyStroke, Action action) {
    	//Binds keyStroke to action
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        im.put(keyStroke, name);
        am.put(name, action);
    }

    private Action hardDrop(){
		//Starts hard drop
		return new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent ae){
				if(!inHardDrop && !paused){
					if(inSoftDrop){
						inSoftDrop = false;
						score += Math.min(rowsDropped, 20);
					}
					inHardDrop = true;
					timer.setDelay(1);
					timer.setInitialDelay(0);
					timer.restart();
				}
			}
		};
    }

    private Action softDrop(){
		//Starts soft drop
		return new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent ae){
				if(!inHardDrop && !paused && !inSoftDrop){
					inSoftDrop = true;
					timer.setDelay(50);
					timer.setInitialDelay(50);
					timer.restart();
				}
			}
		};
    }

    private Action stopSoftDrop(){
		//Stops soft drop
		return new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent ae){
				if(inSoftDrop && !paused){
					inSoftDrop = false;
					score += Math.min(rowsDropped, 20);
					repaint(POINTSX, POINTSY, POINTSWIDTH - 1, POINTSHEIGHT);
					rowsDropped = 0;
					timer.setDelay(delay);
				}
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
			if(!inSoftDrop && !paused){
        			if(currentPiece.moveSide(right)){
					timer.restart();
				}
			}
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
			if(!inSoftDrop && !paused){
        			if(currentPiece.rotate(clockwise)){
					timer.restart();
				}
			}
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
    			if(!holding && !inHardDrop && !inSoftDrop && !paused){
    				heldPiece = currentPiece;
    				heldPiece.hold();
    				makeNewPiece();
    				holding=true;
    				repaint(HELDSTARTX, HELDSTARTY, HELDWIDTH - 1, HELDHEIGHT - 1);
    			}
    		}
    	};
    }

    private void moveCurrentDown(){
    	/*Moves the current piece down, if possible
    	If it cannot, checks to see if the game is over
    	If the game isn't over, stores the piece in the grid, checks for cleared rows, and starts the next piece*/
		if(!currentPiece.moveDown()){
			if(inSoftDrop || inHardDrop){
				timer.setInitialDelay(500);
				timer.setDelay(delay);
				score += inHardDrop ? 2*Math.min(rowsDropped, 20) : Math.min(rowsDropped, 20);
				inSoftDrop = false;
				inHardDrop = false;
				rowsDropped = 0;
				repaint(POINTSX, POINTSY, POINTSWIDTH - 1, POINTSHEIGHT);
				timer.restart();				
			}
			if (currentPiece.isOutsideGrid()){
				gameOver();
			}else{
				int tSpin = currentPiece.checkForTSpin();
				addToGrid();
				clearRows(tSpin);
				levelUp();
				makeNewPiece();
				repaint(GRIDSTARTX, GRIDSTARTY, BOARDWIDTH*PIXELSIZE, BOARDHEIGHT*PIXELSIZE);
			}
		}else if(inSoftDrop || inHardDrop){
			rowsDropped ++;
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

	private void clearRows(int tSpin){
		/*Checks if rows are empty. If so, clears it and moves the rows above down
		Then awards exp and points based on how many rows were cleared simultaneously and on whether a TSpin was performed*/
		int rowsCleared = 0;
		int expGained = 0;
		int scoreGained = 0;

		for (int row=0; row<BOARDHEIGHT; row++){
			if(rowFull(row)){
				rowsCleared++;
				moveRowsDown(row);	
			}
		}
		linesCleared += rowsCleared;

		switch(tSpin + rowsCleared){
			case 1:
				scoreGained = 100;
				expGained = 1;
				break;
			case 2: case 12:
				scoreGained = 300;
				expGained = 3;
				break;
			case 3: case 13:
				scoreGained = 500;
				expGained = 5;
				break;
			case 4:
				scoreGained = b2b == 4 ? 1200 : 800;
				expGained = 8;
				break;
			case 10:
				scoreGained = b2b == 10? 150 : 100;
				break;
			case 11:
				scoreGained = 200;
				expGained = 1;
				break;
			case 20:
				scoreGained = 400;
				expGained = 1;
				break;
			case 21:
				scoreGained = b2b == 21 ? 1200 : 800;
				expGained = 3;
				break;
			case 22:
				scoreGained = b2b == 22 ? 1800 : 1200;
				expGained = 7;
				break;
			case 23:
				scoreGained = b2b == 23 ? 2400 : 1600;
				expGained = 6;
				break;
			default:
				break;
		}
		b2b = rowsCleared == 0 ? b2b : tSpin + rowsCleared;
		scoreGained *= level;		

		if(rowsCleared > 0){
			combo++;
			scoreGained += rowsCleared == 1 ? 20*combo*level : 50*combo*level;
		}else{
			combo = -1;
		}

		exp += expGained;
		score += scoreGained;
		repaint(POINTSX, POINTSY, POINTSWIDTH, POINTSHEIGHT);
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
			repaint(NEXTSTARTX, NEXTSTARTY, NEXTWIDTH, NEXTHEIGHT);
		}else{
			currentPiece = heldPiece;
			currentPiece.makeCurrent();
			holding = false;
			repaint(HELDSTARTX, HELDSTARTY, HELDWIDTH, HELDHEIGHT);
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
				if(!grid[row][col].equals(Color.BLACK) && gameStarted){
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
		g2d.drawLine(NEXTSTARTX, NEXTSTARTY + 3*NEXTSIZE + NEXTSIZE/2, NEXTSTARTX + NEXTWIDTH - 1, NEXTSTARTY + 3*NEXTSIZE + NEXTSIZE/2);


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
		if(paused){
			paintPauseScreen(g2d);
		}else if(countdown !=0){
			g2d.setColor(Color.WHITE);
			g2d.setFont(new Font("Consolas", Font.BOLD, 60));
			fm = g2d.getFontMetrics();
			String time = countdown + "";
			g2d.drawString(time, GRIDSTARTX + (BOARDWIDTH*PIXELSIZE- fm.stringWidth(time))/2, GRIDSTARTY + 1 + fm.getHeight() - fm.getAscent()/2);
		}else if(gameStarted){
			currentPiece.paint(g2d);
			paintNextPieces(g2d);
			paintHeldPiece(g2d);
		}else{
			paintTitleScreen(g2d);
		}
	}

	private void paintTitleScreen(Graphics2D g2d){
		//Paints the title screen
		paintLogo(g2d);
		if(flicker){
			paintControls(g2d);
		}
		flicker = !flicker;
	}

	private void paintLogo(Graphics2D g2d){
		//Paints the TETRIS logo
		g2d.setFont(new Font("Consolas", Font.BOLD, 73));
		FontMetrics fm = g2d.getFontMetrics();
		int textHeight = fm.getHeight();
		int textAscent = fm.getAscent();
		g2d.setColor(Color.BLUE);
		g2d.fillRect(GRIDSTARTX, GRIDSTARTY, BOARDWIDTH*PIXELSIZE, textHeight - 3*textAscent/8);
		g2d.fillRect(GRIDSTARTX + BOARDWIDTH*PIXELSIZE/3, GRIDSTARTY + textHeight - 3*textAscent/8, BOARDWIDTH*PIXELSIZE/3, textHeight - 3*textAscent/8);
		g2d.setColor(Color.RED);
		g2d.drawString("T", GRIDSTARTX + (BOARDWIDTH*PIXELSIZE - fm.stringWidth("TETRIS"))/2, GRIDSTARTY + textHeight - textAscent/2);
		g2d.setColor(Color.ORANGE);
		g2d.drawString("E", GRIDSTARTX + (BOARDWIDTH*PIXELSIZE - fm.stringWidth("ETRIS") + fm.stringWidth("T"))/2, GRIDSTARTY + textHeight - textAscent/2);
		g2d.setColor(Color.YELLOW);
		g2d.drawString("T", GRIDSTARTX + (BOARDWIDTH*PIXELSIZE - fm.stringWidth("TRIS") + fm.stringWidth("TE"))/2, GRIDSTARTY + textHeight - textAscent/2);
		g2d.setColor(Color.GREEN);
		g2d.drawString("R", GRIDSTARTX + (BOARDWIDTH*PIXELSIZE - fm.stringWidth("RIS") + fm.stringWidth("TET"))/2, GRIDSTARTY + textHeight - textAscent/2);
		g2d.setColor(Color.CYAN);
		g2d.drawString("I", GRIDSTARTX + (BOARDWIDTH*PIXELSIZE - fm.stringWidth("IS") + fm.stringWidth("TETR"))/2, GRIDSTARTY + textHeight - textAscent/2);
		g2d.setColor(new Color(128,0,128));
		g2d.drawString("S", GRIDSTARTX + (BOARDWIDTH*PIXELSIZE - fm.stringWidth("S") + fm.stringWidth("TETRI"))/2, GRIDSTARTY + textHeight - textAscent/2);
	}

	private void paintControls(Graphics2D g2d){
		//Paints controls on screen
		g2d.setColor(Color.WHITE);
		g2d.setFont(new Font("Consolas", Font.PLAIN, 15));
		FontMetrics fm = g2d.getFontMetrics();
		int startWidth = BOARDWIDTH*PIXELSIZE;
		int startHeight = (BOARDHEIGHT*PIXELSIZE - fm.getHeight()) / 2 + fm.getAscent();
		g2d.drawString("LEFT/RIGHT: Left/Right", GRIDSTARTX + (startWidth - fm.stringWidth("LEFT/RIGHT: Left/Right"))/2, startHeight - 40);
		g2d.drawString("DOWN: Soft Drop", GRIDSTARTX + (startWidth - fm.stringWidth("DOWN: Soft Drop"))/2, startHeight);
		g2d.drawString("SPACE: Hard Drop", GRIDSTARTX + (startWidth - fm.stringWidth("SPACE: Hard Drop"))/2, startHeight + 40);

		g2d.drawString("UP/X: Clockwise", GRIDSTARTX + (startWidth - fm.stringWidth("UP/X: Clockwise"))/2, startHeight + 80);
		g2d.drawString("Z/CTRL: Counterclockwise", GRIDSTARTX + (startWidth - fm.stringWidth("Z/CTRL: Counterclockwise"))/2, startHeight + 120);
		g2d.drawString("C/SHIFT: Hold", GRIDSTARTX + (startWidth - fm.stringWidth("C/SHIFT: Hold"))/2, startHeight + 160);
		g2d.drawString("ESC/F1: Pause & Unpause", GRIDSTARTX + (startWidth - fm.stringWidth("ESC/F1: Pause & Unpause"))/2, startHeight + 200);
		if(!paused){
			g2d.drawString("-Press Enter to Start-", GRIDSTARTX + (startWidth - fm.stringWidth("-Press Enter to Start-"))/2, startHeight + 240);
		}else{
			g2d.drawString("-Press ESC/F1 to Unpause-", GRIDSTARTX + (startWidth - fm.stringWidth("-Press ESC/F1 to Unpause-"))/2, startHeight + 240);
		}
	}

	private void paintPauseScreen(Graphics2D g2d){
		//Paints the pause screen
		g2d.setFont(new Font("Consolas", Font.BOLD, 60));
		FontMetrics fm = g2d.getFontMetrics();
		g2d.setColor(Color.WHITE);
		g2d.drawString("PAUSE", GRIDSTARTX + (BOARDWIDTH*PIXELSIZE - fm.stringWidth("PAUSE"))/2, GRIDSTARTY + fm.getHeight() - fm.getAscent()/2);
		paintControls(g2d);
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
		JOptionPane.showMessageDialog(this, "Score: " + score + "\nLines Cleared: " + linesCleared, "Game Over", JOptionPane.YES_NO_OPTION);
		System.exit(ABORT);
	}
}	