package structures.basic.cards;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Unit;

/**
 * The ShadowWatcher class represents a unit with a Deathwatch ability.
 * Every time a unit dies, Shadow Watcher gains +1 attack and +1 health.
 */
public class ShadowWatcher extends Unit {

    /**
     * Checks if a Shadow Watcher is on the board and increments its attack 
     * and health by 1 each time a unit dies.
     * 
     * @param out       The ActorRef for communication with the UI.
     * @param gameState The current game state.
     */
    public static void ShadowWatcherDeathwatch(ActorRef out, GameState gameState) {
        for (int i = 0; i < gameState.getUnits().size(); i++) {
            Unit unit = gameState.getUnits().get(i);
            
            if (unit.getName().equals("Shadow Watcher")) {
                System.out.println("Found ShadowWatcher");
                gameState.getUnitManager().updateUnitAttack(unit, unit.getAttack() + 1);
                gameState.getUnitManager().modifyUnitHealth(unit, unit.getHealth() + 1);
            }
        }
    }
}
