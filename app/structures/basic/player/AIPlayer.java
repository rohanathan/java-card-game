package structures.basic.player;

import akka.actor.ActorRef;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import structures.GameState;
import structures.basic.Unit;

import java.util.List;

/**
 * The {@code AIPlayer} class represents an AI-controlled player in the game.
 * It extends the {@link Player} class and delegates AI decision-making 
 * to an instance of {@link AIManager}.
 * <p>
 * Responsibilities:
 * - Delegates turn execution to the {@link AIManager}.
 * - Provides access to AI-controlled units.
 * </p>
 */

public class AIPlayer extends Player {
    
	/** Manages AI decision-making and actions during the game. */
    @JsonIgnore
	private final AIManager aiManager;
	
	/**
     * Constructs an AI player instance and initializes its AI manager.
     *
     * @param gameState The current game state.
     */
	public AIPlayer(GameState gameState) {
		super();
		this.aiManager = new AIManager(gameState, this);
	}
    
	/**
     * Retrieves the AI Manager responsible for decision-making.
     *
     * @return The AIManager instance.
     */
	public AIManager getAiManager() {
		return aiManager;
	}

    /**
     * Executes the AI's turn by delegating to the AI Manager.
     *
     * @param out     The actor reference used for communication with the UI.
     * @param message The JSON message representing game state updates.
     */
	public void takeTurn(ActorRef out, JsonNode message) {
		aiManager.takeTurn(out, message);
	}

    /**
     * Retrieves the list of units controlled by the AI player.
     *
     * @return A list of AI-controlled units.
     */
	@Override
	public List<Unit> getUnits() {
		return super.getUnits();
	}
	
    /**
     * Returns a string representation of the AI player.
     *
     * @return The string "AI Player".
     */
	@Override
	public String toString() {
		return "AI Player";
	}

}