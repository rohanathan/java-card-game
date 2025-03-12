package structures.basic.player;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import commands.BasicCommands;
import events.EndTurnClicked;
import scala.concurrent.impl.FutureConvertersImpl;
import structures.GameState;
import structures.basic.*;
import structures.basic.cards.Card;

/**
 * AIManager is responsible for coordinating AI decision-making processes, including movement,
 * attack execution, and card selection during its turn.
 * 
 * It delegates actions to {@link AIMoveHandler} for movement and attacks and 
 * {@link AICardSelector} for card-based decisions.
 * 
 */

public class AIManager {
    private final GameState gameState;
    private final AICardSelector cardSelector;
    private final AIMoveHandler moveHandler;
    private final AIPlayer aiPlayer;
    private Unit stunnedUnit;

    /**
     * Constructs an AIManager to manage AI-controlled gameplay actions.
     *
     * @param gameState The current game state, containing board details, players, and units.
     * @param aiPlayer  The AI player instance controlled by this manager.
     */
    public AIManager(GameState gameState, AIPlayer aiPlayer) {
        this.gameState = gameState;
        this.aiPlayer = aiPlayer;
        this.cardSelector = new AICardSelector(gameState, aiPlayer);
        this.moveHandler = new AIMoveHandler(gameState, aiPlayer);
    }

    /**
     * Initiates the AI's turn by executing moves, attacks, and card-based actions sequentially.
     * If the game is not over, the AI ends its turn.
     *
     * @param out     The ActorRef used for communication with the frontend.
     * @param message The event message received from the UI.
     */
    public void takeTurn(ActorRef out, JsonNode message) {
        notifyIfStunnedUnitRecovered(out);
        makeBestMove();
        if (!gameState.isGameOver) endTurn(out, message);
    }

    /**
     * Notifies the user if a stunned unit has recovered and resets its stunned state.
     *
     * @param out The ActorRef used for sending messages to the frontend.
     */
    private void notifyIfStunnedUnitRecovered(ActorRef out) {
        if (stunnedUnit != null && stunnedUnit.getHealth() > 0) {
            BasicCommands.addPlayer1Notification(out, stunnedUnit.getName() + " recovered from stun", 2);
            stunnedUnit = null;
        }
    }

    /**
     * Ends the AI player's turn and triggers UI updates.
     *
     * @param out     The ActorRef used for communication.
     * @param message The event message received.
     */
    private void endTurn(ActorRef out, JsonNode message) {
        new EndTurnClicked().processEvent(out, gameState, message);
        BasicCommands.addPlayer1Notification(out, "AI ended turn", 2);
    }

    /**
     * Executes AI decisions by performing movement, attacks, and card actions
     * in a strategic sequence.
     */
    private void makeBestMove() {
        try {
            moveHandler.performAttacks();
            moveHandler.performMovements();
            cardSelector.performCardEffects();
            moveHandler.performAttacks();
            moveHandler.performMovements();
            moveHandler.performAttacks();
        } catch (Exception e) {
            System.err.println("AI decision failure: " + e.getMessage());
        }
    }

    /**
     * Retrieves the AI player instance managed by this AIManager.
     *
     * @return The AI player instance.
     */
    public Player getAIPlayer() { return aiPlayer; }
   
    /**
     * Retrieves the current game state managed by this AIManager.
     *
     * @return The game state instance.
     */
     public GameState getGameState() { return gameState; }

    /**
     * Retrieves the unit currently marked as stunned.
     *
     * @return The stunned unit.
     */
    public Unit getStunnedUnit(){
        return stunnedUnit;
    }

    /**
     * Sets a unit as stunned, preventing it from acting during the AI's turn.
     *
     * @param stunnedUnit The unit to mark as stunned.
     */
    public void setStunnedUnit(Unit stunnedUnit) {
        this.stunnedUnit = stunnedUnit;
    }
}
