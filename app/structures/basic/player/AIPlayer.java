package structures.basic.player;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import commands.BasicCommands;
import events.EndTurnClicked;
import structures.GameState;
import structures.basic.*;
import structures.basic.cards.Card;

import java.util.*;

/**
 * AIPlayer class encapsulating AI logic for automated gameplay within the game.
 */
public class AIPlayer extends Player {
	private final GameState gameState;
	public Unit stunnedUnit;

	public AIPlayer(GameState gameState) {
		super();
		this.gameState = gameState;
		this.stunnedUnit = null;
	}

	public void takeTurn(ActorRef out, JsonNode message) {
		notifyIfStunnedUnitRecovered(out);
		makeBestMove();

		if (!gameState.isGameOver) {
			endTurn(out, message);
		}
	}

	private void notifyIfStunnedUnitRecovered(ActorRef out) {
		if (stunnedUnit != null && stunnedUnit.getHealth() > 0) {
			BasicCommands.addPlayer1Notification(out, stunnedUnit.getName() + " is not stunned anymore", 2);
			stunnedUnit = null;
		}
	}

	private void endTurn(ActorRef out, JsonNode message) {
		EndTurnClicked endTurn = new EndTurnClicked();
		BasicCommands.addPlayer1Notification(out, "AI ends its turn", 2);
		endTurn.processEvent(out, gameState, message);
	}

	private void makeBestMove() {
		try {
			performActionsInOrder();
		} catch (Exception e) {
			System.out.println("AIPlayer interrupted");
		}
	}

	private void performActionsInOrder() {
		performAttacks();
		performMovements();
		performCardActions();
		performAttacks();
		performMovements();
		performAttacks();
	}

	private List<PossibleMovement> returnAllMovements() {
		List<PossibleMovement> movements = new ArrayList<>();
		for (Unit unit : this.units) {
			if (!unit.hasMoved() && !unit.hasAttacked()) {
				gameState.getBoardManager().getValidMoves(gameState.getBoard().getTiles(), unit).stream()
						.filter(tile -> !tile.isOccupied())
						.forEach(tile -> movements.add(new PossibleMovement(unit, tile)));
			}
		}
		return movements;
	}

	private List<PossibleAttack> returnAllAttacks() {
		List<PossibleAttack> attacks = new ArrayList<>();
		for (Unit unit : this.units) {
			if (!unit.hasAttacked()) {
				gameState.gameManager.findAttackTargets(unit).forEach(tile -> attacks.add(new PossibleAttack(unit, tile)));
			}
		}
		return attacks;
	}

	private Set<PossibleMovement> rankMovements(List<PossibleMovement> movements) {
		if (movements == null) return null;
		movements.forEach(this::calculateMoveQuality);
		return new HashSet<>(movements);
	}

	private void calculateMoveQuality(PossibleMovement move) {
		int score = 0;
		Unit unit = move.unit;

		if (!unit.hasMoved() && !unit.hasAttacked()) {
			score = calculateProvokeMoveQuality(move, unit, score);
			score = calculateNonProvokeMoveQuality(move, unit, score);
		}

		move.moveQuality = score;
	}

	private int calculateProvokeMoveQuality(PossibleMovement move, Unit unit, int score) {
		if (isProvoker(unit)) {
			int distanceToAvatar = calculateDistance(move.tile, avatar.getActiveTile(gameState.getBoard()));
			score += Math.max(0, 100 - distanceToAvatar * 10);

			for (Unit enemy : gameState.getHuman().getUnits()) {
				int distanceToEnemy = calculateDistance(move.tile, enemy.getActiveTile(gameState.getBoard()));
				score -= Math.max(0, distanceToEnemy - 2) * 5;
			}
		}
		return score;
	}

	private int calculateNonProvokeMoveQuality(PossibleMovement move, Unit unit, int score) {
		if (!isProvoker(unit)) {
			for (Unit enemy : gameState.getHuman().getUnits()) {
				int distanceScore = 9 - calculateDistance(move.tile, enemy.getActiveTile(gameState.getBoard()));
				score += distanceScore + (20 - enemy.getHealth());

				if (enemy == gameState.getHuman().getAvatar()) {
					score += 18;
				}
				if (enemy.getName().equals("Shadow Watcher")) {
					score += 20;
				}
				if (enemy.getHealth() <= unit.getAttack()) {
					score += 20;
				}

				if (unit == avatar && wouldBeLeftmostAfterMoving(move.tile)) {
					score -= 50;
				}
			}
		}
		return score;
	}

	private boolean isProvoker(Unit unit) {
		return List.of("Swamp Entangler", "Rock Pulveriser", "Silverguard Knight", "Ironcliff Guardian")
				.contains(unit.getName());
	}

	private boolean wouldBeLeftmostAfterMoving(Tile moveTo) {
		return this.units.stream()
				.filter(ally -> ally != this.avatar)
				.noneMatch(ally -> ally.getActiveTile(gameState.getBoard()).getTilex() < moveTo.getTilex());
	}

	private int calculateDistance(Tile a, Tile b) {
		return Math.abs(a.getTilex() - b.getTilex()) + Math.abs(a.getTiley() - b.getTiley());
	}

