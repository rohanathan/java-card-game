package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Unit;
import structures.basic.player.AIPlayer;
import structures.basic.Tile;

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
		// End the current turn and prepare the game state for the next player
		endCurrentTurn(gameState);

		// Reset unit movement and attack statuses, and unhighlight all tiles
		resetUnitsAndTiles(gameState);

		// Clear the action history to remove records of the previous turn's actions
		clearActionHistory(gameState);

		// If the current player is an AI, initiate the AI's turn
		initiateAITurn(out, gameState, message);
	}

	/**
	 * Ends the current turn and prepares the game state for the next player.
	 *
	 * @param gameState The current state of the game.
	 */
	private void endCurrentTurn(GameState gameState) {
		gameState.getTurnManager().endTurn();
	}

	/**
	 * Resets unit movement and attack statuses, and unhighlights all tiles on the board.
	 *
	 * @param gameState The current state of the game.
	 */
	private void resetUnitsAndTiles(GameState gameState) {
		// Unhighlight all tiles on the board
		gameState.getBoardManager().removeHighlightFromAll();

		// Iterate through all tiles on the board
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 5; j++) {
				Tile tile = gameState.getBoard().getTile(i, j);
				if (tile.isOccupied()) {
					Unit unit = tile.getUnit();
					// Reset the unit's movement and attack statuses
					unit.setHasMoved(false);
					unit.setHasAttacked(false);
				}
			}
		}
	}

	/**
	 * Clears the action history to remove records of the previous turn's actions.
	 *
	 * @param gameState The current state of the game.
	 */
	private void clearActionHistory(GameState gameState) {
		gameState.getActionHistory().clear();
	}

	/**
	 * Initiates the AI player's turn if the current player is an AI.
	 *
	 * @param out       The ActorRef for sending commands to the front-end.
	 * @param gameState The current state of the game.
	 * @param message   The JSON message containing event details.
	 */
	private void initiateAITurn(ActorRef out, GameState gameState, JsonNode message) {
		if (gameState.getCurrentPlayer() instanceof AIPlayer) {
			((AIPlayer) gameState.getCurrentPlayer()).takeTurn(out, message);
		}
	}
}
