package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

public class LightningAction extends BaseSpellAction {
    private boolean effectOnly;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        effectOnly = parameters.getBoolean("effect_only", false);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Block block = context.getTargetBlock();
        if (effectOnly) {
            block.getWorld().strikeLightningEffect(block.getLocation());
        } else {
            if (!context.hasBuildPermission(block))
            {
                return SpellResult.INSUFFICIENT_PERMISSION;
            }
            block.getWorld().strikeLightning(block.getLocation());
        }
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
        return !effectOnly;
    }

    @Override
    public boolean requiresTarget()
    {
        return true;
    }
}
