package structures.basic.cards;

import java.util.Comparator;
import java.util.List;

import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.player.AIPlayer;

/**
 * The {@code BeamShock} class represents a special ability in the game
 * that stuns the enemy unit with the highest attack value.
 */
public class BeamShock {

    /**
     * Finds the enemy unit (controlled by the human player) with the highest attack value
     * and applies a stun effect to it.
     *
     * @param gs The current {@code GameState} instance representing the game's state.
     */
    public static void unitStunned(GameState gs) {
        // Get all units controlled by the human player
        List<Unit> humanUnits = gs.getHuman().getUnits();

        // Find the unit with the highest attack value
        Unit u = humanUnits.stream().max(Comparator.comparingInt(Unit::getAttack)).orElse(null);

        // Ensure a valid unit was found before proceeding
        if (u == null) {
            return;
        }

        // Get the AI player and set the stunned unit
        AIPlayer ai = (AIPlayer) gs.getAi();
        ai.getAiManager().setStunnedUnit(u);

        // Get the tile where the stunned unit is located
        Tile activeTile = u.getActiveTile(gs.getBoard());

        // If the tile is valid, animate the stun effect
        if (activeTile != null) {
            gs.getAbilityHandler().animateStunEffect(activeTile);
        }
    }
}
