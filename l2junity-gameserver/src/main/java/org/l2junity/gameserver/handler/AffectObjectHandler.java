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
package org.l2junity.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import org.l2junity.gameserver.model.skills.targets.AffectObject;

/**
 * @author Nik
 */
public class AffectObjectHandler implements IHandler<IAffectObjectHandler, Enum<AffectObject>>
{
	private final Map<Enum<AffectObject>, IAffectObjectHandler> _datatable;
	
	protected AffectObjectHandler()
	{
		_datatable = new HashMap<>();
	}
	
	@Override
	public void registerHandler(IAffectObjectHandler handler)
	{
		_datatable.put(handler.getAffectObjectType(), handler);
	}
	
	@Override
	public synchronized void removeHandler(IAffectObjectHandler handler)
	{
		_datatable.remove(handler.getAffectObjectType());
	}
	
	@Override
	public IAffectObjectHandler getHandler(Enum<AffectObject> targetType)
	{
		return _datatable.get(targetType);
	}
	
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	public static AffectObjectHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AffectObjectHandler _instance = new AffectObjectHandler();
	}
}