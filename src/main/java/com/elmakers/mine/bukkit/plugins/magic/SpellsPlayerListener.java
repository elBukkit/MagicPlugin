package com.elmakers.mine.bukkit.plugins.magic;

import java.util.logging.Logger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

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
	public void onPlayerEquip(PlayerItemHeldEvent event)
	{
		master.onPlayerEquip(event);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		master.onPlayerQuit(event);
	}

	@EventHandler
	public void onPluginDisable(PluginDisableEvent event)
	{
		master.onPluginDisable(event);
	}
	
	@EventHandler
	public void onPluginEnable(PluginEnableEvent event)
	{
		master.onPluginEnable(event);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		master.onPlayerJoin(event);
	}
}
