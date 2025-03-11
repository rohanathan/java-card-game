package structures.manager;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.*;
import structures.basic.cards.*;
import structures.basic.player.Hand;
import structures.basic.player.HumanPlayer;
import structures.basic.player.Player;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.Set;
/**
 * The {@code AbilityHandler} class is responsible for managing and executing 
 * card abilities, including spell effects, unit summoning, and triggered actions.
 * 
 * This class serves as an intermediary between the game's event flow and 
 * individual card actions. It ensures proper execution of spells, highlights valid
 * targets, updates the board state, and manages mana costs and UI notifications.
 * 
 * This class interacts with:
 * <ul>
 *     <li>{@link GameState} - For accessing the current game state and players.</li>
 *     <li>{@link PlayerManager} - To modify player stats.</li>
 *     <li>{@link BoardManager} - To highlight valid tile selections.</li>
 *     <li>{@link UnitManager} - For summoning units and modifying stats.</li>
 * </ul>
 */
public class AbilityHandler {
    

    private final ActorRef out;
    private final GameState gameState;
    /**
     * Constructs an {@code AbilityHandler} with a reference to the game's 
     * event system and current game state.
     *
     * @param out The ActorRef for handling UI interactions.
     * @param gameState The current game state containing board and player details.
     */
    public AbilityHandler(ActorRef out, GameState gameState) 
    {
        this.out = out;
        this.gameState = gameState; 

    }
    // // highlight tiles for summoning units (does not currently take into account special units)
    /**
    * Highlights the valid spell-casting tiles for a given spell card.
    * This method identifies where a spell can be cast based on its type and
    * highlights the corresponding tiles. It also provides user notifications
    * for certain spells that require interaction.
    *     <li>{@code card} - The spell card being cast.</li>
    *     <li>{@code player} - The player casting the spell.</li>

    * @param card The spell card for which casting tiles should be highlighted.
    * @param player The player attempting to cast the spell.
    */
	public void highlightSpellRange(Card card, Player player) {
		// Validate inputs
		if (card == null  || player == null) {
			System.out.println("Invalid parameters for highlighting summon range.");
			return;
		}
		if (!card.isCreature()) {
			Set<Tile> validCastTiles = gameState.getBoardManager().getSpellRange(card);
			
            // Handle special spell highlighting cases
            switch (card.getCardname()) {
                case "Horn of the Forsaken":
                    gameState.getBoardManager().updateTileHighlight(gameState.getHuman().getAvatar().getActiveTile(gameState.getBoard()), 1);
                    BasicCommands.addPlayer1Notification(out, "Click on the Avatar to apply the spell", 2);
                    break;

                case "Dark Terminus":
                    if (validCastTiles != null) {
                        validCastTiles.forEach(tile -> gameState.getBoardManager().updateTileHighlight(tile, 2)); // Attack target
                    }
                    break;

                case "Wraithling Swarm":
                    if (validCastTiles != null) {
                        validCastTiles.forEach(tile -> gameState.getBoardManager().updateTileHighlight(tile, 1)); // Summoning
                    }
                    BasicCommands.addPlayer1Notification(out, "You have 3 wraithlings to place", 2);
                    break;

                default:
                    System.out.println("Highlighting spell range for: " + card.getCardname());
                    break;
            }
		}

		System.out.println("Highlighting spellrange " + card.getCardname());
	}

    /**
     * Handles the summoning of Wraithlings when "Wraithling Swarm" is cast.
     * Controls the Wraithling counter and highlights valid summoning tiles.
     *
     * @param card The spell card being cast.
     * @param tile The tile where a Wraithling should be summoned.
     */
    public void handleWraithlingSwarmSpell(Card card, Tile tile) {
        // Summon Wraithling on the selected tile
        Wraithling.summonWraithlingToTile(tile, out, gameState);
        HumanPlayer player = (HumanPlayer) gameState.getHuman();
        player.setWsCounter(player.getWsCounter() - 1);

        // If there are more Wraithlings to summon, push the card to action history
        if (player.getWsCounter() > 0) {
            // Highlight tiles for summoning
            highlightSpellRange(card, gameState.getCurrentPlayer());
            BasicCommands.addPlayer1Notification(out, "You can summon " + player.getWsCounter() +" more wraithlings", 5);
            gameState.getActionHistory().push(card);
            gameState.getPlayerManager().modifyPlayerMana(gameState.getCurrentPlayer(), gameState.getCurrentPlayer().getMana() + card.getManacost());
        } else {
            // Remove highlight from all tiles and update hand positions
            BasicCommands.addPlayer1Notification(out, "All wraithlings summoned!", 5);
            player.setWsCounter(3);
        }
    }

