package structures.basic.cards;

import java.util.Comparator;
import java.util.List;

import org.checkerframework.checker.units.qual.A;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.player.AIPlayer;

public class BeamShock {
	
	public static void stunUnit( GameState gs) {
		
		List<Unit> humanUnits = gs.getHuman().getUnits();
		// Use streams to find the unit with the highest attack value
		Unit u = humanUnits.stream().max(Comparator.comparingInt(Unit::getAttack)).orElse(null);
		AIPlayer ai = (AIPlayer) gs.getAi();
		ai.getAiManager().setStunnedUnit(u);
		Tile activeTile = u.getActiveTile(gs.getBoard());
		if (activeTile != null) {
			gs.gameManager.stunning(activeTile);
		}
		
	}
}
