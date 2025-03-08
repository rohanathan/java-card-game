package structures.basic.player;

import structures.GameState;
import structures.basic.*;
import structures.basic.cards.Card;
import java.util.*;

public class AICardSelector {
    private final GameState gameState;
    private final AIPlayer aiPlayer;

    public AICardSelector(GameState gameState, AIPlayer aiPlayer) {
        this.gameState = gameState;
        this.aiPlayer = aiPlayer;
    }

    public void performCardActions() {
        while (true) {
            PossibleSpell bestSpell = evaluateBestSpell();
            PossibleSummon bestSummon = evaluateBestSummon();

            if (bestSpell != null && bestSummon != null) {
                if (bestSpell.score > bestSummon.score) executeSpell(bestSpell);
                else executeSummon(bestSummon);
            } else if (bestSpell != null) {
                executeSpell(bestSpell);
            } else if (bestSummon != null) {
                executeSummon(bestSummon);
            } else {
                break;
            }
        }
    }

    private PossibleSummon evaluateBestSummon() {
        List<PossibleSummon> options = new ArrayList<>();
        for (Card card : aiPlayer.getHand().getCards()) {
            if (card.isCreature() && card.getManacost() <= aiPlayer.getMana()) {
                for (Tile tile : gameState.gameManager.getValidSummonTiles()) {
                    if (!tile.isOccupied()) {
                        PossibleSummon summon = new PossibleSummon(card, tile);
                        calculateSummonQuality(summon);
                        options.add(summon);
                    }
                }
            }
        }
        return options.stream().max(Comparator.comparingInt(s -> s.score)).orElse(null);
    }

    private PossibleSpell evaluateBestSpell() {
        List<PossibleSpell> options = new ArrayList<>();
        for (Card card : aiPlayer.getHand().getCards()) {
            if (!card.isCreature()) {
                for (Tile tile : gameState.gameManager.getSpellRange(card)) {
                    PossibleSpell spell = new PossibleSpell(card, tile);
                    spell.score = calculateSpellScore(card, tile);
                    options.add(spell);
                }
            }
        }
        return options.stream().max(Comparator.comparingInt(s -> s.score)).orElse(null);
    }

    private int calculateSpellScore(Card card, Tile tile) {
        // 完整评分逻辑
        switch (card.getCardname()) {
            case "Sundrop Elixir":
                return (tile.getUnit() != null && tile.getUnit().getOwner() == aiPlayer) ? 40 : 0;
            case "True Strike":
                return (tile.getUnit() != null && tile.getUnit().getOwner() != aiPlayer) ? 30 : 0;
            case "Beam Shock":
                return 20;
            default: return 0;
        }
    }

    private void calculateSummonQuality(PossibleSummon summon) {
        int score = 0;
        Tile summonTile = summon.tile;
        Unit humAvatar = gameState.getHuman().getAvatar();
        score += 100 - calculateDistance(summonTile, humAvatar.getActiveTile(gameState.getBoard())) * 5;
        if (summon.card.getCardname().contains("Provoke")) score += 50;
        summon.score = score;
    }

    private int calculateDistance(Tile a, Tile b) {
        return Math.abs(a.getTilex() - b.getTilex()) + Math.abs(a.getTiley() - b.getTiley());
    }

    private void executeSummon(PossibleSummon summon) {
        gameState.getAbilityHandler().castCardFromHand(summon.card, summon.tile);
        aiPlayer.setMana(aiPlayer.getMana() - summon.card.getManacost());
    }

    private void executeSpell(PossibleSpell spell) {
        gameState.getAbilityHandler().removeFromHandAndCast(gameState, spell.card, spell.tile);
        aiPlayer.setMana(aiPlayer.getMana() - spell.card.getManacost());
    }


    // 内部数据结构
    static class PossibleSummon {
        final Card card;
        final Tile tile;
        int score;

        PossibleSummon(Card card, Tile tile) {
            this.card = card;
            this.tile = tile;
        }
    }

    static class PossibleSpell {
        final Card card;
        final Tile tile;
        int score;

        PossibleSpell(Card card, Tile tile) {
            this.card = card;
            this.tile = tile;
        }
    }
}
