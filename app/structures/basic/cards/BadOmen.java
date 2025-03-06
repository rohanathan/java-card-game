package structures.basic.cards;

import akka.actor.ActorRef;
import akka.stream.StreamRefMessages;
import commands.BasicCommands;
import structures.GameService;
import structures.GameState;
import structures.basic.BigCard;
import structures.basic.EffectAnimation;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import static utils.BasicObjectBuilders.loadUnit;
import static utils.StaticConfFiles.card_badOmen;

public class BadOmen extends Unit {

    // method to check if bad omen is onthe board and increment attack each time someone dies.
    public static void BadOmenDeathwatch( ActorRef out, GameState gameState, GameService gs, Unit victim) {
        for (Unit unit : gameState.getUnits()) {
            if (unit.getName().equals("Bad Omen") &&
            		!victim.getName().equals("Bad Omen")) {
                gs.updateUnitAttack(unit, unit.getAttack() + 1);            }
        }

    }
}
