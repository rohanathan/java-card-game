package events;


import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.GameAction;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.cards.BeamShock;
import structures.basic.cards.Card;
import structures.basic.player.AIPlayer;
import structures.basic.player.Player;
import structures.manager.*;
/**
 * Indicates that the user has clicked an object on the game canvas, in this case a tile.
 * The event returns the x (horizontal) and y (vertical) indices of the tile that was
 * clicked. Tile indices start at 1.
 *
 * {
 *   messageType = “tileClicked”
 *   tilex = <x index of the tile>
 *   tiley = <y index of the tile>
 * }
 *
 * @author Dr. Richard McCreadie
 *
 */
public class TileClicked implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		// Ignore events when the AI is taking its turn
		if (gameState.getCurrentPlayer().equals(gameState.getAi())) {
			return;
		}

		// Extract tile coordinates from the message
		int tilex = message.get("tilex").asInt();
		int tiley = message.get("tiley").asInt();
		Tile tile = gameState.getBoard().getTile(tilex, tiley);

		// Check if there's a previous action in the action history
		if (!gameState.getActionHistory().isEmpty()) {
			GameAction lastAction = gameState.getActionHistory().peek();

			// Handle spell casting or unit interaction based on last action type
			processLastAction(out, gameState, lastAction, tile);

			// Clear last action if it's not related to current tile interaction
			System.out.println("Popped " + gameState.getActionHistory().pop());;
		} else {
			// No prior action, check for unit on tile for possible movement or attack highlighting
			processUnitHighlighting(gameState, tile);
		}
	}

	/**
	 * Processes the last action in the history stack.
	 *
	 * @param out        The ActorRef for sending commands to the front-end.
	 * @param gameState  The current state of the game.
	 * @param lastAction The last action performed by the player.
	 * @param tile       The tile that was clicked.
	 */
	private void processLastAction(ActorRef out, GameState gameState, GameAction lastAction, Tile tile) {
		GameActionState state = StateFactory.getStateForAction(lastAction);
		// If a valid state is found, handle the action
		if (state != null) {
			state.handleAction(out, gameState, tile);
		} else {
			System.out.println("No valid state found for the last action.");
		}

	}

	/**
	 * Highlights valid moves and attacks for a unit.
	 *
	 * @param gameState The current state of the game.
	 * @param tile      The tile where the unit is located.
	 */
	private void processUnitHighlighting(GameState gameState, Tile tile) {
		if (tile.isOccupied() && tile.getUnit().getOwner() == gameState.getCurrentPlayer()) {
			Unit unit = tile.getUnit();
			gameState.getBoardManager().removeHighlightFromAll();
			if (!unit.hasAttacked() && !unit.hasMoved()) {
				gameState.getCombatHandler().highlightValidMoves(unit);
			} else if (unit.hasMoved()) {
				gameState.getCombatHandler().highlightPotentialAttacks(unit);
			}
			gameState.getActionHistory().push(unit);
		}
	}
}
