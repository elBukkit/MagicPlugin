package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;

public class BridgeSpell extends Spell 
{
	int MAX_SEARCH_DISTANCE = 16;
	
	@Override
	public boolean onCast(Map<String, Object> parameters) 
	{
		Block playerBlock = getPlayerBlock();
		if (playerBlock == null) 
		{
			// no spot found to bridge
			player.sendMessage("You need to be standing on something");
			return false;
		}
		
		BlockFace direction = getPlayerFacing();
		Block attachBlock = playerBlock;
		Block targetBlock = attachBlock.getFace(direction);
		
		Material material = targetBlock.getType();
		byte data = targetBlock.getData();
		
		ItemStack buildWith = getBuildingMaterial();
		if (buildWith != null)
		{
			material = buildWith.getType();
			data = getItemData(buildWith);
		}
		
		int distance = 0;
		while (isTargetable(targetBlock.getType()) && distance <= MAX_SEARCH_DISTANCE)
		{
			distance++;
			attachBlock = targetBlock;
			targetBlock = attachBlock.getFace(direction);
		}
		if (isTargetable(targetBlock.getType()))
		{
			player.sendMessage("Can't bridge any further");
			return false;
		}
		BlockList bridgeBlocks = new BlockList();
		bridgeBlocks.add(targetBlock);
		targetBlock.setType(material);
		targetBlock.setData(data);
		
		castMessage(player, "A bridge extends!");
		spells.addToUndoQueue(player, bridgeBlocks);
		
		//castMessage(player, "Facing " + playerRot + " : " + direction.name() + ", " + distance + " spaces to " + attachBlock.getType().name());
		
		return true;
	}
}
