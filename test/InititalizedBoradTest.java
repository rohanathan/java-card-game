import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import events.Initalize;
import play.libs.Json;
import structures.GameState;
import structures.basic.Tile;

/**
 * MyTest
 */
public class InititalizedBoradTest {
  GameState gameState = new GameState(); // create state storage
  Initalize initalizeProcessor = new Initalize(); // create an initalize event processor

  ObjectNode eventMessage = Json.newObject(); // create a dummy message

  @Test
  public void checkTileNumbers() {

    initalizeProcessor.processEvent(null, gameState, eventMessage); // send it to the initalize event processor

    int count = 0;
    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 5; j++) {
        if (gameState.getBoard().getTile(i, j) != null) {
          count++;  
        }

      }
    }

    assertEquals(count, 45);
  }

  @Test
  public void humanAvatarLoaded() {

    initalizeProcessor.processEvent(null, gameState, eventMessage); // send it to the initalize event processor
    Tile playerTile = gameState.getBoard().getTile(1, 2);
    
    assertNotNull(playerTile.getUnit());
    assertTrue(playerTile.isOccupied());
  }

  @Test
  public void AiAvatarLoaded() {

    initalizeProcessor.processEvent(null, gameState, eventMessage); // send it to the initalize event processor
    Tile aiTile = gameState.getBoard().getTile(7, 2);

    assertNotNull(aiTile.getUnit());
    assertTrue(aiTile.isOccupied());
  }
}





