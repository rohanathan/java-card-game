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

public class CombatHandler {
    

     private final ActorRef out;
    private final GameState gameState;
    
    public CombatHandler(ActorRef out, GameState gameState) 
    {
        this.out = out;
        // Store reference to GameState
        this.gameState = gameState; 

    }
    // Attack an enemy unit and play the attack animation
	public void attack(Unit attacker, Unit attacked) {
		if (!attacker.hasAttacked()) {
			// remove highlight from all tiles
			gameState.getBoardManager().updateTileHighlight(attacked.getActiveTile(gameState.getBoard()), 2);
			BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.attack);
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			BasicCommands.playUnitAnimation(out, attacked, UnitAnimationType.hit);
			try {
				Thread.sleep(750);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.idle);
			BasicCommands.playUnitAnimation(out, attacked, UnitAnimationType.idle);

			// update health
			gameState.getUnitManager().modifyUnitHealth(attacked, attacked.getHealth() - attacker.getAttack());
			if (attacker.getAttack() >= 0 && attacker.equals(gameState.getHuman().getAvatar()) 
					&& gameState.getHuman().getRobustness() > 0) {
 
					Wraithling.summonAvatarWraithling(out, gameState);
				}
                gameState.getBoardManager().removeHighlightFromAll();

				
			}

			// Only counter attack if the attacker is the current player
			// To avoid infinitely recursive counter attacking
			if (attacker.getOwner() == gameState.getCurrentPlayer()) {
				counterAttack(attacker, attacked);
			}

