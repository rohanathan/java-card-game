package events;


import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.cards.CardAction;

/**
 * Indicates that the user has clicked an object on the game canvas, in this case a card.
 * The event returns the position in the player's hand the card resides within.
 * 
 * { 
 *   messageType = “cardClicked”
 *   position = <hand index position [1-6]>
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class CardClicked implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		// Ignores any card click events when the AI player is taking its turn
		if (gameState.getCurrentPlayer().equals(gameState.getAi())) {
			return;
		}

		// Retrieves the position of the clicked card from the event message
		int handPosition = message.get("position").asInt();

		// Creates a CardAction object for the clicked card, using the game state and card position
		CardAction cardAction = new CardAction(gameState, handPosition);

		// Determines the type of the clicked card and performs the corresponding pre-action
		// If the card is a creature, it performs a creature pre-action; otherwise, it performs a spell pre-action
		if (gameState.getActiveCard().isCreature()) {
			cardAction.creaturePreAction();
		} else {
			cardAction.spellPreAction();
		}
	}

}
