package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.Map;

import net.minecraft.server.EntityTNTPrimed;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;

public class GrenadeSpell extends Spell
{
    @Override
    public boolean onCast(ConfigurationNode parameters) 
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
        grenade.yield = 6;
        grenade.fuseTicks = 80;
        world.addEntity(grenade);
        
        return true;
    }
}
