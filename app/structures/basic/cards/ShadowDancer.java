package structures.basic.cards;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Unit;

public class ShadowDancer extends Unit {
	
	public static void Deathwatch(GameState gs, ActorRef out) {
		
		Unit humanAvatar=gs.getHuman().getAvatar();
		Unit aiAvatar=gs.getAi().getAvatar();
		
        for (Unit unit : gs.getUnits()) {
            if (unit.getName().equals("Shadowdancer")) {
            	
            	gs.getUnitManager().modifyUnitHealth(humanAvatar, humanAvatar.getHealth() + 1);
            	gs.getAbilityHandler().healing(humanAvatar.getActiveTile(gs.getBoard()));
            	
            	gs.getUnitManager().modifyUnitHealth(aiAvatar, aiAvatar.getHealth() - 1);
            	gs.getAbilityHandler().strike(aiAvatar.getActiveTile(gs.getBoard()));

				BasicCommands.addPlayer1Notification(out, "Shadowdancer heals avatar and hurts enemy!", 3);

			}
        }
		System.err.println("ShadowDance");
	}

}
