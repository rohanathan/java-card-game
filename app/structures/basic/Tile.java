package structures.basic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.BasicObjectBuilders;

/**
 * A basic representation of a tile on the game board. Tiles have both a pixel position
 * and a grid position. Tiles also have a width and height in pixels and a series of urls
 * that point to the different renderable textures that a tile might have.
 *
 * @author Dr. Richard McCreadie
 *
 */
public class Tile {

	@JsonIgnore
	private static ObjectMapper mapper = new ObjectMapper(); // Jackson Java Object Serializer, is used to read java objects from a file

	List<String> tileTextures;
	int xpos;
	int ypos;
	int width;
	int height;
	int tilex;
	int tiley;
	int highlightMode = 0;
	private Unit unit;
	private boolean isOccupied = false;

	public Tile() {}

	public Tile(String tileTexture, int xpos, int ypos, int width, int height, int tilex, int tiley) {
		super();
		tileTextures = new ArrayList<String>(1);
		tileTextures.add(tileTexture);
		this.xpos = xpos;
		this.ypos = ypos;
		this.width = width;
		this.height = height;
		this.tilex = tilex;
		this.tiley = tiley;
	}

	public Tile(List<String> tileTextures, int xpos, int ypos, int width, int height, int tilex, int tiley) {
		super();
		this.tileTextures = tileTextures;
		this.xpos = xpos;
		this.ypos = ypos;
		this.width = width;
		this.height = height;
		this.tilex = tilex;
		this.tiley = tiley;
	}
	public List<String> getTileTextures() {
		return tileTextures;
	}
	public void setTileTextures(List<String> tileTextures) {
		this.tileTextures = tileTextures;
	}
	public int getXpos() {
		return xpos;
	}
	public void setXpos(int xpos) {
		this.xpos = xpos;
	}
	public int getYpos() {
		return ypos;
	}
	public void setYpos(int ypos) {
		this.ypos = ypos;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getTilex() {
		return tilex;
	}
	public void setTilex(int tilex) {
		this.tilex = tilex;
	}
	public int getTiley() {
		return tiley;
	}
	public void setTiley(int tiley) {
		this.tiley = tiley;
	}

	/**
	 * Loads a tile from a configuration file
	 * parameters.
	 * @param configFile
	 * @return
	 */
	public static Tile constructTile(String configFile) {

		try {
			Tile tile = mapper.readValue(new File(configFile), Tile.class);
			return tile;
		} catch (Exception e) {
			e.printStackTrace();

		}
		return null;

	}

	public List<Tile> getAllAdjacentTiles(Board board){
		List<Tile> tiles = new ArrayList<>();

		// Define relative positions of adjacent tiles
		int[][] directions = {
				{-1, 0}, {-2, 0}, {1, 0}, {2, 0}, // Left and Right
				{0, -1}, {0, -2}, {0, 1}, {0, 2}, // Up and Down
				{-1, 1}, {1, 1}, {-1, -1}, {1, -1} // Diagonals
		};
		// Iterate through each direction to find adjacent tiles
		for (int i = 0; i < directions.length; i++) {
			int newX = tilex + directions[i][0];
			int newY = tiley + directions[i][1];

			// Check if the new position is within bounds
			if (isValidTile(newX, newY)) {
				tiles.add(BasicObjectBuilders.loadTile(newX, newY));
			}
		} return tiles;
	}

	/**
	 * Checks if a tile position is valid (within the bounds of the board).
	 *
	 * @param x The x-coordinate of the tile.
	 * @param y The y-coordinate of the tile.
	 * @return True if the tile position is valid, false otherwise.
	 */
	boolean isValidTile(int x, int y) {
		return x >= 0 && x <= 8 && y >=0 && y <=4;
	}

	/**
	 * Checks if the tile is occupied by a unit.
	 *
	 * @return True if the tile is occupied, false otherwise.
	 */
	public boolean isOccupied() {
		return this.isOccupied;
	}

	/**
	 * Marks the tile as unoccupied (no unit is present on the tile).
	 */
	public void markAsUnoccupied() {
		this.isOccupied = false;
	}

	/**
	 * Marks the tile as occupied (a unit is present on the tile).
	 */
	public void markAsOccupied() {
		this.isOccupied = true;
	}

	/**
	 * Gets the unit currently occupying the tile.
	 *
	 * @return The unit on the tile, or null if the tile is unoccupied.
	 */
	public Unit getUnit() {
		return unit;
	}


	/**
	 * Places a unit on the tile if it is not already occupied.
	 *
	 * @param unit The unit to place on the tile.
	 */
	public void setUnit(Unit unit) {
		if (this.isOccupied()) {
			System.out.println("Tile is already occupied. Cannot place unit.");
			return;
		}
		this.markAsOccupied();
		this.unit = unit;
		System.out.println("Unit placed successfully on the tile.");
	}

	/**
	 * Removes a Unit from the Tile
	 */
	public void removeUnit() {
		if (this.unit != null) {
			this.unit = null;
			this.markAsUnoccupied();
		}
	}

	/**
	 * Gets the current highlight mode of the tile.
	 *
	 * @return The highlight mode (0: normal, 1: white-highlighted, 2: red-highlighted).
	 */
	public int getHighlightMode() {
		return highlightMode;
	}

	/**
	 * Sets the highlight mode of the tile.
	 *
	 * @param highlightMode The highlight mode to set (0: normal, 1: white-highlighted, 2: red-highlighted).
	 */
	public void setHighlightMode(int highlightMode) {
		this.highlightMode = highlightMode;
	}
}
