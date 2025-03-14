package events;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.player.AIPlayer;

/**
 * State class for handling unit actions such as moving and attacking.
 * This class implements the GameActionState interface and encapsulates the logic
 * for processing unit actions based on the target tile's state.
 */
public class UnitActionState implements GameActionState {
    private final Unit unit;

    /**
     * Constructor for UnitActionState.
     *
     * @param unit The unit to perform actions with.
     */
    public UnitActionState(Unit unit) {
        this.unit = unit;
    }

    /**
     * Handles the unit's action (move or attack) based on the target tile's state.
     *
     * @param out        The ActorRef for sending commands to the front-end.
     * @param gameState  The current state of the game.
     * @param targetTile The target tile for the unit's action.
     */
    @Override
    public void handleAction(ActorRef out, GameState gameState, Tile targetTile) {
        // Early return if the target tile is null
        if (targetTile == null) {
            System.out.println("Target tile is null.");
            gameState.getBoardManager().removeHighlightFromAll();
            return;
        }

        // Check if the unit is stunned (prevent actions)
        AIPlayer ai = (AIPlayer) gameState.getAi();
        if (ai.getAiManager().getStunnedUnit() == unit) {
            System.out.println("Unit is stunned.");
            unit.setHasMoved(true);
            gameState.getBoardManager().removeHighlightFromAll();
            gameState.getAbilityHandler().stunnedUnit(unit.getName());
            return;
        }

        // Early return if the unit is null
        if (unit == null) {
            System.out.println("Unit is null.");
            gameState.getBoardManager().removeHighlightFromAll();
            return;
        }

        // Determine action based on tile's occupancy and highlight mode
        if (!targetTile.isOccupied()) {
            // Move the unit to the target tile
            gameState.getBoardManager().updateUnitPositionAndMove(unit, targetTile);
            System.out.println("Unit " + unit.getId() + " moved to " + targetTile.getTilex() + ", " + targetTile.getTiley());
        } else if (targetTile.getHighlightMode() == 2) {
            // Handle attack logic
            Tile attackerTile = unit.getActiveTile(gameState.getBoard());

            if (gameState.getCombatHandler().isWithinAttackRange(attackerTile, targetTile)) {
                // Attack adjacent unit
                if (targetTile.isOccupied()) {
                    System.out.println("Target tile is occupied by " + targetTile.getUnit());
                }
                gameState.getCombatHandler().performAttack(unit, targetTile.getUnit());
                unit.setHasAttacked(true);
                unit.setHasMoved(true);
            } else {
                // Move and attack
                if (targetTile.isOccupied()) {
                    System.out.println("Target tile is occupied by " + targetTile.getUnit() + " and is attacked by " + unit);
                }
                gameState.getCombatHandler().moveAndAttack(unit, targetTile.getUnit());
                unit.setHasAttacked(true);
                unit.setHasMoved(true);
            }

            // Remove highlights after the action
            gameState.getBoardManager().removeHighlightFromAll();
        }
    }
}