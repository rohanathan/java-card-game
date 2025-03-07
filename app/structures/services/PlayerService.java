package structures.services;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.player.Player;
import structures.basic.player.HumanPlayer;
import structures.basic.cards.Card;
import structures.GameState;



/**
 * PlayerService handles all player-related actions such as health, mana, and card drawing.
 */
public class PlayerService {
    private final ActorRef out;
    private final GameState gs;


    public PlayerService(ActorRef out, GameState gs) {
        this.out = out;
        // Store reference to GameState
        this.gs = gs; 

    }
    // Method to modify the health of a player
	public void modifyPlayerHealth(Player player, int newHealth) {
		// Set the new health value on the player object first
		player.setHealth(newHealth);

		// Now modify the health on the frontend using the BasicCommands
		if (player instanceof HumanPlayer) {
			BasicCommands.setPlayer1Health(out, player);
		} else {
			BasicCommands.setPlayer2Health(out, player);
		}
        // Check if the player's health is less than or equal to 0
        if (newHealth <= 0) {
            gs.endGame(out);  
            }
	}

    // Method to modify the mana of a player
	public void modifyPlayerMana(Player player, int newMana) {
		// Set the new mana value on the player object first
		player.setMana(newMana);

		// Now modify the mana on the frontend using the BasicCommands
		if (player instanceof HumanPlayer) {
			BasicCommands.setPlayer1Mana(out, player);
		} else {
			BasicCommands.setPlayer2Mana(out, player);
		}
	}

    // Method to draw a card for a player
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