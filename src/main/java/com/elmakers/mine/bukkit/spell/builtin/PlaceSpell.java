package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BrushSpell;
import com.elmakers.mine.bukkit.utility.Target;

public class PlaceSpell extends BrushSpell 
{
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target attachToBlock = getTarget();
		if (!attachToBlock.isValid()) return SpellResult.NO_TARGET;
		Block placeBlock = getPreviousBlock();

		MaterialBrush buildWith = getBrush();
		buildWith.setTarget(attachToBlock.getLocation(), placeBlock.getLocation());

		if (!hasBuildPermission(placeBlock)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		registerForUndo(placeBlock);
		buildWith.modify(placeBlock);

		registerForUndo();

		return SpellResult.CAST;
	}
}
