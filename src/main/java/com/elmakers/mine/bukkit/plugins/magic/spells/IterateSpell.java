package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.blocks.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.BrushSpell;
import com.elmakers.mine.bukkit.plugins.magic.MaterialBrush;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class IterateSpell extends BrushSpell
{
	private int				DEFAULT_SIZE			= 16;
	
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		int timeToLive = parameters.getInt("undo", 0);
		boolean incrementData = parameters.getBoolean("increment_data", false);
		int size = parameters.getInt("size", DEFAULT_SIZE);
		size = (int)(mage.getConstructionMultiplier() * (float)size);
		
		Block target = getTargetBlock();
		if (target == null) 
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(target) || !hasBuildPermission(getPlayer().getLocation().getBlock())) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		int iterateBlocks = (int)getPlayer().getLocation().distance(target.getLocation());
		if (iterateBlocks <= 0) return SpellResult.NO_TARGET;
		iterateBlocks = Math.min(iterateBlocks, size);

		Vector targetLoc = new Vector(target.getX(), target.getY(), target.getZ());
		Vector playerLoc = new Vector(getPlayer().getLocation().getX(), getPlayer().getLocation().getY() + 1, getPlayer().getLocation().getZ());

		// Create aim vector - this should probably replace Spell.getAimVector, which seems broken!
		Vector aim = targetLoc;
		aim.subtract(playerLoc);
		aim.normalize();
		targetLoc = playerLoc;

		// Move out a bit for safety!
		targetLoc.add(aim);
		targetLoc.add(aim);

		MaterialBrush buildWith = getMaterialBrush();
		buildWith.setTarget(target.getLocation());
		buildWith.update(target.getLocation());
		
		Material material = buildWith.getMaterial();
		byte data = buildWith.getData();
		
		BlockList iteratedBlocks = new BlockList();
		for (int i = 0; i < iterateBlocks; i++)
		{
			Block currentTarget = target.getWorld().getBlockAt(targetLoc.getBlockX(), targetLoc.getBlockY(), targetLoc.getBlockZ());
			if (currentTarget.getType() == Material.AIR && !isIndestructible(currentTarget) && hasBuildPermission(currentTarget))
			{
				iteratedBlocks.add(currentTarget);
				if (incrementData) {
					data = i > 15 ? 15 : (byte)i;
				}

				currentTarget.setType(material);
				currentTarget.setData(data);
				
				buildWith.update(currentTarget.getLocation());
				controller.updateBlock(currentTarget);
				
				Location effectLocation = currentTarget.getLocation();	
				effectLocation.getWorld().playEffect(effectLocation, Effect.STEP_SOUND, material.getId());	
			}
			targetLoc.add(aim);
		}

		if (iteratedBlocks.size() > 0)
		{
			if (timeToLive == 0)
			{
				controller.addToUndoQueue(getPlayer(), iteratedBlocks);
			}
			else
			{
				iteratedBlocks.setTimeToLive(timeToLive);
				controller.scheduleCleanup(getPlayer().getName(), iteratedBlocks);
			}
		}

		castMessage("Filled " + iteratedBlocks.size() + " blocks");

		return SpellResult.SUCCESS;
	}
}
