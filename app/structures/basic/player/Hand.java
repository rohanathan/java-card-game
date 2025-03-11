package structures.basic.player;
import structures.basic.cards.Card;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code Hand} class represents a player's hand of cards.
 * It manages the addition, retrieval, and removal of cards while keeping track of
 * the number of cards in hand.
 *
 * @author Junzhe Wu
 */
public class Hand {
	
    private ArrayList<Card> cards;
    private int numCardsInHand;

    /**
     *  Constructor of hand. Default empty.
     */
    public Hand() {
        this.cards = new ArrayList<>();
    }

    /**
     * Getters of total numbers of cards in hand.
     *
     * @return The number of cards in hand.
     */
    public int getNumCardsInHand() {
        return this.numCardsInHand;
    }

    /**
     * Adds a card to the player's hand.
     *
     * @param drawnCard The card drawn.
     */
    public void addCard(Card drawnCard) {
        cards.add(drawnCard);
        this.numCardsInHand++;
    }

    /**
     * Get a card at a specific index (zero-based).
     *
     * @param index The index of the card in the hand (starting from 0).
     * @return The card at the specified index, or {@code null} if the index out of bound.
     */
    public Card getCardAtIndex(int index) {
        if (index >= 0 && index < cards.size()) {
            return cards.get(index);
        } else {
            return null; 
        }
    }

    /**
     * Get a card at a specific position (one-based).
     *
     * @param position The position of the card in the hand
     * @return The card at the specified position, or {@code null} if the position out of bound.
     */
    public Card getCardAtPosition(int position) {
    	if (position >= 1 && position <= numCardsInHand) {
    		return cards.get(position-1);
    	} else {
    		return null;
    	}
    }

    /**
     * Removes and returns a card at a specific position (one-based).
     *
     * @param position The position of the card in the hand (starting from 1).
     * @return The removed card, or {@code null} if the position is out of bounds.
     */
    public Card removeCard(int position) {
    	if (position >= 1 && position <= numCardsInHand) {
    		this.numCardsInHand--;
    		return cards.remove(position-1);
    	} else {
    		return null;
    	}
    }

    /**
     * A public getter for Jackson.
     *
     * @return A list of cards in the player's hand.
     */
    public List<Card> getCards() {
        return cards;
    }
}


