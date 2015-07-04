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
package instances.FaeronTrainingGrounds1;

import org.l2junity.gameserver.enums.QuestSound;
import org.l2junity.gameserver.instancemanager.InstanceManager;
import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.holders.ItemHolder;
import org.l2junity.gameserver.model.instancezone.InstanceWorld;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.send.ExShowScreenMessage;
import org.l2junity.gameserver.network.client.send.TutorialShowHtml;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;

import instances.AbstractInstance;
import quests.Q10735_ASpecialPower.Q10735_ASpecialPower;

/**
 * Fearon Training Grounds Instance Zone.
 * @author Sdw, malyelfik
 */
public final class FaeronTrainingGrounds1 extends AbstractInstance
{
	// Instance world
	protected class FTG1World extends InstanceWorld
	{
		public Npc[] spawnedMonsters = new Npc[2];
	}
	
	// NPCs
	private static final int AYANTHE = 33942;
	private static final int AYANTHE_2 = 33944;
	// Monsters
	private static final int FLOATO = 27526;
	private static final int FLOATO2 = 27531;
	private static final int RATEL = 27527;
	// Items
	private static final ItemHolder SPIRITSHOTS_TRAINING = new ItemHolder(2509, 150);
	// Locations
	private static final Location START_LOC = new Location(-74808, 240640, -3568);
	private static final Location EXIT_LOC = new Location(-82132, 249836, -3360);
	private static final Location[] MOB_SPAWNS =
	{
		new Location(-74721, 240513, -3584),
		new Location(-74760, 240773, -3560)
	};
	// Misc
	private static final int TEMPLATE_ID = 251;
	private static final double DAMAGE_BY_SKILL = 0.5d; // Percent
	
	public FaeronTrainingGrounds1()
	{
		super(FaeronTrainingGrounds1.class.getSimpleName());
		addStartNpc(AYANTHE, AYANTHE_2);
		addFirstTalkId(AYANTHE_2);
		addTalkId(AYANTHE, AYANTHE_2);
		addKillId(FLOATO, FLOATO2, RATEL);
		addSkillSeeId(RATEL);
	}
	
