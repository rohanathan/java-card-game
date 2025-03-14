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
			if (lastAction instanceof Card && !((Card) lastAction).isCreature()) {
				SpellCastingHandler(out, gameState, (Card) lastAction, tile);
			} else if (lastAction instanceof Unit) {
				UnitActionHandler(gameState, (Unit) lastAction, tile);
				// Change this to instanceof CreatureCard in the future
			} else if (lastAction instanceof Card) {
				CardSummoningHandler(gameState, (Card) lastAction, tile);
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
	 * Handles the logic for casting a spell.
	 *
	 * @param out       The ActorRef to send notifications to the player.
	 * @param gameState The current state of the game.
	 * @param card      The spell card being cast.
	 * @param tile      The target tile for the spell.
	 */
	private void SpellCastingHandler(ActorRef out, GameState gameState, Card card, Tile tile) {
	    // Check if player has sufficient mana for casting the spell
	    if (gameState.getHuman().getMana() < card.getManacost()) {
	        // Notify the player of insufficient mana
	        BasicCommands.addPlayer1Notification(out, "Mana reserves depleted!", 2);
	        gameState.getBoardManager().removeHighlightFromAll();
	        gameState.getPlayerManager().notClickingCard();
	        return; // Exit the method early if mana is insufficient
	    }

		// Cast the spell and update the player's hand
	    gameState.getAbilityHandler().castSpellAndUpdateHand(gameState, card, tile);
	}

	/**
	 * Process unit move or attack based on targetTile's state
	 * @param gameState
	 * @param unit
	 * @param targetTile
	 */
	private void UnitActionHandler(GameState gameState, Unit unit, Tile targetTile) {
		// Early return if targetTile is null
		if (targetTile == null) {
			System.out.println("Target tile is null.");
			gameState.getBoardManager().removeHighlightFromAll();
			return;
		}

		// Check if the unit is stunned (prevent actions)
		AIPlayer ai = (AIPlayer) gameState.getAi();
		if (ai.getAiManager().getStunnedUnit()==unit) {
			System.out.println("Unit is stunned.");
			unit.setHasMoved(true);
			gameState.getBoardManager().removeHighlightFromAll();
			gameState.getAbilityHandler().stunnedUnit(unit.getName());
			return;
		}

		// Early return if the unit is null
		if (unit == null) {
			System.out.println("Unit is null.");
			gameState.getBoardManager().removeHighlightFromAll();
			return;
		}

		// Determine action based on tile's occupancy and highlight mode
		if (!targetTile.isOccupied()) {
			// Assuming all valid moves are already checked, directly move the unit
			gameState.getBoardManager().updateUnitPositionAndMove(unit, targetTile);
			System.out.println("Unit " + unit.getId() + " moved to " + targetTile.getTilex() + ", " + targetTile.getTiley());
		} else if (targetTile.getHighlightMode() == 2) {
			// Directly handle attack as validity should have been ensured beforehand
			System.out.println("Attacking unit on tile " + targetTile.getTilex() + ", " + targetTile.getTiley());
			Tile attackerTile = unit.getActiveTile(gameState.getBoard());

			if (gameState.getCombatHandler().isWithinAttackRange(attackerTile, targetTile)) {
				// Attack adjacent unit
				if (targetTile.isOccupied()) {
					System.out.println("Target tile is occupied by " + targetTile.getUnit());
				}
				gameState.getCombatHandler().performAttack(unit, targetTile.getUnit());
				unit.setHasAttacked(true);
				unit.setHasMoved(true);
			} else {
				// Move and attack
				if (targetTile.isOccupied()) {
					System.out.println("Target tile is occupied by " + targetTile.getUnit() + " and is attacked by " + unit);
				}
				gameState.getCombatHandler().moveAndAttack(unit, targetTile.getUnit());
				unit.setHasAttacked(true);
				unit.setHasMoved(true);
			}

			// Remove highlight from all tiles after action
			gameState.getBoardManager().removeHighlightFromAll();
		}
	}

	/**
	 * Handles the summoning of a unit card onto the board.
	 *
	 * @param gameState The current state of the game.
	 * @param card      The card being summoned.
	 * @param tile      The target tile for the summon.
	 */
	private void CardSummoningHandler(GameState gameState, Card card, Tile tile) {
		if (gameState.getUnitManager().isValidSummon(card, tile)) {
			gameState.getAbilityHandler().castCardFromHand(card, tile);
		} else {
			gameState.getBoardManager().removeHighlightFromAll();
		}
	}

	/**
	 * Highlights valid moves and attacks for a unit.
	 *
	 * @param gameState The current state of the game.
	 * @param unit      The unit whose actions are being highlighted.
	 * @param tile      The tile the unit is currently on.
	 */	private void highlightUnitActions(GameState gameState, Unit unit, Tile tile) {
		// Clear all highlighted tiles
		gameState.getBoardManager().removeHighlightFromAll();

		// Highlight move and attack range based on unit's turn state
		if (!unit.hasAttacked() && !unit.hasMoved()) {
			gameState.getCombatHandler().highlightValidMoves(unit);
		// Highlight attack range only, if unit has moved but not attacked
		} else if (unit.hasMoved()) {
			gameState.getCombatHandler().highlightPotentialAttacks(unit);
		}
	}
}