	private Set<PossibleAttack> rankAttacks(List<PossibleAttack> attacks) {
		if (attacks == null) return null;
		attacks.forEach(this::calculateAttackQuality);
		return new HashSet<>(attacks);
	}

	private void calculateAttackQuality(PossibleAttack attack) {
		Unit target = attack.tile.getUnit();
		Unit attacker = attack.unit;
		if (!attacker.hasAttacked()) {
			attack.moveQuality = determineAttackQuality(target, attacker);
		}
	}

	private int determineAttackQuality(Unit target, Unit attacker) {
		if (attacker.getAttack() == 0) return -1;
		if (isLethalAttack(target, attacker)) return 100;
		if (target.getName().equals("Rock Pulveriser")) return 50;
		if (target == gameState.getHuman().getAvatar() && attacker != this.avatar) return target.getHealth() <= 10 ? 20 : 10;
		if (target == gameState.getHuman().getAvatar() && attacker == this.avatar && attacker.getHealth() > target.getHealth()) return 5;
		if (target.getName().equals("Shadow Watcher")) return target.getAttack() > 6 && attacker.getName().equals("AI Avatar") ? 0 : 120;
		if (target.getAttack() > attacker.getAttack()) return 0;
		return 5;
	}

	private boolean isLethalAttack(Unit target, Unit attacker) {
		return target.getHealth() <= attacker.getAttack();
	}

	private void performCardActions() {
		while (true) {
			PossibleSpell bestSpell = returnBestSpell();
			PossibleSummon bestSummon = returnBestSummon();
			if (bestSpell != null && bestSummon != null) {
				if (bestSpell.moveQuality > bestSummon.moveQuality) {
					gameState.gameManager.removeFromHandAndCast(gameState, bestSpell.card, bestSpell.tile);
				} else {
					gameState.gameManager.castCardFromHand(bestSummon.card, bestSummon.tile);
				}
			} else if (bestSpell != null) {
				gameState.gameManager.removeFromHandAndCast(gameState, bestSpell.card, bestSpell.tile);
			} else if (bestSummon != null) {
				gameState.gameManager.castCardFromHand(bestSummon.card, bestSummon.tile);
			} else {
				return;
			}
		}
	}

	private PossibleSummon returnBestSummon() {
		List<PossibleSummon> possibleSummons = returnAllSummons();
		return possibleSummons.isEmpty() ? null : findBestSummon(rankSummons(possibleSummons));
	}

	private PossibleSpell returnBestSpell() {
		List<PossibleSpell> allSpells = returnAllSpells();
		return allSpells.isEmpty() ? null : findBestSpell(rankSpells(allSpells));
	}

	private Set<PossibleSummon> rankSummons(List<PossibleSummon> summons) {
		if (summons == null) return null;
		summons.forEach(this::calculateSummonQuality);
		return new HashSet<>(summons);
	}

	private void calculateSummonQuality(PossibleSummon summon) {
		int score = 0;
		Tile summonTile = summon.tile;
		Unit opponentAvatar = gameState.getHuman().getAvatar();
		Tile opponentAvatarTile = opponentAvatar.getActiveTile(gameState.getBoard());
		Tile aiAvatarTile = this.avatar.getActiveTile(gameState.getBoard());

		score += (100 - calculateDistance(summonTile, opponentAvatarTile) * 5);

		Unit shadowWatcher = findShadowWatcher(gameState.getHuman().getUnits());
		if (shadowWatcher != null) {
			score += Math.max(0, 1000 - calculateDistance(summonTile, shadowWatcher.getActiveTile(gameState.getBoard())) * 10);
		}

		score += isTileBetween(aiAvatarTile, summonTile, opponentAvatarTile) ? 50 : 0;

		if (summon.card.getManacost() < summon.card.getBigCard().getAttack()) {
			score += 30;
		} else {
			score += 20;
		}

		if (summon.card.getCardname().equals("Silverguard Squire")) {
			score += calculateSilverguardSquireScore(aiAvatarTile);
		}

		if (summon.card.getCardname().equals("Saberspine Tiger")) {
			score += calculateSaberspineTigerScore(summonTile, opponentAvatarTile, shadowWatcher);
		}

		summon.moveQuality = score;
	}

	private int calculateSilverguardSquireScore(Tile aiAvatarTile) {
		int score = 0;
		Tile avatarLeft = gameState.getBoard().getTile(aiAvatarTile.getTilex() - 1, aiAvatarTile.getTiley());
		Tile avatarRight = gameState.getBoard().getTile(aiAvatarTile.getTilex() + 1, aiAvatarTile.getTiley());

		if (avatarLeft.isOccupied() && avatarLeft.getUnit().getOwner() == this && avatarRight.isOccupied()
				&& avatarRight.getUnit().getOwner() == this) {
			score += 50;
		} else if (avatarLeft.isOccupied() && avatarLeft.getUnit().getOwner() == this) {
			score += 20;
		} else if (avatarRight.isOccupied() && avatarRight.getUnit().getOwner() == this) {
			score += 20;
		} else {
			score -= 50;
		}
		return score;
	}

