import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import events.Initalize;
import play.libs.Json;
import structures.GameState;

public class InitializedGameStateTests {
  GameState gameState = new GameState(); // create state storage
  Initalize initalizeProcessor = new Initalize(); // create an initalize event processor

  ObjectNode eventMessage = Json.newObject(); // create a dummy message

  @Test
  public void checkBoartdLoaded() {

    initalizeProcessor.processEvent(null, gameState, eventMessage); // send it to the initalize event processor

    assertNotNull(gameState.getBoard());
  }

  @Test
  public void checkHumanPlayerLoaded() {
    
    initalizeProcessor.processEvent(null, gameState, eventMessage); // send it to the initalize event processor
    assertNotNull(gameState.getHuman());
  }

  @Test
  public void chekAiLoaded() {
    
    initalizeProcessor.processEvent(null, gameState, eventMessage); // send it to the initalize event processor
    assertNotNull(gameState.getAi());
  }

  @Test
  public void checkGameServicesLoaded() {
    
    initalizeProcessor.processEvent(null, gameState, eventMessage); // send it to the initalize event processor
    assertNotNull(gameState.gameService);
  }

}
