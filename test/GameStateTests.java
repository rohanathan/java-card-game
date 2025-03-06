import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import events.Initalize;
import play.libs.Json;
import structures.GameState;
import structures.basic.player.AIPlayer;
import structures.basic.player.HumanPlayer;

public class GameStateTests {

  GameState gameState = new GameState(); // create state storage
  Initalize initalizeProcessor = new Initalize(); // create an initalize event processor
  
  ObjectNode eventMessage = Json.newObject(); // create a dummy message

  @Test
  public void changePlayer() {

    initalizeProcessor.processEvent(null, gameState, eventMessage); // send it to the initalize event processor
    // check if human is set as current player
    assertTrue(gameState.getCurrentPlayer() instanceof HumanPlayer);
    // switch the players
    gameState.switchCurrentPlayer();
    // check and see if that worked
    assertTrue(gameState.getCurrentPlayer() instanceof AIPlayer);
  }
  @Test
  public void endTurn() {
    initalizeProcessor.processEvent(null, gameState, eventMessage); // send it to the initalize event processor
    
    // Check human is the currentPlayer
    assertTrue(gameState.getCurrentPlayer() instanceof HumanPlayer);

    // simulate endTurn clicked
    gameState.endTurn();
    // Updated player state
    assertEquals(gameState.getHuman().getMana(), 0);
    assertEquals(gameState.getHuman().getTurn(), 2);

    // Check the Ai is in charge
    assertTrue(gameState.getCurrentPlayer() instanceof AIPlayer);
    assertEquals(gameState.getAi().getMana(), 2);
  }

  @Test
  public void gameFinisehd() {
    initalizeProcessor.processEvent(null, gameState, eventMessage); // send it to the initalize event processor

    // simulate a death
    gameState.getHuman().setHealth(0);
    gameState.endGame(null);
    assertTrue(gameState.isGameOver);
  }
}


