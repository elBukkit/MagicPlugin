package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class PillarSpell extends Spell 
{
	int MAX_SEARCH_DISTANCE = 255;

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCast(ConfigurationNode parameters) 
	{
		Block attachBlock = getTargetBlock();
		if (attachBlock == null)
		{
			castMessage(player, "No target");
			return false;
		}	

		BlockFace direction = BlockFace.UP;	
		String typeString = parameters.getString("type", "");
		if (typeString.equals("down"))
		{
			direction = BlockFace.DOWN;
		}

		Block targetBlock = attachBlock.getRelative(direction);
		int distance = 0;

		while (isTargetable(targetBlock.getType()) && distance <= MAX_SEARCH_DISTANCE)
		{
			distance++;
			attachBlock = targetBlock;
			targetBlock = attachBlock.getRelative(direction);
		}
		if (isTargetable(targetBlock.getType()))
		{
			player.sendMessage("Can't pillar any further");
			return false;
		}

		Material material = attachBlock.getType();
		byte data = attachBlock.getData();

		ItemStack buildWith = getBuildingMaterial();
		if (buildWith != null)
		{
			material = buildWith.getType();
			data = getItemData(buildWith);
		}

		BlockList pillarBlocks = new BlockList();
		Block pillar = getBlockAt(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
		pillarBlocks.add(pillar);
		pillar.setType(material);
		pillar.setData(data);

		castMessage(player, "Creating a pillar of " + attachBlock.getType().name().toLowerCase());
		spells.addToUndoQueue(player, pillarBlocks);

		return true;
	}
	
	@Override
	public boolean usesMaterial() {
		return true;
	}
}
