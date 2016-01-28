/*
 * Copyright (C) 2004-2015 L2J Unity
 * 
 * This file is part of L2J Unity.
 * 
 * L2J Unity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Unity is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import java.util.ArrayList;
import java.util.List;

import org.l2junity.gameserver.enums.StatModifierType;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.conditions.Condition;
import org.l2junity.gameserver.model.conditions.ConditionPlayerIsInCombat;
import org.l2junity.gameserver.model.conditions.ConditionTargetUsesWeaponKind;
import org.l2junity.gameserver.model.conditions.ConditionUsingItemType;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.items.type.ArmorType;
import org.l2junity.gameserver.model.items.type.WeaponType;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.model.stats.Stats;

/**
 * @author Sdw
 */
public abstract class AbstractStatEffect extends AbstractEffect
{
	protected final Stats _addStat;
	protected final Stats _mulStat;
	protected final double _amount;
	protected final StatModifierType _mode;
	protected final List<Condition> _conditions = new ArrayList<>();
	
	public AbstractStatEffect(StatsSet params, Stats stat)
	{
		this(params, stat, stat);
	}
	
	public AbstractStatEffect(StatsSet params, Stats mulStat, Stats addStat)
	{
		_addStat = addStat;
		_mulStat = mulStat;
		_amount = params.getDouble("amount", 0);
		_mode = params.getEnum("mode", StatModifierType.class, StatModifierType.DIFF);
		
		int weaponTypesMask = 0;
		final String[] weaponTypes = params.getString("weaponType", "ALL").split(";");
		for (String weaponType : weaponTypes)
		{
			if (weaponType.equalsIgnoreCase("ALL"))
			{
				weaponTypesMask = 0;
				break;
			}
			
			try
			{
				weaponTypesMask |= WeaponType.valueOf(weaponType).mask();
			}
			catch (IllegalArgumentException e)
			{
				final IllegalArgumentException exception = new IllegalArgumentException("weaponType should contain WeaponType enum value but found " + weaponType);
				exception.addSuppressed(e);
				throw exception;
			}
		}
		
		int armorTypesMask = 0;
		final String[] armorTypes = params.getString("armorType", "ALL").split(";");
		for (String armorType : armorTypes)
		{
			if (armorType.equalsIgnoreCase("ALL"))
			{
				armorTypesMask = 0;
				break;
			}
			
			try
			{
				armorTypesMask |= ArmorType.valueOf(armorType).mask();
			}
			catch (IllegalArgumentException e)
			{
				final IllegalArgumentException exception = new IllegalArgumentException("armorTypes should contain ArmorType enum value but found " + armorType);
				exception.addSuppressed(e);
				throw exception;
			}
		}
		
		if (weaponTypesMask != 0)
		{
			_conditions.add(new ConditionTargetUsesWeaponKind(weaponTypesMask));
		}
		
		if (armorTypesMask != 0)
		{
			_conditions.add(new ConditionUsingItemType(armorTypesMask));
		}
		
		if (params.contains("inCombat"))
		{
			_conditions.add(new ConditionPlayerIsInCombat(params.getBoolean("inCombat")));
		}
	}
	
	@Override
	public void pump(Creature effected, Skill skill)
	{
		if (_conditions.isEmpty() || _conditions.stream().allMatch(cond -> cond.test(effected, effected, skill)))
		{
			switch (_mode)
			{
				case DIFF:
				{
					effected.getStat().mergeAdd(_addStat, _amount);
					break;
				}
				case PER:
				{
					effected.getStat().mergeMul(_mulStat, (_amount / 100) + 1);
					break;
				}
			}
		}
	}
}
