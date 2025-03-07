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

		int tilex = message.get("tilex").asInt();
		int tiley = message.get("tiley").asInt();
		Tile tile = gameState.getBoard().getTile(tilex, tiley);

		// Check if there's an action in history
		if (!gameState.getActionHistory().isEmpty()) {
			GameAction lastAction = gameState.getActionHistory().peek();

			// Handle spell casting or unit interaction based on last action type
			if (lastAction instanceof Card && !((Card) lastAction).isCreature()) {
				handleSpellCasting(out, gameState, (Card) lastAction, tile);
			} else if (lastAction instanceof Unit) {
				handleUnitAction(gameState, (Unit) lastAction, tile);
			// Change this to instanceof CreatureCard in the future
			} else if (lastAction instanceof Card) {
				handleCardSummoning(gameState, (Card) lastAction, tile);
			}

			// Clear last action if it's not related to current tile interaction
			System.out.println("Popped " + gameState.getActionHistory().pop());;
		} else {
			// No prior action, check for unit on tile for possible movement or attack highlighting
			if (tile.isOccupied() && tile.getUnit().getOwner() == gameState.getCurrentPlayer()) {
				Unit unit = tile.getUnit();
				highlightUnitActions(gameState, unit, tile);
				gameState.getActionHistory().push(unit);
			}
		}
	}

	/**
	 * this will be used to handle the spelling logic
	 *
	 * @param out
	 * @param gameState
	 * @param card
	 * @param tile
	 */
	private void handleSpellCasting(ActorRef out, GameState gameState, Card card, Tile tile) {
	    // Check if player has sufficient mana for casting the spell
	    if (gameState.getHuman().getMana() < card.getManacost()) {
	        // Notify the player of insufficient mana
	        BasicCommands.addPlayer1Notification(out, "Mana reserves depleted!", 2);
	        gameState.gameManager.removeHighlightFromAll();
	        gameState.gameManager.notClickingCard();
	        return; // Exit the method early if mana is insufficient
	    }

	    // Call the method to remove the card from hand and cast the spell
	    gameState.gameManager.removeFromHandAndCast(gameState, card, tile);


	    // Remove highlight from all tiles

	}

	/**
	 * Process unit move or attack based on targetTile's state
	 * @param gameState
	 * @param unit
	 * @param targetTile
	 */
	private void handleUnitAction(GameState gameState, Unit unit, Tile targetTile) {
		// Early return if targetTile is null
		if (targetTile == null) {
			System.out.println("Target tile is null.");
			gameState.gameManager.removeHighlightFromAll();
			return;
		}
		AIPlayer ai = (AIPlayer) gameState.getAi();
		if (ai.stunnedUnit==unit) {
			System.out.println("Unit is stunned.");
			unit.setHasMoved(true);
			gameState.gameManager.removeHighlightFromAll();
			gameState.gameManager.stunnedUnit(unit.getName());
			return;
		}

		if (unit == null) {
			System.out.println("Unit is null.");
			gameState.gameManager.removeHighlightFromAll();
			return;
		}

		// Determine action based on tile's occupancy and highlight mode
		if (!targetTile.isOccupied()) {
			// Assuming all valid moves are already checked, directly move the unit
			gameState.gameManager.updateUnitPositionAndMove(unit, targetTile);
			System.out.println("Unit " + unit.getId() + " moved to " + targetTile.getTilex() + ", " + targetTile.getTiley());
		} else if (targetTile.getHighlightMode() == 2) {
			// Directly handle attack as validity should have been ensured beforehand
			System.out.println("Attacking unit on tile " + targetTile.getTilex() + ", " + targetTile.getTiley());
			Tile attackerTile = unit.getActiveTile(gameState.getBoard());

			if (gameState.gameManager.isAttackable(attackerTile, targetTile)) {
				// Attack adjacent unit
				if (targetTile.isOccupied()) {
					System.out.println("Target tile is occupied by " + targetTile.getUnit());
				}
				gameState.gameManager.attack(unit, targetTile.getUnit());
				unit.setHasAttacked(true);
				unit.setHasMoved(true);
			} else {
				// Move and attack
				if (targetTile.isOccupied()) {
					System.out.println("Target tile is occupied by " + targetTile.getUnit() + " and is attacked by " + unit);
				}
				gameState.gameManager.moveAndAttack(unit, targetTile.getUnit());
				unit.setHasAttacked(true);
				unit.setHasMoved(true);
			}

			// Remove highlight from all tiles after action
			gameState.gameManager.removeHighlightFromAll();
		}
	}

	// Place unit card on board if tile is valid
	private void handleCardSummoning(GameState gameState, Card card, Tile tile) {
		if (gameState.gameManager.isValidSummon(card, tile)) {
			gameState.gameManager.castCardFromHand(card, tile);
		} else {
			gameState.gameManager.removeHighlightFromAll();
		}
	}

	// Highlight valid moves and attacks for unit
	private void highlightUnitActions(GameState gameState, Unit unit, Tile tile) {
		// Clear all highlighted tiles
		gameState.gameManager.removeHighlightFromAll();

		// Highlight move and attack range based on unit's turn state
		if (!unit.hasAttacked() && !unit.hasMoved()) {
			gameState.gameManager.highlightValidMoves(unit);
		// Highlight attack range only, if unit has moved but not attacked
		} else if (unit.hasMoved()) {
			gameState.gameManager.highlightAttackRange(unit);
		}
	}
}
