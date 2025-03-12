package structures.basic.cards;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Unit;
import structures.basic.player.HumanPlayer;

/**
 * The {@code Strike} class represents the True Strike spell card.
 * This spell reduces the health of a specified unit by 2 if the unit
 * belongs to the human player.
 */
public class Strike {

    /**
     * Applies the True Strike spell effect on a given unit, reducing its health by 2.
     * If the unit belongs to the human player, an animation is played, and a notification
     * is sent to the player.
     *
     * @param gs  The current game state.
     * @param u   The target unit to apply the strike effect on.
     * @param out The actor reference for sending game commands.
     */
    public static void TrueStrike(GameState gs, Unit u, ActorRef out) {
        
        if (u != null && u.getOwner() instanceof HumanPlayer) {
            // Reduce unit health by 2
            gs.getUnitManager().modifyUnitHealth(u, u.getHealth() - 2);
            
            // Play strike effect animation on the unit's active tile
            gs.getAbilityHandler().animateStrikeEffect(u.getActiveTile(gs.getBoard()));

            // Notify the player about the True Strike effect
            BasicCommands.addPlayer1Notification(out, "True Strike! -2 Health", 2);
        }
    }
}
