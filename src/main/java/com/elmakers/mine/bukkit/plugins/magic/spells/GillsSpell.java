package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellEventType;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class GillsSpell extends Spell
{
	public class PlayerGills
	{
		public long lastHealTick;
		public long lastMoveTick;
		public int timeRemaining;
		
		public PlayerGills(int duration)
		{
			lastMoveTick = System.currentTimeMillis();
			lastHealTick = lastMoveTick;
			timeRemaining = duration;
		}
		
		public void move()
		{
			
			long thisMoveTick = System.currentTimeMillis();
			int timeDelta = (int)(thisMoveTick - lastMoveTick);
			lastMoveTick = thisMoveTick;
			timeRemaining -= timeDelta;
		}
		
		public void heal()
		{
			lastHealTick = System.currentTimeMillis();
		}
		
		public boolean isTimeToHeal(int frequency)
		{
			int healDelta = (int)(lastMoveTick - lastHealTick);
			return healDelta > frequency;
		}
		
		public boolean isTimeToRecede()
		{
			return timeRemaining < 0;
		}
	}
	
	private int gillDuration = 60;
	private int healFrequency = 1000;
	private int healAmount = 4;
	private HashMap<String, PlayerGills> gillPlayers = new HashMap<String, PlayerGills>();
	
	@Override
	public boolean onCast(ConfigurationNode parameters) 
	{
		PlayerGills hasGills = gillPlayers.get(player.getName());
		
		if (hasGills != null)
		{
			sendMessage(player, "Your gills recede");
			gillPlayers.remove(player.getName());
		}
		else
		{
			sendMessage(player, "You grow gills!");
			gillPlayers.put(player.getName(), new PlayerGills(gillDuration * 1000));
		}
		checkListener();
		return true;
	}
	
	protected void checkListener()
	{
		if (gillPlayers.size() == 0)
		{
			spells.unregisterEvent(SpellEventType.PLAYER_MOVE, this);
		}
		else
		{
			spells.registerEvent(SpellEventType.PLAYER_MOVE, this);
		}
	}
	
	@Override
	public void onPlayerMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		PlayerGills gills = gillPlayers.get(player.getName());
		if (gills != null)
		{
			gills.move();
			if (gills.isTimeToRecede())
			{
				sendMessage(player, "Your gills recede!");
				gillPlayers.remove(player.getName());
				checkListener();
			}
			else
			{
			if (gills.isTimeToHeal(healFrequency))
			{
					gills.heal();
					if (isUnderwater())
					{
						int health = player.getHealth();
						if (health < 20) 
						{
							health = health + healAmount;
						}
						player.setHealth(health);
					}
				}
			}
		}
	}

	@Override
	public void onLoad(ConfigurationNode properties)  
	{
	    disableTargeting();
		gillDuration = properties.getInteger("duration", gillDuration);
		healFrequency = properties.getInteger("heal_frequency", healFrequency);
		healAmount = properties.getInteger("heal_amount", healAmount);
	}
}
