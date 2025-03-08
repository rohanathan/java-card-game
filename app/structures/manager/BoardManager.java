package structures.manager;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Board;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.player.HumanPlayer;
import structures.basic.player.Player;
import utils.BasicObjectBuilders;
import structures.basic.cards.*;

public class BoardManager {
    private final ActorRef out;
    private final GameState gameState;
    
    public BoardManager(ActorRef out, GameState gameState) 
    {
        this.out = out;
        // Store reference to GameState
        this.gameState = gameState; 

    }
    // initial board setup
	public Board initializeBoard() {
		Board board = new Board();
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 5; j++) {
				Tile tile = BasicObjectBuilders.loadTile(i, j);
				tile.setHighlightMode(0);
				board.setTile(tile, i, j);
				BasicCommands.drawTile(out, tile, 0);
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return board;
	}
    
    private boolean isEnemyOnTile(Tile tile, Player currentPlayer) {
		return tile.isOccupied() && tile.getUnit().getOwner() != currentPlayer;
	}

    // Checks if a tile position is within the boundaries of the game board
	private boolean isValidTile(int x, int y) {
		Tile[][] board = gameState.getBoard().getTiles();
		return x >= 0 && y >= 0 && x < board.length && y < board[x].length;
	}

    // Helper method to add a valid tile to the set of valid actions if the conditions are met
	private void addValidTileInDirection(Tile[][] board, Unit unit, int dx, int dy, Set<Tile> validTiles, Player currentPlayer) {
		int startX = unit.getPosition().getTilex();
		int startY = unit.getPosition().getTiley();
		int endX = startX + dx;
		int endY = startY + dy;

		// Check each step along the path for enemy occupation
		int pathLength = Math.max(Math.abs(dx), Math.abs(dy));
		for (int step = 1; step <= pathLength; step++) {
			int stepX = startX + (int)Math.signum(dx) * step;
			int stepY = startY + (int)Math.signum(dy) * step;

			// Additional check for diagonal moves
			if (Math.abs(dx) == 1 && Math.abs(dy) == 1 && step == 1) {
				if (isValidTile(startX + dx, startY) && isValidTile(startX, startY + dy)) {
					Tile orthogonal1 = board[startX + dx][startY];
					Tile orthogonal2 = board[startX][startY + dy];
					if (isEnemyOnTile(orthogonal1, currentPlayer) && isEnemyOnTile(orthogonal2, currentPlayer)) {
						// If both orthogonal tiles are blocked by enemy units, the diagonal move is invalid
						return;
					}
				}
				else {
					// One of the orthogonal tiles is out of bounds
					return;
				}
			}

			// If any step along the path is invalid, abort adding this tile
			if (!isValidTile(stepX, stepY) || isEnemyOnTile(board[stepX][stepY], currentPlayer)) {
				return;
			}
		}


        
		// If the path is clear, add the final tile to validTiles
		if (isValidTile(endX, endY)) {
			Tile tile = board[endX][endY];
			if (!tile.isOccupied()) { // Ensure the final tile is not occupied
				validTiles.add(tile);
			}
		}
	}

