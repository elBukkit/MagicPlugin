package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.BlockList;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.spell.BrushSpell;

public class PillarSpell extends BrushSpell 
{
	int MAX_SEARCH_DISTANCE = 255;

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Block attachBlock = getTargetBlock();
		if (attachBlock == null)
		{
			return SpellResult.NO_TARGET;
		}	

		BlockFace direction = BlockFace.UP;	
		String typeString = parameters.getString("type", "");
		if (typeString.equals("down"))
		{
			direction = BlockFace.DOWN;
		}

		Block targetBlock = attachBlock.getRelative(direction);
		int distance = 0;

		if (!hasBuildPermission(targetBlock)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
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

		BlockList pillarBlocks = new BlockList();
		Block pillar = targetBlock;
		buildWith.setTarget(attachBlock.getLocation(), pillar.getLocation());
		buildWith.update(mage, pillar.getLocation());
		pillarBlocks.add(pillar);
		buildWith.modify(pillar);
		
		pillarBlocks.setTimeToLive(parameters.getInt("undo", 0));
		
		registerForUndo(pillarBlocks);
		controller.updateBlock(pillar);
		
		return SpellResult.CAST;
	}
}
