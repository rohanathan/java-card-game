package structures.services;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.player.Player;
import structures.basic.player.HumanPlayer;
import structures.basic.cards.Card;
import structures.GameState;



/**
 * Handles player-related functionalities, including health, mana, and card management.
 */
public class PlayerService {
    private final ActorRef out;
    private final GameState gameState;

    /**
     * Constructor to initialize PlayerService with references to the Actor system and game state.
     * @param out The ActorRef used for communication.
     * @param gameState The current state of the game.
     */
    public PlayerService(ActorRef out, GameState gameState) {
        this.out = out;
        // Store reference to GameState
        this.gameState = gameState; 

    }
    /**
     * Adjusts a player's health and updates the UI accordingly.
     * If health reaches zero, the game ends.
     * @param player The player whose health is being modified.
     * @param updatedHealth The new health value.
     */
	public void modifyPlayerHealth(Player player, int updatedHealth) {
		// Set the new health value on the player object first
		player.setHealth(updatedHealth);
		// Now modify the health on the frontend using the BasicCommands
		if (player instanceof HumanPlayer) {
			BasicCommands.setPlayer1Health(out, player);
		} else {
			BasicCommands.setPlayer2Health(out, player);
		}
        // End the game if a player's health drops to zero
        if (updatedHealth <= 0) {
            gameState.endGame(out);  
            }
	}
    /**
     * Updates the player's mana and reflects changes in the UI.
     * @param player The player whose mana is being updated.
     * @param updatedMana The new mana value.
     */
	public void modifyPlayerMana(Player player, int updatedMana) {
		player.setMana(updatedMana);

		if (player instanceof HumanPlayer) {
			BasicCommands.setPlayer1Mana(out, player);
		} else {
			BasicCommands.setPlayer2Mana(out, player);
		}
	}

    /**
     * Draws a specified number of cards for a player and updates the UI accordingly.
     * Prevents drawing if the deck is empty.
     * @param player The player who will draw the cards.
     * @param cardCount The number of cards to draw.
     */
    public void drawCards(Player player, int numberOfCards) {
		if (player instanceof HumanPlayer) {
			for (int i = 0; i < numberOfCards; i++) {
				Card cardDrawn = player.drawCard();
				if (cardDrawn == null) {
					BasicCommands.addPlayer1Notification(out, "Card reserves depleted!", 2);
					return;
				}
				int handPosition = player.getHand().getNumberOfCardsInHand();
				BasicCommands.drawCard(out, cardDrawn, handPosition, 0);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			for (int i = 0; i < numberOfCards; i++) {
				player.drawCard();
			}
		}
	}
}