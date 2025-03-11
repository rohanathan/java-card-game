package structures.basic.cards;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.EffectAnimation;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

public class HornOfTheForsaken {

    public static void castSpell(GameState gameState, ActorRef out) { 
        EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.something); 
        BasicCommands.playEffectAnimation(out, effect, gameState.getHuman().getAvatar().getActiveTile(gameState.getBoard())); 
        gameState.getPlayerManager().notClickingCard(); 
        BasicCommands.addPlayer1Notification(out, "You gained +3 robustness!", 2); 
        gameState.getBoardManager().removeHighlightFromAll(); 

        try { 
            Thread.sleep(1000); 
        } catch (InterruptedException e) { 
            e.printStackTrace(); 
        } 
    }
}