    // Check if a tile is adjacent to a friendly unit of the specified player
	private boolean isAdjacentToAlly(int x, int y, Player player) {
		Tile[][] tiles = gameState.getBoard().getTiles();
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				if (dx == 0 && dy == 0) continue; // Skip the current tile
				int adjX = x + dx;
				int adjY = y + dy;
				if (isValidTile(adjX, adjY)) {
					Tile adjTile = tiles[adjX][adjY];
					if (adjTile.isOccupied() && adjTile.getUnit().getOwner() == player) {
						return true;
					}
				}
			}
		}
		return false;
	}

    
    // remove highlight from all tiles
	public void removeHighlightFromAll() {
		Board board = gameState.getBoard();
		Tile[][] tiles = board.getTiles();
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 5; j++) {
				if (tiles[i][j].getHighlightMode() != 0) {
					Tile currentTile = tiles[i][j];
					currentTile.setHighlightMode(0);
					BasicCommands.drawTile(out, currentTile, 0);
				}
			}
		}
		if (!gameState.getHuman().getHand().getCards().isEmpty()) {
			gameState.getPlayerManager().notClickingCard();
		}
	}
    

    // helper method to update tile highlight
	public void updateTileHighlight(Tile tile, int tileHighlightMode) {
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		tile.setHighlightMode(tileHighlightMode);
		BasicCommands.drawTile(out, tile, tileHighlightMode);
	}

    // Method to calculate and return the set of valid actions (tiles) for a given unit
	public Set<Tile> getValidMoves(Tile[][] board, Unit unit) {
		
		Set<Tile> validTiles = new HashSet<>();

		// Skip calculation if unit is provoked or has moved/attacked this turn
		if (gameState.getCombatHandler().checkProvoked(unit) || unit.hasMoved() || unit.hasAttacked()) {
			return validTiles;
		}

		if (unit.getName().equals("Young Flamewing")) {
			return gameState.getBoard().getAllUnoccupiedTiles(board);
		}

		Player currentPlayer = unit.getOwner();
		int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
		int[][] extendedDirections = {{-2, 0}, {2, 0}, {0, -2}, {0, 2}, {-1, -1}, {1, 1}, {-1, 1}, {1, -1}};

		// Handle immediate adjacent tiles (including diagonals)
		for (int[] direction : directions) {
			addValidTileInDirection(board, unit, direction[0], direction[1], validTiles, currentPlayer);
		}

		// Handle two tiles away horizontally or vertically, making sure not to pass through enemy units
		for (int[] direction : extendedDirections) {
			addValidTileInDirection(board, unit, direction[0], direction[1], validTiles, currentPlayer);
		}

		return validTiles;
	}
    private void highlightAdjacentAttackTiles(Tile tile, Unit unit) {
		Board board = gameState.getBoard();
		int x = tile.getTilex();
		int y = tile.getTiley();

		int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
		for (int[] direction : directions) {
			int newX = x + direction[0];
			int newY = y + direction[1];
			if (isValidTile(newX, newY) && board.getTile(newX, newY).isOccupied() && board.getTile(newX, newY).getUnit().getOwner() != unit.getOwner()) {
				updateTileHighlight(board.getTile(newX, newY), 2); // Highlight tile for attack
			}
		}
	}
    public void performHighlightAdjacentAttackTiles(Tile tile, Unit unit) {
        highlightAdjacentAttackTiles(tile, unit);
    }
    
    // Returns the set of valid tiles for summoning units
	public Set<Tile> getValidSummonTiles() {
		Player player = gameState.getCurrentPlayer();
		Set<Tile> validTiles = new HashSet<>();
		Tile[][] tiles = gameState.getBoard().getTiles();
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				Tile currentTile = tiles[i][j];
				if (isAdjacentToAlly(i, j, player) && !currentTile.isOccupied()) {
					validTiles.add(currentTile);
				}
			}
		}
		return validTiles;
	}
    // highlight tiles for summoning units
	public void highlightSummonRange() {
		Set<Tile> validTiles = getValidSummonTiles();
		validTiles.forEach(tile -> updateTileHighlight(tile, 1));
	}
    // Check for spell range 
	public Set<Tile> getSpellRange(Card card) {
	    Set<Tile> validTiles = new HashSet<>();
	    Tile[][] tiles = gameState.getBoard().getTiles();

	    if (card.getCardname().equals("Dark Terminus")) {
	        for (int i = 0; i < tiles.length; i++) {
	            for (int j = 0; j < tiles[i].length; j++) {
	                Tile currentTile = tiles[i][j];
	                Unit unit = currentTile.getUnit();
	                
	                // Check if the tile is adjacent to a friendly unit and not occupied
	                if (unit != null && !(unit.getOwner() instanceof HumanPlayer) && !unit.getName().equals("AI Avatar")) {
	                    validTiles.add(currentTile);
	                }
	            }
	        }
	    }
	    
	    if (card.getCardname().equals("Wraithling Swarm")) {
	    		return getValidSummonTiles();
	    }
	    
	    if (card.getCardname().equals("Beamshock")) {
	    	List<Unit> humanUnits = gameState.getHuman().getUnits();
			Unit u = humanUnits.stream().max(Comparator.comparingInt(Unit::getAttack)).orElse(null);
			validTiles.add(u.getActiveTile(gameState.getBoard()));
        }
	    if (card.getCardname().equals("Truestrike")) {
    		validTiles.add(gameState.getHuman().getAvatar().getActiveTile(gameState.getBoard()));
        }
	    if (card.getCardname().equals("Sundrop Elixir")) {
	    	//could potentially change for different units as well
    		validTiles.add(gameState.getAi().getAvatar().getActiveTile(gameState.getBoard()));
        }
	    
	    return validTiles;
	}
    // Method to actually move the unit to the new tile
	public void updateUnitPositionAndMove(Unit unit, Tile newTile) {
		if (newTile.getHighlightMode() != 1 && gameState.getCurrentPlayer() instanceof HumanPlayer) {
			System.out.println("New tile is not highlighted for movement");
			removeHighlightFromAll();
			return;
		}

		Board board = gameState.getBoard();

		// get current tile
		Tile currentTile = unit.getActiveTile(board);

		// update unit position
		currentTile.removeUnit();
		newTile.setUnit(unit);
		unit.setPositionByTile(newTile);

		// remove highlight from all tiles
		removeHighlightFromAll();

		try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}

		// Move unit to tile according to the result of yFirst
		BasicCommands.moveUnitToTile(out, unit, newTile, yFirst(currentTile, newTile, unit));

		if (unit.getName().equals("Young Flamewing")) {
			BasicCommands.addPlayer1Notification(out, "Flamewing is flying!", 3);
		}

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		unit.setHasMoved(true);
	}

    // Method to determine whether to move in the x or y direction first
	private boolean yFirst(Tile currentTile, Tile newTile, Unit unit) {
		Board board = gameState.getBoard();
		int dx = newTile.getTilex() - currentTile.getTilex();
		int dy = newTile.getTiley() - currentTile.getTiley();

		if (Math.abs(dx) == 1 && Math.abs(dy) == 1) {
			// Check for enemies along the path when moving x then y and y then x
			Tile xFirstTile = board.getTile(currentTile.getTilex() + dx, currentTile.getTiley());
			Tile yFirstTile = board.getTile(currentTile.getTilex(), currentTile.getTiley() + dy);

			boolean xFirstBlocked = xFirstTile.isOccupied() && xFirstTile.getUnit().getOwner() != unit.getOwner();
			boolean yFirstBlocked = yFirstTile.isOccupied() && yFirstTile.getUnit().getOwner() != unit.getOwner();

			// If the path is blocked in the x direction, try y first, and vice versa.
			if (xFirstBlocked && !yFirstBlocked) {
				return true; // Prioritize y if x is blocked.
			} else if (!xFirstBlocked && yFirstBlocked) {
				return false; // Prioritize x if y is blocked.
			}
		}
		return false; // Default to false
	}
}
// initializeBoard
// removeHighlightFromAll
// updateTileHighlight
// highlightAdjacentAttackTiles
// isValidTile
// isEnemyOnTile
// getValidMoves
// getValidSummonTiles
// highlightSummonRange
// getSpellRange
// updateUnitPositionAndMove
// yFirst
