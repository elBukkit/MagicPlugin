package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class LavaSpell extends Spell
{
	@Override
	public boolean onCast(ConfigurationNode parameters) 
	{
		Block target = getTargetBlock();
		if (target == null) 
		{
			castMessage(player, "No target");
			return false;
		}
		
		int lavaBlocks = (int)getDistance(player, target);
		if (lavaBlocks <= 0) return false;
		
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
		
		BlockList burnedBlocks = new BlockList();
		for (int i = 0; i < lavaBlocks; i++)
		{
			Block currentTarget = target.getWorld().getBlockAt(targetLoc.getBlockX(), targetLoc.getBlockY(), targetLoc.getBlockZ());
			if (currentTarget.getType() == Material.AIR)
			{
				burnedBlocks.add(currentTarget);
				Material mat = i > 15 ? Material.STATIONARY_LAVA : Material.LAVA;
				byte data = i > 15 ? 15 : (byte)i;
				
				currentTarget.setType(mat);
				currentTarget.setData(data);
			}
			targetLoc.add(aim);
		}
		
		if (burnedBlocks.size() > 0)
		{
			burnedBlocks.setTimeToLive(2);
			spells.addToUndoQueue(player, burnedBlocks);
		}
		
		castMessage(player, "Blasted " + burnedBlocks.size() + " lava blocks");
		
		return true;
	}
}
