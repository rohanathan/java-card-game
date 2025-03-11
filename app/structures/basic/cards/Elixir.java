package structures.basic.cards;

import java.util.Comparator;
import java.util.Optional;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.player.AIPlayer;

public class Elixir {

	public static void silverguardSquire( ActorRef out, GameState gs) {
		
		Tile avatarPosition= gs.getAi().getAvatar().getActiveTile(gs.getBoard());
		boostAdjacentUnits(avatarPosition, 1, 1, gs);
		BasicCommands.addPlayer1Notification(out, "Silverguard Squire's Opening Gambit!", 3);
		try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
	}
	
	public static void boostAdjacentUnits(Tile tile, int healthBoost, int attackBoost, GameState gameState) {
	    // Get the x and y coordinates of the given tile
	    int x = tile.getTilex();
	    int y = tile.getTiley();


	    int newLeft = x - 1;
	    int newRight = x + 1;
		if (newLeft >= 0 ) {
			// Check the left tile
			Tile leftTile = gameState.getBoard().getTile(newLeft, y);
			
			if (leftTile != null && leftTile.isOccupied() &&
					leftTile.getUnit().getOwner()instanceof AIPlayer) {
				gameState.getUnitManager().modifyUnitHealth(leftTile.getUnit(), leftTile.getUnit().getHealth() + healthBoost);
				gameState.getUnitManager().updateUnitAttack(leftTile.getUnit(), leftTile.getUnit().getAttack() + attackBoost);
				gameState.getAbilityHandler().animateHealingEffect(leftTile);

	        }
			
		}
		if (newRight < 9 ) {
			// Check the right tile
			Tile rightTile = gameState.getBoard().getTile(newRight, y);
			
			if (rightTile != null && rightTile.isOccupied() &&
					rightTile.getUnit().getOwner() instanceof AIPlayer) {
				gameState.getUnitManager().modifyUnitHealth(rightTile.getUnit(), rightTile.getUnit().getHealth() + healthBoost);
				gameState.getUnitManager().updateUnitAttack(rightTile.getUnit(), rightTile.getUnit().getAttack() + attackBoost);
				gameState.getAbilityHandler().animateHealingEffect(rightTile);

			}

		}
	}
	
	public static void Sundrop(Unit unit, GameState gs, ActorRef out) {
        // Implement healing effect by 4 health
        if (unit != null && unit.getOwner() instanceof AIPlayer) {
            AIPlayer aiPlayer = (AIPlayer) gs.getAi(); // Assuming gs is your GameState object

            if (unit.getHealth() == unit.getMaxHealth()) {
                // Find the unit with the lowest health
                Optional<Unit> unitWithLowestHealth = aiPlayer.getUnits().stream()
                        .min(Comparator.comparingInt(Unit::getHealth));

                // Check if a unit with the lowest health is found
                Unit lowestHealthUnit = unitWithLowestHealth.orElse(null);

                if (lowestHealthUnit != null) {
                    int newHealth = lowestHealthUnit.getHealth() + 4;
                    if (newHealth > lowestHealthUnit.getMaxHealth()) {
                        gs.getUnitManager().modifyUnitHealth(lowestHealthUnit, lowestHealthUnit.getMaxHealth());
                    } else {
                        gs.getUnitManager().modifyUnitHealth(lowestHealthUnit, newHealth);
                    }
                }
            } else {
                int newHealth = unit.getHealth() + 4;
                if (newHealth > unit.getMaxHealth()) {
                    gs.getUnitManager().modifyUnitHealth(unit, unit.getMaxHealth());
                } else {
                    gs.getUnitManager().modifyUnitHealth(unit, newHealth);
                }
            }
        }
		BasicCommands.addPlayer1Notification(out, "Sundrop Elixir heals a unit!", 3);
        gs.getAbilityHandler().animateHealingEffect(unit.getActiveTile(gs.getBoard()));
    }

}
