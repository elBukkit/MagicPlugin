package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.elmakers.mine.bukkit.magic.Magic;

class SpellsPlayerListener extends PlayerListener
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
    public void onPlayerQuit(PlayerEvent event)
    {
        master.onPlayerQuit(event);
    }

    public void setSpells(Magic master)
    {
        this.master = master;
    }
}
