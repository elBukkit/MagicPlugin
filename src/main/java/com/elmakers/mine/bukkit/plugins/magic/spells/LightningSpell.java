package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.SimpleBlockAction;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class LightningSpell extends Spell
{
    protected int    maxRadius = 32;
     
    public class ShockAction extends SimpleBlockAction
    {
        protected double density;
        protected int    thunderThreshold;
        protected Player player;

        public ShockAction(Player player, double density, int thunderThreshold)
        {
            this.player = player;
            this.density = density;
            this.thunderThreshold = thunderThreshold;
        }
        
        public boolean perform(Block block)
        {
            if (Math.random() > density) return false;
            
            CraftWorld craftWorld = ((CraftWorld)player.getWorld());
            craftWorld.strikeLightning(block.getLocation());
            super.perform(block);
            if (blocks.size() > thunderThreshold)
            {
                craftWorld.setThundering(true);
            }
            
            return true;
        }
    }
    
    @Override
    public boolean onCast(ConfigurationNode parameters) 
    {
        Target target = getTarget();
        if (!target.hasTarget())
        {
            sendMessage(player, "No target");
            return false;
        }
       
        int radius = parameters.getInt("radius", defaultRadius);
        if (radius > maxRadius && maxRadius > 0)
        {
            radius = maxRadius;
        }

        double ratio = (radius < 2) ? 1.0 : (radius < 4) ? 0.5 : 0.25;
        ShockAction action = new ShockAction(player, ratio, 5);

        if (radius <= 1)
        {
            action.perform(target.getBlock());
        }
        else
        {
            this.coverSurface(target.getLocation(), radius, action);
        }

        spells.addToUndoQueue(player, action.getBlocks());
        castMessage(player, "Zapped " + action.getBlocks().size() + " blocks");
        
        return true;
    }
}
