package structures.manager;



import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Board;
import structures.basic.EffectAnimation;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import structures.basic.player.Hand;
import structures.basic.player.HumanPlayer;
import structures.basic.Position;
import structures.basic.player.Player;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;
import structures.basic.cards.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbilityHandler {
    

    private final ActorRef out;
    private final GameState gameState;
    
    public AbilityHandler(ActorRef out, GameState gameState) 
    {
        this.out = out;
        // Store reference to GameState
        this.gameState = gameState; 

    }
    // // highlight tiles for summoning units (does not currently take into account special units)
	public void highlightSpellRange(Card card, Player player) {
		// Validate inputs
		if (card == null  || player == null) {
			System.out.println("Invalid parameters for highlighting summon range.");
			return;
		}
		if (!card.isCreature()) {
			Set<Tile> validCastTiles = gameState.getBoardManager().getSpellRange(card);
			
			if (card.getCardname().equals("Horn of the Forsaken")) {
				gameState.getBoardManager().updateTileHighlight(gameState.getHuman().getAvatar().getActiveTile(gameState.getBoard()), 1);
				BasicCommands.addPlayer1Notification(out, "Click on the Avatar to apply the spell", 2);
				return;
			}
			if (card.getCardname().equals("Dark Terminus")) {
				validCastTiles.forEach(tile -> gameState.getBoardManager().updateTileHighlight(tile, 2));
				return;
			}
			if (card.getCardname().equals("Wraithling Swarm")) {
				validCastTiles.forEach(tile -> gameState.getBoardManager().updateTileHighlight(tile, 1));
				BasicCommands.addPlayer1Notification(out, "You have 3 wraithlingameState to place", 2);
				return;
			}
		}

		System.out.println("Highlighting spellrange " + card.getCardname());
	}
    // Method for casting the Horn of the Forsaken spell 
	public void HornOfTheForesaken(Card card) { 
        EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.something); 
        BasicCommands.playEffectAnimation(out, effect, gameState.getHuman().getAvatar().getActiveTile(gameState.getBoard())); 
        gameState.getPlayerManager().notClickingCard(); 
        BasicCommands.addPlayer1Notification(out, "You gained +3 robustness!", 2); 
        gameState.getBoardManager().removeHighlightFromAll(); 
 
        try { 
            Thread.sleep(1000); 
        } catch (InterruptedException e) { 
            e.printStackTrace(); 
        } 
    } 
 // Method for casting the Wraithling Swarm spell
 public void WraithlingameStatewarm(Card card, Tile tile) {
    // Number of WraithlingameState to summon
    Wraithling.summonWraithlingToTile(tile, out, gameState);
    HumanPlayer player = (HumanPlayer) gameState.getHuman();
    player.setWraithlingSwarmCounter(player.getWraithlingSwarmCounter() - 1);

    // If there are more WraithlingameState to summon, push the card to action history
    if (player.getWraithlingSwarmCounter() > 0) {
        // Highlight tiles for summoning
        highlightSpellRange(card, gameState.getCurrentPlayer());
        BasicCommands.addPlayer1Notification(out, "You can summon " + player.getWraithlingSwarmCounter() +" more wraithlingameState", 5);
        gameState.getActionHistory().push(card);
        gameState.getPlayerManager().modifyPlayerMana(gameState.getCurrentPlayer(), gameState.getCurrentPlayer().getMana() + card.getManacost());
    } else {
        // Remove highlight from all tiles and update hand positions
        BasicCommands.addPlayer1Notification(out, "All wraithlingameState summoned!", 5);
        player.setWraithlingSwarmCounter(3);
    }
}

