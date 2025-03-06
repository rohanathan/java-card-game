import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import events.EndTurnClicked;
import events.Initalize;
import play.libs.Json;
import structures.GameState;
import structures.basic.Board;
import structures.basic.Tile;
import structures.basic.player.AIPlayer;

public class EndTurnClickedTest {

  GameState gameState = new GameState(); // create state storage
  Initalize initalizeProcessor = new Initalize(); // create an initalize event processor
  EndTurnClicked endTurnProcessor = new EndTurnClicked(); // create an initalize event processor

  ObjectNode eventMessage = Json.newObject(); // create a dummy message

  @Test
  public void CheckEndTurn() {
    initalizeProcessor.processEvent(null, gameState, eventMessage); // send it to the initalize event processor
    endTurnProcessor.processEvent(null, gameState, eventMessage); // send it to the initalize event processor

    Board board = gameState.getBoard();

    Tile[][] tiles = board.getTiles();
    for (Tile[] tiles2 : tiles) {
      for (Tile tile : tiles2) {
        if (!tile.isOccupied()) {
          assertEquals(tile.getHighlightMode(), 0);
        } else {
          assertFalse(tile.getUnit().hasMoved());
          assertFalse(tile.getUnit().hasAttacked());
        }
      }
    }
    assertTrue(gameState.getActionHistory().isEmpty());

  }
}
