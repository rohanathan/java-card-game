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
 * The board consists of a 9x5 grid (width x height), where each tile is generated 
 * using the BasicObjectBuilders utility. It sets each tile's default highlight mode to 0 
 * (unhighlighted) and visually updates the UI.
 *
 * After initialization, the board is ready for unit placement and interaction.
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
    /**
     * Checks if a given tile contains an enemy unit.
     *
     * @param targetTile The tile being examined.
     * @param player The current player whose perspective is considered.
     * @return True if an enemy unit occupies the tile, false otherwise.
     */    
    private boolean hasEnemyUnit(Tile targetTile, Player player) {
        return targetTile.isOccupied() && targetTile.getUnit().getOwner() != player;
    }

    /**
     * Verifies whether a tile position is within the valid boundaries of the game board.
     *
     * @param x The x-coordinate of the tile.
     * @param y The y-coordinate of the tile.
     * @return True if the tile is within the board's dimensions, false otherwise.
     */
		private boolean isValidTile(int x, int y) {
			Tile[][] gridTiles = gameState.getBoard().getTiles();
			return (x >= 0 && y >= 0 && x < gridTiles.length && y < gridTiles[x].length);
		}

    /**
     * Identifies and adds valid movement tiles for a unit, considering obstacles and enemy units.
     *
     * @param gameGrid The 2D board grid.
     * @param movingUnit The unit attempting to move.
     * @param deltaX Movement along the x-axis.
     * @param deltaY Movement along the y-axis.
     * @param potentialMoves Set to store valid movement tiles.
     * @param activePlayer The player controlling the unit.
     */
	private void detectValidMoveTiles(Tile[][] gameGrid, Unit movingUnit, int deltaX, int deltaY,
										Set<Tile> potentialMoves, Player activePlayer) {
		int originX = movingUnit.getPosition().getTilex();
		int originY = movingUnit.getPosition().getTiley();
		int targetX = originX + deltaX;
		int targetY = originY + deltaY;

		int movementRange = Math.max(Math.abs(deltaX), Math.abs(deltaY));

		// Check step-by-step movement towards the target tile
		for (int step = 1; step <= movementRange; step++) {
			int stepX = originX + (int) Math.signum(deltaX) * step;
			int stepY = originY + (int) Math.signum(deltaY) * step;

			// Additional verification for diagonal moves
			if (Math.abs(deltaX) == 1 && Math.abs(deltaY) == 1 && step == 1) {
				if (isWithinBoardLimits(originX + deltaX, originY) && isWithinBoardLimits(originX, originY + deltaY)) {
					Tile firstTile = gameGrid[originX + deltaX][originY];
					Tile secondTile = gameGrid[originX][originY + deltaY];

					// Ensure diagonal moves are not blocked by enemy units
					if (hasEnemyUnit(firstTile, activePlayer) && hasEnemyUnit(secondTile, activePlayer)) {
					return;
					}
				} else {
					return;
			}
		}

		// If any tile along the path is invalid or blocked, stop checking
		if (!isWithinBoardLimits(stepX, stepY) || hasEnemyUnit(gameGrid[stepX][stepY], activePlayer)) {
			return;
		}
	}


		// If movement is valid, add the final tile to the set of potential moves
		if (isWithinBoardLimits(targetX, targetY)) {
			Tile destinationTile = gameGrid[targetX][targetY];
		if (!destinationTile.isOccupied()) {
			potentialMoves.add(destinationTile);
			}
		}
	}
    /**
     * Verifies whether a tile position is within the valid boundaries of the game board.
     *
     * @param x The x-coordinate of the tile.
     * @param y The y-coordinate of the tile.
     * @return True if the tile is within the board's dimensions, false otherwise.
     */
    private boolean isWithinBoardLimits(int x, int y) {
        Tile[][] gridTiles = gameState.getBoard().getTiles();
        return (x >= 0 && y >= 0 && x < gridTiles.length && y < gridTiles[x].length);
    }

    /**
     * Determines whether a specified tile is adjacent to a friendly unit.
     *
     * @param posX The x-coordinate of the tile.
     * @param posY The y-coordinate of the tile.
     * @param activePlayer The player checking for adjacent allies.
     * @return True if an adjacent tile contains a friendly unit, false otherwise.
     */
	private boolean hasFriendlyAdjacent(int posX, int posY, Player activePlayer) {
        Tile[][] boardTiles = gameState.getBoard().getTiles();
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue; // Skip the current tile
                int adjX = posX + dx;
                int adjY = posY + dy;
                if (isWithinBoardLimits(adjX, adjY)) {
                    Tile adjacentTile = boardTiles[adjX][adjY];
                    if (adjacentTile.isOccupied() && adjacentTile.getUnit().getOwner() == activePlayer) {
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
		if (gameState.getCombatHandler().isUnitProvoked(unit) || unit.hasMoved() || unit.hasAttacked()) {
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
			detectValidMoveTiles(board, unit, direction[0], direction[1], validTiles, currentPlayer);
		}

		// Handle two tiles away horizontally or vertically, making sure not to pass through enemy units
		for (int[] direction : extendedDirections) {
			detectValidMoveTiles(board, unit, direction[0], direction[1], validTiles, currentPlayer);
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
            	if (hasFriendlyAdjacent(potentialTile.getTilex(), potentialTile.getTiley(), currentPlayer) 
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
 * Uses the summoning rules to determine eligible tiles and updates their 
 * visual appearance to indicate possible summoning locations.
 */
	public void highlightSummonRange() {
		Set<Tile> validTiles = getValidSummonTiles();
		validTiles.forEach(tile -> updateTileHighlight(tile, 1));
	}

/**
 * Determines the valid tiles where a given spell can be cast.
 *
 * Different spells have unique targeting rules. This method 
 * evaluates the board state and identifies all legal spell targets.
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
// hasEnemyUnit
// getValidMoves
// getValidSummonTiles
// highlightSummonRange
// getSpellRange
// updateUnitPositionAndMove
// yFirst
