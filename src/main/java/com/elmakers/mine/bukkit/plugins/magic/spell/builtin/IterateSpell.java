package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.effect.ParticleType;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.BlockList;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.effect.EffectUtils;
import com.elmakers.mine.bukkit.plugins.magic.spell.BrushSpell;

public class IterateSpell extends BrushSpell
{
	private int				DEFAULT_SIZE			= 16;
	
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		int timeToLive = parameters.getInt("undo", 0);
		boolean incrementData = parameters.getBoolean("increment_data", false);
		int radius = parameters.getInt("radius", 0);
		// radius = (int)(radius * mage.getRadiusMultiplier());
		int size = parameters.getInt("size", DEFAULT_SIZE);
		boolean reverse = parameters.getBoolean("reverse", false);
		size = (int)(mage.getConstructionMultiplier() * (float)size);
		
		boolean reverseTargeting = parameters.getBoolean("transparent_reverse", false);
		if (reverseTargeting) {
			setReverseTargeting(true);
		}
		
		Block target = getTargetBlock();
		if (target == null) 
		{
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(target) || !hasBuildPermission(getLocation().getBlock())) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		int iterateBlocks = (int)getLocation().distance(target.getLocation());
		if (iterateBlocks <= 0) return SpellResult.NO_TARGET;
		iterateBlocks = Math.min(iterateBlocks, size);

		Vector targetLoc = new Vector(target.getX(), target.getY(), target.getZ());
		Vector playerLoc = new Vector(getLocation().getX(), getLocation().getY() + 1, getLocation().getZ());

		Vector aim = null;
		if (reverse) {
			aim = playerLoc;
			aim.subtract(targetLoc);
			aim.normalize();
		} else {
			aim = targetLoc;
			aim.subtract(playerLoc);
			aim.normalize();
			
			targetLoc = playerLoc;

			// Move out a bit for safety!
			targetLoc.add(aim);
			targetLoc.add(aim);
		}

		MaterialBrush buildWith = getMaterialBrush();
		buildWith.setTarget(target.getLocation());
		buildWith.update(mage, target.getLocation());
		BlockList iteratedBlocks = new BlockList();
		for (int dr = 0; dr <= radius; dr++) {
			int spokes = 1;
			// TODO: Handle radius > 1 algorithmically....
			if (dr > 0) {
				// 8, 16, 24, 32
				spokes = dr * 8;
			}
			for (int dspoke = 0; dspoke < spokes; dspoke++) {
				Vector currentLoc = targetLoc.clone();
				if (dr > 0) {
					// Arbitrary axis rotation would be better, but... math is hard! :P
					// TODO: Arbitrary axis rotation.
					double q = (double)dspoke * Math.PI * 2 / spokes;
					if (aim.getY() > 0.7) {
						Vector axis = new Vector(1, 0 ,0);
						Vector perp = aim.clone().crossProduct(axis).multiply(dr);

						double x = perp.getZ() * Math.sin(q) - perp.getX() * Math.cos(q);
						double z = perp.getZ() * Math.cos(q) - perp.getX() * Math.sin(q);
						perp.setX(x);
						perp.setZ(z);
						
						currentLoc.add(perp);
					} else if (aim.getX() > 0.7) {
						Vector axis = new Vector(0, 1 ,0);
						Vector perp = aim.clone().crossProduct(axis).multiply(dr);
						
						double y = perp.getZ() * Math.sin(q) - perp.getY() * Math.cos(q);
						double z = perp.getZ() * Math.cos(q) - perp.getY() * Math.sin(q);
						perp.setY(y);
						perp.setZ(z);

						currentLoc.add(perp);
					} else {
						Vector axis = new Vector(0, 1 ,0);
						Vector perp = aim.clone().crossProduct(axis).multiply(dr);
						
						double y = perp.getX() * Math.sin(q) - perp.getY() * Math.cos(q);
						double x = perp.getX() * Math.cos(q) - perp.getY() * Math.sin(q);
						perp.setY(y);
						perp.setX(x);

						currentLoc.add(perp);
					}
				}
				for (int i = 0; i < iterateBlocks; i++)
				{
					Block currentTarget = target.getWorld().getBlockAt(currentLoc.getBlockX(), currentLoc.getBlockY(), currentLoc.getBlockZ());
					if (!isTargetable(currentTarget.getType()) && isDestructible(currentTarget) && hasBuildPermission(currentTarget))
					{
						iteratedBlocks.add(currentTarget);
		
						buildWith.update(mage, currentTarget.getLocation());
		
						if (incrementData) {
							byte data = buildWith.getData();
							data = i > 15 ? 15 : (byte)i;
							buildWith.setData(data);
						}
						
						buildWith.modify(currentTarget);
						
						controller.updateBlock(currentTarget);
						
						Location effectLocation = currentTarget.getLocation();	
						effectLocation.add(0.5f, 0.5f, 0.5f);
						
						if (dr == 0) {
							Material material = buildWith.getMaterial();
							// Kinda hacky.
							// TODO: Customize with effects system
							if (material == Material.AIR) {
								EffectUtils.playEffect(effectLocation, ParticleType.LARGE_EXPLOSION, 0.3f, 0.3f, 0.3f, 1, 1);
							} else {
								effectLocation.getWorld().playEffect(effectLocation, Effect.STEP_SOUND, material.getId());
							}
						}	
					}					
					currentLoc.add(aim);
				}
			}
		}

		if (iteratedBlocks.size() > 0)
		{
			iteratedBlocks.setTimeToLive(timeToLive);
			registerForUndo(iteratedBlocks);
		}

		return SpellResult.CAST;
	}
}
