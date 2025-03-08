package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Unit;
import structures.basic.player.AIPlayer;

/**
 * Indicates that the user has clicked an object on the game canvas, in this case
 * the end-turn button.
 * 
 * { 
 *   messageType = “endTurnClicked”
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class EndTurnClicked implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		// Ends the current turn and prepares the game state for the next player
		gameState.endTurn();

		// Resets unit movement and attack statuses, and unhighlights all game board tiles
		gameState.getBoardManager().removeHighlightFromAll();
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 5; j++) {
				if (gameState.getBoard().getTile(i, j).isOccupied()) {
					Unit unit = gameState.getBoard().getTile(i, j).getUnit();
					unit.setHasMoved(false);
					unit.setHasAttacked(false);
				}
			}
		}

		// Clears the action history to remove records of the previous turn's actions
		gameState.getActionHistory().clear();

		// Initiates AI player's turn if the current player is an AI
		if (gameState.getCurrentPlayer() instanceof AIPlayer) {
			((AIPlayer) gameState.getCurrentPlayer()).takeTurn(out, message);
		}
	}

}
