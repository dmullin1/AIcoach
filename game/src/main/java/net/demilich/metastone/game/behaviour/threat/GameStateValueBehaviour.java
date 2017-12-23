package net.demilich.metastone.game.behaviour.threat;

import java.util.ArrayList;
import java.util.List;

import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.trainingmode.RequestTrainingDataNotification;
import net.demilich.metastone.trainingmode.TrainingData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.DiscoverAction;

import net.demilich.metastone.game.*;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.events.CardPlayedEvent;
import net.demilich.metastone.game.events.GameEvent;
//import net.demilich.metastone.gui.playmode.GameBoardView;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.actions.PlayCardAction;

import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.IBehaviour;
import net.demilich.metastone.game.behaviour.heuristic.IGameStateHeuristic;
import net.demilich.metastone.game.cards.Card;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.cards.MinionCard;

//human controlled part
import net.demilich.metastone.BuildConfig;
import net.demilich.metastone.GameNotification;
//import net.demilich.metastone.game.behaviour.human.HumanActionOptions;
import net.demilich.metastone.game.behaviour.human.*;
import net.demilich.metastone.game.actions.IActionSelectionListener;
//import net.demilich.metastone.game.behaviour.human.HumanBehavior;

import java.lang.*;
import java.io.*;
import java.net.*;




