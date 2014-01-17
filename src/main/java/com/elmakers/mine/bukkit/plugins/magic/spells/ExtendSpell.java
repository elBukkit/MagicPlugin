package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.blocks.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.MaterialBrush;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class ExtendSpell extends Spell 
{
	int MAX_SEARCH_DISTANCE = 16;

	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Block playerBlock = getPlayerBlock();
		if (playerBlock == null) 
		{
			// no spot found to bridge
			castMessage("You need to be standing on something");
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(playerBlock)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		BlockFace direction = getPlayerFacing();
		Block attachBlock = playerBlock;
		Block targetBlock = attachBlock.getRelative(direction);

		MaterialBrush buildWith = getMaterialBrush();
		buildWith.setTarget(targetBlock.getLocation());

		Material material = buildWith.getMaterial();
		byte data = buildWith.getData();

		int distance = 0;
		while (isTargetable(targetBlock.getType()) && distance <= MAX_SEARCH_DISTANCE)
		{
			distance++;
			attachBlock = targetBlock;
			targetBlock = attachBlock.getRelative(direction);
		}
		if (isTargetable(targetBlock.getType()))
		{
			castMessage("Can't bridge any further");
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(targetBlock)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		BlockList bridgeBlocks = new BlockList();
		bridgeBlocks.add(targetBlock);
		targetBlock.setType(material);
		targetBlock.setData(data);

		castMessage("A bridge extends!");
		controller.addToUndoQueue(getPlayer(), bridgeBlocks);
		controller.updateBlock(targetBlock);
		//castMessage("Facing " + playerRot + " : " + direction.name() + ", " + distance + " spaces to " + attachBlock.getType().name());

		return SpellResult.SUCCESS;
	}
	
	@Override
	public boolean usesBrush() {
		return true;
	}
}
