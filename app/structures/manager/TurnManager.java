package structures.manager;

import structures.GameState;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.*;
import structures.basic.cards.Card;
import structures.basic.player.AIPlayer;
import structures.basic.player.HumanPlayer;
import structures.basic.player.Player;
import structures.manager.*;
import java.util.*;

public class TurnManager {
    private final ActorRef out;
    private final GameState gameState;

    public TurnManager(ActorRef out, GameState gameState) {
        this.out = out;
        this.gameState = gameState;
    }

    /**
     * Handles the end of a player's turn. This includes:
     * 1. Managing card draws and discards.
     * 2. Incrementing the player's turn count.
     * 3. Switching to the next player.
     * 4. Refreshing the new player's mana.
     */
    public void endTurn() {
        manageCardsAtTurnEnd();
        Player currentPlayer = gameState.getCurrentPlayer();
        currentPlayer.increaseTurnNum();
        gameState.getPlayerManager().modifyPlayerMana(currentPlayer, 0);
        switchToNextPlayer();
        gameState.getPlayerManager().modifyPlayerMana(gameState.getCurrentPlayer(), gameState.getCurrentPlayer().getTurn() + 1);
    }

    /**
     * Switches the current player to the opponent.
     */
    public void switchToNextPlayer() {
        Player currentPlayer = gameState.getCurrentPlayer();
        if (currentPlayer == gameState.getHuman()) {
            gameState.setCurrentPlayer(gameState.getAi());
        } else {
            gameState.setCurrentPlayer(gameState.getHuman());
        }
    }

    /**
     * Manages card drawing and discarding at the end of a turn.
     * If the player's hand is full, the top card is discarded.
     * Otherwise, a new card is drawn.
     */
    public void manageCardsAtTurnEnd() {
        Player currentPlayer = gameState.getCurrentPlayer();
        try {
            if (currentPlayer.getHand().isFull()) {
                // Discard the top card from the hand if it's at maximum size.
                if (!currentPlayer.getDeck().getDeck().isEmpty()) {
                    currentPlayer.getDeck().drawCard();
                }
            } else {
                // The hand is not full, draw a new card.
                gameState.getPlayerManager().drawCards(currentPlayer, 1);
            }
        } catch (IllegalStateException e) {
            // Handle the exception, for example, by displaying an error message or logging it
            System.err.println("Cannot draw a card: " + e.getMessage());
        }
    }

}
//endTurn
//switchToNextPlayer
//manageCardsAtTurnEnd
//Class EndTurnClicked()
//Class GameState()
//Class GameManager
//Initalize
