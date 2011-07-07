package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.spells.Spell;

public class PillarSpell extends Spell 
{
	int MAX_SEARCH_DISTANCE = 255;
	
	public PillarSpell()
	{
		addVariant("stalactite", Material.WOOD_AXE, "construction", "Create a downward pillar", "down");
	}
	
	@Override
	public boolean onCast(String[] parameters) 
	{
		Block attachBlock = getTargetBlock();
		if (attachBlock == null)
		{
			castMessage(player, "No target");
			return false;
		}	

		BlockFace direction = BlockFace.UP;	
		if (parameters.length > 0 && parameters[0].equalsIgnoreCase("down"))
		{
			direction = BlockFace.DOWN;
		}
		
		Block targetBlock = attachBlock.getFace(direction);
		int distance = 0;
		
		while (isTargetable(targetBlock.getType()) && distance <= MAX_SEARCH_DISTANCE)
		{
			distance++;
			attachBlock = targetBlock;
			targetBlock = attachBlock.getFace(direction);
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
	public String getName() 
	{
		return "pillar";
	}

	@Override
	public String getDescription() 
	{
		return "Raises a pillar up (or down)";
	}

	@Override
	public String getCategory() 
	{
		return "construction";
	}

	@Override
	public Material getMaterial()
	{
		return Material.GOLD_AXE;
	}
}
