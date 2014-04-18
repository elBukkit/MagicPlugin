package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.BlockList;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.plugins.magic.spell.BrushSpell;

public class BridgeSpell extends BrushSpell 
{
	int MAX_SEARCH_DISTANCE = 16;

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Block playerBlock = getPlayerBlock();
		if (playerBlock == null) 
		{
			// no spot found to bridge
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(playerBlock)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		BlockFace direction = getPlayerFacing();
		Block attachBlock = playerBlock;
		Block targetBlock = attachBlock;

		int distance = 0;
		while (isTargetable(targetBlock.getType()) && distance <= MAX_SEARCH_DISTANCE)
		{
			distance++;
			attachBlock = targetBlock;
			targetBlock = attachBlock.getRelative(direction);
		}
		if (isTargetable(targetBlock.getType()))
		{
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(targetBlock)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		MaterialBrush buildWith = getMaterialBrush();
		buildWith.setTarget(attachBlock.getLocation(), targetBlock.getLocation());
		buildWith.update(mage, targetBlock.getLocation());
		
		BlockList bridgeBlocks = new BlockList();
		bridgeBlocks.add(targetBlock);
		buildWith.modify(targetBlock);

		bridgeBlocks.setTimeToLive(parameters.getInt("undo", 0));
		registerForUndo(bridgeBlocks);
		controller.updateBlock(targetBlock);

		//castMessage("Facing " + playerRot + " : " + direction.name() + ", " + distance + " spaces to " + attachBlock.getType().name());

		return SpellResult.CAST;
	}
}
