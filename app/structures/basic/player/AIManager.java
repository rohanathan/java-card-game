package structures.basic.player;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import commands.BasicCommands;
import events.EndTurnClicked;
import scala.concurrent.impl.FutureConvertersImpl;
import structures.GameState;
import structures.basic.*;
import structures.basic.cards.Card;

public class AIManager {
    private final GameState gameState;
    private final AICardSelector cardSelector;
    private final AIMoveHandler moveHandler;
    private final AIPlayer aiPlayer;
    private Unit stunnedUnit;

    public AIManager(GameState gameState, AIPlayer aiPlayer) {
        this.gameState = gameState;
        this.aiPlayer = aiPlayer;
        this.cardSelector = new AICardSelector(gameState, aiPlayer);
        this.moveHandler = new AIMoveHandler(gameState, aiPlayer);
    }

    public void takeTurn(ActorRef out, JsonNode message) {
        notifyIfStunnedUnitRecovered(out);
        makeBestMove();
        if (!gameState.isGameOver) endTurn(out, message);
    }

    private void notifyIfStunnedUnitRecovered(ActorRef out) {
        if (stunnedUnit != null && stunnedUnit.getHealth() > 0) {
            BasicCommands.addPlayer1Notification(out, stunnedUnit.getName() + " recovered from stun", 2);
            stunnedUnit = null;
        }
    }

    private void endTurn(ActorRef out, JsonNode message) {
        new EndTurnClicked().processEvent(out, gameState, message);
        BasicCommands.addPlayer1Notification(out, "AI ended turn", 2);
    }

    private void makeBestMove() {
        try {
            moveHandler.performAttacks();
            moveHandler.performMovements();
            cardSelector.performCardActions();
            moveHandler.performAttacks();
            moveHandler.performMovements();
            moveHandler.performAttacks();
        } catch (Exception e) {
            System.err.println("AI decision failure: " + e.getMessage());
        }
    }

    public Player getAIPlayer() { return aiPlayer; }
    public GameState getGameState() { return gameState; }

    public Unit getStunnedUnit(){
        return stunnedUnit;
    }

    public void setStunnedUnit(Unit stunnedUnit) {
        this.stunnedUnit = stunnedUnit;
    }
}
