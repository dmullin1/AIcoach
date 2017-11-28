package net.demilich.metastone.game.behaviour.heuristic;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.minions.Minion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeightedHeuristic implements IGameStateHeuristic {

	private final Logger logger = LoggerFactory.getLogger(WeightedHeuristic.class);

	private float calculateMinionScore(Minion minion) {
		float minionScore = minion.getAttack() + minion.getHp();
		float baseScore = minionScore;
		if (minion.hasAttribute(Attribute.FROZEN)) {
			return minion.getHp();
		}
		if (minion.hasAttribute(Attribute.TAUNT)) {
			minionScore += 2;
		}
		if (minion.hasAttribute(Attribute.WINDFURY)) {
			minionScore += minion.getAttack() * 0.5f;
		}
		if (minion.hasAttribute(Attribute.DIVINE_SHIELD)) {
			minionScore += 1.5f * baseScore;
		}
		if (minion.hasAttribute(Attribute.SPELL_DAMAGE)) {
			minionScore += minion.getAttributeValue(Attribute.SPELL_DAMAGE);
		}
		if (minion.hasAttribute(Attribute.ENRAGED)) {
			minionScore += 1;
		}
		if (minion.hasAttribute(Attribute.STEALTH)) {
			minionScore += 1;
		}
		if (minion.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS)) {
			minionScore += 1.5f * baseScore;
		}
		logger.info("Minion score in weighted heuristic is {}", minionScore);
		return minionScore;
		
	}

	@Override
	public double getScore(GameContext context, int playerId) {
		float score = 0;
		Player player = context.getPlayer(playerId);
		Player opponent = context.getOpponent(player);
		if (player.getHero().isDestroyed()) {
			return Float.NEGATIVE_INFINITY;
		}
		if (opponent.getHero().isDestroyed()) {
			return Float.POSITIVE_INFINITY;
		}
		int ownHp = player.getHero().getHp() + player.getHero().getArmor();
		int opponentHp = opponent.getHero().getHp() + opponent.getHero().getArmor();
		score += ownHp - opponentHp;

		score += player.getHand().getCount() * 3;
		score -= opponent.getHand().getCount() * 3;
		score += player.getMinions().size() * 2;
		score -= opponent.getMinions().size() * 2;
		for (Minion minion : player.getMinions()) {
			score += calculateMinionScore(minion);
		}
		for (Minion minion : opponent.getMinions()) {
			score -= calculateMinionScore(minion);
		}
		logger.info("score in weighted heuristic is {}", score);
		return score;
	}

	@Override
	public void onActionSelected(GameContext context, int playerId) {
	}

}
