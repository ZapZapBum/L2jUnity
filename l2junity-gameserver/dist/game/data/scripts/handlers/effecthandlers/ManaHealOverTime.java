/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.skills.BuffInfo;
import org.l2junity.gameserver.network.client.send.StatusUpdate;

/**
 * Mana Heal Over Time effect implementation.
 */
public final class ManaHealOverTime extends AbstractEffect
{
	private final double _power;
	
	public ManaHealOverTime(StatsSet params)
	{
		_power = params.getDouble("power", 0);
	}
	
	@Override
	public boolean onActionTime(BuffInfo info)
	{
		if (info.getEffected().isDead())
		{
			return false;
		}
		
		double mp = info.getEffected().getCurrentMp();
		double maxmp = info.getEffected().getMaxRecoverableMp();
		
		// Not needed to set the MP and send update packet if player is already at max MP
		if (mp >= maxmp)
		{
			return true;
		}
		
		mp += _power * getTicksMultiplier();
		mp = Math.min(mp, maxmp);
		info.getEffected().setCurrentMp(mp, false);
		final StatusUpdate su = new StatusUpdate(info.getEffected());
		su.addAttribute(StatusUpdate.CUR_MP, (int) mp);
		su.addCaster(info.getEffector());
		info.getEffected().broadcastPacket(su);
		return info.getSkill().isToggle();
	}
}
