package structures.manager;


import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Board;
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
			playerManager.modifyPlayerHealth(unit.getOwner(), newHealth);
		}
	}

}
