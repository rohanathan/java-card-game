package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.*;
import structures.basic.cards.Card;
import structures.basic.cards.Wraithling;
import structures.basic.player.AIPlayer;
import structures.basic.player.HumanPlayer;
import structures.basic.player.Player;
import structures.manager.*;

import java.util.*;

/**
 * This class can be used to hold information about the on-going game. Its
 * created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class GameState {
	

	public boolean gameInitalised = false;

	public boolean isGameOver = false;
	private PlayerManager playerManager; // Add PlayerManager reference

	// Keep track of the player currently taking their turn
	private Player currentPlayer;

	// Keep track of the card that is currently clicked
	private Card activeCard;
	// Keep track of the position of the card that is currently clicked
	private int currentCardPosition;

	// Keep track of the previous plays of the current turn
	private Stack<GameAction> actionHistory;
	// Keep track of the total number of units on the board
	private int totalUnits = 0;

	// Entity objects that are part of the game state
	public GameManager gameManager;
	private Player human;
	private Player ai;
	private Board board;
	private ArrayList<Unit> unitsOnBoard =  new ArrayList<Unit>();
	/**
	 * This function initialises all the assets Board, Player etc As well as
	 * tracking critical game states
	 * 
	 * @param out
	 */

	public void init(ActorRef out, PlayerManager playerManager) {
		
		
		
		this.gameManager = new GameManager(out, this, playerManager);
		this.board = gameManager.initializeBoard();
		
		// Initialize playerManager
		this.playerManager = playerManager; 


		// Initialize stack of action history
		this.actionHistory = new Stack<>();

		// Create the human and AI players
		this.human = new HumanPlayer();
		this.ai = new AIPlayer(this);

		// Health initialised to 20
		playerManager.modifyPlayerHealth(human,20);
		playerManager.modifyPlayerHealth(ai,20);

		// Player mana initialised to 2
		playerManager.modifyPlayerMana(human, 2);

		// Create the human and AI avatars
		gameManager.initializeAvatar(board, human);
		gameManager.initializeAvatar(board, ai);
		// gameManager.loadUnitsForTesting(ai);

		// Set the current player to the human player
		this.currentPlayer = human;

		//Drawing initial 3 cards from the deck for the game start
		playerManager.drawCards(human,3);
		playerManager.drawCards(ai,3);
	}

	// Switch the current player
	public void switchCurrentPlayer() {
		if (this.currentPlayer == this.human) {
			this.currentPlayer = this.ai;
		} else {
			this.currentPlayer = this.human;
		}
	}

	/**
	 * This will be called when endTurn event
	 * is clicked, to manage the states
	 */
	public void endTurn(){
		handleCardManagement();
		currentPlayer.incrementTurn();
		this.playerManager.modifyPlayerMana(currentPlayer, 0);
		switchCurrentPlayer();
		this.playerManager.modifyPlayerMana(currentPlayer, currentPlayer.getTurn() + 1);
	}

	
	/**
	 * This will handle the cards in the deck
	 * and the hand of the player
	 * used when the endTurn is clicked 
	 */
	public void handleCardManagement() {
		try {
			if (currentPlayer.getHand().getNumberOfCardsInHand() >= 6) {
				// Discard the top card from the hand if it's at maximum size.
				if((currentPlayer.getDeck()).getDeck().isEmpty()) {
					} else {
					currentPlayer.getDeck().drawCard();
				}

			} else {
				// The hand is not full, draw a new card.
				playerManager.drawCards(currentPlayer, 1);
			}
		}
		catch (IllegalStateException e){
				// Handle the exception, for example by displaying an error message or logging
				System.err.println("Cannot draw a card: " + e.getMessage());
		}
	}


	
	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	public void addUnitstoBoard(Unit unit) {
		this.unitsOnBoard.add(unit);
		}

	public Player getHuman() {
		return this.human;
	}

	public Player getAi() {
		return this.ai;
	}

	public Player getCurrentPlayer() {
		return currentPlayer;
	}
	public Player getInactivePlayer() {
		if (currentPlayer == human) {
			return ai;
		} else {
			return human;
		}
	}

	public Card getActiveCard() {
		return activeCard;
	}

	public void setActiveCard(Card card) {
		activeCard = card;
	}

	public int getCurrentCardPosition() {
		return currentCardPosition;
	}

	public void setCurrentCardPosition(int position) {
		currentCardPosition = position;
	}

	public Stack<GameAction> getActionHistory() {
		return actionHistory;
	}

	public int getTotalUnits() {
		return totalUnits;
	}

	public void addToTotalUnits(int numberToAdd) {
		this.totalUnits += numberToAdd;
	}

	public void removeFromTotalUnits(int numberToRemove) {
		this.totalUnits -= numberToRemove;
	}
	

	// Get all the units on the board
	public ArrayList<Unit> getUnits() {
		ArrayList<Unit> combinedUnits = new ArrayList<>();
		combinedUnits.addAll(ai.getUnits());
		combinedUnits.addAll(human.getUnits());
		return combinedUnits;
	}


	/**
	 * Checks and see if the game has ended If so it will send the apropiate
	 * notifcation
	 * 
	 * @param out
	 */
	public void endGame(ActorRef out) {
		if (this.ai != null && this.ai.getHealth() == 0) {
			BasicCommands.addPlayer1Notification(out, "You Won!", 1000);
			isGameOver = true;
		} else if (this.human != null && this.human.getHealth() == 0) {
			BasicCommands.addPlayer1Notification(out, "You Lost", 1000);
			isGameOver = true;
		}
	}
}
