package structures.manager;


import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.BigCard;
import structures.basic.Board;
import structures.basic.EffectAnimation;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import structures.basic.player.HumanPlayer;
import structures.basic.Position;
import structures.basic.player.Player;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;
import structures.basic.cards.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static utils.BasicObjectBuilders.loadUnit;
public class UnitManager {
    

     private final ActorRef out;
    private final GameState gameState;
    
    public UnitManager(ActorRef out, GameState gameState) 
    {
        this.out = out;
        // Store reference to GameState
        this.gameState = gameState; 

    }

/**
 * Sets up the avatar for a given player on the specified board, initializing
 * its position, health, and attack stats, then rendering it on the UI.
 *
 * If the player is a HumanPlayer, the avatar spawns at (1,2); otherwise,
 * it spawns at (7,2). This method also applies default values (20 health, 
 * 2 attack) and updates the UI to reflect the new avatar.
 *
 * @param board  The Board instance where the avatar is placed.
 * @param player The player who will control this avatar.
 */
public void initializeAvatar(Board board, Player player) {
    // Choose spawn coordinates based on whether it's Human or AI
    Tile spawnTile;
    Unit avatarUnit;
    if (player instanceof HumanPlayer) {
        spawnTile = board.getTile(1, 2);
        avatarUnit = BasicObjectBuilders.loadUnit(StaticConfFiles.humanAvatar, 0, Unit.class);
        avatarUnit.setName("Player Avatar");
    } else {
        spawnTile = board.getTile(7, 2);
        avatarUnit = BasicObjectBuilders.loadUnit(StaticConfFiles.aiAvatar, 1, Unit.class);
        avatarUnit.setName("AI Avatar");
    }

    // Position the avatar on the tile
    avatarUnit.setPositionByTile(spawnTile);
    spawnTile.setUnit(avatarUnit);

    // Render the avatar on the UI
    BasicCommands.drawUnit(out, avatarUnit, spawnTile);

    // Establish ownership and add the avatar to the player's unit list
    avatarUnit.setOwner(player);
    player.setAvatar(avatarUnit);
    player.addUnit(avatarUnit);

    // Increase total unit count in GameState
    gameState.addToTotalUnits(1);

    // Apply initial stats (health & attack)
    modifyUnitHealth(avatarUnit, 20);   // sets health in range [0..20], triggers UI updates
    updateUnitAttack(avatarUnit, 2);    // sets attack, triggers UI updates

    // Set the same values locally as well
    avatarUnit.setHealth(20);
    avatarUnit.updateMaxHealth(20);
    avatarUnit.setAttack(2);

    // Small UI delay for smoothing animation
    try {
        Thread.sleep(30);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        System.err.println("Avatar initialization interrupted: " + e.getMessage());
    }

    // Update UI for health after the short delay
    BasicCommands.setUnitHealth(out, avatarUnit, 20);

    // Another small UI delay
    try {
        Thread.sleep(30);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        System.err.println("Avatar health UI update interrupted: " + e.getMessage());
    }

    // Finally, update the avatar's attack stat in the UI
    BasicCommands.setUnitAttack(out, avatarUnit, 2);
}

/**
 * Adjusts a unit's health value on the board, applying any necessary
 * special rules or animations. This includes handling avatar robustness,
 * triggering Zeal for the AI Avatar, and clamping health within [0..20].
 *
 * If the new health reaches zero, the unit is removed from the board
 * and appropriate death animations are played. Additionally, if the unit
 * is an avatar, the player's health is updated accordingly.
 *
 * @param targetUnit  The unit whose health is being changed.
 * @param desiredHealth The new health value to apply.
 */
public void modifyUnitHealth(Unit targetUnit, int desiredHealth) {
    // Constrain health within [0..20]
    if (desiredHealth > 20) {
        desiredHealth = 20;
    } else if (desiredHealth < 0) {
        desiredHealth = 0;
    }

    // Handle player's avatar robustness
    if ("Player Avatar".equals(targetUnit.getName())
        && targetUnit.getHealth() > desiredHealth
        && gameState.getHuman().getRobustness() > 0) 
    {
        int newRobustness = gameState.getHuman().getRobustness() - 1;
        gameState.getHuman().setRobustness(newRobustness);
        BasicCommands.addPlayer1Notification(
            out,
            "Avatar robustness left: " + newRobustness,
            4
        );
    }
    
    // Short delay for smoother animation
    try {
        Thread.sleep(30);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }

    // If health is zero, destroy the unit
    if (desiredHealth == 0) {
        destroyUnit(targetUnit);
        return;
    }

    // If the AI Avatar is being damaged, trigger Zeal
    if (targetUnit.getHealth() > desiredHealth
        && "AI Avatar".equals(targetUnit.getName())) {
        zeal();
    }

    // Update the unit's health in-game and on UI
    targetUnit.setHealth(desiredHealth);
    BasicCommands.setUnitHealth(out, targetUnit, desiredHealth);

    // If it's an avatar, also adjust the player's health
    if ("Player Avatar".equals(targetUnit.getName()) 
        || "AI Avatar".equals(targetUnit.getName())) 
    {
        gameState.getPlayerManager().modifyPlayerHealth(targetUnit.getOwner(), desiredHealth);
    }
}
/**
 * Adjusts a unit's attack power, clamping it within a safe range and updating
 * the UI to reflect the changes. A brief delay is used to ensure animation
 * synchronization.
 *
 * @param Unit  The unit whose attack power is being updated.
 * @param newAttack The new attack value to assign.
 */
	public void updateUnitAttack(Unit unit, int newAttack) {
		// Reject negative or zero attack; clamp very high values

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

/**
 * Removes a unit from the board and processes any associated death effects
 * such as triggering Deathwatch abilities. If the unit is an avatar, it also
 * updates the owning player's health to zero, effectively ending the game.
 *
 * @param unit The unit to be removed from the board.
 */
	public void destroyUnit(Unit unit) {
		
		if(unit.getId()<999 && !unit.getName().equals("Player Avatar")  && !unit.getName().equals("AI Avatar")) {
			if (!unit.getName().equals("Shadowdancer")) {
				ShadowDancer.Deathwatch(gameState, out);
			}
			if (!unit.getName().equals("Bloodmoon Priestess")) {
				BloodmoonPriestess.BloodmoonPriestessDeathwatch(out, gameState);
			}
			if (!unit.getName().equals("Shadow Watcher")) {
				ShadowWatcher.ShadowWatcherDeathwatch(out, gameState);
			}
			if (!unit.getName().equals("Bad Omen")) {
				BadOmen.BadOmenDeathwatch(out, gameState, unit);
			}
		}

    
         // remove unit from board
	    	unit.getActiveTile(gameState.getBoard()).removeUnit();
	    	unit.setHealth(0);
	    	Player owner = unit.getOwner();
	    	owner.removeUnit(unit);
	    	unit.setOwner(null);
	    	gameState.removeFromTotalUnits(1);
	    	System.out.println("unit removed from totalunits");
	    	BasicCommands.playUnitAnimation(out, unit, UnitAnimationType.death);
	    	try {
	    		Thread.sleep(2000);
	    	} catch (InterruptedException e) {
	    		e.printStackTrace();
	    	}
	    	BasicCommands.deleteUnit(out, unit);


    		if (unit.getName().equals("Player Avatar") || unit.getName().equals("AI Avatar")) {
    			gameState.getPlayerManager().modifyPlayerHealth(owner, 0);
    		}

    	}
    
/**
 * Places a new unit onto the board at a specified tile, initializes 
 * its stats based on the card data, and triggers any relevant animations.
 * 
 * This method also handles setting the unit's movement/attack state 
 * and summoning additional units if certain card abilities (like Gloom Chaser)
 * are triggered.
 *
 * @param unitConfig  The file path or config used to load the unit's base data.
 * @param unit_id      A unique identifier for the newly created unit.
 * @param card  The card whose attributes define the summoned unit's stats.
 * @param tile  The tile where the new unit is placed.
 * @param player The player who controls this unit.
 */
	public void summonUnit(String unitConfig, int unit_id, Card card, Tile tile, Player player) {

		// load unit
		Unit unit = loadUnit(unitConfig, unit_id, Unit.class);

		// set unit position
		tile.setUnit(unit);
		unit.setPositionByTile(tile);
		unit.setOwner(player);
		unit.setName(card.getCardname());
		player.addUnit(unit);
		gameState.addToTotalUnits(1);
		gameState.addUnitstoBoard(unit);
		System.out.println("Unit added to board: " + ( gameState.getTotalUnits()));
		// remove highlight from all tiles
		gameState.getBoardManager().removeHighlightFromAll();

		// draw unit on new tile and play summon animation
		EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_summon);
		BasicCommands.playEffectAnimation(out, effect, tile);
		BasicCommands.drawUnit(out, unit, tile);


		// use BigCard data to update unit health and attack
		BigCard bigCard = card.getBigCard();
		modifyUnitHealth(unit, bigCard.getHealth());
		updateUnitAttack(unit, bigCard.getAttack());
		unit.updateMaxHealth(bigCard.getHealth());
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
		Wraithling.summonGloomChaserWraithling(tile, out, gameState);}
		
