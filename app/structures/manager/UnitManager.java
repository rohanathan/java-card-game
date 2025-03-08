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

    // initial player setup
	public void initializeAvatar(Board board, Player player) {
		// check if player is human or AI
		Tile avatarTile;
		Unit avatar;
		if (player instanceof HumanPlayer) {
			avatarTile = board.getTile(1, 2);
			avatar = BasicObjectBuilders.loadUnit(StaticConfFiles.humanAvatar, 0, Unit.class);
			avatar.setName("Player Avatar");

		} else {
			avatarTile = board.getTile(7, 2);
			avatar = BasicObjectBuilders.loadUnit(StaticConfFiles.aiAvatar, 1, Unit.class);
			avatar.setName("AI Avatar");
		}
		avatar.setPositionByTile(avatarTile);
		avatarTile.setUnit(avatar);
		BasicCommands.drawUnit(out, avatar, avatarTile);
		avatar.setOwner(player);
		player.setAvatar(avatar);
		player.addUnit(avatar);
		gameState.addToTotalUnits(1);
		modifyUnitHealth(avatar, 20);
		updateUnitAttack(avatar, 2);
		avatar.setHealth(20);
		avatar.setMaxHealth(20);
		avatar.setAttack(2);
		try {
			Thread.sleep(30);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BasicCommands.setUnitHealth(out, avatar, 20);
		try {
			Thread.sleep(30);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BasicCommands.setUnitAttack(out, avatar, 2);
	}

    // Method to update the health of a unit on the board
	public void modifyUnitHealth(Unit unit, int newHealth) {

		if (newHealth > 20) {
			newHealth = 20;
		} else if (newHealth < 0) {
			newHealth = 0;
		}

		if (unit.getName().equals("Player Avatar") &&
				unit.getHealth() > newHealth && gameState.getHuman().getRobustness() > 0){
			gameState.getHuman().setRobustness(gameState.getHuman().getRobustness()-1);
		    BasicCommands.addPlayer1Notification(out, "Avatar robustness left: " + gameState.getHuman().getRobustness(), 4);

		}
		
		try {Thread.sleep(30);} catch (InterruptedException e) {e.printStackTrace();}
		if (newHealth == 0) {
			destroyUnit(unit);
			return;
		}
		if (unit.getHealth() > newHealth && unit.getName().equals("AI Avatar")) {
			zeal();
		}
		unit.setHealth(newHealth);
		BasicCommands.setUnitHealth(out, unit, newHealth);
		if (unit.getName().equals("Player Avatar") || unit.getName().equals("AI Avatar")) {
			gameState.getPlayerManager().modifyPlayerHealth(unit.getOwner(), newHealth);
		}
	}
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
				ShadowDancer.Deathwatch(gameState, out);
			}
			if (!unit.getName().equals("Bloodmoon Priestess")) {
				BloodmoonPriestess.BloodmoonPriestessDeathwatch(out, gameState, gameState.gameManager);
			}
			if (!unit.getName().equals("Shadow Watcher")) {
				ShadowWatcher.ShadowWatcherDeathwatch(out, gameState, gameState.gameManager);
			}
			if (!unit.getName().equals("Bad Omen")) {
				BadOmen.BadOmenDeathwatch(out, gameState, gameState.gameManager, unit);
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
    // Method for casting the Zeal spell
    public void zeal() {
		for (Unit unit : gameState.getAi().getUnits()) {
			if (unit.getName().equals("Silverguard Knight")) {
				int newAttack = unit.getAttack() + 2;
				updateUnitAttack(unit, newAttack);
				EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_buff);
				BasicCommands.playEffectAnimation(out, effect, unit.getActiveTile(gameState.getBoard()));
				BasicCommands.addPlayer1Notification(out, "Silverguard Knight's zeal!", 3);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
    // check if summoning is valid
	public boolean isValidSummon(Card card, Tile tile) {
		// depending on cards, this may change
		// for now, all cards can move to tiles highlighted white
		System.out.println("isValidSummon: " + tile.getHighlightMode());
		return tile.getHighlightMode() == 1;
	}

}