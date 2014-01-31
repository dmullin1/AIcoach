package net.pferdimanzug.hearthstone.analyzer.game.cards.concrete.rogue;

import net.pferdimanzug.hearthstone.analyzer.game.GameContext;
import net.pferdimanzug.hearthstone.analyzer.game.Player;
import net.pferdimanzug.hearthstone.analyzer.game.cards.Rarity;
import net.pferdimanzug.hearthstone.analyzer.game.cards.SpellCard;
import net.pferdimanzug.hearthstone.analyzer.game.entities.Entity;
import net.pferdimanzug.hearthstone.analyzer.game.entities.heroes.HeroClass;
import net.pferdimanzug.hearthstone.analyzer.game.entities.minions.Minion;
import net.pferdimanzug.hearthstone.analyzer.game.spells.Spell;
import net.pferdimanzug.hearthstone.analyzer.game.targeting.TargetSelection;

public class Betrayal extends SpellCard {
	
	public Betrayal() {
		super("Betrayal", Rarity.COMMON, HeroClass.ROGUE, 2);
		setSpell(new BetrayalSpell());
		setTargetRequirement(TargetSelection.ENEMY_MINIONS);
	}

	private class BetrayalSpell extends Spell {

		@Override
		protected void onCast(GameContext context, Player player, Entity target) {
			Minion targetMinion = (Minion) target;
			for (Minion adjacentMinion : context.getAdjacentMinions(player, targetMinion)) {
				context.getLogic().damage(player, adjacentMinion, targetMinion.getAttack(), false);
				targetMinion.onAttack(adjacentMinion);
			}
		}
		
	}

}

