package com.elmakers.mine.bukkit.plugins.magic;

import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;

public class SpellsBlockListener implements Listener
{
    protected Spells manager = null;
    
    public void setSpells(Spells manager)
    {
        this.manager = manager;
    }
    
    public SpellsBlockListener()
    {
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event)
    {
        if (!manager.allowPhysics(event.getBlock()))
        {
            event.setCancelled(true);
        }
    }

}
