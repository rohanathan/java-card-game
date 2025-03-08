package structures.basic.cards;

import akka.actor.ActorRef;
import structures.GameManager;
import structures.GameState;
import structures.basic.Unit;

import java.util.ArrayList;

public class ShadowWatcher extends Unit{

    // method to check if shadow watcher is on the board and increment attack and health by 1 each time someone dies.
    public static void ShadowWatcherDeathwatch(ActorRef out, GameState gameState, GameManager gm) {
        for (Unit unit : gameState.getUnits()) {
            if (unit.getName().equals("Shadow Watcher")) {
                System.out.println("Found ShadowWatcher");
                gameState.getUnitManager().updateUnitAttack(unit, unit.getAttack() + 1);
                gameState.getUnitManager().modifyUnitHealth(unit, unit.getHealth() + 1);
                
            }
        }

    }
}