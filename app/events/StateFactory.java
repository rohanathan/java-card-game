package events;

import structures.basic.GameAction;
import structures.basic.cards.Card;
import structures.basic.Unit;

/**
 * Factory class for creating GameActionState instances based on the type of GameAction.
 * This class follows the Factory Pattern to encapsulate the logic for determining
 * the appropriate state for a given action.
 */
public class StateFactory {

    /**
     * Returns the appropriate GameActionState for the given GameAction.
     *
     * @param action The action to be processed (e.g., Card, Unit).
     * @return The corresponding GameActionState, or null if no valid state is found.
     */
    public static GameActionState getStateForAction(GameAction action) {
        // Check if the action is a Card
        if (action instanceof Card) {
            Card card = (Card) action;

            // If the card is a creature, return a CardSummoningState
            if (card.isCreature()) {
                return new CardSummoningState(card);
            }
            // Otherwise, return a SpellCastingState
            else {
                return new SpellCastingState(card);
            }
        }
        // If the action is a Unit, return a UnitActionState
        else if (action instanceof Unit) {
            return new UnitActionState((Unit) action);
        }

        // Return null if no valid state is found
        return null;
    }
}