package events;

import com.fasterxml.jackson.databind.JsonNode;ç
import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;

/**
 * In the user’s browser, the game is running in an infinite loop, where there
 * is around a 1 second delay between each loop. Its during each loop that the
 * UI acts on the commands that have been sent to it. A heartbeat event is fired
 * at the end of each loop iteration. As with all events this is received by the
 * Game Actor, which you can use to trigger game logic.
 * 
 * { String messageType = “heartbeat” }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Heartbeat implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		// Notify players of important events (e.g., low health, new objectives)
		notifyPlayers(out, gameState);
	}

	/**
	 * Notifies players of important events (e.g., low health, new objectives).
	 *
	 * @param out       The ActorRef for sending commands to the front-end.
	 * @param gameState The current state of the game.
	 */
	private void notifyPlayers(ActorRef out, GameState gameState) {
		// Notify players of low health
		if (gameState.getHuman().getHealth() <= 5) {
			BasicCommands.addPlayer1Notification(out, "Warning: Low Health!", 2);
		}
	}
}