		if (card.getCardname().equals("Nightsorrow Assassin")) {
		Nightsorrow.assassin(tile, gameState, out);}
		
		if (card.getCardname().equals("Silverguard Squire")) {
			Elixir.silverguardSquire(out, gameState);
			
		}
		gameState.addUnitstoBoard(unit);

		// wait for animation to play out
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


		System.out.println("Summoning unit " + unit + " to tile " + tile.getTilex() + ", " + tile.getTiley());
	}

/**
 * Applies the Zeal effect to all "Silverguard Knight" units owned by the AI player,
 * increasing their attack value and showcasing a buff animation. A brief delay is
 * introduced to ensure the animation can be displayed properly.
 */
public void zeal() {
    // Iterate over every unit the AI owns
    for (Unit knightCandidate : gameState.getAi().getUnits()) {
        if ("Silverguard Knight".equals(knightCandidate.getName())) {
            // Increase its attack by +2
            int updatedAttack = knightCandidate.getAttack() + 2;
            updateUnitAttack(knightCandidate, updatedAttack);

            // Play the buff effect animation
            EffectAnimation buffEffect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_buff);
            BasicCommands.playEffectAnimation(out, buffEffect, knightCandidate.getActiveTile(gameState.getBoard()));

            // Notify the UI about the Zeal effect
            BasicCommands.addPlayer1Notification(out, "Silverguard Knight's zeal!", 3);

            // Short sleep to let the animation complete
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Zeal animation interrupted: " + e.getMessage());
            }
        }
    }
}
/**
 * Determines if a unit can be summoned on the given tile.
 *
 * Currently, summoning is only valid if the tile's highlight mode is set to 1.
 * This may change in future implementations where different cards have specific summoning rules.
 *
 * @param card The card representing the unit to be summoned.
 * @param targetTile The tile where the unit is being summoned.
 * @return {@code true} if summoning is allowed on this tile, otherwise {@code false}.
 */
	public boolean isValidSummon(Card card, Tile targetTile) {
    	// Debugging output to check highlight mode
    	System.out.println("Checking if tile is valid for summoning: " + targetTile.getHighlightMode());

    	// Summoning is only valid if the tile's highlight mode is set to 1
    	return targetTile.getHighlightMode() == 1;
	}

}