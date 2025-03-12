package structures.basic.cards;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Unit;

/**
 * The BadOmen class represents a unit with a Deathwatch ability.
 * Increases its attack each time a unit dies.
 */
public class BadOmen extends Unit {

    /**
     * Implements the Deathwatch ability for Bad Omen.
     * Increases attack by 1 whenever another unit dies.
     *
     * @param out       The ActorRef for communication with the UI.
     * @param gameState The current game state.
     * @param victim    The unit that just died.
     */
    public static void BadOmenDeathwatch(ActorRef out, GameState gameState, Unit victim) {
        for (int i = 0; i < gameState.getUnits().size(); i++) {
            Unit unit = gameState.getUnits().get(i);
            
            if (unit.getName().equals("Bad Omen") && !victim.getName().equals("Bad Omen")) {
                gameState.getUnitManager().updateUnitAttack(unit, unit.getAttack() + 1);
            }
        }
    }
}
