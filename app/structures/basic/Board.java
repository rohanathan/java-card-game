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

    public void setTile(Tile tile, int x, int y) {
        tiles[x][y] = tile;
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    public void setTiles(Tile[][] tiles) {
        this.tiles = tiles;
    }
    
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

	public List<Tile> getAdjacentTiles(Tile tile) {
		
	        List<Tile> adjacentTiles = new ArrayList<>();

	        int currentX = tile.getTilex();
	        int currentY = tile.getTiley();

	        // Check right tile
	        if (currentX + 1 < 9) {
	            adjacentTiles.add(tiles[currentX + 1][currentY]);
	        }

	        // Check left tile
	        if (currentX - 1 >= 0) {
	            adjacentTiles.add(tiles[currentX - 1][currentY]);
	        }

	        // Check bottom tile
	        if (currentY + 1 < 5) {
	            adjacentTiles.add(tiles[currentX][currentY + 1]);
	        }

	        // Check top tile
	        if (currentY - 1 >= 0) {
	            adjacentTiles.add(tiles[currentX][currentY - 1]);
	        }

	        // Check top-right tile
	        if (currentX + 1 < 9 && currentY - 1 >= 0) {
	            adjacentTiles.add(tiles[currentX + 1][currentY - 1]);
	        }

	        // Check top-left tile
	        if (currentX - 1 >= 0 && currentY - 1 >= 0) {
	            adjacentTiles.add(tiles[currentX - 1][currentY - 1]);
	        }

	        // Check bottom-right tile
	        if (currentX + 1 < 9 && currentY + 1 < 5) {
	            adjacentTiles.add(tiles[currentX + 1][currentY + 1]);
	        }

	        // Check bottom-left tile
	        if (currentX - 1 >= 0 && currentY + 1 < 5) {
	            adjacentTiles.add(tiles[currentX - 1][currentY + 1]);
	        }

	        return adjacentTiles;
	    }
}



