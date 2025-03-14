package events;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.cards.Card;

/**
 * State class for handling the summoning of a unit card onto the board.
 * This class implements the GameActionState interface and encapsulates the logic
 * for validating and processing unit card summons.
 */
public class CardSummoningState implements GameActionState {
    private final Card card;

    /**
     * Constructor for CardSummoningState.
     *
     * @param card The unit card to be summoned.
     */
    public CardSummoningState(Card card) {
        this.card = card;
    }

    /**
     * Handles the summoning of the unit card onto the board.
     *
     * @param out       The ActorRef for sending commands to the front-end.
     * @param gameState The current state of the game.
     * @param tile      The target tile for the summon.
     */
    @Override
    public void handleAction(ActorRef out, GameState gameState, Tile tile) {
        // Check if the summon is valid (e.g., the tile is not occupied and the card can be summoned)
        if (gameState.getUnitManager().isValidSummon(card, tile)) {
            // Summon the card and update the game state
            gameState.getAbilityHandler().castCardFromHand(card, tile);
        } else {
            // Remove highlights if the summon is invalid
            gameState.getBoardManager().removeHighlightFromAll();
        }
    }
}