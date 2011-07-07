package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.Target;
import com.elmakers.mine.bukkit.utilities.SimpleBlockAction;

public class LightningSpell extends Spell
{
    protected int    maxRadius = 32;
    
    public LightningSpell()
    {
        setCooldown(2000);
        addVariant("storm", Material.GRILLED_PORK, getCategory(), "Start a lightning storm", "10");
    }
    
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
    public boolean onCast(String[] parameters)
    {
        Target target = getTarget();
        if (!target.hasTarget())
        {
            sendMessage(player, "No target");
            return false;
        }
       

        int radius = 1;
        for (int i = 0; i < parameters.length; i++)
        {
            // try radius;
            try
            {
                radius = Integer.parseInt(parameters[0]);
            }
            catch(NumberFormatException ex)
            {
            }
        }
        
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

    @Override
    public String getName()
    {
        return "lightning";
    }

    @Override
    public String getCategory()
    {
        return "combat";
    }

    @Override
    public String getDescription()
    {
        return "Strike lighting at your target";
    }

    @Override
    public Material getMaterial()
    {
        return Material.COOKED_FISH;
    }

}
