package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;

public class SpellsBlockListener extends BlockListener
{
    protected Spells manager = null;
    
    public void setSpells(Spells manager)
    {
        this.manager = manager;
    }
    
    public SpellsBlockListener()
    {
    }

    @Override
    public void onBlockPhysics(BlockPhysicsEvent event)
    {
        if (!manager.allowPhysics(event.getBlock()))
        {
            event.setCancelled(true);
        }
    }

}
