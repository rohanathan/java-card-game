import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import events.Initalize;
import play.libs.Json;
import structures.GameState;

/**
 * MyTest
 */
public class InitializedAiPlayerTests {
    GameState gameState = new GameState(); // create state storage
    Initalize initalizeProcessor = new Initalize(); // create an initalize event processor

    ObjectNode eventMessage = Json.newObject(); // create a dummy message

  @Test
  public void checkHealth() {

    initalizeProcessor.processEvent(null, gameState, eventMessage); // send it to the initalize event processor

    assertEquals(gameState.getAi().getHealth(), 20 );

  }

  @Test
  public void checkMana() {

    initalizeProcessor.processEvent(null, gameState, eventMessage); // send it to the initalize event processor

    assertEquals(gameState.getAi().getMana(),0);

  }
  
  @Test
  public void checkCardsInHand() {

    initalizeProcessor.processEvent(null, gameState, eventMessage); // send it to the initalize event processor

    assertEquals(gameState.getAi().getHand().getNumberOfCardsInHand(), 3);

  }

}
