package net.demilich.metastone.game.spells.desc.condition;

import java.util.List;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.filter.Operation;
import net.demilich.metastone.game.targeting.EntityReference;

public class AttributeCondition extends Condition {

	public AttributeCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity target) {
		EntityReference sourceReference = (EntityReference) desc.get(ConditionArg.TARGET);
		Actor source = null;
		if (sourceReference == null) {
			source = (Actor) target;
		} else {
			List<Entity> entities = context.resolveTarget(player, null, sourceReference);
			if (entities == null || entities.isEmpty()) {
				return false;
			}
			source = (Actor) entities.get(0);
		}

		Attribute attribute = (Attribute) desc.get(ConditionArg.ATTRIBUTE);
		Operation operation = (Operation) desc.get(ConditionArg.OPERATION);
		if (operation == Operation.HAS) {
			return target.hasAttribute(attribute);
		}

		int targetValue = desc.getInt(ConditionArg.VALUE);

		int actualValue;
		if (attribute == Attribute.ATTACK) {
			actualValue = source.getAttack();
		} else {
			actualValue = source.getAttributeValue(attribute);
		}

		return SpellUtils.evaluateOperation(operation, actualValue, targetValue);
	}

}