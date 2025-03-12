package structures.basic;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import structures.basic.player.Player;

/**
 * This is a representation of a Unit on the game board. A unit has a unique id
 * (this is used by the front-end. Each unit has a current UnitAnimationType,
 * e.g. move, or attack. The position is the physical position on the board.
 * UnitAnimationSet contains the underlying information about the animation
 * frames, while ImageCorrection has information for centering the unit on the
 * tile.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Unit implements GameAction {

	@JsonIgnore
	protected static ObjectMapper mapper = new ObjectMapper(); // Jackson Java Object Serializer, is used to read java
																// objects from a file

	int id;
	String name;
	UnitAnimationType animation;
	Position position;
	UnitAnimationSet animations;
	ImageCorrection correction;
	int health;		// Keep track of the unit's current health
	int attack;		// Keep track of the unit's current attack
	@JsonBackReference
	Player owner;	// Keep track of the owner of the unit
	boolean hasMoved = false;	// Keep track of whether the unit has moved in the current turn
	boolean hasAttacked = false; 	// Keep track of whether the unit has attacked in the current turn
	boolean hasProvoke = false; // Whether the unit has the Provoke ability
	private int maxHealth;
	public Unit() {}

	/**
	 * Constructor for initializing a unit with basic properties.
	 *
	 * @param id         The unique ID of the unit.
	 * @param animations The animation set for the unit.
	 * @param correction The image correction data for rendering.
	 */
	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;
		this.position = new Position(0, 0, 0, 0);
		this.correction = correction;
		this.animations = animations;
	}

	/**
	 * Constructor for initializing a unit with a specific tile position.
	 *
	 * @param id          The unique ID of the unit.
	 * @param animations  The animation set for the unit.
	 * @param correction  The image correction data for rendering.
	 * @param currentTile The tile where the unit is initially placed.
	 */
	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction, Tile currentTile) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;
		this.position = new Position(currentTile.getXpos(), currentTile.getYpos(), currentTile.getTilex(),
				currentTile.getTiley());
		this.correction = correction;
		this.animations = animations;
	}

	/**
	 * Constructor for initializing a unit with all properties.
	 *
	 * @param id         The unique ID of the unit.
	 * @param animation  The current animation type.
	 * @param position   The position of the unit on the board.
	 * @param animations The animation set for the unit.
	 * @param correction The image correction data for rendering.
	 */
	public Unit(int id, UnitAnimationType animation, Position position, UnitAnimationSet animations,
			ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = animation;
		this.position = position;
		this.animations = animations;
		this.correction = correction;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public UnitAnimationType getAnimation() {
		return animation;
	}

	public void setAnimation(UnitAnimationType animation) {
		this.animation = animation;
	}

	public ImageCorrection getCorrection() {
		return correction;
	}

	public void setCorrection(ImageCorrection correction) {
		this.correction = correction;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public UnitAnimationSet getAnimations() {
		return animations;
	}

	public void setAnimations(UnitAnimationSet animations) {
		this.animations = animations;
	}
	public int getHealth() {
		return health;
	}
	public void setHealth(int health) {
		this.health = health;
	}

	/**
	 * Gets the attack value of the unit.
	 *
	 * @return The attack value of the unit.
	 */
	public int getAttack() {
		return attack;
	}

	/**
	 * Sets the attack value of the unit.
	 *
	 * @param attack The new attack value to set.
	 */
	public void setAttack(int attack) {
		this.attack = attack;
	}

	/**
	 * Gets the owner of the unit.
	 *
	 * @return The player who owns the unit.
	 */
	public Player getOwner() {
		return owner;
	}

	/**
	 * Sets the owner of the unit.
	 *
	 * @param owner The player to set as the owner of the unit.
	 */
	public void setOwner(Player owner) {
		this.owner = owner;
	}

	/**
	 * Checks if the unit has moved in the current turn.
	 *
	 * @return True if the unit has moved, false otherwise.
	 */
	public boolean hasMoved() {
		return hasMoved;
	}

	/**
	 * Sets whether the unit has moved in the current turn.
	 *
	 * @param hasMoved True if the unit has moved, false otherwise.
	 */
	public void setHasMoved(boolean hasMoved) {
		this.hasMoved = hasMoved;
	}

	/**
	 * Checks if the unit has attacked in the current turn.
	 *
	 * @return True if the unit has attacked, false otherwise.
	 */
	public boolean hasAttacked() {
		return hasAttacked;
	}

	/**
	 * Sets whether the unit has attacked in the current turn.
	 *
	 * @param hasAttacked True if the unit has attacked, false otherwise.
	 */
	public void setHasAttacked(boolean hasAttacked) {
		this.hasAttacked = hasAttacked;
	}

	/**
	 * This command sets the position of the Unit to a specified tile.
	 * 
	 * @param tile
	 */
	@JsonIgnore
	public void setPositionByTile(Tile tile) {
		position = new Position(tile.getXpos(), tile.getYpos(), tile.getTilex(), tile.getTiley());
	}

	/**
	 * Returns a string representation of the Unit object.
	 * The string includes the unit's ID, name, position, owner, and whether it has moved.
	 *
	 * @return A string representation of the Unit.
	 */
	@Override
	public String toString() {
		return "Unit [id=" + id + ", name=" + name + ", position=" + position + ", owner=" + owner + ", hasMoved=" + hasMoved + "]";
	}

	public Tile getActiveTile(Board board) {
		return board.getTile(position.getTilex(), position.getTiley());

	}

	public int getMaxHealth() {
		// TODO Auto-generated method stub
		return this.maxHealth; // Return the maximum health of the unit
	}
	
	public int updateMaxHealth(int newMaxHealth) {
		return this.maxHealth = newMaxHealth;
	} // Set the maximum health of the unit

}
