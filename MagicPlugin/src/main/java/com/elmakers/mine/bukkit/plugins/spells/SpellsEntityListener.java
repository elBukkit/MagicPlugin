package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

import com.elmakers.mine.bukkit.magic.Magic;

class SpellsEntityListener extends EntityListener 
{
	private Magic master;
	
	public void setSpells(Magic master)
	{
		this.master = master;
	}
	
	public void onEntityDamage(EntityDamageEvent event)
	{
		if (Player.class.isInstance(event.getEntity()))
		{
			Player player = (Player)event.getEntity();
			master.onPlayerDamage(player, event);
		}
	}
}
