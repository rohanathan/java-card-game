package structures.manager;


import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Board;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import structures.basic.player.HumanPlayer;
import structures.basic.Position;
import structures.basic.player.Player;
import utils.BasicObjectBuilders;
import structures.basic.cards.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * Handles combat-related interactions between units within the game.
 * 
 * Responsible for managing unit attacks, counterattacks, determining valid attack and movement tiles,
 * handling special combat situations (e.g., provoke), and ensuring units perform correct animations 
 * during combat events.
 * 
 * This class interacts closely with the GameState, BoardManager, and UnitManager to enforce game rules
 * and maintain accurate representation of the combat state within the UI.
 */
public class CombatHandler {
    

    private final ActorRef out;
    private final GameState gameState;
    
    public CombatHandler(ActorRef out, GameState gameState) 
    {
        this.out = out;
        // Store reference to GameState
        this.gameState = gameState; 

    }
	/**
 	* Performs an attack from one unit to another, handling all relevant
 	* animations, UI updates, health modifications, and special conditions such
 	* as summoning Wraithlings for the human avatar.
 	*
 	* @param attacker the unit performing the attack
 	* @param defender the unit receiving the attack
 	*/
	 public void performAttack(Unit attacker, Unit defender) {
		if (attacker.hasAttacked()) {
			return;
		}
	
		// Highlight target tile and initiate animations
		Tile defenderTile = defender.getActiveTile(gameState.getBoard());
		gameState.getBoardManager().updateTileHighlight(defenderTile, 2);
		
		BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.attack);
		sleepSafely(1500);
	
		BasicCommands.playUnitAnimation(out, defender, UnitAnimationType.hit);
		sleepSafely(750);
	
		BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.idle);
		BasicCommands.playUnitAnimation(out, defender, UnitAnimationType.idle);
	
		// Adjust health of the defending unit
		int adjustedHealth = defender.getHealth() - attacker.getAttack();
		gameState.getUnitManager().modifyUnitHealth(defender, adjustedHealth);
	
		// Special condition: Human avatar summons Wraithling on successful attack
		if (attacker.equals(gameState.getHuman().getAvatar()) && 
			gameState.getHuman().getRobustness() > 0 && attacker.getAttack() > 0) {
			Wraithling.summonAvatarWraithling(out, gameState);
		}
	
		// Clear tile highlights
		gameState.getBoardManager().removeHighlightFromAll();
	
		// Allow the defender to counterattack if applicable
		if (attacker.getOwner() == gameState.getCurrentPlayer()) {
			counterAttack(attacker, defender);
		}
	
		// Mark attacker as having moved and attacked
		attacker.setHasAttacked(true);
		attacker.setHasMoved(true);
	}
	
	/**
	 * Sleeps the current thread for a specified duration without requiring 
	 * explicit try-catch blocks.
	 *
	 * @param millis duration in milliseconds to sleep
	 */
	private void sleepSafely(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			System.err.println("Animation sleep interrupted: " + e.getMessage());
		}
	}
	
       /**
 	* Moves the attacking unit closer to the target unit and initiates an attack if adjacent.
 	* 
 	* The method performs the following actions:
 	* 
 	*   Determines all tiles available for the attacker to move.
 	*   Selects the closest available unoccupied tile adjacent to the target unit.
 	*   Moves the attacker to the selected tile, updating the UI accordingly.
 	*   If the attacker successfully reaches an adjacent position, it initiates an attack on the target unit.
 	* 
 	*
 	* @param attacker the unit initiating the move-and-attack action.
 	* @param target the defending unit being attacked.
 	*/
	public void moveAndAttack(Unit attacker, Unit target) {
		// Get all tiles from the board
		Tile[][] boardTiles = gameState.getBoard().getTiles();
		
		// Obtain valid movement options for the attacker
		Set<Tile> availableTiles = gameState.getBoardManager().getValidMoves(boardTiles, attacker);
		
		// Retrieve target's tile position
		Tile targetTile = target.getActiveTile(gameState.getBoard());
	
		// Track the best tile to move to and its distance
		Tile optimalTile = null;
		double shortestDistance = Double.MAX_VALUE;
	
		// Iterate over potential movement tiles to find closest position
		for (Tile tile : availableTiles) {
			if (!tile.isOccupied()) {
				double currentDistance = calculateDistance(tile, targetTile);
				if (currentDistance < shortestDistance) {
					optimalTile = tile;
					shortestDistance = currentDistance;
				}
			}
		}
	
		// Move attacker to optimal tile, if found
		if (optimalTile != null) {
			gameState.getBoardManager().updateUnitPositionAndMove(attacker, optimalTile);
			pauseForMovementAnimation();
		}
	
		// Attack the target if attacker is now adjacent
		if (isWithinAttackRange(attacker.getActiveTile(gameState.getBoard()), targetTile)) {
			System.out.println("Attacker has successfully moved adjacent. Initiating attack!");
			performAttack(attacker, target);
		} else {
			System.out.println("Attack aborted. Target is out of attack range.");
		}
	}
	/**
 	* Calculates the Euclidean distance between two tiles.
 	*
	* @param sourceTile the first tile.
 	* @param destinationTile the second tile.
 	* @return the distance between the two tiles.
 	*/ 
	private double calculateDistance(Tile sourceTile, Tile destinationTile) {
		int xDiff = sourceTile.getTilex() - destinationTile.getTilex();
		int yDiff = sourceTile.getTiley() - destinationTile.getTiley();
		return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
	}
	
	/**
 	* Introduces a delay to synchronize animations with unit movements.
 	*/
	private void pauseForMovementAnimation() {
		try {
			Thread.sleep(1000); // Pause to allow movement animations to finish
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			System.err.println("Movement animation interrupted: " + e.getMessage());
		}
	}

	/**
	* Initiates a counterattack from the defending unit onto the original attacker.
	* 
	* A counterattack is executed if the defending unit survives the initial attack (health > 0).
	* After the counterattack, the defending unit's attack and movement statuses are reset
	* to allow future actions in subsequent turns.
	*
	* @param attacker The unit that initiated the original attack.
	* @param defender The unit performing the counterattack.
	*/
    // Counter attack an enemy unit and play the attack animation
	public void counterAttack(Unit attacker, Unit defender) {
		if (defender.getHealth() <= 0) {
			System.out.println("Defender cannot counterattack; it has been defeated.");
			return;
		}
	
		System.out.println(defender.getName() + " is initiating a counterattack on " + attacker.getName());
		performAttack(defender, attacker);
	
		// Reset defender's state to allow future actions
		defender.setHasAttacked(false);
		defender.setHasMoved(false);
	}

    // Highlight tiles for attacking only
	public void highlightPotentialAttacks(Unit unit) {
		Set<Tile> validAttackTiles = findAttackTargets(unit);

		// Highlight valid attack tiles
		validAttackTiles.forEach(tile -> gameState.getBoardManager().updateTileHighlight(tile, 2)); // 2 for attack highlight mode
	}

	/**
 	* Retrieves all valid tiles that the specified unit can target for an attack in the current turn.
 	*
	 * This method first ensures the unit has not already attacked during the current turn.
 	* If the unit is eligible to attack, it collects all potential enemy tiles within range.
 	*
 	* @param attacker The unit for which to determine potential attack targets.
 	* @return A set of tiles representing all valid attack targets for this unit.
	 */
	public Set<Tile> findAttackTargets(Unit attacker) {
		Set<Tile> attackableTiles = new HashSet<>();
	
		// Only calculate if the unit hasn't attacked yet
		if (!attacker.hasAttacked()) {
			Player opposingPlayer = gameState.getInactivePlayer();
			attackableTiles.addAll(getValidTargets(attacker, opposingPlayer));
		}
	
		return attackableTiles;
	}
	
	/**
 	* Determines and returns a set of tiles that contain enemy units which the given unit can attack.
 	*
 	* If the unit is provoked by adjacent enemy units with a provoke ability, this method returns 
 	* only the provoking units' tiles. Otherwise, it identifies all enemy units within the unit's standard
 	* attack range.
 	*
 	* @param attacker The unit for which valid attack targets need to be determined.
 	* @param opponent The opposing player whose units are potential attack targets.
 	* @return A set of Tiles that the attacker can target this turn.
 	*/	
	public Set<Tile> getValidTargets(Unit attacker, Player opponent) {
		Set<Tile> attackableTiles = new HashSet<>();
		// Check for provokers around the attacker first

		Set<Position> provokerPositions = identifyProvokingUnits(attacker.getActiveTile(gameState.getBoard()));
		Tile attackerTile = attacker.getActiveTile(gameState.getBoard());

		// If provokers exist, attacker must prioritize them
		if (!provokerPositions.isEmpty()) {
			for (Position position : provokerPositions) {
				System.out.println(position + "provoker position");
				Tile provokingTile = gameState.getBoard().getTile(position.getTilex(), position.getTiley());
				attackableTiles.add(provokingTile);
			}
			return attackableTiles;
		}
    	// Otherwise, find all opponent units in regular attack range
    	opponent.getUnits().stream()
            	.map(unit -> unit.getActiveTile(gameState.getBoard()))
            	.filter(opponentTile -> isWithinAttackRange(attackerTile, opponentTile))
            	.forEach(attackableTiles::add);

   	 	return attackableTiles;
	}

	// Boolean method to check if a unit is within attack range of another unit
	public boolean isWithinAttackRange(Tile unitTile, Tile targetTile) {
		int dx = Math.abs(unitTile.getTilex() - targetTile.getTilex());
		int dy = Math.abs(unitTile.getTiley() - targetTile.getTiley());
		return dx < 2 && dy < 2;
	}
	/**
 	* Identifies and returns positions of enemy units with the "Provoke" ability
 	* surrounding a given tile. Provoking units force adjacent enemy units to prioritize
 	* attacking them.
 	*
 	* @param targetTile The tile to check around for provoking enemy units.
 	* @return A Set of Positions where enemy units with provoke ability are located.
 	*/
	public Set<Position> identifyProvokingUnits(Tile targetTile) {
	    Set<Position> provokingPositions = new HashSet<>();

    	int targetX = targetTile.getTilex();
    	int targetY = targetTile.getTiley();

    	for (Unit enemyUnit : gameState.getInactivePlayer().getUnits()) {
        	int enemyX = enemyUnit.getPosition().getTilex();
        	int enemyY = enemyUnit.getPosition().getTiley();

        	boolean isAdjacent = Math.abs(targetX - enemyX) <= 1 && Math.abs(targetY - enemyY) <= 1;
       	 	boolean hasProvokeAbility = isUnitProvoking(enemyUnit);

        	if (isAdjacent && hasProvokeAbility) {
            	System.out.println("Provoking unit found: " + enemyUnit.getName());
            	provokingPositions.add(enemyUnit.getPosition());
       		}
    	}
    return provokingPositions;
	}
	
	/**	
 	* Checks if a given unit is currently provoked by adjacent enemy units.
 	* A unit is considered provoked if at least one adjacent enemy unit possesses
 	* the provoke ability.
 	*
	* @param unit The unit to check for provocation status.
 	* @return True if the unit is provoked by an adjacent enemy unit; false otherwise.
 	*/	
	 public boolean isUnitProvoked(Unit unit) {
		Player opponent = (gameState.getCurrentPlayer() == gameState.getHuman()) 
							? gameState.getAi() 
							: gameState.getHuman();
	
		int unitX = unit.getPosition().getTilex();
		int unitY = unit.getPosition().getTiley();
	
		for (Unit enemyUnit : opponent.getUnits()) {
			int enemyX = enemyUnit.getPosition().getTilex();
			int enemyY = enemyUnit.getPosition().getTiley();
	
			boolean isAdjacent = Math.abs(unitX - enemyX) <= 1 && Math.abs(unitY - enemyY) <= 1;
			boolean hasProvokeAbility = isUnitProvoking(enemyUnit);
	
			if (isAdjacent && hasProvokeAbility) {
				BasicCommands.addPlayer1Notification(out, unit.getName() + " has been provoked by " + enemyUnit.getName(), 2);
				return true;
			}
		}
		return false;
	}
	/**
 	* Determines if a given unit possesses the provoke ability based on its name.
 	*
 	* @param unit The unit to be checked.
 	* @return True if the unit has the provoke ability; false otherwise.
 	*/
	private boolean isUnitProvoking(Unit unit) {
    	String name = unit.getName();
    	return name.equals("Rock Pulveriser") || name.equals("Swamp Entangler") ||
           	   name.equals("Silverguard Knight") || name.equals("Ironcliff Guardian");
	}

	/**
 	* Highlights all tiles where a given unit can move or attack, 
 	* updating the game board accordingly.
 	*
 	* Movement tiles are highlighted first, and then adjacent enemy units within attack range
 	* are marked distinctly. This method visually guides the player to understand possible moves
 	* and attacks clearly.
 	*
 	* @param unit The unit for which valid moves and attacks are highlighted.
 	*/	
	public void highlightValidMoves(Unit unit) {
	    Tile[][] tiles = gameState.getBoard().getTiles();
	    // Identify possible movement and attack tiles

	    Set<Tile> validMoveTiles = gameState.getBoardManager().getValidMoves(tiles, unit);
	    Set<Tile> validAttackTiles = gameState.getCombatHandler().findAttackTargets(unit);

	    // Highlight valid movement and attack tiles
	    if (validMoveTiles != null) {
	        for (Tile tile : validMoveTiles) {
	            if (!tile.isOccupied()) {
	                // Highlight tile for movement
	                gameState.getBoardManager().updateTileHighlight(tile, 1);
					// Highlight additional attack tiles if adjacent to valid movement tiles
					gameState.getBoardManager().performHighlightAdjacentAttackTiles(tile, unit);
	            }
	        }
	    }

	    // Highlight valid attack tiles
	    for (Tile tile : validAttackTiles) {
	        if (tile.isOccupied() && tile.getUnit().getOwner() != unit.getOwner()) {
	            // Highlight tile for attack
	            gameState.getBoardManager().updateTileHighlight(tile, 2);
	        }
	    }
	}    
}
