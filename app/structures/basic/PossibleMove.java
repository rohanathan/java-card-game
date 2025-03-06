package structures.basic;

public abstract class PossibleMove {
    public Unit unit;
    public Tile tile;
    public int moveQuality;

    public PossibleMove(Unit unit, Tile tile) {
        this.unit = unit;
        this.tile = tile;
        this.moveQuality = 0;
    }
}
