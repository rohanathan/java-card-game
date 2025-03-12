package structures.basic.cards;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Unit;

/**
 * The BloodmoonPriestess class represents a unit with a Deathwatch ability.
 * Summons a Wraithling whenever a unit dies.
 */
public class BloodmoonPriestess {

    /**
     * Implements the Deathwatch ability for Bloodmoon Priestess.
     * Summons a Wraithling whenever a unit dies.
     *
     * @param out       The ActorRef for communication with the UI.
     * @param gameState The current game state.
     */
    public static void BloodmoonPriestessDeathwatch(ActorRef out, GameState gameState) {
        for (int i = 0; i < gameState.getUnits().size(); i++) {
            Unit unit = gameState.getUnits().get(i);
            
            if (unit.getName().equals("Bloodmoon Priestess")) {
                System.out.println("Bloodmoon Priestess is on the board");
                Wraithling.summonWraithlingForBloodmoonPriestess(unit, out, gameState);
            }
        }
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
