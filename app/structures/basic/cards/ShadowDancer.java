package structures.basic.cards;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Unit;

/**
 * The ShadowDancer class represents a unit with a Deathwatch ability.
 * Whenever a unit dies, it heals its owner’s avatar and damages the opponent’s avatar.
 */
public class ShadowDancer extends Unit {

    /**
     * Implements the Deathwatch ability for ShadowDancer.
     * Heals the human player's avatar by 1 health and damages the AI's avatar by 1.
     *
     * @param gs  The current game state.
     * @param out The ActorRef for communication with the UI.
     */
    public static void Deathwatch(GameState gs, ActorRef out) {
        Unit humanAvatar = gs.getHuman().getAvatar();
        Unit aiAvatar = gs.getAi().getAvatar();

        for (int i = 0; i < gs.getUnits().size(); i++) {
            Unit unit = gs.getUnits().get(i);
            
            if (unit.getName().equals("Shadowdancer")) {
                gs.getUnitManager().modifyUnitHealth(humanAvatar, humanAvatar.getHealth() + 1);
                gs.getAbilityHandler().animateHealingEffect(humanAvatar.getActiveTile(gs.getBoard()));

                gs.getUnitManager().modifyUnitHealth(aiAvatar, aiAvatar.getHealth() - 1);
                gs.getAbilityHandler().animateStrikeEffect(aiAvatar.getActiveTile(gs.getBoard()));

                BasicCommands.addPlayer1Notification(out, "Shadowdancer heals avatar and hurts enemy!", 3);
            }
        }
        System.err.println("ShadowDance");
    }
}
