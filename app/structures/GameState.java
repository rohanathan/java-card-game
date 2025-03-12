package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.*;
import structures.basic.cards.Card;
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

	// Track whether the game has been initialized
	public boolean gameInitalised = false;

	// Track whether the game is over
	public boolean isGameOver = false;

	// Managers and handlers for game components
	private AbilityHandler abilityHandler;// Handles unit abilities
	private CombatHandler combatHandler; // Handles combat logic
	private BoardManager boardManager;// Manages the game board
	private PlayerManager playerManager; // Manages player-related logic
	private TurnManager turnManager; // Manages turn logic
	private UnitManager unitManager; // Manages units on the board

	// Track the player currently taking their turn	private Player currentPlayer;
	private Player currentPlayer;

	// Track the card that is currently clicked by the player
	private Card activeCard;

	// Track the position of the currently clicked card in the hand
	private int currentCardPosition;

	// Track the history of actions taken in the current turn
	private Stack<GameAction> actionHistory;

	// Track the total number of units on the board
	private int totalUnits = 0;

	// Players in the game
	private Player human;
	private Player ai;

	// The game board
	private Board board;

	// List of units currently on the board
	private ArrayList<Unit> unitsOnBoard =  new ArrayList<Unit>();
	/**
	 * This function initialises all the assets Board, Player etc As well as
	 * tracking critical game states
	 *
	 * @param out
	 */

	public void init(ActorRef out, PlayerManager playerManager,BoardManager boardManager,CombatHandler combatHandler,
					 UnitManager unitManager,AbilityHandler abilityHandler, TurnManager turnManager) {
		this.boardManager=boardManager;
		this.combatHandler=combatHandler;
		 this.unitManager=unitManager;
		 this.abilityHandler=abilityHandler;
		this.turnManager = turnManager;
		this.board = boardManager.initializeBoard();

		// Initialize playerManager
		this.playerManager = playerManager;

		// Initialize stack of action history
		this.actionHistory = new Stack<>();

		// Create the human and AI players
		this.human = new HumanPlayer();
		this.ai = new AIPlayer(this);

		// Initialize player health to 20
		playerManager.modifyPlayerHealth(human,20);
		playerManager.modifyPlayerHealth(ai,20);

		// Initialize player mana to 2
		playerManager.modifyPlayerMana(human, 2);

		// Create the human and AI avatars on the board
		unitManager.initializeAvatar(board, human);
		unitManager.initializeAvatar(board, ai);

		// Set the current player to the human player
		this.currentPlayer = human;

		//Draw initial 3 cards for both players
		playerManager.drawCards(human,3);
		playerManager.drawCards(ai,3);
	}

	/**
	 * Gets the game board.
	 *
	 * @return The game board.
	 */
	public Board getBoard() {
		return board;
	}

	/**
	 * Sets the game board.
	 *
	 * @param board The game board to set.
	 */
	public void setBoard(Board board) {
		this.board = board;
	}

	/**
	 * Adds a unit to the board.
	 *
	 * @param unit The unit to add.
	 */
	public void addUnitstoBoard(Unit unit) {
		this.unitsOnBoard.add(unit);
		}

	/**
	 * Gets the human player.
	 *
	 * @return The human player.
	 */
	public Player getHuman() {
		return this.human;
	}

	/**
	 * Gets the AI player.
	 *
	 * @return The AI player.
	 */
	public Player getAi() {
		return this.ai;
	}

	/**
	 * Gets the current player (the player whose turn it is).
	 *
	 * @return The current player.
	 */
	public Player getCurrentPlayer() {
		return currentPlayer;
	}

	/**
	 * Sets the current player.
	 *
	 * @param player The player to set as the current player.
	 * @return The current player.
	 */
	public Player setCurrentPlayer(Player player) {
		return this.currentPlayer = player;
	}

	/**
	 * Gets the inactive player (the player whose turn it is not).
	 *
	 * @return The inactive player.
	 */
	public Player getInactivePlayer() {
		if (currentPlayer == human) {
			return ai;
		} else {
			return human;
		}
	}

	/**
	 * Gets the currently active card (the card clicked by the player).
	 *
	 * @return The active card.
	 */
	public Card getActiveCard() {
		return activeCard;
	}

	/**
	 * Sets the currently active card.
	 *
	 * @param card The card to set as active.
	 */
	public void setActiveCard(Card card) {
		activeCard = card;
	}

	/**
	 * Gets the position of the currently active card in the player's hand.
	 *
	 * @return The position of the active card.
	 */
	public int getCurrentCardPosition() {
		return currentCardPosition;
	}

	/**
	 * Sets the position of the currently active card in the player's hand.
	 *
	 * @param position The position to set.
	 */
	public void setCurrentCardPosition(int position) {
		currentCardPosition = position;
	}

	/**
	 * Gets the action history for the current turn.
	 *
	 * @return The stack of actions taken in the current turn.
	 */
	public Stack<GameAction> getActionHistory() {
		return actionHistory;
	}

	/**
	 * Gets the total number of units on the board.
	 *
	 * @return The total number of units.
	 */
	public int getTotalUnits() {
		return totalUnits;
	}

	/**
	 * Adds to the total number of units on the board.
	 *
	 * @param numberToAdd The number of units to add.
	 */
	public void addToTotalUnits(int numberToAdd) {
		this.totalUnits += numberToAdd;
	}

	/**
	 * Removes from the total number of units on the board.
	 *
	 * @param numberToRemove The number of units to remove.
	 */
	public void removeFromTotalUnits(int numberToRemove) {
		this.totalUnits -= numberToRemove;
	}

	/**
	 * Gets all units on the board (both human and AI units).
	 *
	 * @return A list of all units on the board.
	 */	public ArrayList<Unit> getUnits() {
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

	/**
	 * Gets the PlayerManager.
	 *
	 * @return The PlayerManager.
	 */
	public PlayerManager getPlayerManager() {
		return playerManager;
	}

	/**
	 * Gets the TurnManager.
	 *
	 * @return The TurnManager.
	 */
	public TurnManager getTurnManager() { return turnManager;}

	/**
	 * Gets the BoardManager.
	 *
	 * @return The BoardManager.
	 */
	public BoardManager getBoardManager() {
		return boardManager;
	}

	/**
	 * Gets the CombatHandler.
	 *
	 * @return The CombatHandler.
	 */
	public CombatHandler getCombatHandler() {
		return combatHandler;
	}

	/**
	 * Gets the UnitManager.
	 *
	 * @return The UnitManager.
	 */
	public UnitManager getUnitManager() {
		return unitManager;
	}

	/**
	 * Gets the AbilityHandler.
	 *
	 * @return The AbilityHandler.
	 */
	public AbilityHandler getAbilityHandler()
	{
		return abilityHandler;
	}

}

