package structures.basic.player;

import akka.actor.ActorRef;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import structures.GameState;
import structures.basic.Unit;

import java.util.List;

public class AIPlayer extends Player {
	@JsonIgnore
	private final AIManager aiManager;

	public AIPlayer(GameState gameState) {
		super();
		this.aiManager = new AIManager(gameState, this);
	}

	public AIManager getAiManager() {
		return aiManager;
	}

	public void takeTurn(ActorRef out, JsonNode message) {
		aiManager.takeTurn(out, message);
	}

	@Override
	public List<Unit> getUnits() {
		return super.getUnits();
	}

	@Override
	public String toString() {
		return "AI Player";
	}

}