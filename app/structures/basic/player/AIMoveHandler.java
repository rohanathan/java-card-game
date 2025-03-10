package structures.basic.player;

import structures.GameState;
import structures.basic.*;
import structures.manager.BoardManager;
import structures.manager.CombatHandler;

import java.util.*;

/**
 * The {@code AIMoveHandler} class is responsible for handling AI-controlled movement and attack decisions.
 * It gathers potential movements and attacks, evaluates their effectiveness, and executes the best possible action.
 * <p>
 * It collaborates with the {@link GameState}, {@link CombatHandler}, and {@link BoardManager} to determine
 * valid movements and attacks based on the current game state.
 * </p>
 */

public class AIMoveHandler {
    private final GameState gameState;
    private final AIPlayer aiPlayer;
    /**
     * Constructs an {@code AIMoveHandler} to manage the AI's movement and attack execution.
     *
     * @param gameState The current game state.
     * @param aiPlayer  The AI player instance.
     */
    public AIMoveHandler(GameState gameState, AIPlayer aiPlayer) {
        this.gameState = gameState;
        this.aiPlayer = aiPlayer;
    }

    /**
     * Performs attacks by selecting the best available target and executing the attack.
     * The process continues until no valid attacks remain.
     */
    public void performAttacks() {
        List<PossibleAttack> attacks = gatherPossibleAttacks();
        while (!attacks.isEmpty()) {
            PossibleAttack best = selectBestAttack(attacks);
            if (best == null || best.unit.hasAttacked()) break;

            executeAttack(best);
            attacks = gatherPossibleAttacks(); // Refresh list
            
        }
    }

    /**
     * Performs movements by selecting the best possible move for each unit.
     * The process continues until no valid moves remain.
     */
    public void performMovements() {
        List<PossibleMovement> moves = gatherPossibleMoves();
        while (!moves.isEmpty()) {
            PossibleMovement best = selectBestMove(moves);
            if (best == null || best.unit.hasMoved()) break;

            executeMovement(best);
            moves = gatherPossibleMoves(); // Refresh list
        }
    }

    /**
     * Gathers all possible attacks that the AI can perform.
     *
     * @return A list of {@link PossibleAttack} objects representing potential attacks.
     */
    private List<PossibleAttack> gatherPossibleAttacks() {
        List<PossibleAttack> attacks = new ArrayList<>();
        for (Unit unit : aiPlayer.getUnits()) {
            if (!unit.hasAttacked()) {
                for (Tile target : gameState.getCombatHandler().findAttackTargets(unit)) {
                    attacks.add(new PossibleAttack(unit, target));
                }
            }
        }
        return attacks;
    }

    /**
     * Gathers all possible movements that the AI can perform.
     *
     * @return A list of {@link PossibleMovement} objects representing potential movements.
     */
    private List<PossibleMovement> gatherPossibleMoves() {
        List<PossibleMovement> moves = new ArrayList<>();
        for (Unit unit : aiPlayer.getUnits()) {
            if (!unit.hasMoved()) {
                for (Tile tile : gameState.getBoardManager().getValidMoves(gameState.getBoard().getTiles(), unit)) {
                    if (!tile.isOccupied()) {
                        moves.add(new PossibleMovement(unit, tile));
                    }
                }
            }
        }
        return moves;
    }

    /**
     * Selects the best attack option based on a scoring mechanism.
     *
     * @param attacks A list of possible attacks.
     * @return The best {@link PossibleAttack} or {@code null} if no valid attack exists.
     */
    private PossibleAttack selectBestAttack(List<PossibleAttack> attacks) {
        attacks.forEach(this::evaluateAttack);
        return attacks.stream()
                .filter(a -> a.score > 0)
                .max(Comparator.comparingInt(a -> a.score))
                .orElse(null);
    }

    /**
     * Selects the best movement option based on a scoring mechanism.
     *
     * @param moves A list of possible movements.
     * @return The best {@link PossibleMovement} or {@code null} if no valid move exists.
     */
    private PossibleMovement selectBestMove(List<PossibleMovement> moves) {
        moves.forEach(this::evaluateMovement);
        return moves.stream()
                .filter(m -> m.score > 0)
                .max(Comparator.comparingInt(m -> m.score))
                .orElse(null);
    }

    /**
     * Evaluates an attack by assigning a score based on priority conditions.
     *
     * @param attack The attack option to evaluate.
     */
    private void evaluateAttack(PossibleAttack attack) {
        Unit target = attack.tile.getUnit();
        int score = 0;

        if (target == null) return;

        if (target == gameState.getHuman().getAvatar()) score += 50;
        if (target.getHealth() <= attack.unit.getAttack()) score += 100;
        if (isProvoker(target)) score += 80;

        attack.score = score;
    }

    /**
     * Evaluates a movement by assigning a score based on positioning advantages.
     *
     * @param move The movement option to evaluate.
     */
    private void evaluateMovement(PossibleMovement move) {
        int score = 0;
        score += 100 - calculateDistance(move.tile,
                gameState.getHuman().getAvatar().getActiveTile(gameState.getBoard())) * 10;

        if (isProvoker(move.unit)) {
            score += 50;
        }

        move.score = score;
    }

    /**
     * Calculates the Manhattan distance between two tiles.
     *
     * @param a The first tile.
     * @param b The second tile.
     * @return The Manhattan distance between the tiles.
     */
    private int calculateDistance(Tile a, Tile b) {
        return Math.abs(a.getTilex() - b.getTilex()) +
                Math.abs(a.getTiley() - b.getTiley());
    }

    /**
     * Executes an attack action for the AI.
     *
     * @param attack The attack action to execute.
     */
    private void executeAttack(PossibleAttack attack) {
        gameState.getCombatHandler().attack(attack.unit, attack.tile.getUnit());
        attack.unit.setHasAttacked(true);
    }

    /**
     * Executes a movement action for the AI.
     *
     * @param move The movement action to execute.
     */
    private void executeMovement(PossibleMovement move) {
        gameState.getBoardManager().updateUnitPositionAndMove(move.unit, move.tile);
        move.unit.setHasMoved(true);
    }

    /**
     * Represents a potential attack that an AI unit can perform.
     */
    static class PossibleAttack {
        final Unit unit;
        final Tile tile;
        int score;

        /**
         * Constructs a PossibleAttack instance.
         *
         * @param unit The attacking unit.
         * @param tile The target tile containing the enemy unit.
         */
        PossibleAttack(Unit unit, Tile tile) {
            this.unit = unit;
            this.tile = tile;
        }
    }

    /**
     * Represents a potential movement that an AI unit can make.
     */
    static class PossibleMovement {
        final Unit unit;
        final Tile tile;
        int score;

        /**
         * Constructs a PossibleMovement instance.
         *
         * @param unit The unit moving.
         * @param tile The tile to which the unit moves.
         */
        PossibleMovement(Unit unit, Tile tile) {
            this.unit = unit;
            this.tile = tile;
        }
    }
    /**
     * Checks if a given unit is a "provoker," meaning it has abilities that influence movement.
     *
     * @param unit The unit to check.
     * @return {@code true} if the unit is a provoker, {@code false} otherwise.
     */
    private boolean isProvoker(Unit unit) {
        return List.of("Swamp Entangler", "Rock Pulveriser", "Silverguard Knight", "Ironcliff Guardian")
                .contains(unit.getName());
        }
}