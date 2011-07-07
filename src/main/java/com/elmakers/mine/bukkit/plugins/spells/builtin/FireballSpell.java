package com.elmakers.mine.bukkit.plugins.spells.builtin;

import net.minecraft.server.EntityFireball;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.MathHelper;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.spells.Spell;

public class FireballSpell extends Spell 
{
    public FireballSpell()
    {
        setCooldown(1500);
    }
    
	@Override
	public boolean onCast(String[] parameters) 
	{
        CraftWorld cw = (CraftWorld)player.getWorld();
        WorldServer world = cw.getHandle();
        CraftPlayer craftPlayer = (CraftPlayer)player;
        EntityLiving playerEntity = craftPlayer.getHandle();
        Location playerLoc = player.getLocation();
        Vector aim = getAimVector();
        int fireballX = (int)(playerLoc.getX() + aim.getX() * 2 + 0.5);
        int fireballY = (int)(playerLoc.getY() + aim.getY() * 2 + 2);
        int fireballZ = (int)(playerLoc.getZ() + aim.getZ() * 2 + 0.5);
        double d0 = aim.getX();
        double d1 = aim.getY();
        double d2 = aim.getZ();

        EntityFireball fireball = new EntityFireball(world, playerEntity, aim.getX(), aim.getY(), aim.getZ());
        fireball.setPositionRotation(fireballX, fireballY, fireballZ, playerLoc.getYaw(), playerLoc.getPitch());
 
        // De-randomize aim vector
        double d3 = (double) MathHelper.a(d0 * d0 + d1 * d1 + d2 * d2);

        fireball.c = d0 / d3 * 0.1D;
        fireball.d = d1 / d3 * 0.1D;
        fireball.e = d2 / d3 * 0.1D;
        
        world.addEntity(fireball);
		return true;
	}

	@Override
	public String getName() 
	{
		return "fireball";
	}

	@Override
	public String getDescription() 
	{
		return "Cast an exploding fireball";
	}

	@Override
	public String getCategory() 
	{
		return "combat";
	}

	@Override
	public Material getMaterial()
	{
		return Material.NETHERRACK;
	}
}
