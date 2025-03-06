package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import structures.GameState;
import structures.services.*;
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

		gameState.gameInitalised = true;
		// Create PlayerService instance
		PlayerService playerService = new PlayerService(out); 

		// Initialise the game and pass services to GameState
		gameState.init(out, playerService);

	}

}
