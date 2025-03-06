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
 * AIPlayer extends the Player class, encapsulating AI logic for automated gameplay within the game.
 * This class is responsible for simulating intelligent decision-making for the AI during its turn,
 * including unit movement, attacks, card plays, and other strategic actions. The AI player uses
 * game state information to determine the best moves based on various heuristics and strategies.
 *
 * <p>Key responsibilities include:</p>
 * <ul>
 *     <li>Determining the best sequence of actions during the AI's turn, including moving units,
 *     attacking, summoning units, and casting spells.</li>
 *     <li>Managing the AI's hand, deciding which cards to play based on the current game state.</li>
 *     <li>Evaluating possible movements, attacks, and summons to maximize strategic advantage.</li>
 *     <li>Interacting with the game state to execute the chosen actions and update the board accordingly.</li>
 *     <li>Handling special cases such as stunned units and ensuring the AI adheres to game rules.</li>
 * </ul>
 *
 * <p>This class heavily relies on the {@link GameState} to make informed decisions and interact
 * with the game. It implements logic to automate gameplay in a way that is, hopefully, challenging and engaging
 * for human players.</p>
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

		if (stunnedUnit != null && stunnedUnit.getHealth() > 0) {
			BasicCommands.addPlayer1Notification(out, stunnedUnit.getName() + " is not stunned anymore", 2);
			stunnedUnit = null;
		}
		// make best move
		makeBestMove();
		
		for (Card card : this.hand.getCards()) {
			System.out.println("Card: " + card.getCardname());
		}

		if (gameState.isGameOver) {
			return;
		}

		// ends turn
		EndTurnClicked endTurn = new EndTurnClicked();
		BasicCommands.addPlayer1Notification(out, "   AI ends its turn", 2);
		endTurn.processEvent(out, gameState, message);
	}

	private ArrayList<PossibleMovement> returnAllMovements(GameState gameState) {
		System.out.println("Returning valid movements for AIPlayer");
		ArrayList<PossibleMovement> movements = new ArrayList<>();

		for (Unit unit : this.units) {
			Set<Tile> positions = new HashSet<>();

			if (unit.hasAttacked() && unit.hasMoved()) {
				continue;
			} else if (!unit.hasMoved() && !unit.hasAttacked()) {
				positions = gameState.gameService.getValidMoves(gameState.getBoard().getTiles(), unit);

			}
			for (Tile tile : positions) {
				if (!tile.isOccupied()) {
					movements.add(new PossibleMovement(unit, tile));
				}
			}
		}

		return movements;
	}

	private ArrayList<PossibleAttack> returnAllAttacks(GameState gameState) {
		System.out.println("Returning valid attacks for AIPlayer");
		ArrayList<PossibleAttack> attacks = new ArrayList<>();

		for (Unit unit : this.units) {
			if (unit.hasAttacked()) {
				continue;
			}
			Set<Tile> targets = gameState.gameService.findAttackTargets(unit);

			for (Tile tile : targets) {
				attacks.add(new PossibleAttack(unit, tile));
			}
		}
		return attacks;
	}

	private Set<PossibleMovement> rankMovements(ArrayList<PossibleMovement> movements) {
		System.out.println("Ranking possible movements...");
		if (movements == null) {
			return null;
		}

		Set<PossibleMovement> moves = new HashSet<>(movements);

		for (PossibleMovement move : moves) {
			Unit unit = move.unit;
			// Reset score for each potential movement
			int score = 0;

			if (!unit.hasMoved() && !unit.hasAttacked()) {
				// Check if the unit has the provoke ability
				boolean isProvoker = unit.getName().equals("Swamp Entangler") ||
						unit.getName().equals("Rock Pulveriser") ||
						unit.getName().equals("Silverguard Knight") ||
						unit.getName().equals("Ironcliff Guardian");

				if (isProvoker) {
					// Score based on proximity to the avatar, closer is better
					int distanceToAvatar = calculateDistance(move.tile, avatar.getActiveTile(gameState.getBoard()));
					int proximityScore = Math.max(0, 100 - distanceToAvatar * 10);

					score += proximityScore;

					// Optionally, adjust score based on positioning relative to enemies
					for (Unit enemy : gameState.getHuman().getUnits()) {
						int distanceToEnemy = calculateDistance(move.tile, enemy.getActiveTile(gameState.getBoard()));
						// Optionally, deprioritize movements that would leave the provoker too far from enemies
						score -= Math.max(0, distanceToEnemy - 2) * 5; // Encourage staying within engagement range of enemies
					}
				} else {
					// Scoring for non-provoker units
					ArrayList<Unit> enemyUnits = (ArrayList<Unit>) gameState.getHuman().getUnits();
					for (Unit enemy : enemyUnits) {
						int distanceX = Math.abs(move.tile.getTilex() - enemy.getPosition().getTilex());
						int distanceY = Math.abs(move.tile.getTiley() - enemy.getPosition().getTiley());
						int distanceScore = 9 - (distanceX + distanceY);

						score += distanceScore;
						score += (20 - enemy.getHealth()); // More aggressively move towards lower health units

						if (enemy == gameState.getHuman().getAvatar()) {
							score += 18; // Prioritize attacking the primary human player unit
						}

						if (enemy.getName().equals("Shadow Watcher")) {
							score += 20; // Prioritize quick elimination of Shadow Watcher
						}

						if (enemy.getHealth() <= unit.getAttack()) {
							score += 20; // Prioritize attacking lethal targets
						}

						// Avatar should not lead the line
						if (unit == avatar) {
							// Check if moving would make the avatar the leftmost unit
							if (wouldBeLeftmostAfterMoving(move.tile) && this.units.size() > 1) {
								score -= 50; // Penalize this movement significantly
							}
							boolean shouldAvoid = false;
							int penaltyForCloseEnemies = 100;

							for (Unit enemUnit : gameState.getHuman().getUnits()) {
								if (enemUnit.getAttack() >= 6) {
									int currentDistance = calculateDistance(avatar.getActiveTile(
											gameState.getBoard()), enemy.getActiveTile(gameState.getBoard()));
									int newDistance = calculateDistance(move.tile, enemy.getActiveTile(
											gameState.getBoard()));

									if (newDistance < currentDistance) {
										shouldAvoid = true;
										break; // Found an enemy we should avoid, no need to check further
									}
								}
							}

							if (shouldAvoid) {
								score -= penaltyForCloseEnemies; // Apply penalty if moving closer to a strong enemy
							}
						}
					}
				}

				move.moveQuality = score;
			}
		}
		return moves;
	}

	// Helper method to find the Shadow Watcher unit among human player's units
	private Unit findShadowWatcher(List<Unit> enemyUnits) {
		for (Unit enemy : enemyUnits) {
			if (enemy.getName().equals("Shadow Watcher")) {
				return enemy;
			}
		}
		return null; // Return null if no Shadow Watcher is found
	}

	private boolean wouldBeLeftmostAfterMoving(Tile moveTo) {
		int avatarX = moveTo.getTilex();
		for (Unit ally : this.units) {
			if (ally != this.avatar) {
				if (ally.getActiveTile(gameState.getBoard()).getTilex() < avatarX) {
					// Found an ally that would be left of the avatar's new position
					return false;
				}
			}
		}
		return true; // No ally is left of the avatar's new position, so it would be the leftmost
	}

	// Helper method to calculate distance between two tiles
	private int calculateDistance(Tile a, Tile b) {
		return Math.abs(a.getTilex() - b.getTilex()) + Math.abs(a.getTiley() - b.getTiley());
	}


	private Set<PossibleAttack> rankAttacks(ArrayList<PossibleAttack> attacks) {
		System.out.println("Ranking possible attacks...");
		if (attacks == null) {
			return null;
		}

		Set<PossibleAttack> rankedAttacks = new HashSet<>(attacks);

		for (PossibleAttack attack : rankedAttacks) {
			Unit target = attack.tile.getUnit();
			Unit attacker = attack.unit;
			if (!attacker.hasAttacked()) {
				// Penalize units with no attack
				if (attacker.getAttack() == 0) {
					System.out.println("Unit " + attacker + " has no attack");
					attack.moveQuality = -1;
				}
				else if (target.getName().equals("Player Avatar") && target.getHealth() <= attacker.getAttack()) {
					attack.moveQuality = 690; // Assign the highest value for lethal attacks on the avatar
				}
				else if (target.getName().equals("Shadow Watcher") && target.getHealth() <= attacker.getAttack()) {
					attack.moveQuality = 680; // Assign a really high value for lethal attacks on shadow watcher
				}
				else if (target.getId() >= 1000 && target.getHealth() <= attacker.getAttack()) {
					attack.moveQuality = 90; // Assign a slightly lower value for lethal attacks on wraithlings
				}
				// Prioritize eliminating a unit by checking if the attack is lethal
				else if (target.getHealth() <= attacker.getAttack()) {
					attack.moveQuality = 100; // Assign really high value for lethal attacks

				// Prioritize attacking the provoker at all times to avoid inactivity
				} else if (target.getName().equals("Rock Pulveriser")) {
					attack.moveQuality = 50;

				// Increase value for attacking the primary human player unit, unless it's by the AI's primary unit
				} else if (target == gameState.getHuman().getAvatar() && attacker != this.avatar) {
					if (target.getHealth() <= 10) {
						// Prioritize attacking the primary human player unit if it has low health
						attack.moveQuality = 20;
					} else {
						attack.moveQuality = 10;
					}
				// Avatar should only attack opposing avatar if it has more health than the target
				} else if (target == gameState.getHuman().getAvatar() && attacker == this.avatar) {
					if (attacker.getHealth() > target.getHealth()) {
						attack.moveQuality = 5;
					} else {
						attack.moveQuality = -1;
					}
				// Prioritize attacking Shadow Watcher due to its ability to snowball
				} else if (target.getName().equals("Shadow Watcher")) {
					attack.moveQuality = 120;
					if (target.getAttack() > 6 && attacker.getName().equals("AI Avatar")) {
						attack.moveQuality = 0;
					}
					// Avoid attacking units that will counterattack for greater damage
				} else if (target.getAttack() > attacker.getAttack()) {
					attack.moveQuality = 0;

				} else if (target.getName().equals("Bad Omen") || target.getName().equals("Bloodmoon Priestess")) {
					attack.moveQuality = 8;
					// Don't attack if counterattack will result in death
					if (target.getAttack() > attacker.getHealth()) {
						attack.moveQuality = -1;
					} else {
						attack.moveQuality = 5;
					}
				} else if (target.getName().equals("Shadowdancer")) {
					attack.moveQuality = 7;
					// Don't attack if counterattack will result in death
					if (target.getAttack() > attacker.getHealth()) {
						attack.moveQuality = -1;
					} else {
						attack.moveQuality = 5;
					}
				// Value for attacking any unit not being the avatar by non-avatar AI units
				} else if (target != gameState.getHuman().getAvatar() && attacker != this.avatar) {
					// Don't attack if counterattack will result in death
					if (target.getAttack() > attacker.getHealth()) {
						attack.moveQuality = -1;
					} else {
						attack.moveQuality = 5;
					}
				// Generic attack value
				} else {
					attack.moveQuality = 0;
				}
			}

			System.out.println("Attack " + attacker + " value = " + attack.moveQuality);
		}
		return rankedAttacks;
	}

	private Set<PossibleSummon> rankSummons(ArrayList<PossibleSummon> summons) {
		System.out.println("Ranking possible summons...");
		if (summons == null) {
			return null;
		}

		Unit opponentAvatar = gameState.getHuman().getAvatar();
		Tile opponentAvatarTile = opponentAvatar.getActiveTile(gameState.getBoard());
		Tile aiAvatarTile = this.avatar.getActiveTile(gameState.getBoard());

		Set<PossibleSummon> rankedSummons = new HashSet<>(summons);

		for (PossibleSummon summon : rankedSummons) {
			if (summon.card.getManacost() <= this.mana) {
				Tile summonTile = summon.tile;
				int score = 0;

				// Encourage summoning closer to the opponent's avatar
				int distanceToOpponentAvatar = calculateDistance(summonTile, opponentAvatarTile);
				score += (100 - distanceToOpponentAvatar * 5); // Adjust scoring as needed

				// Encourage summoning closer to Shadow Watcher
				Unit shadowWatcher = findShadowWatcher(gameState.getHuman().getUnits());
				if (shadowWatcher != null) {
					int distanceToShadowWatcher = calculateDistance(summonTile, shadowWatcher.getActiveTile(gameState.getBoard()));
					// Decrease score based on distance, so closer distances have higher scores
					int shadowWatcherScore = Math.max(0, 1000 - distanceToShadowWatcher * 10);
					score += shadowWatcherScore;
				}

				// Optionally, increase score for positions between the AI avatar and opponent units
				boolean isBetween = isTileBetween(aiAvatarTile, summonTile, opponentAvatarTile);
				if (isBetween) {
					score += 50; // Bonus for strategic positioning
				}

				// Score adjustments for summoning powerful units in advantageous positions
				if (summon.card.getManacost() < summon.card.getBigCard().getAttack()) {
					score += 30;
				} else {
					score += 20;
				}

				if (summon.card.getCardname().equals("Silverguard Squire")) {
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
				}

				if (summon.card.getCardname().equals("Saberspine Tiger")) {
					if (distanceToOpponentAvatar <= 3) {
						score += 50;
					}
					if (shadowWatcher != null) {
						int distanceToShadowWatcher = calculateDistance(summonTile, shadowWatcher.getActiveTile(gameState.getBoard()));
						if (distanceToShadowWatcher <= 3) {
							score += 100;
						}
					}
					// Encourage summoning closer to units that have less health remaining than the tiger's attack
					for (Unit enemy : gameState.getHuman().getUnits()) {
						if (enemy.getHealth() <= summon.card.getBigCard().getAttack()) {
							int distanceToEnemy = calculateDistance(summonTile, enemy.getActiveTile(gameState.getBoard()));
							score += 10 * (3 - distanceToEnemy);
						}
					}
				}

				summon.moveQuality = score;
			}
		}
		return rankedSummons;
	}

	// Helper method to determine if a tile is between two other tiles
	private boolean isTileBetween(Tile aiAvatarTile, Tile summonTile, Tile opponentAvatarTile) {
		boolean isBetweenX = (summonTile.getTilex() >= Math.min(aiAvatarTile.getTilex(), opponentAvatarTile.getTilex())) &&
				(summonTile.getTilex() <= Math.max(aiAvatarTile.getTilex(), opponentAvatarTile.getTilex()));
		boolean isBetweenY = (summonTile.getTiley() >= Math.min(aiAvatarTile.getTiley(), opponentAvatarTile.getTiley())) &&
				(summonTile.getTiley() <= Math.max(aiAvatarTile.getTiley(), opponentAvatarTile.getTiley()));

		return isBetweenX && isBetweenY;
	}

	private PossibleSummon findBestSummon(Set<PossibleSummon> summons) {
		System.out.println("AI finding best summon...");
		if (summons == null || summons.isEmpty()) {
			System.out.println("No available summons to evaluate.");
			return null;
		}

		int minValue = 0;
		PossibleSummon bestSummon = null;

		for (PossibleSummon summon : summons) {
			if (summon.moveQuality > minValue) {
				minValue = summon.moveQuality;
				bestSummon = summon;
			}
		}

		if (bestSummon != null) {
			System.out.println("Best summon found: Tile " + bestSummon.tile.getTilex() + " " + bestSummon.tile.getTiley() + " and Unit " + bestSummon.card.getCardname() + " with value = " + bestSummon.moveQuality);
		} else {
			System.out.println("No summon meets the evaluation criteria.");
		}
		return bestSummon;
	}

	private PossibleMovement findBestMovement(Set<PossibleMovement> moves) {
		System.out.println("AI finding best movement...");
		int minValue = 0;
		PossibleMovement bestMove = null;

		for (PossibleMovement move : moves) {
			if (move.moveQuality > minValue) { // Looking for a higher score
				minValue = move.moveQuality;
				bestMove = move;
			}
		}
		if (bestMove != null) {
			System.out.println("Move " + bestMove.unit + " value = " + bestMove.moveQuality);
		} else {
			System.out.println("No available moves");
		}
		return bestMove;
	}


	private PossibleAttack findBestAttack(Set<PossibleAttack> attacks) {
		System.out.println("AI finding best attack...");
		if (attacks == null || attacks.isEmpty()) {
			System.out.println("No available attacks to evaluate.");
			return null;
		}

		int minValue = 0;
		PossibleAttack bestAttack = null;

		for (PossibleAttack attack : attacks) {
			if (attack.moveQuality > minValue) {
				minValue = attack.moveQuality;
				bestAttack = attack;
			}
		}

		if (bestAttack != null) {
			System.out.println("Best attack found: Defender " + bestAttack.tile.getUnit() + " and attacker " + bestAttack.unit + " with value = " + bestAttack.moveQuality);
		} else {
			System.out.println("No attack meets the evaluation criteria.");
		}
		return bestAttack;
	}



	private void makeBestMove() {
		try {
			// AIPlayer makes the best move
			// Some are repeated to accommodate special cases like  Saberspine Tiger
			performAttacks();
			performMovements();
			performCardActions();
			performAttacks();
			performMovements();
			performAttacks();
		} catch (Exception e) {
			System.out.println("AIPlayer interrupted");
		}
	}

	private void performCardActions() {
		while (true) {
			ArrayList<Card> cards = (ArrayList<Card>) this.hand.getCards();
			if (cards.isEmpty()) {
				System.out.println("Cards is empty");
				return;
			}
			PossibleSpell bestSpell = returnBestSpell();
			PossibleSummon bestSummon = returnBestSummon();
			if (bestSpell != null && bestSummon != null) {
				if (bestSpell.moveQuality > bestSummon.moveQuality) {
					gameState.gameService.removeFromHandAndCast(gameState, bestSpell.card, bestSpell.tile);
				} else {
					gameState.gameService.castCardFromHand(bestSummon.card, bestSummon.tile);
				}
			} else if (bestSpell != null) {
				gameState.gameService.removeFromHandAndCast(gameState, bestSpell.card, bestSpell.tile);
			} else if (bestSummon != null) {
				gameState.gameService.castCardFromHand(bestSummon.card, bestSummon.tile);
			} else {
				return;
			}
		}
	}

	// Returns the best summon to perform
	private PossibleSummon returnBestSummon() {
		ArrayList<PossibleSummon> possibleSummons = returnAllSummons(gameState);
		if (possibleSummons.isEmpty()) {
			System.out.println("Summons is empty");
			return null;
		}
		Set<PossibleSummon> rankedSummons = new HashSet<>(rankSummons(possibleSummons));
		PossibleSummon bestSummon = findBestSummon(rankedSummons);
		return bestSummon;
	}

	// Returns the best spell card to cast
	private PossibleSpell returnBestSpell() {
		// Implement spell card casting
		ArrayList<PossibleSpell> allSpells = returnAllSpells(gameState);
		if (allSpells.isEmpty()) {
			System.out.println("Spells is empty");
			return null;
		}
		System.out.println("Spells is not empty, ranking spells to return the best one...to cast");
		Set<PossibleSpell> rankedSpells = new HashSet<>(rankSpells(allSpells));
		PossibleSpell bestSpell = findBestSpell(rankedSpells);
		System.out.println("Best spell found: " + bestSpell.toString());
		return bestSpell;
	}

	private Set<PossibleSpell> rankSpells(ArrayList<PossibleSpell> possibleSpells) {
	    // Create a comparator to compare spells based on their effectiveness
	    Comparator<PossibleSpell> spellComparator = Comparator.comparingInt(spell -> {
	        if (spell.card.getCardname().equals("Sundrop Elixir")) {
	            // Sundrop Elixir should be ranked higher because it heals for 4 health at a low cost
	            return 4 * spell.card.getManacost();
	        } else if (spell.card.getCardname().equals("True Strike")) {
	            // True Strike deals 2 damage at a low cost
	            return 2 * spell.card.getManacost();
	        } else if (spell.card.getCardname().equals("Beam Shock")) {
	            return 5; // Arbitrarily assigning a value of 5 for Beam Shock
	        } else {
	            // Default value for other spells
	            return 0;
	        }
	    });

	    // Sort the spells based on the comparator
	    possibleSpells.sort(spellComparator);
	    
        System.out.println("Spells ranked:");
        
        Iterator<PossibleSpell> iterator = possibleSpells.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
	    // Return a HashSet of the sorted spells
	    return new HashSet<>(possibleSpells);
	    
	}


	private PossibleSpell findBestSpell(Set<PossibleSpell> rankedSpells) {
	    // Initialise variables to keep track of the best spell and its rank
	    PossibleSpell bestSpell = null;
	    int bestRank = Integer.MIN_VALUE;

	    // Iterate over the set of ranked spells
	    for (PossibleSpell spell : rankedSpells) {
	        // Check if the current spell has a higher rank than the best spell found so far
	        if (getSpellRank(spell) > bestRank) {
	            // Update the best spell and its rank
	            bestSpell = spell;
	            bestRank = getSpellRank(spell);
	        }
	    }
	    System.out.println("Best spell found: " + bestSpell.toString() + " with rank " + bestRank);

	    // Return the best spell found
	    return bestSpell;
	}

	// Helper method to calculate the rank of a spell
	private int getSpellRank(PossibleSpell spell) {
	    if (spell.card.getCardname().equals("Sundrop Elixir")) {
	        return 4 * spell.card.getManacost();
	    } else if (spell.card.getCardname().equals("True Strike")) {
	        return 2 * spell.card.getManacost();
	    } else if (spell.card.getCardname().equals("Beam Shock")) {
	        return 5;
	    } else {
	        return 0;
	    }
	}

	private ArrayList<PossibleSpell> returnAllSpells(GameState gameState) {
		ArrayList<PossibleSpell> spells = new ArrayList<>();
		for (Card card : this.hand.getCards()) {
			if (!card.isCreature()) {
				Set<Tile> positions;
				positions = gameState.gameService.getSpellRange(card);
				for (Tile tile : positions) {
					spells.add(new PossibleSpell(card, tile));
					System.out.println("Adding spell " + card.getCardname() + " to list of possible spells");
				}
			}
		}
		System.out.println(Arrays.toString(spells.toArray())+ "Spells");
		
		return spells;
	}

	private ArrayList<PossibleSummon> returnAllSummons(GameState gameState) {
		ArrayList<PossibleSummon> summons = new ArrayList<>();
		for (Card card : this.hand.getCards()) {
			System.out.println("Checking card " + card.getCardname() + " for summoning");
			System.out.println("Card is creature: " + card.isCreature());
			System.out.println("Card manacost: " + card.getManacost());
			System.out.println("AIPlayer mana: " + this.mana);
			if (card.isCreature() && card.getManacost() <= this.mana) {
				Set<Tile> positions;
				positions = gameState.gameService.getValidSummonTiles();
				for (Tile tile : positions) {
					if (!tile.isOccupied()) {
						summons.add(new PossibleSummon(card, tile));
					}
				}
			}
		}
		return summons;
	}


	private void performAttacks() {
		while (true) {
			ArrayList<PossibleAttack> attacks = returnAllAttacks(gameState);
			if (attacks == null || attacks.isEmpty()) {
				System.out.println("No more actions left on the board");
				break;
			}

			Set<PossibleAttack> rankedAttacks = new HashSet<>(rankAttacks(attacks));
			PossibleAttack bestAttack = findBestAttack(rankedAttacks);

			if (bestAttack == null || bestAttack.unit.hasAttacked()) {
				return;
			}
			if (gameState.gameService.isAttackable(bestAttack.unit.getActiveTile(gameState.getBoard()), bestAttack.tile)) {
				System.out.println("Attacking unit on tile " + bestAttack.tile.getTilex() + ", " + bestAttack.tile.getTiley());
				gameState.gameService.attack(bestAttack.unit, bestAttack.tile.getUnit());
			}
		}
	}
	private void performMovements() {
		while (true) {
			ArrayList<PossibleMovement> possibleMoves = returnAllMovements(gameState);
			if (possibleMoves.isEmpty()) {
				System.out.println("No more moves left on the board");
				return;
			}

			Set<PossibleMovement> rankedMovements = new HashSet<>(rankMovements(possibleMoves));
			PossibleMovement bestMove = findBestMovement(rankedMovements);

			if (bestMove == null || bestMove.unit.hasMoved() || bestMove.unit.hasAttacked()) {
				return;
			}

			// Check if the destination tile is unoccupied
			if (!bestMove.tile.isOccupied()) {
				// Perform the move
				gameState.gameService.updateUnitPositionAndMove(bestMove.unit, bestMove.tile);
			}
		}
	}

	@Override
	public String toString() {
		return "AIPlayer";
	}

}
