/*
 * Copyright (C) 2004-2016 L2J Unity
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
package org.l2junity.gameserver.enums;

/**
 * @author UnAfraid
 */
public enum OneDayRewardStatus
{
	AVAILABLE(1),
	NOT_AVAILABLE(2),
	COMPLETED(3);
	
	private int _clientId;
	
	private OneDayRewardStatus(int clientId)
	{
		_clientId = clientId;
	}
	
	public int getClientId()
	{
		return _clientId;
	}
	
	public static OneDayRewardStatus valueOf(int clientId)
	{
		for (OneDayRewardStatus type : values())
		{
			if (type.getClientId() == clientId)
			{
				return type;
			}
		}
		return null;
	}
}
