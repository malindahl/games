package orig2011.v6;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


/**
 * A somewhat defective implementation of the game Reversi. The purpose
 * of this class is to illustrate shortcomings in the game framework.
 * 
 * @author evensen
 * 
 */
public class ReversiModel implements GameModel {
	public enum Direction {
		EAST(1, 0),
		SOUTHEAST(1, 1),
		SOUTH(0, 1),
		SOUTHWEST(-1, 1),
		WEST(-1, 0),
		NORTHWEST(-1, -1),
		NORTH(0, -1),
		NORTHEAST(1, -1),
		NONE(0, 0);

		private final int xDelta;
		private final int yDelta;

		Direction(final int xDelta, final int yDelta) {
			this.xDelta = xDelta;
			this.yDelta = yDelta;
		}

		public int getXDelta() {
			return this.xDelta;
		}

		public int getYDelta() {
			return this.yDelta;
		}
	}

	public enum Turn {
		BLACK,
		WHITE;

		public static Turn nextTurn(final Turn t) {
			return t == BLACK ? WHITE : BLACK;
		}
	}

	public enum PieceColor {
		BLACK,
		WHITE,
		EMPTY;

		public static PieceColor opposite(final PieceColor t) {
			return t == BLACK ? WHITE : BLACK;
		}
	}

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	/** Graphical representation of a coin. */
	private static final GameTile blackTile = new RoundTile(Color.BLACK,
			Color.BLACK, 1.0, 0.8);
	private static final GameTile whiteTile = new RoundTile(Color.BLACK,
			Color.WHITE, 1.0, 0.8);
	private static final GameTile blankTile = new SquareTile(Color.BLACK,
			new Color(0, 200, 0), 2.0);
	private static final GameTile whiteGridTile = new CompositeTile(blankTile,
			whiteTile);
	private static final GameTile blackGridTile = new CompositeTile(blankTile,
			blackTile);
	private static final GameTile cursorRedTile = new CrossTile(Color.RED, 2.0);
	private static final GameTile cursorBlackTile = new RoundTile(Color.RED,
			new Color(0, 50, 0), 2.0, 0.8);
	private static final GameTile cursorWhiteTile = new RoundTile(Color.RED,
			new Color(210, 255, 210), 2.0, 0.8);

	private Turn turn;
	private Position cursorPos;
	private final PieceColor[][] board;
	private int whiteScore;
	private int blackScore;
	private final int width;
	private final int height;
	private boolean gameOver;
	private int updateSpeed =-1;

