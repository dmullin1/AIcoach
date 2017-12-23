package net.demilich.metastone.game.behaviour.human;

import java.util.ArrayList;
import java.util.List;

import net.demilich.metastone.BuildConfig;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.IActionSelectionListener;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.mcts.Node;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.behaviour.threat.GameStateValueBehaviour;
import net.demilich.metastone.game.actions.PlayCardAction;

import javafx.scene.input.MouseEvent;

import java.lang.*;
import java.io.*;
import java.net.*;

import java.util.Scanner;

public class HumanBehaviour extends Behaviour implements IActionSelectionListener {

	private GameAction selectedAction;
	private boolean waitingForInput;
	private List<Card> mulliganCards;
	Scanner reader = new Scanner(System.in);  // Reading from System.in

	@Override
	public String getName() {
		return "<Human controlled>";
	}

	@Override
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		if (context.ignoreEvents()) {
			return new ArrayList<Card>();
		}
		waitingForInput = true;
		HumanMulliganOptions options = new HumanMulliganOptions(player, this, cards);
		NotificationProxy.sendNotification(GameNotification.HUMAN_PROMPT_FOR_MULLIGAN, options);
		while (waitingForInput) {
			try {
				Thread.sleep(BuildConfig.DEFAULT_SLEEP_DELAY);
			} catch (InterruptedException e) {
			}
		}
		return mulliganCards;
	}

	@Override
	public void onActionSelected(GameAction action) {
		this.selectedAction = action;
		waitingForInput = false;
	}

	@Override
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		waitingForInput = true;
		
		GameStateValueBehaviour test = new GameStateValueBehaviour();

			int depth = 2;
			// when evaluating battlecry and discover actions, only optimize the immediate value
			if (validActions.get(0).getActionType() == ActionType.BATTLECRY) {
				depth = 0;
			}// else if (validActions.get(0).getActionType() == ActionType.DISCOVER) {
				//return validActions.get(0);
			//}
			GameAction bestAction = validActions.get(0); //these were all 0
			GameAction secondAction = validActions.get(0);
			GameAction thirdAction = validActions.get(0);
			GameAction fourthAction = validActions.get(0);
			GameAction fifthAction = validActions.get(0);

			double bestScore = Double.NEGATIVE_INFINITY;
			double secondScore = Double.NEGATIVE_INFINITY;
			double thirdScore = Double.NEGATIVE_INFINITY;
			double fourthScore = Double.NEGATIVE_INFINITY;
			double fifthScore = Double.NEGATIVE_INFINITY;
	//&& (bestAction.isSameActionGroup(secondAction))
			for (GameAction gameAction : validActions) {
				double score = GameStateValueBehaviour.alphaBeta(context, player.getId(), gameAction, depth);
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
			}
			//Node newnode = new Node(bestAction, 1);
			//System.out.println(newnode.getVisits());

			System.out.println();
			String explanation1;
			String explanation2;
			String explanation3;
			String explanation4;
			String explanation5;
			if(bestAction.getActionType() == ActionType.SUMMON || bestAction.getActionType() == ActionType.SPELL || bestAction.getActionType() == ActionType.HERO_POWER || bestAction.getActionType() == ActionType.EQUIP_WEAPON){
			PlayCardAction playCardAction1 = (PlayCardAction) bestAction;
			Card card1 = context.resolveCardReference(playCardAction1.getCardReference());

			System.out.println("Best action is " + card1.getName());
			}
			else if (bestAction.getActionType() == ActionType.PHYSICAL_ATTACK){
				System.out.println("Best action is a physical attack");
			}
			else{
				System.out.println("Best Action is " +bestAction);
			}

			explanation1 = test.printExplanation(context,player,bestAction);
			System.out.println(explanation1 + "\n");

			if(secondAction.getActionType() == ActionType.SUMMON || secondAction.getActionType() == ActionType.SPELL || secondAction.getActionType() == ActionType.HERO_POWER || secondAction.getActionType() == ActionType.EQUIP_WEAPON){
			PlayCardAction playCardAction2 = (PlayCardAction) secondAction;
			Card card2 = context.resolveCardReference(playCardAction2.getCardReference());

			System.out.println("Alternative action 1 is " + card2.getName());
			}
			else if (secondAction.getActionType() == ActionType.PHYSICAL_ATTACK){
				System.out.println("Alternative action 1 is a physical attack");
			}
			else{
				System.out.println("Alternative action 1 is " +secondAction);
			}

			explanation2 = test.printExplanation(context,player,secondAction);
			System.out.println(explanation2 + "\n");
			

			if(thirdAction.getActionType() == ActionType.SUMMON || thirdAction.getActionType() == ActionType.SPELL || thirdAction.getActionType() == ActionType.HERO_POWER || thirdAction.getActionType() == ActionType.EQUIP_WEAPON){
			PlayCardAction playCardAction3 = (PlayCardAction) thirdAction;
			Card card3 = context.resolveCardReference(playCardAction3.getCardReference());

			System.out.println("Alternative action 2 is " + card3.getName());
			}
			else if (thirdAction.getActionType() == ActionType.PHYSICAL_ATTACK){
				System.out.println("Alternative action 2 is a physical attack");
			}
			else{
				System.out.println("Alternative action 2 is " +thirdAction);
			}
			explanation3 = test.printExplanation(context,player,thirdAction);
			System.out.println(explanation3 + "\n");

			if(fourthAction.getActionType() == ActionType.SUMMON || fourthAction.getActionType() == ActionType.SPELL || fourthAction.getActionType() == ActionType.HERO_POWER || fourthAction.getActionType() == ActionType.EQUIP_WEAPON){
			PlayCardAction playCardAction4 = (PlayCardAction) fourthAction;
			Card card4 = context.resolveCardReference(playCardAction4.getCardReference());

			System.out.println("Alternative action 3 is " + card4.getName());
			}
			else if (fourthAction.getActionType() == ActionType.PHYSICAL_ATTACK){
				System.out.println("Alternative action 1 is a physical attack");
			}
			else{
				System.out.println("Alternative action 3 is " +fourthAction);
			}
			explanation4 = test.printExplanation(context,player,fourthAction);
			System.out.println(explanation4 + "\n");

			if(fifthAction.getActionType() == ActionType.SUMMON || fifthAction.getActionType() == ActionType.SPELL || fifthAction.getActionType() == ActionType.HERO_POWER || fifthAction.getActionType() == ActionType.EQUIP_WEAPON){
			PlayCardAction playCardAction5 = (PlayCardAction) fifthAction;
			Card card5 = context.resolveCardReference(playCardAction5.getCardReference());

			System.out.println("Alternative action 4 is " + card5.getName());
			}
			else if (fifthAction.getActionType() == ActionType.PHYSICAL_ATTACK){
				System.out.println("Alternative action 4 is a physical attack");
			}
			else{
				System.out.println("Alternative action 4 is " +fifthAction);
			}
			explanation5 =test.printExplanation(context,player,fifthAction);
			System.out.println(explanation5 );
			System.out.println();
		

		int i = 1;

		for (GameAction gameAction : validActions) {
			//System.out.println("Action "+ i + " is " + validActions.get(i-1));

			if(validActions.get(i-1).getActionType() == ActionType.SUMMON || validActions.get(i-1).getActionType() == ActionType.SPELL || validActions.get(i-1).getActionType() == ActionType.HERO_POWER || validActions.get(i-1).getActionType() == ActionType.EQUIP_WEAPON){
			PlayCardAction playCardAction1 = (PlayCardAction) validActions.get(i-1);
			Card card1 = context.resolveCardReference(playCardAction1.getCardReference());
			System.out.println("Action "+ i + " is " + card1.getName());
			}
			else if (validActions.get(i-1).getActionType() == ActionType.PHYSICAL_ATTACK){
				if(i > 1 && (validActions.get(i-2).getActionType() != ActionType.PHYSICAL_ATTACK)){
				System.out.println("Action "+ i + " is " +  "physical attack");
			}
			}
			else{
				System.out.println("Action "+ i + " is " +validActions.get(i-1));
			}

			i++;
		}
		int n = 0;
		System.out.println();
		System.out.println("Enter the corresponding action number for an explanation on this card. To move on to play a card, enter 0");
		if(reader.hasNextInt()){
		n = reader.nextInt(); // Scans the next token of the input as an int.
	}
		//once finished

		if(n <= i && n > 0){
			System.out.println("Explanation for Action " + n+ " is: ");
			System.out.println(test.printExplanation(context,player,validActions.get(n-1)));
		}




		HumanActionOptions options = new HumanActionOptions(this, context, player, validActions);
		NotificationProxy.sendNotification(GameNotification.HUMAN_PROMPT_FOR_ACTION, options);
		while (waitingForInput) {
			try {
				Thread.sleep(BuildConfig.DEFAULT_SLEEP_DELAY);
				if (context.ignoreEvents()) {
					return null;
				}	
			} catch (InterruptedException e) {
			}
		}


	/*	if (MouseEvent.getButton() == 3){
			System.out.println("right clicked");
		} */


		System.out.println("----------------------------------");
		return selectedAction;
	}

	public void setMulliganCards(List<Card> mulliganCards) {
		this.mulliganCards = mulliganCards;
		waitingForInput = false;
	}

}
