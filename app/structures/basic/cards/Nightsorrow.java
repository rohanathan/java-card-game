package structures.basic.cards;

import java.util.List;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

public class Nightsorrow {

	public static void assassin(Tile tile, GameState gameState, ActorRef out) {
		
		        // Check adjacent tiles for enemy units
		        List<Tile> adjacentTiles = gameState.getBoard().getAdjacentTiles(tile);
		        
		        for (Tile adjacentTile : adjacentTiles) {
		        	if (adjacentTile == null) {
                        continue; } // Skip this iteration if the tile is null
		        	if(adjacentTile.getUnit()!=null){		        		
		        		Unit unit = adjacentTile.getUnit();
                		if(unit.getOwner() != gameState.getHuman() && !unit.getName().equals("AI Avatar")) {
                			if(unit.getHealth()< unit.getMaxHealth()) {
                				gameState.getUnitManager().destroyUnit(unit);
								BasicCommands.addPlayer1Notification(out, "Nightsorrow Assassin has killed " + unit.getName(), 3);
                				break;
                			}
                		}
		        	}
		        }
		        
	}
}
	


