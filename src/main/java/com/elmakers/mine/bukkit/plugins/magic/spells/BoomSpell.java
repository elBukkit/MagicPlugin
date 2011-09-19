package com.elmakers.mine.bukkit.plugins.magic.spells;

import net.minecraft.server.EntityLiving;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.MathHelper;
import com.elmakers.mine.bukkit.utilities.Vec3D;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class BoomSpell extends Spell {

	protected int defaultSize = 1;
	
	public boolean createExplosionAt(Location target, float size, boolean incendiary)
	{
		if (target == null) 
		{
			castMessage(player, "No target");
			return false;
		}
		
		castMessage(player, "FOOM!");
		CraftPlayer craftPlayer = (CraftPlayer)player;
		EntityLiving playerEntity = craftPlayer.getHandle();
        WorldServer world = ((CraftWorld)player.getWorld()).getHandle();
		
        world.createExplosion(playerEntity, target.getBlockX(), target.getBlockY(), target.getBlockZ(), size, incendiary);
		return true;
	}
	
	@Override
	public boolean onCast(ConfigurationNode parameters) 
	{
        int size = parameters.getInt("size", defaultSize);
        boolean useFire = parameters.getBoolean("fire", false);
        String targetType = (String)parameters.getString("target", "");
        if (targetType.equals("here"))
        {
            player.damage(100);
            return createExplosionAt(player.getLocation(), size, useFire);
        }
        
		Target target = getTarget();
		if (!target.hasTarget())
		{
		    sendMessage(player, "No target");
            return false;
		}
		
		return createExplosionAt(target.getLocation(), size, useFire);
	}
	
	public Vec3D getLocation(Player player, float f)
    {
		Location playerLoc = player.getLocation();
    	float rotationYaw = playerLoc.getYaw();
    	float rotationPitch = playerLoc.getPitch();
    	float prevRotationYaw = playerLoc.getYaw();
    	float prevRotationPitch = playerLoc.getPitch();
        if(f == 1.0F)
        {
            float f1 = MathHelper.cos(-rotationYaw * 0.01745329F - 3.141593F);
            float f3 = MathHelper.sin(-rotationYaw * 0.01745329F - 3.141593F);
            float f5 = -MathHelper.cos(-rotationPitch * 0.01745329F);
            float f7 = MathHelper.sin(-rotationPitch * 0.01745329F);
            return Vec3D.createVector(f3 * f5, f7, f1 * f5);
        } else
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
}
