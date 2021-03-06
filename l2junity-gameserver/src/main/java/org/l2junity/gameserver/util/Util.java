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
package org.l2junity.gameserver.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import org.l2junity.Config;
import org.l2junity.commons.util.Rnd;
import org.l2junity.commons.util.file.filter.ExtFilter;
import org.l2junity.gameserver.ThreadPoolManager;
import org.l2junity.gameserver.enums.HtmlActionScope;
import org.l2junity.gameserver.enums.IllegalActionPunishmentType;
import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.actor.tasks.player.IllegalPlayerActionTask;
import org.l2junity.gameserver.model.interfaces.ILocational;
import org.l2junity.gameserver.network.client.send.AbstractHtmlPacket;
import org.l2junity.gameserver.network.client.send.NpcHtmlMessage;
import org.l2junity.gameserver.network.client.send.ShowBoard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General Utility functions related to game server.
 */
public final class Util
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);
	private static final NumberFormat ADENA_FORMATTER = NumberFormat.getIntegerInstance(Locale.ENGLISH);
	
	public static void handleIllegalPlayerAction(PlayerInstance actor, String message, IllegalActionPunishmentType punishment)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new IllegalPlayerActionTask(actor, message, punishment), 5000);
	}
	
	/**
	 * @param from
	 * @param to
	 * @return degree value of object 2 to the horizontal line with object 1 being the origin.
	 */
	public static double calculateAngleFrom(ILocational from, ILocational to)
	{
		return calculateAngleFrom(from.getX(), from.getY(), to.getX(), to.getY());
	}
	
	/**
	 * @param fromX
	 * @param fromY
	 * @param toX
	 * @param toY
	 * @return degree value of object 2 to the horizontal line with object 1 being the origin
	 */
	public static double calculateAngleFrom(int fromX, int fromY, int toX, int toY)
	{
		double angleTarget = Math.toDegrees(Math.atan2(toY - fromY, toX - fromX));
		if (angleTarget < 0)
		{
			angleTarget = 360 + angleTarget;
		}
		return angleTarget;
	}
	
	/**
	 * Gets a random position around the specified location.
	 * @param loc the center location
	 * @param minRange the minimum range from the center to pick a point.
	 * @param maxRange the maximum range from the center to pick a point.
	 * @return a random location between minRange and maxRange of the center location.
	 */
	public static Location getRandomPosition(ILocational loc, int minRange, int maxRange)
	{
		final int randomX = Rnd.get(minRange, maxRange);
		final int randomY = Rnd.get(minRange, maxRange);
		final double rndAngle = Math.toRadians(Rnd.get(360));
		
		final int newX = (int) (loc.getX() + (randomX * Math.cos(rndAngle)));
		final int newY = (int) (loc.getY() + (randomY * Math.sin(rndAngle)));
		
		return new Location(newX, newY, loc.getZ());
	}
	
	public static double convertHeadingToDegree(int clientHeading)
	{
		double degree = clientHeading / 182.044444444;
		return degree;
	}
	
	public static int convertDegreeToClientHeading(double degree)
	{
		if (degree < 0)
		{
			degree = 360 + degree;
		}
		return (int) (degree * 182.044444444);
	}
	
	public static int calculateHeadingFrom(ILocational from, ILocational to)
	{
		return calculateHeadingFrom(from.getX(), from.getY(), to.getX(), to.getY());
	}
	
	public static int calculateHeadingFrom(int fromX, int fromY, int toX, int toY)
	{
		double angleTarget = Math.toDegrees(Math.atan2(toY - fromY, toX - fromX));
		if (angleTarget < 0)
		{
			angleTarget = 360 + angleTarget;
		}
		return (int) (angleTarget * 182.044444444);
	}
	
	public static int calculateHeadingFrom(double dx, double dy)
	{
		double angleTarget = Math.toDegrees(Math.atan2(dy, dx));
		if (angleTarget < 0)
		{
			angleTarget = 360 + angleTarget;
		}
		return (int) (angleTarget * 182.044444444);
	}
	
	/**
	 * Calculates distance between one set of x, y, z and another set of x, y, z.
	 * @param x1 - X coordinate of first point.
	 * @param y1 - Y coordinate of first point.
	 * @param z1 - Z coordinate of first point.
	 * @param x2 - X coordinate of second point.
	 * @param y2 - Y coordinate of second point.
	 * @param z2 - Z coordinate of second point.
	 * @param includeZAxis - If set to true, Z coordinates will be included.
	 * @param squared - If set to true, distance returned will be squared.
	 * @return {@code double} - Distance between object and given x, y , z.
	 */
	public static double calculateDistance(double x1, double y1, double z1, double x2, double y2, double z2, boolean includeZAxis, boolean squared)
	{
		final double distance = Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + (includeZAxis ? Math.pow(z1 - z2, 2) : 0);
		return (squared) ? distance : Math.sqrt(distance);
	}
	
	/**
	 * Calculates distance between 2 locations.
	 * @param loc1 - First location.
	 * @param loc2 - Second location.
	 * @param includeZAxis - If set to true, Z coordinates will be included.
	 * @param squared - If set to true, distance returned will be squared.
	 * @return {@code double} - Distance between object and given location.
	 */
	public static double calculateDistance(ILocational loc1, ILocational loc2, boolean includeZAxis, boolean squared)
	{
		return calculateDistance(loc1.getX(), loc1.getY(), loc1.getZ(), loc2.getX(), loc2.getY(), loc2.getZ(), includeZAxis, squared);
	}
	
	/**
	 * @param range
	 * @param obj1
	 * @param obj2
	 * @param includeZAxis
	 * @return {@code true} if the two objects are within specified range between each other, {@code false} otherwise
	 */
	public static boolean checkIfInRange(int range, WorldObject obj1, WorldObject obj2, boolean includeZAxis)
	{
		if ((obj1 == null) || (obj2 == null) || (obj1.getInstanceWorld() != obj2.getInstanceWorld()))
		{
			return false;
		}
		if (range == -1)
		{
			return true; // not limited
		}
		
		int radius = 0;
		if (obj1.isCreature())
		{
			radius += ((Creature) obj1).getTemplate().getCollisionRadius();
		}
		if (obj2.isCreature())
		{
			radius += ((Creature) obj2).getTemplate().getCollisionRadius();
		}
		
		return calculateDistance(obj1, obj2, includeZAxis, false) <= (range + radius);
	}
	
	/**
	 * Checks if object is within short (sqrt(int.max_value)) radius, not using collisionRadius. Faster calculation than checkIfInRange if distance is short and collisionRadius isn't needed. Not for long distance checks (potential teleports, far away castles etc).
	 * @param range
	 * @param obj1
	 * @param obj2
	 * @param includeZAxis if true, check also Z axis (3-dimensional check), otherwise only 2D
	 * @return {@code true} if objects are within specified range between each other, {@code false} otherwise
	 */
	public static boolean checkIfInShortRange(int range, WorldObject obj1, WorldObject obj2, boolean includeZAxis)
	{
		if ((obj1 == null) || (obj2 == null))
		{
			return false;
		}
		if (range == -1)
		{
			return true; // not limited
		}
		
		return calculateDistance(obj1, obj2, includeZAxis, false) <= range;
	}
	
	/**
	 * Checks if the cube intersects the sphere.
	 * @param x1 the cube's first point x
	 * @param y1 the cube's first point y
	 * @param z1 the cube's first point z
	 * @param x2 the cube's second point x
	 * @param y2 the cube's second point y
	 * @param z2 the cube's second point z
	 * @param sX the sphere's middle x
	 * @param sY the sphere's middle y
	 * @param sZ the sphere's middle z
	 * @param radius the sphere's radius
	 * @return {@code true} if cube intersects sphere, {@code false} otherwise
	 */
	public static boolean cubeIntersectsSphere(int x1, int y1, int z1, int x2, int y2, int z2, int sX, int sY, int sZ, int radius)
	{
		double d = radius * radius;
		if (sX < x1)
		{
			d -= Math.pow(sX - x1, 2);
		}
		else if (sX > x2)
		{
			d -= Math.pow(sX - x2, 2);
		}
		if (sY < y1)
		{
			d -= Math.pow(sY - y1, 2);
		}
		else if (sY > y2)
		{
			d -= Math.pow(sY - y2, 2);
		}
		if (sZ < z1)
		{
			d -= Math.pow(sZ - z1, 2);
		}
		else if (sZ > z2)
		{
			d -= Math.pow(sZ - z2, 2);
		}
		return d > 0;
	}
	
	/**
	 * @param text - the text to check
	 * @return {@code true} if {@code text} contains only numbers, {@code false} otherwise
	 */
	public static boolean isDigit(String text)
	{
		if ((text == null) || text.isEmpty())
		{
			return false;
		}
		for (char c : text.toCharArray())
		{
			if (!Character.isDigit(c))
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param text - the text to check
	 * @return {@code true} if {@code text} is integer, {@code false} otherwise
	 */
	public static boolean isInteger(String text)
	{
		if ((text == null) || text.isEmpty())
		{
			return false;
		}
		try
		{
			Integer.parseInt(text);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	/**
	 * @param text - the text to check
	 * @return {@code true} if {@code text} is float, {@code false} otherwise
	 */
	public static boolean isFloat(String text)
	{
		if ((text == null) || text.isEmpty())
		{
			return false;
		}
		try
		{
			Float.parseFloat(text);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	/**
	 * @param text - the text to check
	 * @return {@code true} if {@code text} is double, {@code false} otherwise
	 */
	public static boolean isDouble(String text)
	{
		if ((text == null) || text.isEmpty())
		{
			return false;
		}
		try
		{
			Double.parseDouble(text);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	/**
	 * @param <T>
	 * @param name - the text to check
	 * @param enumType
	 * @return {@code true} if {@code text} is enum, {@code false} otherwise
	 */
	public static <T extends Enum<T>> boolean isEnum(String name, Class<T> enumType)
	{
		if ((name == null) || name.isEmpty())
		{
			return false;
		}
		try
		{
			return Enum.valueOf(enumType, name) != null;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	/**
	 * @param text - the text to check
	 * @return {@code true} if {@code text} contains only letters and/or numbers, {@code false} otherwise
	 */
	public static boolean isAlphaNumeric(String text)
	{
		if ((text == null) || text.isEmpty())
		{
			return false;
		}
		for (char c : text.toCharArray())
		{
			if (!Character.isLetterOrDigit(c))
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Format the specified digit using the digit grouping symbol "," (comma).<br>
	 * For example, 123456789 becomes 123,456,789.
	 * @param amount - the amount of adena
	 * @return the formatted adena amount
	 */
	public static String formatAdena(long amount)
	{
		synchronized (ADENA_FORMATTER)
		{
			return ADENA_FORMATTER.format(amount);
		}
	}
	
	/**
	 * @param val
	 * @param format
	 * @return formatted double value by specified format.
	 */
	public static String formatDouble(double val, String format)
	{
		final DecimalFormat formatter = new DecimalFormat(format, new DecimalFormatSymbols(Locale.ENGLISH));
		return formatter.format(val);
	}
	
	/**
	 * Format the given date on the given format
	 * @param date : the date to format.
	 * @param format : the format to correct by.
	 * @return a string representation of the formatted date.
	 */
	public static String formatDate(Date date, String format)
	{
		if (date == null)
		{
			return null;
		}
		final DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}
	
	public static File[] getDatapackFiles(String dirname, String extention)
	{
		File dir = new File(Config.DATAPACK_ROOT, "data/" + dirname);
		if (!dir.exists())
		{
			return null;
		}
		return dir.listFiles(new ExtFilter(extention));
	}
	
	public static String getDateString(Date date)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(date.getTime());
	}
	
	private static void buildHtmlBypassCache(PlayerInstance player, HtmlActionScope scope, String html)
	{
		String htmlLower = html.toLowerCase(Locale.ENGLISH);
		int bypassEnd = 0;
		int bypassStart = htmlLower.indexOf("=\"bypass ", bypassEnd);
		int bypassStartEnd;
		while (bypassStart != -1)
		{
			bypassStartEnd = bypassStart + 9;
			bypassEnd = htmlLower.indexOf("\"", bypassStartEnd);
			if (bypassEnd == -1)
			{
				break;
			}
			
			int hParamPos = htmlLower.indexOf("-h ", bypassStartEnd);
			String bypass;
			if ((hParamPos != -1) && (hParamPos < bypassEnd))
			{
				bypass = html.substring(hParamPos + 3, bypassEnd).trim();
			}
			else
			{
				bypass = html.substring(bypassStartEnd, bypassEnd).trim();
			}
			
			int firstParameterStart = bypass.indexOf(AbstractHtmlPacket.VAR_PARAM_START_CHAR);
			if (firstParameterStart != -1)
			{
				bypass = bypass.substring(0, firstParameterStart + 1);
			}
			
			if (Config.HTML_ACTION_CACHE_DEBUG)
			{
				LOGGER.info("Cached html bypass(" + scope.toString() + "): '" + bypass + "'");
			}
			player.addHtmlAction(scope, bypass);
			bypassStart = htmlLower.indexOf("=\"bypass ", bypassEnd);
		}
	}
	
	private static void buildHtmlLinkCache(PlayerInstance player, HtmlActionScope scope, String html)
	{
		String htmlLower = html.toLowerCase(Locale.ENGLISH);
		int linkEnd = 0;
		int linkStart = htmlLower.indexOf("=\"link ", linkEnd);
		int linkStartEnd;
		while (linkStart != -1)
		{
			linkStartEnd = linkStart + 7;
			linkEnd = htmlLower.indexOf("\"", linkStartEnd);
			if (linkEnd == -1)
			{
				break;
			}
			
			String htmlLink = html.substring(linkStartEnd, linkEnd).trim();
			if (htmlLink.isEmpty())
			{
				LOGGER.warn("Html link path is empty!");
				continue;
			}
			
			if (htmlLink.contains(".."))
			{
				LOGGER.warn("Html link path is invalid: " + htmlLink);
				continue;
			}
			
			if (Config.HTML_ACTION_CACHE_DEBUG)
			{
				LOGGER.info("Cached html link(" + scope.toString() + "): '" + htmlLink + "'");
			}
			// let's keep an action cache with "link " lowercase literal kept
			player.addHtmlAction(scope, "link " + htmlLink);
			linkStart = htmlLower.indexOf("=\"link ", linkEnd);
		}
	}
	
	/**
	 * Builds the html action cache for the specified scope.<br>
	 * An {@code npcObjId} of 0 means, the cached actions can be clicked<br>
	 * without beeing near an npc which is spawned in the world.
	 * @param player the player to build the html action cache for
	 * @param scope the scope to build the html action cache for
	 * @param npcObjId the npc object id the html actions are cached for
	 * @param html the html code to parse
	 */
	public static void buildHtmlActionCache(PlayerInstance player, HtmlActionScope scope, int npcObjId, String html)
	{
		if ((player == null) || (scope == null) || (npcObjId < 0) || (html == null))
		{
			throw new IllegalArgumentException();
		}
		
		if (Config.HTML_ACTION_CACHE_DEBUG)
		{
			LOGGER.info("Set html action npc(" + scope.toString() + "): " + npcObjId);
		}
		player.setHtmlActionOriginObjectId(scope, npcObjId);
		buildHtmlBypassCache(player, scope, html);
		buildHtmlLinkCache(player, scope, html);
	}
	
	/**
	 * Helper method to send a NpcHtmlMessage to the specified player.
	 * @param activeChar the player to send the html content to
	 * @param html the html content
	 */
	public static void sendHtml(PlayerInstance activeChar, String html)
	{
		final NpcHtmlMessage npcHtml = new NpcHtmlMessage();
		npcHtml.setHtml(html);
		activeChar.sendPacket(npcHtml);
	}
	
	/**
	 * Helper method to send a community board html to the specified player.<br>
	 * HtmlActionCache will be build with npc origin 0 which means the<br>
	 * links on the html are not bound to a specific npc.
	 * @param activeChar the player
	 * @param html the html content
	 */
	public static void sendCBHtml(PlayerInstance activeChar, String html)
	{
		sendCBHtml(activeChar, html, 0);
	}
	
	/**
	 * Helper method to send a community board html to the specified player.<br>
	 * When {@code npcObjId} is greater -1 the HtmlActionCache will be build<br>
	 * with the npcObjId as origin. An origin of 0 means the cached bypasses<br>
	 * are not bound to a specific npc.
	 * @param activeChar the player to send the html content to
	 * @param html the html content
	 * @param npcObjId bypass origin to use
	 */
	public static void sendCBHtml(PlayerInstance activeChar, String html, int npcObjId)
	{
		sendCBHtml(activeChar, html, null, npcObjId);
	}
	
	/**
	 * Helper method to send a community board html to the specified player.<br>
	 * HtmlActionCache will be build with npc origin 0 which means the<br>
	 * links on the html are not bound to a specific npc. It also fills a<br>
	 * multiedit field in the send html if fillMultiEdit is not null.
	 * @param activeChar the player
	 * @param html the html content
	 * @param fillMultiEdit text to fill the multiedit field with(may be null)
	 */
	public static void sendCBHtml(PlayerInstance activeChar, String html, String fillMultiEdit)
	{
		sendCBHtml(activeChar, html, fillMultiEdit, 0);
	}
	
	/**
	 * Helper method to send a community board html to the specified player.<br>
	 * It fills a multiedit field in the send html if {@code fillMultiEdit}<br>
	 * is not null. When {@code npcObjId} is greater -1 the HtmlActionCache will be build<br>
	 * with the npcObjId as origin. An origin of 0 means the cached bypasses<br>
	 * are not bound to a specific npc.
	 * @param activeChar the player
	 * @param html the html content
	 * @param fillMultiEdit text to fill the multiedit field with(may be null)
	 * @param npcObjId bypass origin to use
	 */
	public static void sendCBHtml(PlayerInstance activeChar, String html, String fillMultiEdit, int npcObjId)
	{
		if ((activeChar == null) || (html == null))
		{
			return;
		}
		
		activeChar.clearHtmlActions(HtmlActionScope.COMM_BOARD_HTML);
		
		if (npcObjId > -1)
		{
			buildHtmlActionCache(activeChar, HtmlActionScope.COMM_BOARD_HTML, npcObjId, html);
		}
		
		if (fillMultiEdit != null)
		{
			activeChar.sendPacket(new ShowBoard(html, "1001"));
			fillMultiEditContent(activeChar, fillMultiEdit);
		}
		else
		{
			if (html.length() < 16250)
			{
				activeChar.sendPacket(new ShowBoard(html, "101"));
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
			}
			else if (html.length() < (16250 * 2))
			{
				activeChar.sendPacket(new ShowBoard(html.substring(0, 16250), "101"));
				activeChar.sendPacket(new ShowBoard(html.substring(16250), "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
			}
			else if (html.length() < (16250 * 3))
			{
				activeChar.sendPacket(new ShowBoard(html.substring(0, 16250), "101"));
				activeChar.sendPacket(new ShowBoard(html.substring(16250, 16250 * 2), "102"));
				activeChar.sendPacket(new ShowBoard(html.substring(16250 * 2), "103"));
			}
			else
			{
				activeChar.sendPacket(new ShowBoard("<html><body><br><center>Error: HTML was too long!</center></body></html>", "101"));
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
			}
		}
	}
	
	/**
	 * Fills the community board's multiedit window with text. Must send after sendCBHtml
	 * @param activeChar
	 * @param text
	 */
	public static void fillMultiEditContent(PlayerInstance activeChar, String text)
	{
		activeChar.sendPacket(new ShowBoard(Arrays.asList("0", "0", "0", "0", "0", "0", activeChar.getName(), Integer.toString(activeChar.getObjectId()), activeChar.getAccountName(), "9", " ", " ", text.replaceAll("<br>", System.lineSeparator()), "0", "0", "0", "0")));
	}
	
	public static boolean isInsideRangeOfObjectId(WorldObject obj, int targetObjId, int radius)
	{
		final WorldObject target = World.getInstance().findObject(targetObjId);
		return (target != null) && (obj.calculateDistance(target, true, false) <= radius);
	}
	
	public static String readAllLines(File file, Charset cs, String newLineDelimiter) throws IOException
	{
		final StringBuilder sb = new StringBuilder();
		try (final InputStream in = new FileInputStream(file);
			final InputStreamReader reader = new InputStreamReader(in, cs);
			final BufferedReader buffer = new BufferedReader(reader))
		{
			String line;
			while ((line = buffer.readLine()) != null)
			{
				sb.append(line);
				if (newLineDelimiter != null)
				{
					sb.append(newLineDelimiter);
				}
			}
		}
		return sb.toString();
	}
}
