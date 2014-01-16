package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.blocks.BlockList;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class IterateSpell extends Spell
{
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		int timeToLive = parameters.getInt("undo", 0);
		boolean incrementData = parameters.getBoolean("increment_data", false);
		Block target = getTargetBlock();
		if (target == null) 
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(target) || !hasBuildPermission(player.getLocation())) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		int iterateBlocks = (int)player.getLocation().distance(target.getLocation());
		if (iterateBlocks <= 0) return SpellResult.NO_TARGET;

		Vector targetLoc = new Vector(target.getX(), target.getY(), target.getZ());
		Vector playerLoc = new Vector(player.getLocation().getX(), player.getLocation().getY() + 1, player.getLocation().getZ());

		// Create aim vector - this should probably replace Spell.getAimVector, which seems broken!
		Vector aim = targetLoc;
		aim.subtract(playerLoc);
		aim.normalize();
		targetLoc = playerLoc;

		// Move out a bit for safety!
		targetLoc.add(aim);
		targetLoc.add(aim);

		Material material = Material.DIRT;
		byte data = 0;
		ItemStack buildWith = getBuildingMaterial();
		if (buildWith != null)
		{
			material = buildWith.getType();
			data = getItemData(buildWith);
		}
		
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
				controller.addToUndoQueue(player, iteratedBlocks);
			}
			else
			{
				iteratedBlocks.setTimeToLive(timeToLive);
				controller.scheduleCleanup(player.getName(), iteratedBlocks);
			}
		}

		castMessage("Filled " + iteratedBlocks.size() + " blocks");

		return SpellResult.SUCCESS;
	}
	
	@Override
	public boolean usesMaterial() {
		return true;
	}
}
