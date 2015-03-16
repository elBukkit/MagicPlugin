package com.elmakers.mine.bukkit.spell.builtin;

import com.elmakers.mine.bukkit.action.ActionContext;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.batch.BlockRecurse;
import com.elmakers.mine.bukkit.action.builtin.ReplaceMaterialAction;
import com.elmakers.mine.bukkit.spell.BrushSpell;

public class RecurseSpell extends BrushSpell 
{
	private final BlockRecurse blockRecurse = new BlockRecurse();

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Block targetBlock = getTargetBlock();
		
		if (targetBlock == null) 
		{
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(targetBlock)) 
		{
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

        if (!isDestructible(targetBlock))
        {
            return SpellResult.NO_TARGET;
        }
		
		int size = parameters.getInt("size", 8);
		size = (int)(mage.getRadiusMultiplier() * size);
		blockRecurse.setMaxRecursion(size);

		ReplaceMaterialAction action = new ReplaceMaterialAction();
        action.initialize(parameters);
        action.addReplaceable(new MaterialAndData(targetBlock));
        Material targetMaterial = targetBlock.getType();

		// A bit hacky, but is very handy!
		if (targetMaterial == Material.STATIONARY_WATER || targetMaterial == Material.WATER)
		{
			for (byte i = 0; i < 9; i++) {
				action.addReplaceable(Material.STATIONARY_WATER, i);
				action.addReplaceable(Material.WATER, i);
			}
		}
		else if (targetMaterial == Material.STATIONARY_LAVA || targetMaterial == Material.LAVA)
		{
			for (byte i = 0; i < 9; i++) {
				action.addReplaceable(Material.STATIONARY_LAVA, i);
				action.addReplaceable(Material.LAVA, i);
			}
		}
		else if (targetMaterial == Material.SNOW) {
			for (byte i = 0; i < 8; i++) {
				action.addReplaceable(Material.SNOW, i);
			}
		}
        CastContext context = getCurrentCast();
        context.setTargetLocation(targetBlock.getLocation());
		blockRecurse.recurse(new ActionContext(action, parameters), context);
		registerForUndo();
		controller.updateBlock(targetBlock);
		
		return SpellResult.CAST;
	}
}
