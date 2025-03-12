package structures.basic.cards;

import akka.actor.ActorRef;
import structures.GameState;

/**
 * The {@code CardEffect} class is responsible for handling the effects
 * of a card when it is selected from the player's hand.
 * It updates the frontend by highlighting appropriate tiles
 * and pushing the selected card to the action history.
 */
public class CardEffect {
  
  /** The current game state instance. */
  private GameState gameState;

  /** The position of the card in the player's hand. */
  private int handPosition;

  /**
   * Constructs a {@code CardEffect} object for a given card in the player's hand.
   * It initializes the game state and the card's position and prepares the frontend.
   *
   * @param gameState    The current game state.
   * @param handPosition The position of the card in the player's hand.
   */
  public CardEffect(GameState gameState, int handPosition) {
    this.gameState = gameState;
    this.handPosition = handPosition;
    // Reflects the action in the frontend
    preAction();
  }

  /**
   * Prepares the frontend by updating the UI to reflect the selected card.
   * It clears any previous highlights and highlights the selected card.
   */
  private void preAction() {
    // Clear all highlighted tiles
    gameState.getBoardManager().removeHighlightFromAll();

    // Set the current card clicked to the card at the specified position in the player's hand
    gameState.getPlayerManager().highlightSelectedCard(handPosition);
  }

  /**
   * Prepares the frontend when a creature card is selected.
   * Highlights the summon range and pushes the selected card to the action history.
   */
  public void creaturePreEffect() {
    Card currentCard = gameState.getActiveCard();

    // Highlight the summon range of the selected creature card
    gameState.getBoardManager().highlightSummonRange();

    // Push the selected card to the action history
    gameState.getActionHistory().push(currentCard);

    // Debug message
    System.out.println("Pushed to action history: " + currentCard.getCardname() + " " + currentCard.getId());
  }

  /**
   * Prepares the frontend and backend for spell cards.
   * Highlights the spell range if the selected card is a spell.
   */
  public void spellPreEffect() {
    preAction();

    Card currentCard = gameState.getActiveCard();
    if (!currentCard.isCreature) {
      // Highlight the spell range for the selected card
      gameState.getAbilityHandler().highlightSpellRange(currentCard, gameState.getHuman());

      // Push the selected spell card to the action history
      gameState.getActionHistory().push(currentCard);

      // Debug message
    }
  }
}
