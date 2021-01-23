package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.action.builtin.CoverAction;
import com.elmakers.mine.bukkit.action.builtin.LightningAction;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.Target;

@Deprecated
public class LightningSpell extends BlockSpell
{
    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        Target target = getTarget();
        if (target == null || !target.isValid())
        {
            return SpellResult.NO_TARGET;
        }

        Block targetBlock = target.getBlock();
        if (!hasBuildPermission(targetBlock))
        {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        CoverAction cover = new CoverAction();
        cover.addAction(new LightningAction());
        ActionHandler handler = new ActionHandler();
        handler.loadAction(cover);
        handler.initialize(this, parameters);
        registerForUndo();
        return handler.cast(getCurrentCast(), parameters);
    }
}
