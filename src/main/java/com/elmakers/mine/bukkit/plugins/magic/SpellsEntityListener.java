package com.elmakers.mine.bukkit.plugins.magic;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

class SpellsEntityListener implements Listener 
{
	private Spells master;
	
	public void setSpells(Spells master)
	{
		this.master = master;
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event)
	{
		if (Player.class.isInstance(event.getEntity()))
		{
			Player player = (Player)event.getEntity();
			master.onPlayerDamage(player, event);
		}
	}
}