//class Server{
	public class GameStateValueBehaviour extends Behaviour{
	
			//private final FeatureVector weights;
	
		//public GameStateValueBehaviour(FeatureVector vector) {
			//	this.weights = vector;
		//}
		private final Logger logger = LoggerFactory.getLogger(GameStateValueBehaviour.class);
	//	private final jdk.internal.instrumentation.Logger logger = LoggerFactory.getLogger(GameStateValueBehaviour.class);
	
		private static IGameStateHeuristic heuristic;
		private FeatureVector featureVector;
		private String nameSuffix = "";
		private boolean waitingForInput;
	
	
		public GameStateValueBehaviour() {	
		}
	
		
	
		public GameStateValueBehaviour(FeatureVector featureVector, String nameSuffix) {
			this.featureVector = featureVector;
			this.nameSuffix = nameSuffix;
			this.heuristic = new ThreatBasedHeuristic(featureVector);
		}
	
		public static double alphaBeta(GameContext context, int playerId, GameAction action, int depth) {
			GameContext simulation = context.clone();
			simulation.getLogic().performGameAction(playerId, action);
			if (depth == 0 || simulation.getActivePlayerId() != playerId || simulation.gameDecided()) {
				return heuristic.getScore(simulation, playerId);
			}
	
			List<GameAction> validActions = simulation.getValidActions();
	
			double score = Float.NEGATIVE_INFINITY;
	
			for (GameAction gameAction : validActions) {
				score = Math.max(score, alphaBeta(simulation, playerId, gameAction, depth - 1));
				if (score >= 100000) {
					break;
				}
			}
	
			return score;
		}
	
		private void answerTrainingData(TrainingData trainingData) {
			featureVector = trainingData != null ? trainingData.getFeatureVector() : FeatureVector.getFittest();
			heuristic = new ThreatBasedHeuristic(featureVector);
			nameSuffix = trainingData != null ? "(trained)" : "(untrained)";
		}
	
		@Override
		public IBehaviour clone() {
			if (featureVector != null) {
				return new GameStateValueBehaviour(featureVector.clone(), nameSuffix);
			}
			return new GameStateValueBehaviour();
		}
	
		@Override
		public String getName() {
			return "Game state value (human controlled) " + nameSuffix;
		}
	
		@Override
		public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
			requestTrainingData(player);
			List<Card> discardedCards = new ArrayList<Card>();
			for (Card card : cards) {
				if (card.getBaseManaCost() > 3) {
					discardedCards.add(card);
				}
			}
			return discardedCards;
		}
	
		@Override
		public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
			if (validActions.size() == 1) {
				return validActions.get(0);
			}
	
			int depth = 2;
			// when evaluating battlecry and discover actions, only optimize the immediate value
			if (validActions.get(0).getActionType() == ActionType.BATTLECRY) {
				depth = 0;
			} else if (validActions.get(0).getActionType() == ActionType.DISCOVER) {
				return validActions.get(0);
			}
	
			GameAction bestAction = validActions.get(0); //these were all 0

		double bestScore = Double.NEGATIVE_INFINITY;

		for (GameAction gameAction : validActions) {
			double score = alphaBeta(context, player.getId(), gameAction, depth);
			if (score > bestScore) {
				bestAction = gameAction;
				bestScore = score;
			}
		}
		/*	GameAction secondAction = validActions.get(1);
			GameAction thirdAction = validActions.get(1);
			GameAction fourthAction = validActions.get(1);
			GameAction fifthAction = validActions.get(1);
			GameAction selectedAction;

			double bestScore = Double.NEGATIVE_INFINITY;
			double secondScore = Double.NEGATIVE_INFINITY;
			double thirdScore = Double.NEGATIVE_INFINITY;
			double fourthScore = Double.NEGATIVE_INFINITY;
			double fifthScore = Double.NEGATIVE_INFINITY;
	//&& (bestAction.isSameActionGroup(secondAction))
			for (GameAction gameAction : validActions) {
				double score = alphaBeta(context, player.getId(), gameAction, depth);
				if (score > bestScore ) {
					fifthAction = fourthAction;
					fourthAction = thirdAction;
					thirdAction = secondAction;
					if(!bestAction.isSameActionGroup(secondAction)){
					secondAction = bestAction;
				}
					bestAction = gameAction;
					fifthScore = fourthScore;
					fourthScore = thirdScore;
					thirdScore = secondScore;
					if(!bestAction.isSameActionGroup(secondAction)){
					secondScore = bestScore;
				}
					bestScore = score;
				}
				else if(score > secondScore && score <= bestScore){
					fifthAction = fourthAction;
					fourthAction = thirdAction;
					if(!secondAction.isSameActionGroup(thirdAction)){
					thirdAction = secondAction;
				}
					secondAction = gameAction;
					fifthScore = fourthScore;
					fourthScore = thirdScore;
					if(!secondAction.isSameActionGroup(thirdAction)){
					thirdScore = secondScore;
				}
					secondScore = score;
				}
				else if(score > thirdScore && score <= secondScore){
					fifthAction = fourthAction;
					if(!thirdAction.isSameActionGroup(fourthAction)){
					fourthAction = thirdAction;
				}
					thirdAction = gameAction;
					fifthScore = fourthScore;
					if(!thirdAction.isSameActionGroup(fourthAction)){
					fourthScore = thirdScore;
				}
					thirdScore = score;
				}
				else if(score > fourthScore && score <= thirdScore){
					fifthAction = fourthAction;
					fourthAction = gameAction;
					fifthScore = fourthScore;
					fourthScore = score;
				}
				else if(score > fifthScore && score <= fifthScore){
					fifthAction = gameAction;
					fifthScore = score;
				}
			}*/

			/*System.out.println("Best action is " + bestAction + "\n");
			printExplanation(context,player,bestAction);
			System.out.println("Alternative action 1 is " + secondAction + "\n");
			printExplanation(context,player,secondAction);
			System.out.println("Alternative action 2 is " + thirdAction + "\n");
			printExplanation(context,player,thirdAction);
			System.out.println("Alternative action 3 is " + fourthAction + "\n");
			printExplanation(context,player,fourthAction);
			System.out.println("Alternative action 4 is " + fifthAction + "\n");
			printExplanation(context,player,fifthAction);*/

		
			
		/*	if(bestAction.isSameActionGroup(secondAction)){
			System.out.println("secondAction is in the same action group as bestAction");
			}
			else{
			System.out.println("secondAction is in a different action group as bestAction");
			}
			if(bestAction.isSameActionGroup(thirdAction)){
			System.out.println("thirdAction is in the same action group as bestAction");
			}
			else{
			System.out.println("thirdAction is in a different action group as bestAction");
			}
			if(bestAction.isSameActionGroup(fourthAction)){
			System.out.println("fourthAction is in the same action group as bestAction");
			}
			else{
			System.out.println("fourthAction is in a different action group as bestAction");
			}
			if(bestAction.isSameActionGroup(fifthAction)){
			System.out.println("fifthAction is in the same action group as bestAction");
			}
			else{
			System.out.println("fifthAction is in a different action group as bestAction" + "\n");
			}*/

	
			return bestAction;

			//HumanBehavior behavior;

			/*waitingForInput = true;
			HumanActionOptions options = new HumanActionOptions(this, context, player, validActions);
			NotificationProxy.sendNotification(GameNotification.HUMAN_PROMPT_FOR_ACTION,options);
			while (waitingForInput) {
				try {
					Thread.sleep(BuildConfig.DEFAULT_SLEEP_DELAY);
					if (context.ignoreEvents()) {
						return null;
					}
				} catch (InterruptedException e) {
				}
			}
			return selectedAction;*/
		}
		public String printExplanation(GameContext context, Player player,GameAction action){

			Player opponent = context.getOpponent(player);
			int opponentHp = opponent.getHero().getEffectiveHp(); //this is actually the "players" hp 
			int remainingHp = player.getHero().getEffectiveHp(); //this is the AI's reamining hp 
			int cardInHand = player.getHand().getCount(); //cards in AI's hand
			if(!(action.getActionType() == ActionType.END_TURN) && !(action.getActionType() == ActionType.PHYSICAL_ATTACK)){
			if(cardInHand >= 5){
				System.out.println("Use surplus of cards in hand / card advantage to gain a tempo");
			}
			if(cardInHand < 4 ){
				System.out.println("This is a risky play because of the small amount of cards that you have. Commit to overall strategy (Go on offensive / protect hero)");
			}
}
			
			int opponentMinions = 0;
			int playerMinions = 0;
			for (Minion minion : opponent.getMinions()) {
			opponentMinions += 1;
		}
			for (Minion minion : player.getMinions()) {
			playerMinions += 1;
		}

			if(action.getActionType() == ActionType.SUMMON){

			GameAction test = action;
			Card card = null;
			PlayCardAction playCardAction = null;
			playCardAction = (PlayCardAction) test;
			card = context.resolveCardReference(playCardAction.getCardReference());


			if(playerMinions < 2){
				return "This minion will not have a significant impact on the current board, but will provide some protection for your hero which you are currently lacking";
			}
			if(remainingHp <= 10){
				return "Play this minion to protect yourself and try to conserve your HP, it is dangerously low";
			}
			if(opponentHp <= 10){
				return "play this minion to go on the offensive / maintain your tempo, your opponent is almost defeated";
			}

			if(card.getAttributeValue(Attribute.ATTACK) > 4){
				return "This card has a high attack value. Use it to get a tempo advantage / minion advantage on the board";
			}
			if(card.getAttributeValue(Attribute.HP) > 4){
				return "This card has a high HP value. Use it to hold a minion advantage and protect your hero";
			}
			if(card.getAttributeValue(Attribute.HP) <= 2 && card.getAttributeValue(Attribute.ATTACK) <= 2){
				return "This card does not have great stats but will put some minions on the board to protect your hero";
			}
			if(card.getAttributeValue(Attribute.HP) > 3 && card.getAttributeValue(Attribute.ATTACK) > 3 && card.getManaCost(context,player) < 5){ //need to add and no attribute
				return "This card has a decent HP / Attack for it's mana cost, but has no other special effects. This will provide some pressure on the board";
			}

			if(remainingHp < 10 && card.getAttributeValue(Attribute.TAUNT) == 1){
				return "Protecting your HP is currently a priority and taunt minions are great for protecting this resource";
			}
			if(opponentHp < 10 && card.getAttributeValue(Attribute.CHARGE) == 1){
				return "You have pressure on the board right now, and Charge minions are great for maintaining that tempo lead";
			}
			if(remainingHp < 10 && card.getAttributeValue(Attribute.DIVINE_SHIELD) == 1){
				return "HP is a priority right now and divine shield minions are great for protecting this resource";
			}
			if(remainingHp > 15 && card.getAttributeValue(Attribute.STEALTH) == 1){
				return "Stealth minions are good for making patient plays, and you have the HP to allow you to wait";
			}
			if(opponentMinions > playerMinions){ //opponent minion advantage cases
				if(card.getAttributeValue(Attribute.CHARGE) == 1){
					return "The opponent has more pressure on the board right now. Use a charge minion to help clear some minions from the board and alleviate pressure";
				}
				if(card.getAttributeValue(Attribute.WINDFURY) == 1){
					return "The opponent has more pressure on the board right now. Use a windfury minion to help clear some minions from the board and alleviate pressure";
				}
				if(card.getAttributeValue(Attribute.FROZEN) == 1){ //Not sure if this is right attribute
					return "The opponent has more pressure on the board right now. Freeze some of your opponent’s minions in order to buy yourself some time";
				}
				if(card.getAttributeValue(Attribute.SILENCED) == 1){
					return "The opponent has more pressure on the board right now. Silence some of your opponent’s minions or order to buy yourself some time";
				}
				if(card.getAttributeValue(Attribute.SPELL_DAMAGE) == 1){ //not sure if right, maybe spell_damage_multiplier
					return "Your spells are stronger now. Use a spell card to clear minions from the field and alleviate pressure, or hit your opponent’s HP directly if it is low to gain tempo advantage";
				}
			}
			if(card.getAttributeValue(Attribute.BATTLECRY) == 1){
				return "If you have a return effect card, you can combo it with this card to send it back to your hand! This will let you re-activate your battlecry minion’s effect again";
			}
			if(card.getAttributeValue(Attribute.ARMOR) == 1 && remainingHp < 10){
				return "Your HP is low. Play this card to help strengthen your defenses";
			}
			if((card.getAttributeValue(Attribute.IMMUNE) == 1 || card.getAttributeValue(Attribute.IMMUNE_HERO) == 1) && remainingHp < 10){
				return "Your HP is low. Play this card to help strengthen your defenses";
			}
			if(card.getAttributeValue(Attribute.OVERLOAD) == 1 && opponentMinions > playerMinions){
				return "Use this card to summon surprisingly strong minions, allowing you to destroy your opponent’s minions and regain tempo";
			}
			if(card.getAttributeValue(Attribute.DECK_TRIGGER) == 1 && (opponentHp > remainingHp || opponentMinions > playerMinions)){
				return "Your opponent has the advantage right now. Play this card to re-gain some tempo, at the cost of discarding some cards";
			}
			if(card.getAttributeValue(Attribute.HEAL_AMPLIFY_MULTIPLIER) == 1 && remainingHp < 10){ //is this the right attribute?
				return "Your HP is low. Summon this card to quickly gain some HP back";
			}
			if(card.getAttributeValue(Attribute.MANA_COST_MODIFIER) == 1){
				return "This card will spend all of your mana in exchange for added attack power. Calculate how much mana you can afford to sink into this effect before playing this card!";
			}

			}

			if(action.getActionType() == ActionType.END_TURN){
				return "There are no advantageous moves left. You should end your turn";
			}

			if(action.getActionType() == ActionType.PHYSICAL_ATTACK){
				
				if(opponentMinions > playerMinions){
					return "Use this attack on an opponents minion to try and gain some board advantage";
				}
				if(opponentMinions < playerMinions){
					return "Use this attack on the opponent's hero if you believe that you could hold a minion advantage after doing so";
				}
				if(opponentHp > remainingHp && opponentMinions < 3){
					return "The opponent has an HP advantage and not many minions on the board. Use this attack to try and regain an HP advantage";
				}
				if(opponentHp < remainingHp && opponentMinions < 3){
					return "The opponent has an HP disadvantage and not many minions on the board. Using this attack on opponents minions or hero would be equally advantageous";
				}
				if(opponentHp == remainingHp && opponentMinions >= 1){
					return "Use this attack on either a minion or the enemy hero to deal some damage";
				}
				if(opponentHp == remainingHp && opponentMinions == 0){
					return "Use this attack on the enemy hero to get an HP advantage";
				}

			}
			if(action.getActionType() == ActionType.SPELL){
			GameAction test = action;
			Card card = null;
			PlayCardAction playCardAction = null;
			playCardAction = (PlayCardAction) test;
			card = context.resolveCardReference(playCardAction.getCardReference());
			//System.out.println("card is: " + card.getName() + "**********");
			//System.out.println("attributes are: " +card.getAttributes());

			if(card.getName().equals("The Coin")){
				return "Using this spell will allow you to play a higher mana card in your hand";
			}
			if(card.getName().equals("Frostbolt") && opponentMinions >= 1){
				return "Use this card on a powerful enemy minion to freeze it and render it useless";
			}
			if(card.getName().equals("Arcane Intellect")){
				return "Only use this card if you are low on cards or are unhappy with your current hand";
			}
			if(card.getName().equals("Fireball")){
				return "Use this card to deal heavy amounts of damage to a powerful enemy minion or to gain an HP advantage over the enemy hero";
			}
			if(card.getName().equals("Blizzard")){
				return "Only use this card when the enemy has a significant minion advantage as it will freeze and deal damage to all";
			}
			if(card.getName().equals("Frost Nova")){
				return "Use this card when the opponent can attack with a large amount of minions as it will freeze them and disallow this";
			}
			if(card.getName().equals("Ice Lance")){
				return "Use this spell on an already frozen minion as it will deal significant bonus damage";
			}
			if(card.getName().equals("Flamestrike")){
				return "Use this spell to try and clear the enemy board when they have a large amount of minions";
			}
			if(card.getName().equals("Forgotten Torch")){
				return "Use this card to deal some damage as well get more cards in your hand. This is best when you are low on cards";
			}
			}

			if(action.getActionType() == ActionType.HERO_POWER){

			}
			if(action.getActionType() == ActionType.EQUIP_WEAPON){
				return "This weapon will make your hero more powerful as well as give it more defenses. Play it with the intention of attacking with your hero";

			}
			return "No futher Explanation";

		}
	
		private void requestTrainingData(Player player) {
			if (heuristic != null) {
				return;
			}
	
			RequestTrainingDataNotification request = new RequestTrainingDataNotification(player.getDeckName(), this::answerTrainingData);
			NotificationProxy.notifyObservers(request);
		}
	
	}
//}