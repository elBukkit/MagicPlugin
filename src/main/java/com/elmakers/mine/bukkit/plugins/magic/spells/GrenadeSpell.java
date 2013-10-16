package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class GrenadeSpell extends Spell
{
    @Override
    public boolean onCast(ConfigurationNode parameters) 
    {
        Block target = getNextBlock();
        Location loc = target.getLocation();
        TNTPrimed grenade = (TNTPrimed)player.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
        
        Vector aim = getAimVector();
        grenade.setVelocity(aim);
        grenade.setYield(6);
        grenade.setFuseTicks(80);
        
        return true;
    }
}
