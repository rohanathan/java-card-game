package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import structures.GameState;
import structures.manager.*;
/**
 * Indicates that both the core game loop in the browser is starting, meaning
 * that it is ready to recieve commands from the back-end.
 * 
 * {
 * messageType = “initalize”
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Initalize implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		// Mark the game as initialized
		gameState.gameInitalised = true;

		// Create instances of all managers and handlers
		PlayerManager playerManager = new PlayerManager(out, gameState);
		BoardManager boardManager=new BoardManager(out, gameState);
		CombatHandler combatHandler=new CombatHandler(out, gameState);
		UnitManager unitManager=new UnitManager(out, gameState);
		AbilityHandler abilityHandler=new AbilityHandler(out, gameState);
		TurnManager turnManager = new TurnManager(out, gameState);

		// Initialize the game state with the created managers and handlers
		gameState.init(out, playerManager,boardManager,combatHandler,unitManager,abilityHandler, turnManager);
	}

}
