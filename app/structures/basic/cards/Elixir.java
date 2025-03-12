package structures.basic.cards;

import java.util.Comparator;
import java.util.Optional;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.player.AIPlayer;

/**
 * The {@code Elixir} class provides special abilities related to unit buffs 
 * and healing effects in the game. It includes effects for boosting 
 * adjacent units and healing units with the Sundrop ability.
 */
public class Elixir {

    /**
     * Applies the Silverguard Squire's Opening Gambit ability.
     * It boosts adjacent units around the AI Avatar.
     *
     * @param out The actor reference used for sending game commands.
     * @param gs  The current game state instance.
     */
    public static void silverguardSquire(ActorRef out, GameState gs) {
        // Get the AI avatar's position
        Tile avatarPosition = gs.getAi().getAvatar().getActiveTile(gs.getBoard());

        // Boost adjacent units
        boostAdjacentUnits(avatarPosition, 1, 1, gs);

        // Display notification
        BasicCommands.addPlayer1Notification(out, "Silverguard Squire's Opening Gambit!", 3);

        // Pause briefly for effect
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Boosts the health and attack of AI-controlled units adjacent to a given tile.
     *
     * @param tile        The reference tile.
     * @param healthBoost The amount of health to add.
     * @param attackBoost The amount of attack to add.
     * @param gameState   The current game state.
     */
    public static void boostAdjacentUnits(Tile tile, int healthBoost, int attackBoost, GameState gameState) {
        // Get the x and y coordinates of the given tile
        int x = tile.getTilex();
        int y = tile.getTiley();

        int newLeft = x - 1;
        int newRight = x + 1;

        // Check the left adjacent tile
        if (newLeft >= 0) {
            Tile leftTile = gameState.getBoard().getTile(newLeft, y);

            if (leftTile != null && leftTile.isOccupied() &&
                    leftTile.getUnit().getOwner() instanceof AIPlayer) {
                gameState.getUnitManager().modifyUnitHealth(leftTile.getUnit(), leftTile.getUnit().getHealth() + healthBoost);
                gameState.getUnitManager().updateUnitAttack(leftTile.getUnit(), leftTile.getUnit().getAttack() + attackBoost);
                gameState.getAbilityHandler().animateHealingEffect(leftTile);
            }
        }

        // Check the right adjacent tile
        if (newRight < 9) {
            Tile rightTile = gameState.getBoard().getTile(newRight, y);

            if (rightTile != null && rightTile.isOccupied() &&
                    rightTile.getUnit().getOwner() instanceof AIPlayer) {
                gameState.getUnitManager().modifyUnitHealth(rightTile.getUnit(), rightTile.getUnit().getHealth() + healthBoost);
                gameState.getUnitManager().updateUnitAttack(rightTile.getUnit(), rightTile.getUnit().getAttack() + attackBoost);
                gameState.getAbilityHandler().animateHealingEffect(rightTile);
            }
        }
    }

    /**
     * Applies the Sundrop Elixir ability, which heals a unit controlled by the AI.
     * If the selected unit is already at max health, it heals the AI's lowest-health unit instead.
     *
     * @param unit The unit to heal.
     * @param gs   The current game state.
     * @param out  The actor reference for sending game commands.
     */
    public static void Sundrop(Unit unit, GameState gs, ActorRef out) {
        // Ensure the unit is controlled by AI
        if (unit != null && unit.getOwner() instanceof AIPlayer) {
            AIPlayer aiPlayer = (AIPlayer) gs.getAi();

            if (unit.getHealth() == unit.getMaxHealth()) {
                // Find the AI-controlled unit with the lowest health
                Optional<Unit> unitWithLowestHealth = aiPlayer.getUnits().stream()
                        .min(Comparator.comparingInt(Unit::getHealth));

                // If found, heal the lowest health unit
                Unit lowestHealthUnit = unitWithLowestHealth.orElse(null);
                if (lowestHealthUnit != null) {
                    int newHealth = lowestHealthUnit.getHealth() + 4;
                    gs.getUnitManager().modifyUnitHealth(lowestHealthUnit, 
                            Math.min(newHealth, lowestHealthUnit.getMaxHealth()));
                }
            } else {
                // Heal the selected unit directly
                int newHealth = unit.getHealth() + 4;
                gs.getUnitManager().modifyUnitHealth(unit, Math.min(newHealth, unit.getMaxHealth()));
            }
        }

        // Display notification
        BasicCommands.addPlayer1Notification(out, "Sundrop Elixir heals a unit!", 3);

        // Play healing animation
        gs.getAbilityHandler().animateHealingEffect(unit.getActiveTile(gs.getBoard()));
    }
}
