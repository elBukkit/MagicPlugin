package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BrushSpell;

@Deprecated
public class TossSpell extends BrushSpell
{
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Location location = getLocation();
		if (!hasBuildPermission(location.getBlock())) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		location.setY(location.getY() - 1);
		MaterialBrush buildWith = getBrush();
		buildWith.setTarget(location);

		Material material = buildWith.getMaterial();
		byte data = buildWith.getBlockData();
		
		int tossCount = 1;
		tossCount = parameters.getInt("count", tossCount);
		tossCount = (int)(mage.getRadiusMultiplier() * tossCount);	
		float speed = 0.6f;
		speed = (float)parameters.getDouble("speed", speed);
		
		Vector direction = getDirection();
		direction.normalize().multiply(speed);
		Vector up = new Vector(0, 1, 0);
		Vector perp = new Vector();
		perp.copy(direction);
		perp.crossProduct(up);
		
		for (int i = 0; i < tossCount; i++)
		{
			FallingBlock block = null;
			location = getEyeLocation();
			location.setX(location.getX() + perp.getX() * (Math.random() * tossCount / 4 - tossCount / 8));
			location.setY(location.getY());
			location.setZ(location.getZ() + perp.getZ() * (Math.random() * tossCount / 4 - tossCount / 8));
			block = getWorld().spawnFallingBlock(location, material, data);

			if (block == null)
			{
				registerForUndo();
				return SpellResult.FAIL;
			}
			registerForUndo(block);
			block.setDropItem(false);
			block.setVelocity(direction);	
		}

		registerForUndo();
		return SpellResult.CAST;
	}
}
