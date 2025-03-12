package structures.basic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Board {
    private Tile[][] tiles = new Tile[9][5];;

    public Tile getTile(int x, int y) {
        return tiles[x][y];
    }

	public Tile[][] getTiles() {
		return tiles;
	}

	public void setTile(Tile tile, int x, int y) {
        tiles[x][y] = tile;
    }

    public void setTiles(Tile[][] tiles) {
        this.tiles = tiles;
    }

	/**
	 * Gets all unoccupied tiles on the board.
	 *
	 * @return A set of unoccupied tiles.
	 */
    public  Set<Tile> getAllUnoccupiedTiles(Tile[][] tiles) {
        Set<Tile> unoccupiedTiles = new HashSet<>();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 5; j++) {
                Tile tile = tiles[i][j];
                if (!tile.isOccupied()) {
                    unoccupiedTiles.add(tile);
                }
            }
        }
        return unoccupiedTiles;
    }
}



