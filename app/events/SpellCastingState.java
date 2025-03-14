package events;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.cards.Card;

/**
 * State class for handling the casting of a spell card.
 * This class implements the GameActionState interface and encapsulates the logic
 * for validating and processing spell card casts.
 */
public class SpellCastingState implements GameActionState {
    private final Card card;

    /**
     * Constructor for SpellCastingState.
     *
     * @param card The spell card to be cast.
     */
    public SpellCastingState(Card card) {
        this.card = card;
    }

    /**
     * Handles the casting of the spell card.
     *
     * @param out       The ActorRef for sending commands to the front-end.
     * @param gameState The current state of the game.
     * @param tile      The target tile for the spell.
     */
    @Override
    public void handleAction(ActorRef out, GameState gameState, Tile tile) {
        // Check if the player has enough mana to cast the spell
        if (gameState.getHuman().getMana() < card.getManacost()) {
            // Notify the player of insufficient mana
            BasicCommands.addPlayer1Notification(out, "Mana reserves depleted!", 2);
            gameState.getBoardManager().removeHighlightFromAll();
            gameState.getPlayerManager().notClickingCard();
            return;
        }

        // Cast the spell and update the player's hand
        gameState.getAbilityHandler().castSpellAndUpdateHand(gameState, card, tile);
    }
}