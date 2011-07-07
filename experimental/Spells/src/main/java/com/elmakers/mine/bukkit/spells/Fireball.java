package com.elmakers.mine.bukkit.spells;

import net.minecraft.server.EntityFireball;
import net.minecraft.server.EntityLiving;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;

public class Fireball extends Spell
{
    @Override
    public String getDescription()
    {
        return "Cast an exploding fireball";
    }

    @Override
    public String getName()
    {
        return "fireball";
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        Block target = targeting.getTargetBlock();
        Location playerLoc = player.getLocation();
        if (target == null)
        {
            castMessage(player, "No target");
            return false;
        }

        double dx = target.getX() - playerLoc.getX();
        double height = 1;
        double dy = target.getY() + height / 2.0F - (playerLoc.getY() + height / 2.0F);
        double dz = target.getZ() - playerLoc.getZ();

        castMessage(player, "FOOM!");
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityLiving playerEntity = craftPlayer.getHandle();
        EntityFireball fireball = new EntityFireball(((CraftWorld) player.getWorld()).getHandle(), playerEntity, dx, dy, dz);

        // Start it off a bit away from the player
        double distance = 4;
        Vector aim = targeting.getAimVector();
        fireball.locX = playerLoc.getX() + aim.getX() * distance;
        fireball.locY = playerLoc.getY() + (height / 2.0) + 0.5;
        fireball.locZ = playerLoc.getZ() + aim.getZ() * distance;

        ((CraftWorld) player.getWorld()).getHandle().a(fireball);
        return true;
    }
}
