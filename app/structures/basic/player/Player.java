package structures.basic.player;
import structures.basic.*;
import structures.basic.cards.Card;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class representing a Player in the game.
 * A player has attributes such as health, mana, a hand of cards,
 * a deck of cards, and a list of active units.
 *
 * The Player class also manages the avatar unit, turn progression,
 * and card drawing mechanics.
 *
 * @author Dr. Richard McCreadie
 * @author Junzhe Wu
 */
public abstract class Player {

	protected int health;
	protected int mana;
	protected Hand hand;
	protected Deck deck;
	@JsonManagedReference
	protected Unit avatar;
	protected List<Unit> units;
	protected int turn = 1;
	private int robustness;

	/**
	 * Constructor that initialises the player with standard attributes.
	 * Health starts at 20, mana starts at 0, and a new hand and deck are created.
	 */
	public Player() {
		super();
		this.health = 20;
		this.mana = 0;
		this.hand = new Hand();
		this.deck=new Deck(this);
		this.units = new ArrayList<>();
	}

	/**
	 * Constructor with custom health and mana values.
	 *
	 * @param health The initial health value.
	 * @param mana The initial mana value.
	 */
	public Player(int health, int mana) {
		super();
		this.health = health;
		this.mana = mana;
	}

	/**
	 * Getters and Setters of related attribute
	 */

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public int getMana() {
		return mana;
	}

	public void setMana(int mana) {
		this.mana = mana;
	}

	public Deck getDeck() {
		return deck;
	}

	public Hand getHand() {
		return hand;
	}

	public void setHand(Hand hand) {
		this.hand = hand;
	}

	public int getTurn() {
		return turn;
	}

	public int increaseTurnNum() {
		return this.turn++;
	}

	public Unit getAvatar() {
		return avatar;
	}

	public void setAvatar(Unit avatar) {
		this.avatar = avatar;
	}

	public List<Unit> getUnits() {
		return units;
	}

	public void addUnit(Unit unit) {
		this.units.add(unit);
	}

	public void removeUnit(Unit unit) {
		this.units.remove(unit);
	}

	/**
	 * Draw a card from the deck and add it to the hand.
	 * If the deck is empty or null, it returns null.
	 *
	 * @return The drawn card, or null if no card can be drawn.
	 */
	public Card drawCard() {
		if (this.deck == null) {
			System.err.println("Deck is null.");
			return null;
		}
        Card card = this.deck.drawCard();
		if (card != null) {
			this.hand.addCard(card);
			return card;
		}
		return null;
	}

	/**
	 * Sets the player's robustness level. If the value is negative, it is set to 0.
	 *
	 * @param value The new robustness value.
	 */
	public void setRobustness(int value) {
		this.robustness = value;
		this.robustness = Math.max(this.robustness, 0);
	}

	public int getRobustness() {
		return this.robustness;
	}

}
