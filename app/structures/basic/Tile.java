package structures.basic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A basic representation of a tile on the game board. Tiles have both a pixel
 * position
 * and a grid position. Tiles also have a width and height in pixels and a
 * series of urls
 * that point to the different renderable textures that a tile might have.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Tile {

	@JsonIgnore
	private static ObjectMapper mapper = new ObjectMapper(); // Jackson Java Object Serializer, is used to read java
	// objects from a file

	List<String> tileTextures;
	int xpos;
	int ypos;
	int width;
	int height;
	int tilex;
	int tiley;
	// Tile holds a unit
	private Unit unit;
	// Flag to check the current mode of the tile
	// i.e., 0: normal, 1: white-highlighted, 2: red-highlighted
	private int highlightMode = 0;
	// tracks occupied tiles
	private boolean occupied = false;

	public Tile() {
	}

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

	// added by Renos
	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		if (this.isOccupied()) {
			System.out.println("Tile is already occupied");
			return;
		}
		this.setOccupied();
		this.unit = unit;
	}

	/**
	 * Removes a Unit from the Tile
	 */
	public void removeUnit() {
		if (this.unit != null) {
			this.unit = null;
			this.setNotOccupied();
		}
	}

	public int getHighlightMode() {
		return highlightMode;
	}

	public void setHighlightMode(int highlightMode) {
		this.highlightMode = highlightMode;
	}

	/**
	 * Loads a tile from a configuration file
	 * parameters.
	 *
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

	public boolean isOccupied() {
		return this.occupied;
	}

	public void setNotOccupied() {
		this.occupied = false;
	}

	public void setOccupied() {
		this.occupied = true;
	}
}
