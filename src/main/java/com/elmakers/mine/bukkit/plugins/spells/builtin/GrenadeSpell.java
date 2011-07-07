package com.elmakers.mine.bukkit.plugins.spells.builtin;

import net.minecraft.server.EntityTNTPrimed;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.spells.Spell;

public class GrenadeSpell extends Spell
{
    public GrenadeSpell()
    {
       setCooldown(2000);
    }
    
    @Override
    public String getDescription()
    {
        return "Place a primed grenade";
    }

    @Override
    public String getName()
    {
        return "grenade";
    }

    @Override
    public boolean onCast(String[] parameters) 
    {
        Block target = getNextBlock();

        CraftWorld cw = (CraftWorld)player.getWorld();
        WorldServer world = cw.getHandle();
        Location loc = target.getLocation();
        
        EntityTNTPrimed grenade = new EntityTNTPrimed(world);
        grenade.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
        
        Vector aim = getAimVector();
        grenade.motX = aim.getX();
        grenade.motY = aim.getY();
        grenade.motZ = aim.getZ();
        grenade.fuseTicks = 80;
        world.addEntity(grenade);
        
        return true;
    }

    @Override
    public String getCategory()
    {
        return "combat";
    }

    @Override
    public Material getMaterial()
    {
        return Material.TNT;
    }
}
