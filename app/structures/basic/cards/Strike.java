package structures.basic.cards;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Unit;
import structures.basic.player.HumanPlayer;

public class Strike {

	public static void TrueStrike(GameState gs, Unit u, ActorRef out) {
		
		if (u != null && u.getOwner() instanceof HumanPlayer) {

			gs.getUnitManager().modifyUnitHealth(u, u.getHealth() - 2);
			gs.getAbilityHandler().animateStrikeEffect(u.getActiveTile(gs.getBoard()));
			BasicCommands.addPlayer1Notification(out, "True Strike! -2 Health", 2);
		}
		
		
	}
}
