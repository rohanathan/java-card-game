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

	public GameManager(ActorRef out, GameState gs, PlayerManager playerManager,BoardManager boardManager,CombatHandler combatHandler) {
		this.out = out;
		this.gs = gs;
		this.playerManager = playerManager; // Initialize PlayerManager
		this.boardManager=boardManager;
		this.combatHandler=combatHandler;

	}

	

	// // initial player setup
	// public void initializeAvatar(Board board, Player player) {
	// 	// check if player is human or AI
	// 	Tile avatarTile;
	// 	Unit avatar;
	// 	if (player instanceof HumanPlayer) {
	// 		avatarTile = board.getTile(1, 2);
	// 		avatar = BasicObjectBuilders.loadUnit(StaticConfFiles.humanAvatar, 0, Unit.class);
	// 		avatar.setName("Player Avatar");

	// 	} else {
	// 		avatarTile = board.getTile(7, 2);
	// 		avatar = BasicObjectBuilders.loadUnit(StaticConfFiles.aiAvatar, 1, Unit.class);
	// 		avatar.setName("AI Avatar");
	// 	}
	// 	avatar.setPositionByTile(avatarTile);
	// 	avatarTile.setUnit(avatar);
	// 	BasicCommands.drawUnit(out, avatar, avatarTile);
	// 	avatar.setOwner(player);
	// 	player.setAvatar(avatar);
	// 	player.addUnit(avatar);
	// 	gs.addToTotalUnits(1);
	// 	modifyUnitHealth(avatar, 20);
	// 	updateUnitAttack(avatar, 2);
	// 	avatar.setHealth(20);
	// 	avatar.setMaxHealth(20);
	// 	avatar.setAttack(2);
	// 	try {
	// 		Thread.sleep(30);
	// 	} catch (InterruptedException e) {
	// 		e.printStackTrace();
	// 	}
	// 	BasicCommands.setUnitHealth(out, avatar, 20);
	// 	try {
	// 		Thread.sleep(30);
	// 	} catch (InterruptedException e) {
	// 		e.printStackTrace();
	// 	}
	// 	BasicCommands.setUnitAttack(out, avatar, 2);
	// }

	// // Method to update the health of a unit on the board
	// public void modifyUnitHealth(Unit unit, int newHealth) {

	// 	if (newHealth > 20) {
	// 		newHealth = 20;
	// 	} else if (newHealth < 0) {
	// 		newHealth = 0;
	// 	}

	// 	if (unit.getName().equals("Player Avatar") &&
	// 			unit.getHealth() > newHealth && gs.getHuman().getRobustness() > 0){
	// 		gs.getHuman().setRobustness(gs.getHuman().getRobustness()-1);
	// 	    BasicCommands.addPlayer1Notification(out, "Avatar robustness left: " + gs.getHuman().getRobustness(), 4);

	// 	}
		
	// 	try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}
	// 	if (newHealth == 0) {
	// 		destroyUnit(unit);
	// 		return;
	// 	}
	// 	if (unit.getHealth() > newHealth && unit.getName().equals("AI Avatar")) {
	// 		zeal();
	// 	}
	// 	unit.setHealth(newHealth);
	// 	BasicCommands.setUnitHealth(out, unit, newHealth);
	// 	if (unit.getName().equals("Player Avatar") || unit.getName().equals("AI Avatar")) {
	// 		playerManager.modifyPlayerHealth(unit.getOwner(), newHealth);
	// 	}
	// }

	// Update a unit's attack on the board
	public void updateUnitAttack(Unit unit, int newAttack) {
		if (newAttack <= 0) {
			return;
		} else if (newAttack > 20) {
			newAttack = 20;
		}

		try {
			Thread.sleep(30);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		unit.setAttack(newAttack);
		BasicCommands.setUnitAttack(out, unit, newAttack);
	}

	// Method to cause a unit to die and manage the consequences
	public void destroyUnit(Unit unit) {
		
		if(unit.getId()<999 && !unit.getName().equals("Player Avatar")  && !unit.getName().equals("AI Avatar")) {
			if (!unit.getName().equals("Shadowdancer")) {
				ShadowDancer.Deathwatch(gs, out);
			}
			if (!unit.getName().equals("Bloodmoon Priestess")) {
				BloodmoonPriestess.BloodmoonPriestessDeathwatch(out, gs, this);
			}
			if (!unit.getName().equals("Shadow Watcher")) {
				ShadowWatcher.ShadowWatcherDeathwatch(out, gs, this);
			}
			if (!unit.getName().equals("Bad Omen")) {
				BadOmen.BadOmenDeathwatch(out, gs, this, unit);
			}
		}

		// remove unit from board
		unit.getActiveTile(gs.getBoard()).removeUnit();
		unit.setHealth(0);
		Player owner = unit.getOwner();
		owner.removeUnit(unit);
		unit.setOwner(null);
		gs.removeFromTotalUnits(1);
		System.out.println("unit removed from totalunits");
		BasicCommands.playUnitAnimation(out, unit, UnitAnimationType.death);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BasicCommands.deleteUnit(out, unit);


		if (unit.getName().equals("Player Avatar") || unit.getName().equals("AI Avatar")) {
			playerManager.modifyPlayerHealth(owner, 0);
		}

	}


	// // Highlight tiles for movement and attack
	public void highlightValidMoves(Unit unit) {
	    Tile[][] tiles = gs.getBoard().getTiles();
	    Set<Tile> validMoveTiles = boardManager.getValidMoves(tiles, unit);
	    Set<Tile> validAttackTiles = gs.getCombatHandler().findAttackTargets(unit);

	    // Highlight valid movement and attack tiles
	    if (validMoveTiles != null) {
	        for (Tile tile : validMoveTiles) {
	            if (!tile.isOccupied()) {
	                // Highlight tile for movement
	                boardManager.updateTileHighlight(tile, 1);
					// Highlight additional attack tiles if adjacent to valid movement tiles
					boardManager.performHighlightAdjacentAttackTiles(tile, unit);
	            }
	        }
	    }

	    // Highlight valid attack tiles
	    for (Tile tile : validAttackTiles) {
	        if (tile.isOccupied() && tile.getUnit().getOwner() != unit.getOwner()) {
	            // Highlight tile for attack
	            boardManager.updateTileHighlight(tile, 2);
	        }
	    }
	}

	

	// Returns the set of valid attack targets for a given unit
	public Set<Tile> getValidTargets(Unit unit, Player opponent) {
		Set<Tile> validAttacks = new HashSet<>();
		Set<Position> provokers = gs.getCombatHandler().checkProvoker(unit.getActiveTile(gs.getBoard()));
		Tile unitTile = unit.getActiveTile(gs.getBoard());

		// Attack adjacent units if there are any
		if (!provokers.isEmpty()) {
			for (Position position : provokers) {
				System.out.println(position + "provoker position");
				Tile provokerTile = gs.getBoard().getTile(position.getTilex(), position.getTiley());
				validAttacks.add(provokerTile);
			}
			return validAttacks;
		}

		opponent.getUnits().stream()
				.map(opponentUnit -> opponentUnit.getActiveTile(gs.getBoard()))
				.filter(opponentTile -> isAttackable(unitTile, opponentTile))
				.forEach(validAttacks::add);

		return validAttacks;
	}

	// Boolean method to check if a unit is within attack range of another unit
	public boolean isAttackable(Tile unitTile, Tile targetTile) {
		int dx = Math.abs(unitTile.getTilex() - targetTile.getTilex());
		int dy = Math.abs(unitTile.getTiley() - targetTile.getTiley());
		return dx < 2 && dy < 2;
	}

		

	// // highlight tiles for summoning units (does not currently take into account special units)
	public void highlightSpellRange(Card card, Player player) {
		// Validate inputs
		if (card == null  || player == null) {
			System.out.println("Invalid parameters for highlighting summon range.");
			return;
		}
		if (!card.isCreature()) {
			Set<Tile> validCastTiles = boardManager.getSpellRange(card);
			
			if (card.getCardname().equals("Horn of the Forsaken")) {
				boardManager.updateTileHighlight(gs.getHuman().getAvatar().getActiveTile(gs.getBoard()), 1);
				BasicCommands.addPlayer1Notification(out, "Click on the Avatar to apply the spell", 2);
				return;
			}
			if (card.getCardname().equals("Dark Terminus")) {
				validCastTiles.forEach(tile -> boardManager.updateTileHighlight(tile, 2));
				return;
			}
			if (card.getCardname().equals("Wraithling Swarm")) {
				validCastTiles.forEach(tile -> boardManager.updateTileHighlight(tile, 1));
				BasicCommands.addPlayer1Notification(out, "You have 3 wraithlings to place", 2);
				return;
			}
		}

		System.out.println("Highlighting spellrange " + card.getCardname());
	}

	

	// check if summoning is valid
	public boolean isValidSummon(Card card, Tile tile) {
		// depending on cards, this may change
		// for now, all cards can move to tiles highlighted white
		System.out.println("isValidSummon: " + tile.getHighlightMode());
		return tile.getHighlightMode() == 1;
	}

	




    // remove card from hand and summon unit
    public void castCardFromHand(Card card, Tile tile) {
    	System.out.println("Removing card from hand and summoning" + card.getCardname());

		Player player = gs.getCurrentPlayer();
		Hand hand = player.getHand();
		int handPosition = gs.getCurrentCardPosition();

		if (handPosition == 0) {
			for (int i = 1; i <= hand.getNumberOfCardsInHand(); i++) {
				if (hand.getCardAtPosition(i).equals(card)) {
					handPosition = i;
					break;
				}
			}
		}
		System.out.println("Current card: " + card.getCardname() + " position " + handPosition);

		// check if enough mana
		if (player.getMana() < card.getManacost()) {
			BasicCommands.addPlayer1Notification(out, "Summoning " + card.getCardname() + " requires more mana!", 2);
			return;
		}

		// update player mana
		playerManager.modifyPlayerMana(player, player.getMana() - card.getManacost());

		// update the positions of the remaining cards if the player is human
		if (player instanceof HumanPlayer) {
			// remove card from hand and delete from UI
			BasicCommands.deleteCard(out, handPosition + 1);
			hand.removeCardAtPosition(handPosition);
			updateHandPositions(hand);
		} else {
			hand.removeCardAtPosition(handPosition);
		}
		if (card.isCreature()) {
			// get unit config and id
			String unit_conf = card.getUnitConfig();
			int unit_id = card.getId();
			summonUnit(unit_conf, unit_id, card, tile, player);
		}
    }

	// Method to summon a unit to the board
	public void summonUnit(String unit_conf, int unit_id, Card card, Tile tile, Player player) {

		// load unit
		Unit unit = loadUnit(unit_conf, unit_id, Unit.class);

		// set unit position
		tile.setUnit(unit);
		unit.setPositionByTile(tile);
		unit.setOwner(player);
		unit.setName(card.getCardname());
		player.addUnit(unit);
		gs.addToTotalUnits(1);
		gs.addUnitstoBoard(unit);
		System.out.println("Unit added to board: " + ( gs.getTotalUnits()));
		// remove highlight from all tiles
		boardManager.removeHighlightFromAll();

		// draw unit on new tile and play summon animation
		EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_summon);
		BasicCommands.playEffectAnimation(out, effect, tile);
		BasicCommands.drawUnit(out, unit, tile);


		// use BigCard data to update unit health and attack
		BigCard bigCard = card.getBigCard();
		modifyUnitHealth(unit, bigCard.getHealth());
		updateUnitAttack(unit, bigCard.getAttack());
		unit.setMaxHealth(bigCard.getHealth());
		if (!unit.getName().equals("Saberspine Tiger")) {
			unit.setHasMoved(true);
			unit.setHasAttacked(true);
		} else {
			BasicCommands.addPlayer1Notification(out, "Rush activated for Saberspine Tiger!", 3);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (card.getCardname().equals("Gloom Chaser")) {
		Wraithling.summonGloomChaserWraithling(tile, out, gs);}
		
		if (card.getCardname().equals("Nightsorrow Assassin")) {
		Nightsorrow.assassin(tile, gs, out);}
		
		if (card.getCardname().equals("Silverguard Squire")) {
			Elixir.silverguardSquire(out, gs);
			
		}
		gs.addUnitstoBoard(unit);

		// wait for animation to play out
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


		System.out.println("Summoning unit " + unit + " to tile " + tile.getTilex() + ", " + tile.getTiley());
	}

	// Method to add highlight to card and set it as clicked
	public void highlightSelectedCard(int handPosition) {
		notClickingCard();
		Card card = gs.getCurrentPlayer().getHand().getCardAtPosition(handPosition);
		gs.setCurrentCardPosition(handPosition);
		gs.setActiveCard(card);
		BasicCommands.drawCard(out, card, handPosition,1);
	}

	// Method to remove the current card clicked and stop highlighting
	public void notClickingCard() {
		gs.setActiveCard(null);
		gs.setCurrentCardPosition(0);

		for (int i = 1; i <= gs.getHuman().getHand().getNumberOfCardsInHand(); i++) {
			Card card = gs.getHuman().getHand().getCardAtPosition(i);
			BasicCommands.drawCard(out, card, i, 0);
		}
	}

	// Method for updating hand positions following a card removal
	public void updateHandPositions(Hand hand) {
		if (hand.getNumberOfCardsInHand() == 0) {
			BasicCommands.deleteCard(out, 1);
		}

		// Iterate over the remaining cards in the hand
		for (int i = 0; i < hand.getNumberOfCardsInHand(); i++) {
			// Draw each card in its new position, positions are usually 1-indexed on the UI
			BasicCommands.deleteCard(out, i + 2);
			BasicCommands.drawCard(out, hand.getCardAtIndex(i), i + 1, 0);
		}
	}

	// Method for casting the Horn of the Forsaken spell
	public void HornOfTheForesaken(Card card) {
        EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.something);
        BasicCommands.playEffectAnimation(out, effect, gs.getHuman().getAvatar().getActiveTile(gs.getBoard()));
        notClickingCard();
        BasicCommands.addPlayer1Notification(out, "You gained +3 robustness!", 2);
        boardManager.removeHighlightFromAll();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

	// Method for casting the Zeal spell
    public void zeal() {
		for (Unit unit : gs.getAi().getUnits()) {
			if (unit.getName().equals("Silverguard Knight")) {
				int newAttack = unit.getAttack() + 2;
				updateUnitAttack(unit, newAttack);
				EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_buff);
				BasicCommands.playEffectAnimation(out, effect, unit.getActiveTile(gs.getBoard()));
				BasicCommands.addPlayer1Notification(out, "Silverguard Knight's zeal!", 3);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}


	// Method for casting the Wraithling Swarm spell
	public void WraithlingSwarm(Card card, Tile tile) {
		// Number of Wraithlings to summon
		Wraithling.summonWraithlingToTile(tile, out, gs);
		HumanPlayer player = (HumanPlayer) gs.getHuman();
		player.setWraithlingSwarmCounter(player.getWraithlingSwarmCounter() - 1);

		// If there are more Wraithlings to summon, push the card to action history
		if (player.getWraithlingSwarmCounter() > 0) {
			// Highlight tiles for summoning
			highlightSpellRange(card, gs.getCurrentPlayer());
			BasicCommands.addPlayer1Notification(out, "You can summon " + player.getWraithlingSwarmCounter() +" more wraithlings", 5);
			gs.getActionHistory().push(card);
			playerManager.modifyPlayerMana(gs.getCurrentPlayer(), gs.getCurrentPlayer().getMana() + card.getManacost());
		} else {
			// Remove highlight from all tiles and update hand positions
			BasicCommands.addPlayer1Notification(out, "All wraithlings summoned!", 5);
			player.setWraithlingSwarmCounter(3);
		}
	}

	// Method in GameManager class
	public void removeFromHandAndCast( GameState gameState, Card card, Tile tile) {
		
		if (gameState.getCurrentPlayer() instanceof HumanPlayer &&
                !validCast(card, tile)) {
			boardManager.removeHighlightFromAll();
			return;
		}
		// Remove the card from the player's hand
		Player player = gs.getCurrentPlayer();
		Hand hand = player.getHand();
		int handPosition = gs.getCurrentCardPosition();

		if (handPosition == 0) {
			for (int i = 1; i <= hand.getNumberOfCardsInHand(); i++) {
				if (hand.getCardAtPosition(i).equals(card)) {
					handPosition = i;
					break;
				}
			}
		}

	    // Remove the card from the player's hand
		// update the positions of the remaining cards if the player is human
		if (player instanceof HumanPlayer) {
			// remove card from hand and delete from UI
			BasicCommands.deleteCard(out, handPosition + 1);
			hand.removeCardAtPosition(handPosition);
			updateHandPositions(hand);
	        notClickingCard();

		} else {
			hand.removeCardAtPosition(handPosition);
		}

	    // Perform actions based on the card type
	    if (card.getCardname().equals("Horn of the Forsaken")) {
	        HornOfTheForesaken(card);
	        // Increase player's robustness after casting the spell
	        gameState.getCurrentPlayer().setRobustness(player.getRobustness() + 3);
	        System.out.println("Player's robustness: " + player.getRobustness());
	    } 
	    if (card.getCardname().equals("Dark Terminus")) {
	        // Check if the targeted tile contains an enemy unit
	        if (tile.getUnit().getOwner() != gameState.getHuman() &&
	                !tile.getUnit().getName().equals("AI Avatar")) {
	        	
	            destroyUnit(tile.getUnit());
	    try {Thread.sleep(100); } catch (InterruptedException e) {e.printStackTrace();}
	            Wraithling.summonWraithlingToTile(tile, out, gameState);
	        }
	    }
	    if (card.getCardname().equals("Wraithling Swarm")) {
	        WraithlingSwarm(card, tile);
	    }
	    if (card.getCardname().equals("Beamshock")) {
	    	BeamShock.stunUnit(gameState);
	    }
	    if (card.getCardname().equals("Truestrike")) {
	        Strike.TrueStrike(gameState, gameState.getHuman().getAvatar(), out);
	    }
	    if (card.getCardname().equals("Sundrop Elixir")) {
	        Elixir.Sundrop(gameState.getAi().getAvatar(), gameState, out);
	    }
	    
	    
	    
	    // Decrease player's mana after casting the spell
	    gameState.getHuman().setMana(player.getMana() - card.getManacost());
	    playerManager.modifyPlayerMana(player, player.getMana());
	}

	// Ensure that the player has selected a valid tile for casting the spell
	private boolean validCast(Card card, Tile tile) {
		if (card.getCardname().equals("Horn of the Forsaken")&&
				!(tile.getHighlightMode() == 1)) {
			return false;
		}
		if (card.getCardname().equals("Dark Terminus") 
				&& !(tile.getHighlightMode() == 2)) {
			return false;
		}
		if (card.getCardname().equals("Wraithling Swarm")
				&& !(tile.getHighlightMode() == 1)) {
			HumanPlayer player = (HumanPlayer) gs.getHuman();

			// Reset the Wraithling Swarm counter and decrease mana as swarm counter < 3 indicates
			// that the player has started but not finished summoning all 3 Wraithlings
			if (player.getWraithlingSwarmCounter() < 3) {
				BasicCommands.addPlayer1Notification(out, "Wraithling Swarm spell broken! Choose another tile!", 3);

				player.setWraithlingSwarmCounter(3);
				// Decrease player's mana after casting the spell
				gs.getHuman().setMana(player.getMana() - card.getManacost());
				playerManager.modifyPlayerMana(player, player.getMana());
			}
			return false;
		}
		return true;
	}
	

	public void stunnedUnit(String name) {
		BasicCommands.addPlayer1Notification(out, name +" is stunned", 2);
	}
	
	public void stunning(Tile tile) {
        EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_martyrdom);
        BasicCommands.playEffectAnimation(out, effect, tile);
        BasicCommands.addPlayer1Notification(out, "Beamshock! " + tile.getUnit().getName() + " is stunned.", 3);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void healing(Tile currentTile) {
        EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_buff);
        BasicCommands.playEffectAnimation(out, effect, currentTile);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
		public void strike(Tile tile) {

        EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_inmolation);
        BasicCommands.playEffectAnimation(out, effect, tile);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}

	    
	    
	
