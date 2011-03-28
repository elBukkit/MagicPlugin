package com.elmakers.mine.bukkit.spells;

import net.minecraft.server.EntityFireball;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.MathHelper;
import net.minecraft.server.Vec3D;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.magic.Spell;

public class FireballSpell extends Spell
{

    @Override
    public String getCategory()
    {
        return "combat";
    }

    @Override
    public String getDescription()
    {
        return "Cast an exploding fireball";
    }

    public Vec3D getLocation(Player player, float f)
    {
        Location playerLoc = player.getLocation();
        float rotationYaw = playerLoc.getYaw();
        float rotationPitch = playerLoc.getPitch();
        float prevRotationYaw = playerLoc.getYaw();
        float prevRotationPitch = playerLoc.getPitch();
        if (f == 1.0F)
        {
            float f1 = MathHelper.cos(-rotationYaw * 0.01745329F - 3.141593F);
            float f3 = MathHelper.sin(-rotationYaw * 0.01745329F - 3.141593F);
            float f5 = -MathHelper.cos(-rotationPitch * 0.01745329F);
            float f7 = MathHelper.sin(-rotationPitch * 0.01745329F);
            return Vec3D.createVector(f3 * f5, f7, f1 * f5);
        }
        else
        {
            float f2 = prevRotationPitch + (rotationPitch - prevRotationPitch) * f;
            float f4 = prevRotationYaw + (rotationYaw - prevRotationYaw) * f;
            float f6 = MathHelper.cos(-f4 * 0.01745329F - 3.141593F);
            float f8 = MathHelper.sin(-f4 * 0.01745329F - 3.141593F);
            float f9 = -MathHelper.cos(-f2 * 0.01745329F);
            float f10 = MathHelper.sin(-f2 * 0.01745329F);
            return Vec3D.createVector(f8 * f9, f10, f6 * f9);
        }
    }

    @Override
    public Material getMaterial()
    {
        return Material.NETHERRACK;
    }

    @Override
    public String getName()
    {
        return "fireball";
    }

    @Override
    public boolean onCast(List<ParameterData> parameters)
    {
        Block target = getTargetBlock();
        Location playerLoc = player.getLocation();
        if (target == null)
        {
            castMessage(player, "No target");
            return false;
        }

        double dx = target.getX() - playerLoc.getX();
        double height = 1;
        double dy = target.getY() + (height / 2.0F) - (playerLoc.getY() + (height / 2.0F));
        double dz = target.getZ() - playerLoc.getZ();

        castMessage(player, "FOOM!");
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityLiving playerEntity = craftPlayer.getHandle();
        EntityFireball fireball = new EntityFireball(((CraftWorld) player.getWorld()).getHandle(), playerEntity, dx, dy, dz);

        double d8 = 4D;
        Vec3D vec3d = getLocation(player, 1.0F);
        fireball.locX = playerLoc.getX() + vec3d.xCoord * d8;
        fireball.locY = playerLoc.getY() + (height / 2.0F) + 0.5D;
        fireball.locZ = playerLoc.getZ() + vec3d.zCoord * d8;

        ((CraftWorld) player.getWorld()).getHandle().a(fireball);
        return true;
    }
}
