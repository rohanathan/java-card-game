package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.*;
import structures.basic.cards.*;
import structures.basic.player.Hand;
import structures.basic.player.HumanPlayer;
import structures.basic.player.Player;
import structures.manager.*;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static utils.BasicObjectBuilders.loadUnit;

/**
 * The GameManager class is responsible for managing various aspects of the game logic and state.
 * It acts as a central service for managing core game mechanics and facilitating communication between the game state and the user interface.
 * This class provides a comprehensive set of methods to handle game functionalities including, but not limited to:
 * - Updating player health and mana 1
 * - Loading and setting up the game board and units 2
 * - Highlighting valid movement and attack ranges for units
 * - Performing unit movements and attacks
 * - Summoning units onto the game board
 * - Drawing cards from the player's deck
 * - Casting spells
 * - Handling various game events and interactions
 * Through these functionalities, the GameManager ensures the smooth execution of game rules and interactions,
 * thereby providing a seamless gaming experience.
 */
public class GameManager {
	
	private final ActorRef out;
	private final GameState gs;
	private final PlayerManager playerManager;
	private final BoardManager boardManager;
	private final CombatHandler combatHandler;
	private final UnitManager unitManager;
	private final AbilityHandler abilityHandler;

	public GameManager(ActorRef out, GameState gs, PlayerManager playerManager,BoardManager boardManager,CombatHandler combatHandler,UnitManager unitManager,AbilityHandler abilityHandler) {
		this.out = out;
		this.gs = gs;
		this.playerManager = playerManager; // Initialize PlayerManager
		this.boardManager=boardManager;
		this.combatHandler=combatHandler;
		this.unitManager=unitManager;
		this.abilityHandler=abilityHandler;
	}
}



	    
	    
	
