package structures.basic.cards;

import java.util.List;
import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.Board;

/**
 * The Nightsorrow class represents a unit that can assassinate weak enemy units 
 * adjacent to its position.
 */
public class Nightsorrow {

    /**
     * Assassination ability for Nightsorrow Assassin.
     * Destroys an adjacent enemy unit if its health is lower than its max health.
     *
     * @param tile      The tile where Nightsorrow Assassin is placed.
     * @param gameState The current game state.
     * @param out       The ActorRef for communication with the UI.
     */
    public static void assassin(Tile tile, GameState gameState, ActorRef out) {
        Board board = gameState.getBoard();
        List<Tile> adjacentTiles = tile.getAllAdjacentTiles(board);

        for (int i = 0; i < adjacentTiles.size(); i++) {
            Tile adjacentTile = adjacentTiles.get(i);
            
            if (adjacentTile == null) {
                continue;
            }
            
            if (adjacentTile.getUnit() != null) {
                Unit unit = adjacentTile.getUnit();
                
                if (unit.getOwner() != gameState.getHuman() && !unit.getName().equals("AI Avatar")) {
                    if (unit.getHealth() < unit.getMaxHealth()) {
                        gameState.getUnitManager().destroyUnit(unit);
                        BasicCommands.addPlayer1Notification(out, "Nightsorrow Assassin has killed " + unit.getName(), 3);
                        break;
                    }
                }
            }
        }
    }
}
