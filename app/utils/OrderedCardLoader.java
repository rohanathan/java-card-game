package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import structures.basic.cards.Card;

/**
 * This is a utility class that provides methods for loading the decks for each
 * player, as the deck ordering is fixed. 
 * @author Richard
 *
 */
public class OrderedCardLoader {

	public static String cardsDIR = "conf/gameconfs/cards/";
	// Counter is used to set IDs. Start from 2, because 0 and 1 are reserved for the avatars.
	public static int counter = 2;
	
	/**
	 * Returns all of the cards in the human player's deck in order
	 * @return
	 */
	public static List<Card> getPlayer1Cards(int copies) {
	
		List<Card> cardsInDeck = new ArrayList<Card>(20);
		for (int i =0; i<copies; i++) {
			String[] filenames = new File(cardsDIR).list();
			Arrays.sort(filenames);
			for (String filename :filenames) {
				if (filename.startsWith("1_")) {
					// this is a deck 1 card
					cardsInDeck.add(BasicObjectBuilders.loadCard(cardsDIR+filename, counter++, Card.class));
				}
			}
		}
		
		
		return cardsInDeck;
	}
	
	
	/**
	 * Returns all of the cards in the human player's deck in order
	 * @return
	 */
	public static List<Card> getPlayer2Cards(int copies) {
	
		List<Card> cardsInDeck = new ArrayList<Card>(20);
		for (int i =0; i<copies; i++) {
			String[] filenames = new File(cardsDIR).list();
			Arrays.sort(filenames);
			for (String filename : new File(cardsDIR).list()) {
				if (filename.startsWith("2_")) {
					// this is a deck 2 card
					cardsInDeck.add(BasicObjectBuilders.loadCard(cardsDIR+filename, counter++, Card.class));
				}
			}
		}
		
		return cardsInDeck;
	}
	
}
