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

/**
 * The BoardManager class is responsible for handling all board-related operations
 * such as initializing the board, highlighting valid movements, updating tile highlights, 
 * and managing unit positions.
 *
 * It interacts with the GameState and is responsible for ensuring 
 * valid board interactions, including movement validation and spell casting.
 *
 * Responsibilities:
 * - Initialize the game board.
 * - Manage tile highlights for movement, attacks, and summoning.
 * - Validate tile accessibility and adjacent tile conditions.
 * - Handle unit movements and positioning.
 * 
 * This class ensures that the board state is updated dynamically based on the game rules.
 * 
 * @author Hemansh Solanki
 */
public class BoardManager {
    private final ActorRef out;
    private final GameState gameState;
    
    public BoardManager(ActorRef out, GameState gameState) 
    {
        this.out = out;
        // Store reference to GameState
        this.gameState = gameState; 

    }
 /**
 * Initializes the game board by creating a grid of tiles and rendering them on the UI.
 * This method ensures that each tile is assigned a default highlight mode and is 
 * properly displayed using the BasicCommands API.
 *
 * <p>The board consists of a 9x5 grid (width x height), where each tile is generated 
 * using the BasicObjectBuilders utility. It sets each tile's default highlight mode to 0 
 * (unhighlighted) and visually updates the UI.</p>
 *
 * <p>After initialization, the board is ready for unit placement and interaction.</p>
 *
 * @return A fully initialized Board object with all tiles set up.
 */
public Board initializeBoard() {
    Board board = new Board();
    for (int x = 0; x < 9; x++) {
        for (int y = 0; y < 5; y++) {
            Tile tile = BasicObjectBuilders.loadTile(x, y);
            tile.setHighlightMode(0); // Default highlight mode: No highlight
            board.setTile(tile, x, y);
            
            // Render tile on UI
            BasicCommands.drawTile(out, tile, 0);
            
            // Prevent rapid execution by introducing a slight delay
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt status
                System.err.println("Board initialization interrupted: " + e.getMessage());
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

/**
 * Removes all highlight effects from tiles on the board.
 *
 * his method iterates through the entire game board (9x5 grid)
 * and resets the highlight mode of every tile to 0 (unhighlighted).
 * It ensures that previously highlighted tiles, whether for movement,
 * attack, or spell casting, are cleared visually.
 *
 * Additionally, if the human player has cards in hand, it triggers
 * the `notClickingCard()` method from `PlayerManager` to reset the 
 * UI state for card selection.
 */
	public void removeHighlightFromAll() {
		Board gameBoard = gameState.getBoard();
		Tile[][] tileGrid = gameBoard.getTiles();
	
		// Iterate over each tile and reset highlight mode
		for (Tile[] row : tileGrid) {
			for (Tile tile : row) {
				if (tile.getHighlightMode() != 0) {
					tile.setHighlightMode(0); // Reset highlight
					BasicCommands.drawTile(out, tile, 0); // Update UI
				}
			}
		}
	
		// Reset UI selection if human player has cards in hand
		if (!gameState.getHuman().getHand().getCards().isEmpty()) {
			gameState.getPlayerManager().notClickingCard();
		}
	}

/**
 * Updates the highlight mode of a specified tile and refreshes the UI.
 *
 * This method applies a new highlight effect to a given tile,
 * waits for a short delay (to ensure proper animation timing),
 * and then visually updates the tile on the board.
 *
 * @param targetTile The tile to be highlighted.
 * @param highlightMode The highlight mode (e.g., 0 for none, 1 for movement, 2 for attack).
 */
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
	/**
 	* Highlights adjacent tiles that contain enemy units to indicate possible attack targets.
 	*
 	* @param centerTile The tile where the unit is currently located.
 	* @param activeUnit The unit that is initiating an attack.
 	*/
	private void highlightAdjacentAttackTiles(Tile centerTile, Unit activeUnit) {
		Board gameBoard = gameState.getBoard();
		int tileX = centerTile.getTilex();
		int tileY = centerTile.getTiley();
	
		int[][] attackDirections = {
			{-1, 0}, {1, 0}, {0, -1}, {0, 1}, 
			{-1, -1}, {1, -1}, {-1, 1}, {1, 1} // Includes diagonal attack positions
		};
	
		for (int[] direction : attackDirections) {
			int adjacentX = tileX + direction[0];
			int adjacentY = tileY + direction[1];
	
			if (isValidTile(adjacentX, adjacentY)) {
				Tile adjacentTile = gameBoard.getTile(adjacentX, adjacentY);
	
				// Check if tile is occupied by an enemy unit
				if (adjacentTile.isOccupied() && adjacentTile.getUnit().getOwner() != activeUnit.getOwner()) {
					updateTileHighlight(adjacentTile, 2); // Apply attack highlight
				}
			}
		}
	}
	
    public void performHighlightAdjacentAttackTiles(Tile tile, Unit unit) {
        highlightAdjacentAttackTiles(tile, unit);
    }
    
/**
 * Identifies valid tiles for summoning new units based on adjacency rules.
 *
 * Checks all tiles on the board and selects those that are unoccupied
 * and adjacent to an allied unit.
 *
 * @return A set of valid tiles where the player can summon units.
 */
	public Set<Tile> getValidSummonTiles() {
    	Player currentPlayer = gameState.getCurrentPlayer();
    	Set<Tile> summonableTiles = new HashSet<>();
    	Tile[][] boardTiles = gameState.getBoard().getTiles();

    	// Iterate through each tile and check if it's adjacent to an ally
    	for (Tile[] row : boardTiles) {
        	for (Tile potentialTile : row) {
            	if (isAdjacentToAlly(potentialTile.getTilex(), potentialTile.getTiley(), currentPlayer) 
                	&& !potentialTile.isOccupied()) {
                	summonableTiles.add(potentialTile);
            	}
        	}
	    }  	
    return summonableTiles;
}
/**
 * Highlights all valid tiles where the current player can summon a unit.
 *
 * <p>Uses the summoning rules to determine eligible tiles and updates their 
 * visual appearance to indicate possible summoning locations.</p>
 */
	public void highlightSummonRange() {
		Set<Tile> validTiles = getValidSummonTiles();
		validTiles.forEach(tile -> updateTileHighlight(tile, 1));
	}

/**
 * Determines the valid tiles where a given spell can be cast.
 *
 * <p>Different spells have unique targeting rules. This method 
 * evaluates the board state and identifies all legal spell targets.</p>
 *
 * @param card The spell card to check for valid casting locations.
 * @return A set of tiles where the spell can be legally cast.
 */	
public Set<Tile> getSpellRange(Card card) {
    Set<Tile> spellTargetTiles = new HashSet<>();
    Tile[][] boardTiles = gameState.getBoard().getTiles();

    switch (card.getCardname()) {
        case "Dark Terminus":
            for (Tile[] row : boardTiles) {
                for (Tile potentialTile : row) {
                    Unit occupyingUnit = potentialTile.getUnit();

                    // Check if the tile has an AI-controlled unit (excluding AI Avatar)
                    if (occupyingUnit != null && !(occupyingUnit.getOwner() instanceof HumanPlayer) 
                        && !occupyingUnit.getName().equals("AI Avatar")) {
                        spellTargetTiles.add(potentialTile);
                    }
                }
            }
            break;

        case "Wraithling Swarm":
            return getValidSummonTiles();

        case "Beamshock":
            List<Unit> humanUnits = gameState.getHuman().getUnits();
            Unit strongestUnit = humanUnits.stream()
                    .max(Comparator.comparingInt(Unit::getAttack))
                    .orElse(null);
            if (strongestUnit != null) {
                spellTargetTiles.add(strongestUnit.getActiveTile(gameState.getBoard()));
            }
            break;

        case "Truestrike":
            spellTargetTiles.add(gameState.getHuman().getAvatar().getActiveTile(gameState.getBoard()));
            break;

        case "Sundrop Elixir":
            spellTargetTiles.add(gameState.getAi().getAvatar().getActiveTile(gameState.getBoard()));
            break;
    }

    return spellTargetTiles;
}

/**
 * Moves a unit to the specified tile, updating its position on the board.
 *
 * @param unit The unit that is being moved.
 * @param newTile The target tile where the unit will be moved.
 */
	public void updateUnitPositionAndMove(Unit unit, Tile newTile) {
    	if (newTile.getHighlightMode() != 1 && gameState.getCurrentPlayer() instanceof HumanPlayer) {
        	System.out.println("Invalid move: Tile is not highlighted for movement.");
        	removeHighlightFromAll();
        	return;
    	}

    	Board gameBoard = gameState.getBoard();
    	Tile previousTile = unit.getActiveTile(gameBoard);

    	// Remove unit from previous tile and update new position
    	previousTile.removeUnit();
    	newTile.setUnit(unit);
    	unit.setPositionByTile(newTile);

    	// Clear highlights
    	removeHighlightFromAll();

    	try {
        	Thread.sleep(30);
    	} catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
    	}

    	// Move unit visually on UI
    	BasicCommands.moveUnitToTile(out, unit, newTile, determineMovementPath(previousTile, newTile, unit));

    	if ("Young Flamewing".equals(unit.getName())) {
        	BasicCommands.addPlayer1Notification(out, "Flamewing is flying!", 3);
    	}

    	try {
        	Thread.sleep(2000);
    	} catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
    	}

    	unit.setHasMoved(true);
	}

/**
 * Determines whether the unit should move along the y-axis before the x-axis.
 *
 * @param startTile The tile where the unit is currently located.
 * @param destinationTile The tile where the unit is moving.
 * @param unit The unit that is moving.
 * @return {@code true} if the unit should move in the y-direction first, otherwise {@code false}.
 */
	private boolean determineMovementPath(Tile startTile, Tile destinationTile, Unit unit) {
    	Board gameBoard = gameState.getBoard();
    	int deltaX = destinationTile.getTilex() - startTile.getTilex();
    	int deltaY = destinationTile.getTiley() - startTile.getTiley();

    	if (Math.abs(deltaX) == 1 && Math.abs(deltaY) == 1) {
        	// Identify tiles in both movement directions
        	Tile xPathTile = gameBoard.getTile(startTile.getTilex() + deltaX, startTile.getTiley());
        	Tile yPathTile = gameBoard.getTile(startTile.getTilex(), startTile.getTiley() + deltaY);

        	boolean xBlocked = xPathTile.isOccupied() && xPathTile.getUnit().getOwner() != unit.getOwner();
        	boolean yBlocked = yPathTile.isOccupied() && yPathTile.getUnit().getOwner() != unit.getOwner();

        	// Prioritize movement based on blocked paths
        	if (xBlocked && !yBlocked) {
            	return true; // Prioritize y-direction
        	} else if (!xBlocked && yBlocked) {
            	return false; // Prioritize x-direction
        	}
    	}
    	return false; // Default behavior
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