    /**
     * Handles casting a spell, updating the player's hand, and applying its effects.
     * 
     * This method removes the spell card from the player's hand, verifies the validity 
     * of the spell cast, applies its effect, and updates the player's mana accordingly.
     *
     * @param gameState The current state of the game.
     * @param spellCard The spell card being cast.
     * @param targetTile The tile where the spell effect is applied.
     */
    public void castSpellAndUpdateHand(GameState gameState, Card spellCard, Tile targetTile) {

        // Validate casting for HumanPlayer
        if (gameState.getCurrentPlayer() instanceof HumanPlayer && !validCast(spellCard, targetTile)) {
            gameState.getBoardManager().removeHighlightFromAll();
            return;
        }

        // Retrieve player and hand details
        Player currentPlayer = gameState.getCurrentPlayer();
        Hand playerHand = currentPlayer.getHand();
        int selectedCardPosition = gameState.getCurrentCardPosition();

        // Find the correct position of the card in hand
        if (selectedCardPosition == 0) {
            for (int i = 1; i <= playerHand.getNumberOfCardsInHand(); i++) {
                if (playerHand.getCardAtPosition(i).equals(spellCard)) {
                    selectedCardPosition = i;
                    break;
                }
            }
        }

        // Remove the card from the player's hand and update UI if it's a HumanPlayer
        if (currentPlayer instanceof HumanPlayer) {
            BasicCommands.deleteCard(out, selectedCardPosition + 1);
            playerHand.removeCardAtPosition(selectedCardPosition);
            gameState.getPlayerManager().updateHandPositions(playerHand);
            gameState.getPlayerManager().notClickingCard();
        } else {
            playerHand.removeCardAtPosition(selectedCardPosition);
        }

        // Process spell effects based on the card name
        switch (spellCard.getCardname()) {
            case "Horn of the Forsaken":
                HornOfTheForsaken.castSpell(gameState, out);
                currentPlayer.setRobustness(currentPlayer.getRobustness() + 3);
                System.out.println("Player's robustness: " + currentPlayer.getRobustness());
                break;

            case "Dark Terminus":
                if (targetTile.getUnit().getOwner() != gameState.getHuman() && 
                    !targetTile.getUnit().getName().equals("AI Avatar")) {
                    
                    gameState.getUnitManager().destroyUnit(targetTile.getUnit());
                    try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
                    Wraithling.summonWraithlingToTile(targetTile, out, gameState);
                }
                break;

            case "Wraithling Swarm":
                handleWraithlingSwarmSpell(spellCard, targetTile);
                break;

            case "Beamshock":
                BeamShock.stunUnit(gameState);
                break;

            case "Truestrike":
                Strike.TrueStrike(gameState, gameState.getHuman().getAvatar(), out);
                break;

            case "Sundrop Elixir":
                Elixir.Sundrop(gameState.getAi().getAvatar(), gameState, out);
                break;

            default:
                System.out.println("Unhandled spell: " + spellCard.getCardname());
                break;
        }

        // Deduct mana cost after spell is cast
        int updatedMana = currentPlayer.getMana() - spellCard.getManacost();
        gameState.getHuman().setMana(updatedMana);
        gameState.getPlayerManager().modifyPlayerMana(currentPlayer, updatedMana);
    }

    /**
     * Validates whether the selected tile is valid for casting the specified spell.
     * 
     * This method checks the tile's highlight mode based on the spell type. For "Horn of the Forsaken"
     * and "Dark Terminus," it verifies that the tile has the correct highlight mode. For "Wraithling Swarm,"
     * if the tile is not highlighted correctly and the player's swarm counter is below 3 (indicating an
     * incomplete summoning process), it resets the counter and deducts the mana cost.
     *
     * @param abilityCard The spell card being cast.
     * @param targetTile The tile selected for casting the spell.
     * @return {@code true} if the cast is valid; {@code false} otherwise.
     */
    // Ensure that the player has selected a valid tile for casting the spell
    private boolean validCast(Card abilityCard, Tile targetTile) {
        if (abilityCard.getCardname().equals("Horn of the Forsaken") &&
                !(targetTile.getHighlightMode() == 1)) {
            return false;
        }
        if (abilityCard.getCardname().equals("Dark Terminus")
                && !(targetTile.getHighlightMode() == 2)) {
            return false;
        }
        if (abilityCard.getCardname().equals("Wraithling Swarm")
                && !(targetTile.getHighlightMode() == 1)) {
            HumanPlayer currentHuman = (HumanPlayer) gameState.getHuman();
    
            // Reset the Wraithling Swarm counter and decrease mana as swarm counter < 3 indicates
            // that the player has started but not finished summoning all 3 Wraithlings
            if (currentHuman.getWsCounter() < 3) {
                BasicCommands.addPlayer1Notification(out, "Wraithling Swarm spell broken! Choose another tile!", 3);
    
                currentHuman.setWsCounter(3);
                // Decrease player's mana after casting the spell
                gameState.getHuman().setMana(currentHuman.getMana() - abilityCard.getManacost());
                gameState.getPlayerManager().modifyPlayerMana(currentHuman, currentHuman.getMana());
            }
            return false;
        }
        return true;
    }
        /**
     * Sends a notification that the specified unit is stunned.
     *
     * @param unitName The name of the unit that has been stunned.
     */
    public void stunnedUnit(String unitName) {     
        BasicCommands.addPlayer1Notification(out, unitName + " is stunned", 2);
    }
      