			attacker.setHasAttacked(true);
			attacker.setHasMoved(true);
		}
       
	// Move to the closest adjacent unit to defender, and call adjacent attack
	public void moveAndAttack(Unit attacker, Unit attacked) {
		// Retrieve the tiles of the board and the valid movement tiles for the attacker
		Tile[][] tiles = gameState.getBoard().getTiles();
		Set<Tile> validMoveTiles = gameState.getBoardManager().getValidMoves(tiles, attacker);
		Tile defenderTile = attacked.getActiveTile(gameState.getBoard());

		// Initialize variables to keep track of the closest tile and its distance
		Tile closestTile = null;
		double closestDistance = Double.MAX_VALUE;

		// Iterate over each valid movement tile
		for (Tile tile : validMoveTiles) {
			// Ensure the tile is not occupied
			if (!tile.isOccupied()) {
				// Calculate the distance to the defender's tile
				double distance = Math.sqrt(Math.pow(tile.getTilex() - defenderTile.getTilex(), 2) + Math.pow(tile.getTiley() - defenderTile.getTiley(), 2));
				// If this tile is closer than any previously examined tile, update closestTile and closestDistance
				if (distance < closestDistance) {
					closestTile = tile;
					closestDistance = distance;
				}
			}
		}

		// If a closest tile has been found, move the attacker to this tile
		if (closestTile != null) {
			gameState.getBoardManager().updateUnitPositionAndMove(attacker, closestTile);
			// Ensure a small delay to let the move action complete before attacking
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// After moving, perform the attack if the attacker is now adjacent to the defender
		if (isAttackable(attacker.getActiveTile(gameState.getBoard()), defenderTile)) {
			System.out.println("Combat initiated! Attacker strikes from an adjacent position!");
			attack(attacker, attacked);
		} else {
			System.out.println("Attack failed—target is out of range!");
		}
	} 
	
    // Counter attack an enemy unit and play the attack animation
	public void counterAttack(Unit originalAttacker, Unit counterAttacker) {
		if (counterAttacker.getHealth() > 0) {
			System.out.println("Counter attacking");
			attack(counterAttacker, originalAttacker);
			counterAttacker.setHasAttacked(false);
			counterAttacker.setHasMoved(false);
		}
	}	
    // // Highlight tiles for attacking only
	public void highlightAttackRange(Unit unit) {
		Set<Tile> validAttackTiles = findAttackTargets(unit);

		// Highlight valid attack tiles
		validAttackTiles.forEach(tile -> gameState.getBoardManager().updateTileHighlight(tile, 2)); // 2 for attack highlight mode
	}

    // Calculate and return the set of valid attack targets for a given unit
	public Set<Tile> findAttackTargets(Unit unit) {
		Set<Tile> validAttacks = new HashSet<>();
		Player opponent = gameState.getInactivePlayer();

		// Default target determination
		if (!unit.hasAttacked()) {
			validAttacks.addAll(getValidTargets(unit, opponent));
		}

		return validAttacks;
	}
    // Returns the set of valid attack targets for a given unit
	public Set<Tile> getValidTargets(Unit unit, Player opponent) {
		Set<Tile> validAttacks = new HashSet<>();
		Set<Position> provokers = checkProvoker(unit.getActiveTile(gameState.getBoard()));
		Tile unitTile = unit.getActiveTile(gameState.getBoard());

		// Attack adjacent units if there are any
		if (!provokers.isEmpty()) {
			for (Position position : provokers) {
				System.out.println(position + "provoker position");
				Tile provokerTile = gameState.getBoard().getTile(position.getTilex(), position.getTiley());
				validAttacks.add(provokerTile);
			}
			return validAttacks;
		}

		opponent.getUnits().stream()
				.map(opponentUnit -> opponentUnit.getActiveTile(gameState.getBoard()))
				.filter(opponentTile -> isAttackable(unitTile, opponentTile))
				.forEach(validAttacks::add);

		return validAttacks;
	}

	// Boolean method to check if a unit is within attack range of another unit
	public boolean isAttackable(Tile unitTile, Tile targetTile) {
		int dx = Math.abs(unitTile.getTilex() - targetTile.getTilex());
		int dy = Math.abs(unitTile.getTiley() - targetTile.getTiley());
		return dx < 2 && dy < 2;
	}
    // Checks if provoke unit is present on the board and around the tile on which an alleged enemy unit (target) is located
	public Set<Position> checkProvoker(Tile tile) {
		Set<Position> provokers = new HashSet<>();

		for (Unit unit : gameState.getInactivePlayer().getUnits()) {
			int tilex = tile.getTilex();
			int tiley = tile.getTiley();

			if (Math.abs(tilex - unit.getPosition().getTilex()) < 2 && Math.abs(tiley - unit.getPosition().getTiley()) < 2) {
				if (unit.getName().equals("Rock Pulveriser") || unit.getName().equals("Swamp Entangler") ||
						unit.getName().equals("Silverguard Knight") || unit.getName().equals("Ironcliff Guardian")) {
					System.out.println("Provoker " + unit.getName() + " in the house.");
					provokers.add(unit.getPosition());
				}
			}
		}
		return provokers;
	}
    // Returns true if the unit should be provoked based on adjacent opponents
	public boolean checkProvoked(Unit unit) {
		
		Player opponent = (gameState.getCurrentPlayer() == gameState.getHuman()) ? gameState.getAi() : gameState.getHuman();
		// Iterate over the opponent's units to check for adjacency and provoking units
		for (Unit other : opponent.getUnits()) {

			// Calculate the distance between the units
			int unitx = unit.getPosition().getTilex();
			int unity = unit.getPosition().getTiley();

			// Check if the opponent unit's name matches any provoking unit
			if (other.getName().equals("Rock Pulveriser") || other.getName().equals("Swamp Entangler") ||
					other.getName().equals("Silverguard Knight") || other.getName().equals("Ironcliff Guardian")) {


				// Check if the opponent unit is adjacent to the current unit
				if (Math.abs(unitx - other.getPosition().getTilex()) <= 1 && Math.abs(unity - other.getPosition().getTiley()) <= 1) {
					BasicCommands.addPlayer1Notification(out, unit.getName() + " has been provoked!", 2);
					return true;
				}
			}
		}
		return false;
	}
    // // Highlight tiles for movement and attack
	public void highlightValidMoves(Unit unit) {
	    Tile[][] tiles = gameState.getBoard().getTiles();
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
