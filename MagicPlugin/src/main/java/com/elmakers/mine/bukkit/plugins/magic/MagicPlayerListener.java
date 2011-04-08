package com.elmakers.mine.bukkit.plugins.magic;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.elmakers.mine.bukkit.magic.Magic;

class MagicPlayerListener extends PlayerListener
{
    private Magic master;

    /**
     * Called when a player attempts to move location in a world
     * 
     * @param event
     *            Relevant event details
     */
    @Override
    public void onPlayerMove(PlayerMoveEvent event)
    {
        master.onPlayerMove(event);
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        master.onPlayerQuit(event);
    }

    public void setMagic(Magic master)
    {
        this.master = master;
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        master.onPlayerInteract(event);
    }
}
