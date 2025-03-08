package structures.basic.player;

import structures.GameState;
import structures.basic.*;
import java.util.*;

public class AIMoveHandler {
    private final GameState gameState;
    private final AIPlayer aiPlayer;

    public AIMoveHandler(GameState gameState, AIPlayer aiPlayer) {
        this.gameState = gameState;
        this.aiPlayer = aiPlayer;
    }

    public void performAttacks() {
        List<PossibleAttack> attacks = gatherPossibleAttacks();
        while (!attacks.isEmpty()) {
            PossibleAttack best = selectBestAttack(attacks);
            if (best == null || best.unit.hasAttacked()) break;

            executeAttack(best);
            attacks = gatherPossibleAttacks(); // Refresh list
        }
    }

    public void performMovements() {
        List<PossibleMovement> moves = gatherPossibleMoves();
        while (!moves.isEmpty()) {
            PossibleMovement best = selectBestMove(moves);
            if (best == null || best.unit.hasMoved()) break;

            executeMovement(best);
            moves = gatherPossibleMoves(); // Refresh list
        }
    }

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

    private PossibleAttack selectBestAttack(List<PossibleAttack> attacks) {
        attacks.forEach(this::evaluateAttack);
        return attacks.stream()
                .filter(a -> a.score > 0)
                .max(Comparator.comparingInt(a -> a.score))
                .orElse(null);
    }

    private PossibleMovement selectBestMove(List<PossibleMovement> moves) {
        moves.forEach(this::evaluateMovement);
        return moves.stream()
                .filter(m -> m.score > 0)
                .max(Comparator.comparingInt(m -> m.score))
                .orElse(null);
    }

    private void evaluateAttack(PossibleAttack attack) {
        Unit target = attack.tile.getUnit();
        int score = 0;

        if (target == null) return;

        // 攻击评分逻辑
        if (target == gameState.getHuman().getAvatar()) score += 50;
        if (target.getHealth() <= attack.unit.getAttack()) score += 100;
        if (isProvoker(target)) score += 80;

        attack.score = score;
    }

    private void evaluateMovement(PossibleMovement move) {
        int score = 0;
        // 移动评分逻辑
        score += 100 - calculateDistance(move.tile,
                gameState.getHuman().getAvatar().getActiveTile(gameState.getBoard())) * 10;

        if (isProvoker(move.unit)) {
            score += 50;
        }

        move.score = score;
    }

    private int calculateDistance(Tile a, Tile b) {
        return Math.abs(a.getTilex() - b.getTilex()) +
                Math.abs(a.getTiley() - b.getTiley());
    }

    private void executeAttack(PossibleAttack attack) {
        gameState.getCombatHandler().attack(attack.unit, attack.tile.getUnit());
        attack.unit.setHasAttacked(true);
    }

    private void executeMovement(PossibleMovement move) {
        gameState.getBoardManager().updateUnitPositionAndMove(move.unit, move.tile);
        move.unit.setHasMoved(true);
    }

    // 内部数据结构
    static class PossibleAttack {
        final Unit unit;
        final Tile tile;
        int score;

        PossibleAttack(Unit unit, Tile tile) {
            this.unit = unit;
            this.tile = tile;
        }
    }

    static class PossibleMovement {
        final Unit unit;
        final Tile tile;
        int score;

        PossibleMovement(Unit unit, Tile tile) {
            this.unit = unit;
            this.tile = tile;
        }
    }

    private boolean isProvoker(Unit unit) {
        return List.of("Swamp Entangler", "Rock Pulveriser", "Silverguard Knight", "Ironcliff Guardian")
                .contains(unit.getName());
        }
}