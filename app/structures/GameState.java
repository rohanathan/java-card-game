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


	public boolean gameInitalised = false;
	public boolean isGameOver = false;
	private TurnManager turnManager;
	private PlayerManager playerManager; // Add PlayerManager reference
	private BoardManager boardManager;
	private CombatHandler combatHandler;
	private UnitManager unitManager;
	private AbilityHandler abilityHandler;
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

	public void init(ActorRef out, PlayerManager playerManager,BoardManager boardManager,CombatHandler combatHandler,UnitManager unitManager,AbilityHandler abilityHandler, TurnManager turnManager) {
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

		// Health initialised to 20
		playerManager.modifyPlayerHealth(human,20);
		playerManager.modifyPlayerHealth(ai,20);

		// Player mana initialised to 2
		playerManager.modifyPlayerMana(human, 2);

		// Create the human and AI avatars
		unitManager.initializeAvatar(board, human);
		unitManager.initializeAvatar(board, ai);

		// Set the current player to the human player
		this.currentPlayer = human;

		//Drawing initial 3 cards from the deck for the game start
		playerManager.drawCards(human,3);
		playerManager.drawCards(ai,3);
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

	public Player setCurrentPlayer(Player player) {
		return this.currentPlayer = player;
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

	public PlayerManager getPlayerManager() {
		return playerManager;
	}

	//Accesing the Turn Manager
	public TurnManager getTurnManager() {
		return turnManager;
	}

	//Accesing the Board Manager
	public BoardManager getBoardManager() {
		return boardManager;
	}
	public CombatHandler getCombatHandler() {
		return combatHandler;
	}
	public UnitManager getUnitManager() {
		return unitManager;
	}
	public AbilityHandler getAbilityHandler()
	{
		return abilityHandler;
	}

}

