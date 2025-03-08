package structures.basic.cards;

import static utils.BasicObjectBuilders.loadUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameManager;
import structures.GameState;

import structures.basic.Board;

import structures.basic.EffectAnimation;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

public class Wraithling extends Unit{
	private static int id=1000;

	public static void summonGloomChaserWraithling(Tile tile, ActorRef out, GameState gameState) {
		
	    	
	        Tile toTheLeft = findTileToLeft(tile, gameState);
	        
	        if (toTheLeft != null && !toTheLeft.isOccupied()) {
	          summonWraithlingToTile(toTheLeft, out, gameState);}
			try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}

	    
	    }


	private static Tile findTileToLeft(Tile base, GameState gameState) {
		
			Tile currentTile = base;
	    
			int newTileX = currentTile.getTilex() - 1; // Two tiles to the left
			int newTileY = currentTile.getTiley(); // Same Y coordinate
	    
	    // Check for array bounds
			if (newTileX < 0 || newTileX >= 8) {
	        return null; // Out of bounds, return null
	    }
	    
	    return gameState.getBoard().getTile(newTileX, newTileY);
	}


	public static void summonWraithlingToTile(Tile tile, ActorRef out, GameState gameState) {	
		
		if(tile==null || tile.isOccupied()) {
			System.out.println("Tile is null or occupied");
			return;
		}
				
	    Unit wraithling = loadUnit(StaticConfFiles.wraithling, id, Unit.class);
		// set unit position
		tile.setUnit(wraithling);
		wraithling.setPositionByTile(tile);
	    wraithling.setOwner(gameState.getHuman());
		gameState.getHuman().addUnit(wraithling);
		gameState.addToTotalUnits(1);
		gameState.addUnitstoBoard(wraithling);
		wraithling.setName("Wraithling");
	    id++;
		
		System.out.println("Wraithling is added to board, all units: " + ( gameState.getTotalUnits()));


		// draw unit on new tile and play summon animation
	    EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.wsummon);
		BasicCommands.playEffectAnimation(out, effect, tile);
		BasicCommands.drawUnit(out, wraithling, tile);


		wraithling.setAttack(1);
		wraithling.setHealth(1);

		wraithling.setHasMoved(true);
		wraithling.setHasAttacked(true);
		// wait for animation to play out
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	   
	    System.out.println("Wraithling summoned to " + tile.getTilex() + ", " + tile.getTiley()+
				" with id: " + wraithling.getId() + " and name: " + wraithling.getName()
				+ " and attack: " + wraithling.getAttack() + " and health: "
				+ wraithling.getHealth());
	    BasicCommands.setUnitHealth(out, wraithling, 1);
	    BasicCommands.setUnitAttack(out, wraithling, 1);
        gameState.getBoardManager().removeHighlightFromAll();

	}

	// Helper method to check if a tile with given coordinates is valid
	public static boolean isValidTile(Board board, int x, int y) {
		// Check if the tile coordinates are within the bounds of the board
		return x >= 0 && y >= 0 && x < board.getTiles().length && y < board.getTiles()[0].length;
	}

	private static Tile getRandomAdjacentUnoccupiedTile(Tile currentTile, Board board) {

		int currentX = currentTile.getTilex();
		int currentY = currentTile.getTiley();

		// List to store adjacent tiles
		List<Tile> adjacentTiles = new ArrayList<>();

		// Iterate over adjacent tiles
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				// Skip the current tile
				if (dx == 0 && dy == 0) continue;

				int adjX = currentX + dx;
				int adjY = currentY + dy;

				// Check if the tile is within bounds
				if (isValidTile(board, adjX, adjY)) {
					// Get the adjacent tile
					Tile adjTile = board.getTile(adjX, adjY);
					// Check if the tile is unoccupied
					if (!adjTile.isOccupied()) {
						adjacentTiles.add(adjTile); // Add unoccupied adjacent tile to the list
					}
				}
			}
		}

		// Shuffle the list to randomize the selection
		Collections.shuffle(adjacentTiles);

		// Return the first unoccupied tile found (or null if none found)
		return adjacentTiles.isEmpty() ? null : adjacentTiles.get(0);
	}


	public static void summonAvatarWraithling(ActorRef out, GameState gs) {
		
		
		Tile tile = getRandomAdjacentUnoccupiedTile(gs.getHuman().getAvatar()
				.getActiveTile(gs.getBoard()), gs.getBoard());			
		summonWraithlingToTile(tile, out, gs);
	}

	public static void summonWraithlingForBloodmoonPriestess(Unit parent, ActorRef out, GameState gameState, GameManager gm) {
		Tile currentTile = parent.getActiveTile(gameState.getBoard());
		Tile randomTile = getRandomAdjacentUnoccupiedTile(currentTile, gameState.getBoard());

		if (randomTile != null ) {
			summonWraithlingToTile(randomTile, out, gameState);
		}


		try {
			Thread.sleep(30);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}



}