	public ReversiModel() {
		this.width = this.getGameboardSize().width;
		this.height = this.getGameboardSize().height;
		this.board = new PieceColor[this.width][this.height];
		// Blank out the whole gameboard...
		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.height; j++) {
				this.board[i][j] = PieceColor.EMPTY;
			}
		}
		this.turn = Turn.BLACK;

		// Insert the four starting bricks.
		int midX = this.width / 2 - 1;
		int midY = this.height / 2 - 1;
		this.board[midX][midY] = PieceColor.WHITE;

		this.board[midX + 1][midY + 1] = PieceColor.WHITE;

		this.board[midX + 1][midY] = PieceColor.BLACK;
		
		this.board[midX][midY + 1] = PieceColor.BLACK;


		// Set the initial score.
		this.whiteScore = 2;
		this.blackScore = 2;

		this.gameOver = false;

		// Insert the collector in the middle of the gameboard.
		this.cursorPos = new Position(midX, midY);
		this.pcs.firePropertyChange("update", new Position(0,0) , cursorPos );
	}

	/**
	 * Return whether the specified position is empty. If it only consists
	 * of a blank tile, it is considered empty.
	 * 
	 * @param pos
	 *            The position to test.
	 * @return true if position is empty, false otherwise.
	 */
	private boolean isPositionEmpty(final Position pos) {
		return this.board[pos.getX()][pos.getY()] == PieceColor.EMPTY;
	}

	/**
	 * Update the direction of the collector
	 * according to the users keypress.
	 * 
	 * @throws GameOverException
	 */
	private Direction updateDirection(final int key) {
		switch (key) {
		case KeyEvent.VK_LEFT:
			return Direction.WEST;
		case KeyEvent.VK_UP:
			return Direction.NORTH;
		case KeyEvent.VK_RIGHT:
			return Direction.EAST;
		case KeyEvent.VK_DOWN:
			return Direction.SOUTH;
		case KeyEvent.VK_SPACE:
			tryPlay();
			return Direction.NONE;
		default:
			// Do nothing if another key is pressed
			return Direction.NONE;
		}
	}

	private void tryPlay() {
		if (isPositionEmpty(this.cursorPos)) {
			if (canTurn(this.turn, this.cursorPos)) {
				turnOver(this.turn, this.cursorPos);
				this.board[this.cursorPos.getX()][this.cursorPos.getY()] =
						(this.turn == Turn.BLACK
						? PieceColor.BLACK
								: PieceColor.WHITE);
				Turn oldTurn = this.turn;
				this.turn = Turn.nextTurn(this.turn);
				this.pcs.firePropertyChange("Turn", oldTurn, this.turn);
				this.pcs.firePropertyChange("update", oldTurn, this.turn);
			}
			if (!canTurn(this.turn)) {
				if (!canTurn(Turn.nextTurn(this.turn))) {
					this.gameOver = true;
					return;
				}
				Turn oldTurn = this.turn;
				this.turn = Turn.nextTurn(this.turn);
				this.pcs.firePropertyChange("Turn", oldTurn, this.turn);
			}
		}

	}

	private void turnOver(final Turn turn, final Position cursorPos) {
		if (isPositionEmpty(cursorPos)) {
			PieceColor myColor =
					(turn == Turn.BLACK ? PieceColor.BLACK : PieceColor.WHITE);
			PieceColor opponentColor = PieceColor.opposite(myColor);
			int blackResult = (turn == Turn.BLACK) ? 1 : -1;
			int whiteResult = -blackResult;

			this.blackScore += Math.max(0, blackResult);
			this.whiteScore += Math.max(0, whiteResult);

			for (int i = 0; i < 8; i++) {
				Direction d = Direction.values()[i];
				int xDelta = d.getXDelta();
				int yDelta = d.getYDelta();
				int x = cursorPos.getX() + xDelta;
				int y = cursorPos.getY() + yDelta;
				boolean canTurn = false;
				while (x >= 0 && x < this.width && y >= 0 && y < this.height) {
					if (this.board[x][y] == opponentColor) {
						canTurn = true;
					} else if (this.board[x][y] == myColor && canTurn) {
						// Move backwards to the cursor, flipping bricks
						// as we go.
						x -= xDelta;
						y -= yDelta;
						while (!(x == cursorPos.getX() && y == cursorPos.getY())) {
							this.board[x][y] = myColor;
							x -= xDelta;
							y -= yDelta;
							this.blackScore += blackResult;
							this.whiteScore += whiteResult;
						}
						break;
					} else {
						break;
					}
					x += xDelta;
					y += yDelta;
				}
			}
		}
	}

	private boolean canTurn(final Turn turn) {
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				if (canTurn(turn, new Position(x, y))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean canTurn(final Turn turn, final Position cursorPos) {
		if (isPositionEmpty(cursorPos)) {
			PieceColor myColor =
					(turn == Turn.BLACK ? PieceColor.BLACK : PieceColor.WHITE);
			PieceColor opponentColor = PieceColor.opposite(myColor);
			for (int i = 0; i < 8; i++) {
				Direction d = Direction.values()[i];
				int xDelta = d.getXDelta();
				int yDelta = d.getYDelta();
				int x = cursorPos.getX() + xDelta;
				int y = cursorPos.getY() + yDelta;
				boolean canTurn = false;
				while (x >= 0 && x < this.width && y >= 0 && y < this.height) {
					if (this.board[x][y] == opponentColor) {
						canTurn = true;
					} else if (this.board[x][y] == myColor && canTurn) {
						return true;
					} else {
						break;
					}
					x += xDelta;
					y += yDelta;
				}
			}
		}
		return false;
	}

	/**
	 * Get the current player's color
	 */
	public Turn getTurnColor() {
		return this.turn;
	}

	/**
	 * Accessor to black's current score.
	 * 
	 * @return black's score
	 */
	public int getBlackScore() {
		return this.blackScore;
	}

	/**
	 * Accessor to white's current score.
	 * 
	 * @return white's score
	 */
	public int getWhiteScore() {
		return this.whiteScore;
	}

	/**
	 * Get next position of the collector.
	 */
	private Position getNextCursorPos(final Direction dir) {
		return new Position(this.cursorPos.getX()
				+ dir.getXDelta(),
				this.cursorPos.getY() + dir.getYDelta());
	}

	/**
	 * This method is called repeatedly so that the
	 * game can update its state.
	 * 
	 * @param lastKey
	 *            The most recent keystroke.
	 */

	public void gameUpdate(final int lastKey) throws GameOverException {
		if (!this.gameOver) {
			Position oldCursorPos = this.cursorPos;
			Position nextCursorPos = getNextCursorPos(updateDirection(lastKey));			
			this.pcs.firePropertyChange("update", oldCursorPos, nextCursorPos );
			Dimension boardSize = getGameboardSize();
			int nextX =
					Math.max(0,
							Math.min(nextCursorPos.getX(), boardSize.width - 1));
			int nextY =
					Math.max(
							0,
							Math.min(nextCursorPos.getY(), boardSize.height - 1));
			nextCursorPos = new Position(nextX, nextY);
			this.cursorPos = nextCursorPos;

		} else {
			throw new GameOverException(this.blackScore - this.whiteScore);
		}

	}

	public GameTile getGameboardState(int x, int y) {
		GameTile top= blankTile;
		GameTile bottom = blankTile;
		if (this.board[x][y] == PieceColor.WHITE) {
			top= whiteGridTile;
		} else if (this.board[x][y] == PieceColor.BLACK) {
			top = blackGridTile;
		}
		if (x==this.cursorPos.getX() && y==this.cursorPos.getY()){
			if (canTurn(this.turn, this.cursorPos)) {
				if (this.turn==Turn.WHITE) {
					top=cursorWhiteTile;	
				} else if (this.turn==Turn.BLACK) {
					top=cursorBlackTile;
				}
			} else {
				bottom=top;
				top = cursorRedTile;
			}
		}
		return new CompositeTile(bottom, top);
	}
	
	public GameTile getGameboardState(Position pos) {
		return getGameboardState(pos.getX(), pos.getY());
	}


	public void addObserver(PropertyChangeListener observer) {
			this.pcs.addPropertyChangeListener(observer);
	}

	public void removeObserver(PropertyChangeListener observer) {
		this.pcs.removePropertyChangeListener(observer);
	}

	public int getUpdateSpeed(){
		return updateSpeed;
	}


	public Dimension getGameboardSize() {
		return GameUtils.getGameboardSize();
	}
}

