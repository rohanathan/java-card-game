package structures.basic;

import structures.basic.cards.Card;
import structures.basic.player.HumanPlayer;
import structures.basic.player.Player;
import utils.OrderedCardLoader;
import java.util.Collections;


import java.util.List;

public class Deck {
    private List<Card> deck;
    /**
     * Constructor for the Deck class. Initializes the deck by loading cards
     * specific to the given player.
     *
     * @param player The player for whom the deck is being loaded.
     */
    public Deck(Player player) {
        this.deck = loadDeckForPlayer(player);
    }

    /**
     * Loads the deck for the given player. If the player is a HumanPlayer,
     * loads Player 1's cards. Otherwise, loads AI's cards.
     *
     * @param player The player for whom the deck is being loaded.
     * @return The list of cards loaded for the player.
     */
    private List<Card> loadDeckForPlayer(Player player) {
        if (player instanceof HumanPlayer) {
            this.deck = OrderedCardLoader.getPlayer1Cards(2);
        } else {
            this.deck = OrderedCardLoader.getPlayer2Cards(2);
        }
        return this.deck;
    }

    /**
     * Checks if the deck is empty.
     *
     * @return True if the deck is empty, false otherwise.
     */
    public boolean isEmpty() {
        return this.deck.isEmpty(); // an empty deck
    }

    /**
     * Draws a card from the top of the deck. If the deck is empty,
     * returns null and prints a message.
     *
     * @return The drawn card, or null if the deck is empty.
     */
    public Card drawCard() {
        if (deck.isEmpty()) {
            System.out.println("Deck is empty. No card drawn");
            return null;
        }
        return deck.remove(0);
    }

    /**
     * Shuffles the deck using Java's built-in shuffle method.
     * This randomizes the order of the cards in the deck.
     */
    public void shuffle() {
        Collections.shuffle(this.deck); // Shuffle the deck using Java's built-in shuffle method
    }

    /**
     * Gets the list of cards in the deck.
     *
     * @return The list of cards in the deck.
     */
    public List<Card> getDeck() {
        return deck;
    }
}

