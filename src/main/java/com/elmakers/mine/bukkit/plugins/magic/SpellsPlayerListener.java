package com.elmakers.mine.bukkit.plugins.magic;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

class SpellsPlayerListener implements Listener 
{
	private Spells master;
	
	public void setSpells(Spells master)
	{
		this.master = master;
	}
	
    /**
     * Called when a player attempts to move location in a world
     *
     * @param event Relevant event details
     */
	@EventHandler
    public void onPlayerMove(PlayerMoveEvent event) 
    {
    	master.onPlayerMove(event);
    }
 
    /**
     * Called when a player uses an item
     * 
     * @param event Relevant event details
     */
	@EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) 
    {
    	master.onPlayerInteract(event);
    }

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		master.onPlayerQuit(event);
	}
}
