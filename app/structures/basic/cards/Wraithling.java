package structures.basic.cards;

import static utils.BasicObjectBuilders.loadUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Board;
import structures.basic.EffectAnimation;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

/**
 * The {@code Wraithling} class represents a summonable unit in the game.
 * This unit can be summoned through various card effects and has the ability
 * to appear adjacent to specified tiles based on game mechanics.
 */
public class Wraithling extends Unit {

    private static int id = 1000; // Unique ID for wraithlings

    /**
     * Summons a Wraithling to the left of the specified tile if the space is unoccupied.
     * Used in conjunction with the Gloom Chaser ability.
     *
     * @param tile       The tile next to which the Wraithling should be summoned.
     * @param out        The actor reference for sending game commands.
     * @param gameState  The current game state.
     */
    public static void summonGloomChaserWraithling(Tile tile, ActorRef out, GameState gameState) {
        Tile toTheLeft = findTileToLeft(tile, gameState);

        if (toTheLeft != null && !toTheLeft.isOccupied()) {
            summonWraithlingToTile(toTheLeft, out, gameState);
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Finds the tile located to the left of the given base tile.
     *
     * @param base       The reference tile.
     * @param gameState  The current game state.
     * @return The tile to the left, or {@code null} if out of bounds.
     */
    private static Tile findTileToLeft(Tile base, GameState gameState) {
        int newTileX = base.getTilex() - 1;
        int newTileY = base.getTiley();

        // Ensure the new tile is within board bounds
        if (newTileX < 0 || newTileX >= 8) {
            return null;
        }

        return gameState.getBoard().getTile(newTileX, newTileY);
    }

    /**
     * Summons a Wraithling to a specified tile, ensuring it is unoccupied.
     * The summoned unit is assigned to the human player.
     *
     * @param tile       The target tile for summoning.
     * @param out        The actor reference for sending game commands.
     * @param gameState  The current game state.
     */
    public static void summonWraithlingToTile(Tile tile, ActorRef out, GameState gameState) {
        if (tile == null || tile.isOccupied()) {
            System.out.println("Tile is null or occupied");
            return;
        }

        // Load the Wraithling unit
        Unit wraithling = loadUnit(StaticConfFiles.wraithling, id, Unit.class);

        // Set Wraithling properties
        tile.setUnit(wraithling);
        wraithling.setPositionByTile(tile);
        wraithling.setOwner(gameState.getHuman());
        gameState.getHuman().addUnit(wraithling);
        gameState.addToTotalUnits(1);
        gameState.addUnitstoBoard(wraithling);
        wraithling.setName("Wraithling");
        id++;

        System.out.println("Wraithling is added to board, all units: " + gameState.getTotalUnits());

        // Play summon animation
        EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.wsummon);
        BasicCommands.playEffectAnimation(out, effect, tile);
        BasicCommands.drawUnit(out, wraithling, tile);

        // Set unit attributes
        wraithling.setAttack(1);
        wraithling.setHealth(1);
        wraithling.setHasMoved(true);
        wraithling.setHasAttacked(true);

        // Wait for animation
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Wraithling summoned to " + tile.getTilex() + ", " + tile.getTiley() +
                " with id: " + wraithling.getId() + " and name: " + wraithling.getName()
                + " and attack: " + wraithling.getAttack() + " and health: "
                + wraithling.getHealth());

        BasicCommands.setUnitHealth(out, wraithling, 1);
        BasicCommands.setUnitAttack(out, wraithling, 1);
        gameState.getBoardManager().removeHighlightFromAll();
    }

    /**
     * Checks if a tile at given coordinates is valid within the board.
     *
     * @param board  The game board.
     * @param x      The x-coordinate of the tile.
     * @param y      The y-coordinate of the tile.
     * @return {@code true} if the tile is within bounds, otherwise {@code false}.
     */
    public static boolean isValidTile(Board board, int x, int y) {
        return x >= 0 && y >= 0 && x < board.getTiles().length && y < board.getTiles()[0].length;
    }

    /**
     * Retrieves a random unoccupied tile adjacent to the specified tile.
     *
     * @param currentTile The reference tile.
     * @param board       The game board.
     * @return A random unoccupied adjacent tile, or {@code null} if none are available.
     */
    private static Tile getRandomAdjacentUnoccupiedTile(Tile currentTile, Board board) {
        int currentX = currentTile.getTilex();
        int currentY = currentTile.getTiley();
        List<Tile> adjacentTiles = new ArrayList<>();

        // Iterate over adjacent tiles
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue; // Skip the original tile

                int adjX = currentX + dx;
                int adjY = currentY + dy;

                // Check if the tile is within bounds
                if (isValidTile(board, adjX, adjY)) {
                    Tile adjTile = board.getTile(adjX, adjY);
                    if (!adjTile.isOccupied()) {
                        adjacentTiles.add(adjTile);
                    }
                }
            }
        }

        // Shuffle the list to randomize the selection
        Collections.shuffle(adjacentTiles);
        return adjacentTiles.isEmpty() ? null : adjacentTiles.get(0);
    }

    /**
     * Summons a Wraithling next to the human player's avatar.
     *
     * @param out The actor reference for sending game commands.
     * @param gs  The current game state.
     */
    public static void summonAvatarWraithling(ActorRef out, GameState gs) {
        Tile tile = getRandomAdjacentUnoccupiedTile(gs.getHuman().getAvatar()
                .getActiveTile(gs.getBoard()), gs.getBoard());
        summonWraithlingToTile(tile, out, gs);
    }

    /**
     * Summons a Wraithling next to the Bloodmoon Priestess when its ability triggers.
     *
     * @param parent     The Bloodmoon Priestess unit.
     * @param out        The actor reference for sending game commands.
     * @param gameState  The current game state.
     */
    public static void summonWraithlingForBloodmoonPriestess(Unit parent, ActorRef out, GameState gameState) {
        Tile currentTile = parent.getActiveTile(gameState.getBoard());
        Tile randomTile = getRandomAdjacentUnoccupiedTile(currentTile, gameState.getBoard());

        if (randomTile != null) {
            summonWraithlingToTile(randomTile, out, gameState);
        }

        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
