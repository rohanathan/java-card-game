package structures.basic.cards;

import akka.actor.ActorRef;
import structures.GameManager;
import structures.GameState;
import structures.basic.Unit;

public class BloodmoonPriestess {

    public static void BloodmoonPriestessDeathwatch( ActorRef out, GameState gameState, GameManager gm) {

        for(Unit unit:gameState.getUnits()){
            if (unit.getName().equals("Bloodmoon Priestess")){
                System.out.println("Bloodmoon Priestess is on the board");
                Wraithling.summonWraithlingForBloodmoonPriestess(unit, out, gameState, gm);
            }
        }
        try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();
        }
    }
}