    /**
     * Plays the stun effect animation on the given tile and notifies the player.
     *
     * <p>This method loads the martyrdom effect, plays it on the specified tile, and then displays a notification
     * indicating that the unit on the tile has been stunned. The method pauses the execution briefly to allow the
     * animation to be visible.
     *
     * @param targetTile The tile on which the stun effect animation will be played.
     */
    public void animateStunEffect(Tile targetTile) {      
        EffectAnimation stunEffect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_martyrdom);
        BasicCommands.playEffectAnimation(out, stunEffect, targetTile);
        BasicCommands.addPlayer1Notification(out, "Beamshock! " + targetTile.getUnit().getName() + " is stunned.", 3);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException interruptionEx) {
            interruptionEx.printStackTrace();
        }
    }
    /**
     * Plays the healing effect animation on the specified tile.
     *
     * <p>This method loads the buff effect, plays the corresponding animation on the provided tile, and pauses
     * the execution momentarily to ensure the animation completes.
     *
     * @param currentTile The tile on which the healing effect animation will be played.
     */
    public void animateHealingEffect(Tile currentTile) {
        EffectAnimation healingEffect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_buff);
        BasicCommands.playEffectAnimation(out, healingEffect, currentTile);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException interruptionEx) {
            interruptionEx.printStackTrace();
        }
    }

    /**
     * Plays the strike effect animation on the specified tile.
     *
     * This method loads the inmolation effect, plays it on the given tile, and introduces a brief pause
     * to ensure that the animation is fully displayed.
     *
     * @param targetTile The tile on which the strike effect animation will be played.
     */
    public void animateStrikeEffect(Tile targetTile) {
        EffectAnimation strikeEffect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_inmolation);
        BasicCommands.playEffectAnimation(out, strikeEffect, targetTile);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException interruptionEx) {
            interruptionEx.printStackTrace();
        }
    }

    /**
     * Removes a card from the player's hand and, if it represents a creature, summons the corresponding unit onto the board.
     * @param card The card to be removed from the hand and potentially used for summoning.
     * @param tile The tile on which the unit should be summoned.
     */
    public void castCardFromHand(Card card, Tile tile) {
        System.out.println("Removing card from hand and summoning " + card.getCardname());

        // Retrieve the current player and their hand
        Player player = gameState.getCurrentPlayer();
        Hand hand = player.getHand();
        int handPosition = gameState.getCurrentCardPosition();

        // If hand position is not set, locate the card in the hand by iterating over the cards
        if (handPosition == 0) {
            for (int i = 1; i <= hand.getNumberOfCardsInHand(); i++) {
                if (hand.getCardAtPosition(i).equals(card)) {
                    handPosition = i;
                    break;
                }
            }
        }
        System.out.println("Current card: " + card.getCardname() + " position " + handPosition);

        // Check if the player has enough mana to cast the card
        if (player.getMana() < card.getManacost()) {
            BasicCommands.addPlayer1Notification(out, "Summoning " + card.getCardname() + " requires more mana!", 2);
            return;
        }

        // Update player's mana after casting the card
        gameState.getPlayerManager().modifyPlayerMana(player, player.getMana() - card.getManacost());

        // Remove the card from hand and update hand positions (if the player is a human)
        if (player instanceof HumanPlayer) {
            // Remove card from hand and delete from UI
            BasicCommands.deleteCard(out, handPosition + 1);
            hand.removeCardAtPosition(handPosition);
            gameState.getPlayerManager().updateHandPositions(hand);
        } else {
            hand.removeCardAtPosition(handPosition);
        }

        // If the card represents a creature, summon the corresponding unit onto the board
        if (card.isCreature()) {
            // Retrieve unit configuration details
            String unit_conf = card.getUnitConfig();
            int unit_id = card.getId();
            gameState.getUnitManager().summonUnit(unit_conf, unit_id, card, tile, player);
        }
    }

}