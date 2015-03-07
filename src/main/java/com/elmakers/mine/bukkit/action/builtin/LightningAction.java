package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.block.Block;

public class LightningAction extends BaseSpellAction {
    public SpellResult perform(CastContext context)
    {
        Block block = context.getTargetBlock();
        if (!context.hasBuildPermission(block))
        {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        block.getWorld().strikeLightning(block.getLocation());
        return SpellResult.CAST;
    }

    @Override
    public boolean isUndoable()
    {
        return false;
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