	private int calculateSaberspineTigerScore(Tile summonTile, Tile opponentAvatarTile, Unit shadowWatcher) {
		int score = 0;
		if (calculateDistance(summonTile, opponentAvatarTile) <= 3) {
			score += 50;
		}
		if (shadowWatcher != null) {
			int distanceToShadowWatcher = calculateDistance(summonTile, shadowWatcher.getActiveTile(gameState.getBoard()));
			if (distanceToShadowWatcher <= 3) {
				score += 100;
			}
		}
		for (Unit enemy : gameState.getHuman().getUnits()) {
			if (enemy.getHealth() <= 3) {
				int distanceToEnemy = calculateDistance(summonTile, enemy.getActiveTile(gameState.getBoard()));
				score += 10 * (3 - distanceToEnemy);
			}
		}
		return score;
	}

	private boolean isTileBetween(Tile aiAvatarTile, Tile summonTile, Tile opponentAvatarTile) {
		boolean isBetweenX = (summonTile.getTilex() >= Math.min(aiAvatarTile.getTilex(), opponentAvatarTile.getTilex())) &&
				(summonTile.getTilex() <= Math.max(aiAvatarTile.getTilex(), opponentAvatarTile.getTilex()));
		boolean isBetweenY = (summonTile.getTiley() >= Math.min(aiAvatarTile.getTiley(), opponentAvatarTile.getTiley())) &&
				(summonTile.getTiley() <= Math.max(aiAvatarTile.getTiley(), opponentAvatarTile.getTiley()));

		return isBetweenX && isBetweenY;
	}

	private Unit findShadowWatcher(List<Unit> enemyUnits) {
		return enemyUnits.stream().filter(unit -> unit.getName().equals("Shadow Watcher")).findFirst().orElse(null);
	}

	private PossibleSummon findBestSummon(Set<PossibleSummon> summons) {
		return summons.stream().max(Comparator.comparingInt(summon -> summon.moveQuality)).orElse(null);
	}

	private PossibleSpell findBestSpell(Set<PossibleSpell> rankedSpells) {
		return rankedSpells.stream().max(Comparator.comparingInt(this::getSpellRank)).orElse(null);
	}

	private Set<PossibleSpell> rankSpells(List<PossibleSpell> possibleSpells) {
		possibleSpells.forEach(spell -> spell.moveQuality = getSpellRank(spell));
		return new HashSet<>(possibleSpells);
	}

	private int getSpellRank(PossibleSpell spell) {
		switch (spell.card.getCardname()) {
			case "Sundrop Elixir":
				return 4 * spell.card.getManacost();
			case "True Strike":
				return 2 * spell.card.getManacost();
			case "Beam Shock":
				return 5;
			default:
				return 0;
		}
	}

	private List<PossibleSpell> returnAllSpells() {
		List<PossibleSpell> spells = new ArrayList<>();
		for (Card card : this.hand.getCards()) {
			if (!card.isCreature()) {
				gameState.getBoardManager().getSpellRange(card).forEach(tile -> spells.add(new PossibleSpell(card, tile)));
			}
		}
		return spells;
	}

	private List<PossibleSummon> returnAllSummons() {
		List<PossibleSummon> summons = new ArrayList<>();
		for (Card card : this.hand.getCards()) {
			if (card.isCreature() && card.getManacost() <= this.mana) {
				gameState.getBoardManager().getValidSummonTiles().stream()
						.filter(tile -> !tile.isOccupied())
						.forEach(tile -> summons.add(new PossibleSummon(card, tile)));
			}
		}
		return summons;
	}

	private void performAttacks() {
		while (true) {
			List<PossibleAttack> attacks = returnAllAttacks();
			if (attacks.isEmpty()) break;

			PossibleAttack bestAttack = findBestAttack(rankAttacks(attacks));
			if (bestAttack == null || bestAttack.unit.hasAttacked()) return;

			if (gameState.gameManager.isAttackable(bestAttack.unit.getActiveTile(gameState.getBoard()), bestAttack.tile)) {
				gameState.gameManager.attack(bestAttack.unit, bestAttack.tile.getUnit());
			}
		}
	}

	private void performMovements() {
		while (true) {
			List<PossibleMovement> possibleMoves = returnAllMovements();
			if (possibleMoves.isEmpty()) return;

			PossibleMovement bestMove = findBestMovement(rankMovements(possibleMoves));
			if (bestMove == null || bestMove.unit.hasMoved() || bestMove.unit.hasAttacked()) return;

			if (!bestMove.tile.isOccupied()) {
				gameState.getBoardManager().updateUnitPositionAndMove(bestMove.unit, bestMove.tile);
			}
		}
	}

	private PossibleMovement findBestMovement(Set<PossibleMovement> moves) {
		return moves.stream().max(Comparator.comparingInt(move -> move.moveQuality)).orElse(null);
	}

	private PossibleAttack findBestAttack(Set<PossibleAttack> attacks) {
		return attacks.stream().max(Comparator.comparingInt(attack -> attack.moveQuality)).orElse(null);
	}

	@Override
	public String toString() {
		return "AIPlayer";
	}
}