	@Override
	public void onEnterInstance(PlayerInstance player, InstanceWorld world, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			world.addAllowed(player.getObjectId());
		}
		teleportPlayer(player, START_LOC, world.getInstanceId());
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState qs = player.getQuestState(Q10735_ASpecialPower.class.getSimpleName());
		String htmltext = null;
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "enter_instance":
				enterInstance(player, new FTG1World(), "FaeronTrainingGrounds1.xml", TEMPLATE_ID);
				break;
			case "exit_instance":
			{
				final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
				world.removeAllowed(player.getObjectId());
				teleportPlayer(player, EXIT_LOC, 0);
				break;
			}
			case "33944-03.html":
			{
				if (qs.isCond(6))
				{
					spawnMonsters(RATEL, player);
					showOnScreenMsg(player, NpcStringId.FIGHT_USING_SKILLS, ExShowScreenMessage.TOP_CENTER, 10000);
				}
				else
				{
					final int npcId = (qs.isCond(4)) ? FLOATO2 : FLOATO;
					spawnMonsters(npcId, player);
					showOnScreenMsg(player, NpcStringId.ATTACK_THE_MONSTER, ExShowScreenMessage.TOP_CENTER, 10000);
				}
				htmltext = event;
				break;
			}
			case "33944-07.html":
			{
				if (qs.isCond(5))
				{
					qs.setCond(6, true);
					showOnScreenMsg(player, NpcStringId.FIGHT_USING_SKILLS, ExShowScreenMessage.TOP_CENTER, 10000);
					spawnMonsters(RATEL, player);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = player.getQuestState(Q10735_ASpecialPower.class.getSimpleName());
		String htmltext = getNoQuestMsg(player);
		if (qs == null)
		{
			return htmltext;
		}
		
		if ((npc.getId() == AYANTHE_2) && qs.isStarted())
		{
			switch (qs.getCond())
			{
				case 1:
				{
					qs.setCond(2, true);
					spawnMonsters(FLOATO, player);
					showOnScreenMsg(player, NpcStringId.ATTACK_THE_MONSTER, ExShowScreenMessage.TOP_CENTER, 10000);
					htmltext = "33944-01.html";
					break;
				}
				case 2:
				case 4:
				case 6:
				{
					htmltext = "33944-02.html";
					break;
				}
				case 3:
				{
					if (qs.getInt("ss") == 1)
					{
						spawnMonsters(FLOATO2, player);
						showOnScreenMsg(player, NpcStringId.ATTACK_THE_MONSTER, ExShowScreenMessage.TOP_CENTER, 10000);
						qs.setCond(4, true);
						htmltext = "33944-05.html";
					}
					else
					{
						qs.set("ss", 1);
						giveItems(player, SPIRITSHOTS_TRAINING);
						showOnScreenMsg(player, NpcStringId.AUTOMATE_SPIRITSHOT_AS_SHOWN_IN_THE_TUTORIAL, ExShowScreenMessage.TOP_CENTER, 10000);
						player.sendPacket(new TutorialShowHtml(npc.getObjectId(), "..\\L2Text\\QT_003_bullet_01.htm", TutorialShowHtml.LARGE_WINDOW));
						htmltext = "33944-04.html";
					}
					break;
				}
				case 5:
				{
					player.sendPacket(new TutorialShowHtml(npc.getObjectId(), "..\\L2Text\\QT_004_skill_01.htm", TutorialShowHtml.LARGE_WINDOW));
					htmltext = "33944-06.html";
					break;
				}
				case 7:
				{
					htmltext = "33944-08.html";
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		// Check if monster is inside instance
		final InstanceWorld wrd = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if ((wrd == null) || !(wrd instanceof FTG1World))
		{
			return super.onKill(npc, killer, isSummon);
		}
		
		// Remove monster from instance spawn holder
		final FTG1World world = (FTG1World) wrd;
		world.spawnedMonsters[npc.getScriptValue()] = null;
		
		// Handle quest state
		final QuestState qs = killer.getQuestState(Q10735_ASpecialPower.class.getSimpleName());
		if (qs != null)
		{
			switch (npc.getId())
			{
				case FLOATO:
				case FLOATO2:
				{
					if ((qs.isCond(2) || qs.isCond(4)) && onKillQuestChange(killer, qs))
					{
						despawnMonsters(killer);
						if (qs.isCond(5) && (killer.getLevel() < 5))
						{
							addExpAndSp(killer, 1716, 0);
						}
					}
					break;
				}
				case RATEL:
				{
					if (qs.isCond(6) && onKillQuestChange(killer, qs))
					{
						despawnMonsters(killer);
						showOnScreenMsg(killer, NpcStringId.TALK_TO_AYANTHE_TO_LEAVE_THE_TRAINING_GROUNDS, ExShowScreenMessage.TOP_CENTER, 10000);
					}
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onSkillSee(Npc npc, PlayerInstance player, Skill skill, org.l2junity.gameserver.model.WorldObject[] targets, boolean isSummon)
	{
		if (!npc.isDead() && (player.getTarget() == npc))
		{
			final double dmg = npc.getMaxHp() * DAMAGE_BY_SKILL;
			npc.reduceCurrentHp(dmg, player, null);
		}
		return super.onSkillSee(npc, player, skill, targets, isSummon);
	}
	
	/**
	 * Handle death of training monster. When all monsters are killed, quest cond is increased.
	 * @param killer player who killed monster
	 * @param qs quest state of killer
	 * @return {@code true} when all monsters are killed, otherwise {@code false}
	 */
	private boolean onKillQuestChange(PlayerInstance killer, QuestState qs)
	{
		final int value = qs.getMemoStateEx(Q10735_ASpecialPower.KILL_COUNT_VAR) + 1;
		if (value >= 2)
		{
			qs.setCond(qs.getCond() + 1, true);
			qs.setMemoStateEx(Q10735_ASpecialPower.KILL_COUNT_VAR, 0);
			return true;
		}
		playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
		qs.setMemoStateEx(Q10735_ASpecialPower.KILL_COUNT_VAR, value);
		qs.getQuest().sendNpcLogList(killer);
		return false;
	}
	
	/**
	 * Spawn training monsters inside instance
	 * @param npcId template id of training monster
	 * @param player player that owns instance
	 */
	private void spawnMonsters(int npcId, PlayerInstance player)
	{
		final InstanceWorld wrd = InstanceManager.getInstance().getPlayerWorld(player);
		if ((wrd == null) || !(wrd instanceof FTG1World))
		{
			return;
		}
		
		final FTG1World world = (FTG1World) wrd;
		for (int i = 0; i < MOB_SPAWNS.length; i++)
		{
			if (world.spawnedMonsters[i] == null)
			{
				final Npc npc = addSpawn(npcId, MOB_SPAWNS[i], false, 0, false, world.getInstanceId());
				npc.setScriptValue(i);
				world.spawnedMonsters[i] = npc;
			}
		}
	}
	
	/**
	 * Despawn training monsters inside instance
	 * @param player player that owns instance
	 */
	private void despawnMonsters(PlayerInstance player)
	{
		final InstanceWorld wrd = InstanceManager.getInstance().getPlayerWorld(player);
		if ((wrd == null) || !(wrd instanceof FTG1World))
		{
			return;
		}
		
		final FTG1World world = (FTG1World) wrd;
		for (int i = 0; i < world.spawnedMonsters.length; i++)
		{
			if (world.spawnedMonsters[i] != null)
			{
				world.spawnedMonsters[i].deleteMe();
				world.spawnedMonsters[i] = null;
			}
		}
	}
}