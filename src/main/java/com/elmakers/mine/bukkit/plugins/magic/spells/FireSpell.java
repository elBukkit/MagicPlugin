package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.EffectTrail;
import com.elmakers.mine.bukkit.utilities.ParticleType;
import com.elmakers.mine.bukkit.utilities.SimpleBlockAction;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class FireSpell extends Spell
{
    private final static int 		maxEffectRange = 16;
    private final static int 		effectSpeed = 1;
    private final static float 		particleSpeed = 0.2f;
    private final static int 		effectPeriod = 2;
    private final static int 		particleCount = 1;
    
	public class FireAction extends SimpleBlockAction
	{
		public SpellResult perform(Block block)
		{
			if (block.getType() == Material.AIR || block.getType() == Material.FIRE)
			{
				return SpellResult.NO_TARGET;
			}
			Material material = Material.FIRE;

			if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER || block.getType() == Material.ICE || block.getType() == Material.SNOW)
			{
				material = Material.AIR;
			}
			else
			{
				block = block.getRelative(BlockFace.UP);
			}

			super.perform(block);
			block.setType(material);

			return SpellResult.SUCCESS;
		}
	}

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		int effectRange = Math.min(getMaxRange(), maxEffectRange);
		Location effectLocation = player.getEyeLocation();
		Vector effectDirection = effectLocation.getDirection();
		EffectTrail effectTrail = new EffectTrail(spells.getPlugin(), effectLocation, effectDirection, effectRange);
		effectTrail.setParticleType(ParticleType.FLAME);
		effectTrail.setParticleCount(particleCount);
		effectTrail.setEffectSpeed(particleSpeed);
		effectTrail.setParticleOffset(0.2f, 0.2f, 0.2f);
		effectTrail.setSpeed(effectSpeed);
		effectTrail.setPeriod(effectPeriod);
		effectTrail.start();
		
		Block target = getTargetBlock();
		if (target == null) 
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}

		int radius = parameters.getInt("radius", defaultRadius);
		radius = (int)(playerSpells.getPowerMultiplier() * radius);
		
		FireAction action = new FireAction();

		if (radius <= 1)
		{
			action.perform(target);
		}
		else
		{
			this.coverSurface(target.getLocation(), radius, action);
		}

		spells.addToUndoQueue(player, action.getBlocks());
		castMessage("Burned " + action.getBlocks().size() + " blocks");
		spells.updateBlock(target);

		return SpellResult.SUCCESS;
	}

	public void burnBlock(int dx, int dy, int dz, Block centerPoint, int radius, BlockList burnedBlocks)
	{
		int x = centerPoint.getX() + dx - radius;
		int y = centerPoint.getY() + dy - radius;
		int z = centerPoint.getZ() + dz - radius;
		Block block = player.getWorld().getBlockAt(x, y, z);
		int depth = 0;

		if (block.getType() == Material.AIR)
		{
			while (depth < verticalSearchDistance && block.getType() == Material.AIR)
			{
				depth++;
				block = block.getRelative(BlockFace.DOWN);
			}	
		}
		else
		{
			while (depth < verticalSearchDistance && block.getType() != Material.AIR)
			{
				depth++;
				block = block.getRelative(BlockFace.UP);
			}
			block = block.getRelative(BlockFace.DOWN);
		}

		if (block.getType() == Material.AIR || block.getType() == Material.FIRE)
		{
			return;
		}
		Material material = Material.FIRE;

		if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER || block.getType() == Material.ICE || block.getType() == Material.SNOW)
		{
			material = Material.AIR;
		}
		else
		{
			block = block.getRelative(BlockFace.UP);
		}

		burnedBlocks.add(block);
		block.setType(material);
	}

	public int checkPosition(int x, int z, int R)
	{
		return (x * x) +  (z * z) - (R * R);
	}

	@Override
	public void onLoadTemplate(ConfigurationNode properties)  
	{
		defaultRadius = properties.getInteger("radius", defaultRadius);
		verticalSearchDistance = properties.getInteger("vertical_search_distance", verticalSearchDistance);
	}

	private int				defaultRadius			= 4;
	private int				verticalSearchDistance	= 8;
}
