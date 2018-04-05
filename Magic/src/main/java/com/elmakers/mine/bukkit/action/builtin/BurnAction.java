package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;

public class BurnAction extends BaseSpellAction
{
    @Override
    public SpellResult perform(CastContext context)
    {
        Block block = context.getTargetBlock();
        if (block == null || block.getType() == Material.AIR || block.getType() == Material.FIRE || block.getType() == Material.WATER)
        {
            return SpellResult.NO_TARGET;
        }
        Material material = Material.FIRE;
        if (block.getType() == Material.ICE || block.getType() == Material.SNOW || block.getType() == Material.PACKED_ICE || block.getType() == Material.FROSTED_ICE)
        {
            material = Material.AIR;
        }
        else
        {
            block = block.getRelative(BlockFace.UP);
        }
        if (block.getType() == Material.FIRE || block.getType() == Material.WATER)
        {
            return SpellResult.NO_TARGET;
        }
        if (!context.isDestructible(block))
        {
            return SpellResult.NO_TARGET;
        }
        context.registerForUndo(block);
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
