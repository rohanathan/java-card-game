package structures.basic.player;

/**
 * The {@code HumanPlayer} class represents a human-controlled player in the game.
 * This class extends the {@code Player} class and introduces a new attribute
 * {@code wsCounter}, which tracks the number of available Wraithling Swarm units.
 *
 * @author Junzhe Wu
 */
public class HumanPlayer extends Player {

    /**
     * The counter for Wraithling Swarm units available.
     * Default value is 3.
     */
    private int wsCounter = 3;

    /**
     * Default constructor for {@code HumanPlayer}.
     * Initialises the player using the parent {@code Player} constructor.
     */
    public HumanPlayer() {
        super();
    }

    /**
     * Gets the current number of Wraithling Swarm units available.
     *
     * @return The number of Wraithling Swarm units.
     */
    public int getWsCounter() {
        return wsCounter;
    }

    /**
     * Sets the number of Wraithling Swarm units available to the player.
     *
     * @param number The new value for the Wraithling Swarm counter.
     */
    public void setWsCounter(int number) {
        wsCounter = number;
    }
}