package structures.basic.player;

import structures.basic.cards.Card;

import java.util.ArrayList;
import java.util.List;

public class Hand {
	
    private ArrayList<Card> cards;
    private int numberOfCardsInHand;
    

	public Hand() {
        this.cards = new ArrayList<>();
        
    }

    public void addCard(Card drawnCard) {
        cards.add(drawnCard);
        this.numberOfCardsInHand++;
    }
    public Card getCardAtIndex(int index) {
        if (index >= 0 && index < cards.size()) {
            return cards.get(index);
        } else {
            return null; 
        }
    }

    public Card getCardAtPosition(int position) {
    	if (position >= 1 && position <= numberOfCardsInHand) {
    		return cards.get(position-1);
    	} else {
    		return null;
    	}
    }

    public Card removeCardAtPosition(int position) {
    	if (position >= 1 && position <= numberOfCardsInHand) {
    		this.numberOfCardsInHand--;
    		return cards.remove(position-1);
    	} else {
    		return null;
    	}
    }

    public void removeCardAtIndex(int index) {
        if (index >= 0 && index < cards.size()) {
            cards.remove(index);
            this.numberOfCardsInHand--;
        }
    }
    
    public int getNumberOfCardsInHand() {
    	return this.numberOfCardsInHand;
    }

    // Public getter for Jackson to use for serialization
    public List<Card> getCards() {
        return cards;
    }
    
   

}


