package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.action.BaseSpellAction;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class BurnAction extends BaseSpellAction
{
	public SpellResult perform(CastContext context)
	{
        Block block = context.getTargetBlock();
		if (block == null || block.getType() == Material.AIR || block.getType() == Material.FIRE || block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
		{
			return SpellResult.NO_TARGET;
		}
		Material material = Material.FIRE;
		if (block.getType() == Material.ICE || block.getType() == Material.SNOW)
		{
			material = Material.AIR;
		}
		else
		{
			block = block.getRelative(BlockFace.UP);
		}
		if (block.getType() == Material.FIRE || block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
		{
			return SpellResult.NO_TARGET;
		}
		context.updateBlock(block);
        context.registerForUndo(block, true);
		MaterialAndData applyMaterial = new MaterialAndData(material);
		applyMaterial.modify(block);

		return SpellResult.CAST;
	}

    @Override
    public boolean isUndoable()
    {
        return true;
    }

    @Override
    public boolean requiresBuildPermission()
    {
        return true;
    }

    @Override
    public boolean requiresTarget()
    {
        return true;
    }
}
