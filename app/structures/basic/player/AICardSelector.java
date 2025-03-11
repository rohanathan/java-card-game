package structures.basic.player;

import structures.GameState;
import structures.basic.*;
import structures.basic.cards.Card;
import structures.manager.AbilityHandler;
import structures.manager.BoardManager;

import java.util.*;

/**
 * The {@code AICardSelector} class is responsible for handling AI-controlled card selection and casting logic.
 * It evaluates available spells and summoning options, determines the best course of action, and executes the corresponding move.
 * 
 * This class interacts with the {@link GameState}, {@link BoardManager}, and {@link AbilityHandler} to ensure AI's
 * decisions align with game mechanics and resource availability.
 * 
 */
public class AICardSelector {
    private final GameState gameState;
    private final AIPlayer aiPlayer;

    /**
     * Constructs an {@code AICardSelector} to manage AI's card selection and execution.
     *
     * @param gameState The current game state.
     * @param aiPlayer  The AI player instance.
     */
    public AICardSelector(GameState gameState, AIPlayer aiPlayer) {
        this.gameState = gameState;
        this.aiPlayer = aiPlayer;
    }

    /**
     * Determines the best available card action (spell or summon) and executes it.
     * This method loops until no valid card plays are available.
     */
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

    /**
     * Evaluates the best summoning option for the AI.
     *
     * @return The best {@link PossibleSummon} option, or {@code null} if no valid summon is available.
     */
    private PossibleSummon evaluateBestSummon() {
        List<PossibleSummon> options = new ArrayList<>();
        for (Card card : aiPlayer.getHand().getCards()) {
            if (card.isCreature() && card.getManacost() <= aiPlayer.getMana()) {
                for (Tile tile : gameState.getBoardManager().getValidSummonTiles()) {
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

    /**
     * Evaluates the best spell card for the AI to cast.
     *
     * @return The best {@link PossibleSpell} option, or {@code null} if no valid spell is available.
     */
    private PossibleSpell evaluateBestSpell() {
        List<PossibleSpell> options = new ArrayList<>();
        for (Card card : aiPlayer.getHand().getCards()) {
            if (!card.isCreature()) {
                for (Tile tile : gameState.getBoardManager().getSpellRange(card)) {
                    PossibleSpell spell = new PossibleSpell(card, tile);
                    spell.score = calculateSpellScore(card, tile);
                    options.add(spell);
                }
            }
        }
        return options.stream().max(Comparator.comparingInt(s -> s.score)).orElse(null);
    }

    /**
     * Assigns a priority score to a given spell card based on its potential impact.
     *
     * @param card The spell card to evaluate.
     * @param tile The target tile for the spell.
     * @return An integer score representing the spell's effectiveness.
     */
    private int calculateSpellScore(Card card, Tile tile) {
        
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

    /**
     * Assigns a priority score to a given summoning action based on its strategic positioning.
     *
     * @param summon The summoning action to evaluate.
     */
    private void calculateSummonQuality(PossibleSummon summon) {
        int score = 0;
        Tile summonTile = summon.tile;
        Unit humAvatar = gameState.getHuman().getAvatar();

        // Higher score for summoning closer to the enemy avatar
        score += 100 - calculateDistance(summonTile, humAvatar.getActiveTile(gameState.getBoard())) * 5;

        // Bonus for summoning provoker units
        if (summon.card.getCardname().contains("Provoke")) score += 50;
        summon.score = score;
    }

    /**
     * Calculates the Manhattan distance between two tiles.
     *
     * @param a The first tile.
     * @param b The second tile.
     * @return The Manhattan distance between the tiles.
     */
    private int calculateDistance(Tile a, Tile b) {
        return Math.abs(a.getTilex() - b.getTilex()) + Math.abs(a.getTiley() - b.getTiley());
    }

    /**
     * Executes a summon action by placing a unit on the board and deducting AI's mana.
     *
     * @param summon The summoning action to execute.
     */
    private void executeSummon(PossibleSummon summon) {
        gameState.getAbilityHandler().castCardFromHand(summon.card, summon.tile);
        aiPlayer.setMana(aiPlayer.getMana() - summon.card.getManacost());
    }

    /**
     * Executes a spell action by casting the spell and deducting AI's mana.
     *
     * @param spell The spell action to execute.
     */
    private void executeSpell(PossibleSpell spell) {
        gameState.getAbilityHandler().removeFromHandAndCast(gameState, spell.card, spell.tile);
        aiPlayer.setMana(aiPlayer.getMana() - spell.card.getManacost());
    }

    /**
     * Represents a potential summoning action for the AI.
     */  
    static class PossibleSummon {
        final Card card;
        final Tile tile;
        int score;

        /**
         * Constructs a {@code PossibleSummon} instance.
         *
         * @param card The card representing the unit to summon.
         * @param tile The tile on which the unit will be summoned.
         */
        PossibleSummon(Card card, Tile tile) {
            this.card = card;
            this.tile = tile;
        }
    }

    /**
     * Represents a potential spell action for the AI.
     */
    static class PossibleSpell {
        final Card card;
        final Tile tile;
        int score;

        /**
         * Constructs a {@code PossibleSpell} instance.
         *
         * @param card The spell card to be played.
         * @param tile The tile that the spell affects.
         */
        PossibleSpell(Card card, Tile tile) {
            this.card = card;
            this.tile = tile;
        }
    }
}
