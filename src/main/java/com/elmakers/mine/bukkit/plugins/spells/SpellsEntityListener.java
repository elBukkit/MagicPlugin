package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

class SpellsEntityListener extends EntityListener 
{
	private Spells master;
	
	public void setSpells(Spells master)
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
