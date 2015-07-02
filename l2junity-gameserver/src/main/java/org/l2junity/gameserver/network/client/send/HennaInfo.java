/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2junity.gameserver.network.client.send;

import java.util.ArrayList;
import java.util.List;

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.Henna;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * This server packet sends the player's henna information.
 * @author Zoey76
 */
public final class HennaInfo implements IClientOutgoingPacket
{
	private final PlayerInstance _activeChar;
	private final List<Henna> _hennas = new ArrayList<>();
	
	public HennaInfo(PlayerInstance player)
	{
		_activeChar = player;
		for (Henna henna : _activeChar.getHennaList())
		{
			if (henna != null)
			{
				_hennas.add(henna);
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.HENNA_INFO.writeId(packet);
		
		packet.writeD(_activeChar.getHennaStatINT()); // equip INT
		packet.writeD(_activeChar.getHennaStatSTR()); // equip STR
		packet.writeD(_activeChar.getHennaStatCON()); // equip CON
		packet.writeD(_activeChar.getHennaStatMEN()); // equip MEN
		packet.writeD(_activeChar.getHennaStatDEX()); // equip DEX
		packet.writeD(_activeChar.getHennaStatWIT()); // equip WIT
		packet.writeD(_activeChar.getHennaStatLUC()); // equip LUC
		packet.writeD(_activeChar.getHennaStatCHA()); // equip CHA
		packet.writeD(3 - _activeChar.getHennaEmptySlots()); // Slots
		packet.writeD(_hennas.size()); // Size
		for (Henna henna : _hennas)
		{
			packet.writeD(henna.getDyeId());
			packet.writeD(henna.isAllowedClass(_activeChar.getClassId()) ? 0x01 : 0x00);
		}
		packet.writeD(0x00); // Premium Slot Dye ID
		packet.writeD(0x00); // Premium Slot Dye Time Left
		packet.writeD(0x00); // Premium Slot Dye ID isValid
		return true;
	}
}
