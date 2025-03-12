package structures.basic.cards;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.EffectAnimation;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

/**
 * The {@code HornOfTheForsaken} class represents a spell card that applies an effect to 
 * the player's avatar, providing a robustness boost when cast.
 */
public class HornOfTheForsaken {

    /**
     * Casts the Horn of the Forsaken spell, playing an effect animation, 
     * providing a robustness boost, and resetting card selection state.
     *
     * @param gameState The current game state.
     * @param out       The actor reference for sending game commands.
     */
    public static void castSpell(GameState gameState, ActorRef out) { 
        // Load the visual effect animation for the spell
        EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.something); 
        
        // Play the effect animation on the human avatar's tile
        BasicCommands.playEffectAnimation(out, effect, gameState.getHuman().getAvatar().getActiveTile(gameState.getBoard())); 
        
        // Reset card selection state after casting the spell
        gameState.getPlayerManager().notClickingCard(); 
        
        // Display notification about the robustness gain
        BasicCommands.addPlayer1Notification(out, "You gained +3 robustness!", 2); 
        
        // Remove highlight effects from the board
        gameState.getBoardManager().removeHighlightFromAll(); 

        // Add a short delay for visual effect purposes
        try { 
            Thread.sleep(1000); 
        } catch (InterruptedException e) { 
            e.printStackTrace(); 
        } 
    }
}