// Method in GameManager class
public void removeFromHandAndCast( GameState gameState, Card card, Tile tile) {
		
    if (gameState.getCurrentPlayer() instanceof HumanPlayer &&
            !validCast(card, tile)) {
        gameState.getBoardManager().removeHighlightFromAll();
        return;
    }
    // Remove the card from the player's hand
    Player player = gameState.getCurrentPlayer();
    Hand hand = player.getHand();
    int handPosition = gameState.getCurrentCardPosition();

    if (handPosition == 0) {
        for (int i = 1; i <= hand.getNumberOfCardsInHand(); i++) {
            if (hand.getCardAtPosition(i).equals(card)) {
                handPosition = i;
                break;
            }
        }
    }

    // Remove the card from the player's hand
    // update the positions of the remaining cards if the player is human
    if (player instanceof HumanPlayer) {
        // remove card from hand and delete from UI
        BasicCommands.deleteCard(out, handPosition + 1);
        hand.removeCardAtPosition(handPosition);
        gameState.getPlayerManager().updateHandPositions(hand);
        gameState.getPlayerManager().notClickingCard();

    } else {
        hand.removeCardAtPosition(handPosition);
    }

    // Perform actions based on the card type
    if (card.getCardname().equals("Horn of the Forsaken")) {
        HornOfTheForesaken(card);
        // Increase player's robustness after casting the spell
        gameState.getCurrentPlayer().setRobustness(player.getRobustness() + 3);
        System.out.println("Player's robustness: " + player.getRobustness());
    } 
    if (card.getCardname().equals("Dark Terminus")) {
        // Check if the targeted tile contains an enemy unit
        if (tile.getUnit().getOwner() != gameState.getHuman() &&
                !tile.getUnit().getName().equals("AI Avatar")) {
            
            gameState.getUnitManager().destroyUnit(tile.getUnit());
    try {Thread.sleep(100); } catch (InterruptedException e) {e.printStackTrace();}
            Wraithling.summonWraithlingToTile(tile, out, gameState);
        }
    }
    if (card.getCardname().equals("Wraithling Swarm")) {
        WraithlingameStatewarm(card, tile);
    }
    if (card.getCardname().equals("Beamshock")) {
        BeamShock.stunUnit(gameState);
    }
    if (card.getCardname().equals("Truestrike")) {
        Strike.TrueStrike(gameState, gameState.getHuman().getAvatar(), out);
    }
    if (card.getCardname().equals("Sundrop Elixir")) {
        Elixir.Sundrop(gameState.getAi().getAvatar(), gameState, out);
    }
    
    
    
    // Decrease player's mana after casting the spell
    gameState.getHuman().setMana(player.getMana() - card.getManacost());
    gameState.getPlayerManager().modifyPlayerMana(player, player.getMana());
}

// Ensure that the player has selected a valid tile for casting the spell
    private boolean validCast(Card card, Tile tile) {
    if (card.getCardname().equals("Horn of the Forsaken")&&
            !(tile.getHighlightMode() == 1)) {
        return false;
    }
    if (card.getCardname().equals("Dark Terminus") 
            && !(tile.getHighlightMode() == 2)) {
        return false;
    }
    if (card.getCardname().equals("Wraithling Swarm")
            && !(tile.getHighlightMode() == 1)) {
        HumanPlayer player = (HumanPlayer) gameState.getHuman();

        // Reset the Wraithling Swarm counter and decrease mana as swarm counter < 3 indicates
        // that the player has started but not finished summoning all 3 WraithlingameState
        if (player.getWraithlingSwarmCounter() < 3) {
            BasicCommands.addPlayer1Notification(out, "Wraithling Swarm spell broken! Choose another tile!", 3);

            player.setWraithlingSwarmCounter(3);
            // Decrease player's mana after casting the spell
            gameState.getHuman().setMana(player.getMana() - card.getManacost());
            gameState.getPlayerManager().modifyPlayerMana(player, player.getMana());
        }
        return false;
    }
    return true;
}
    public void stunnedUnit(String name) {     
    BasicCommands.addPlayer1Notification(out, name +" is stunned", 2);
}
    public void stunning(Tile tile) {      
    EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_martyrdom);
    BasicCommands.playEffectAnimation(out, effect, tile);
    BasicCommands.addPlayer1Notification(out, "Beamshock! " + tile.getUnit().getName() + " is stunned.", 3);
    try {
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
    public void healing(Tile currentTile) {
        EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_buff);
        BasicCommands.playEffectAnimation(out, effect, currentTile);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void strike(Tile tile) {

        EffectAnimation effect = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_inmolation);
        BasicCommands.playEffectAnimation(out, effect, tile);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// remove card from hand and summon unit
    public void castCardFromHand(Card card, Tile tile) {
    	System.out.println("Removing card from hand and summoning" + card.getCardname());

		Player player = gameState.getCurrentPlayer();
		Hand hand = player.getHand();
		int handPosition = gameState.getCurrentCardPosition();

		if (handPosition == 0) {
			for (int i = 1; i <= hand.getNumberOfCardsInHand(); i++) {
				if (hand.getCardAtPosition(i).equals(card)) {
					handPosition = i;
					break;
				}
			}
		}
		System.out.println("Current card: " + card.getCardname() + " position " + handPosition);

		// check if enough mana
		if (player.getMana() < card.getManacost()) {
			BasicCommands.addPlayer1Notification(out, "Summoning " + card.getCardname() + " requires more mana!", 2);
			return;
		}

		// update player mana
		gameState.getPlayerManager().modifyPlayerMana(player, player.getMana() - card.getManacost());

		// update the positions of the remaining cards if the player is human
		if (player instanceof HumanPlayer) {
			// remove card from hand and delete from UI
			BasicCommands.deleteCard(out, handPosition + 1);
			hand.removeCardAtPosition(handPosition);
			gameState.getPlayerManager().updateHandPositions(hand);
		} else {
			hand.removeCardAtPosition(handPosition);
		}
		if (card.isCreature()) {
			// get unit config and id
			String unit_conf = card.getUnitConfig();
			int unit_id = card.getId();
			gameState.getUnitManager().summonUnit(unit_conf, unit_id, card, tile, player);
		}
    }
}
