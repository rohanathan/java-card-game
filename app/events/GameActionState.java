package events;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;

/**
 * Interface for game action states.
 * This interface defines the contract for handling game actions such as spell casting,
 * unit summoning, and unit actions (move/attack).
 */
public interface GameActionState {
    /**
     * Handles the action based on the current game state and target tile.
     *
     * @param out       The ActorRef for sending commands to the front-end.
     * @param gameState The current state of the game.
     * @param tile      The target tile for the action.
     */
    void handleAction(ActorRef out, GameState gameState, Tile tile);
}