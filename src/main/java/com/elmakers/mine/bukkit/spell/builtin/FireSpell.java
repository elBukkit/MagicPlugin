package com.elmakers.mine.bukkit.spell.builtin;

import com.elmakers.mine.bukkit.action.builtin.BurnAction;
import com.elmakers.mine.bukkit.action.builtin.CoverAction;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

@Deprecated
public class FireSpell extends BlockSpell
{
	private final static int		DEFAULT_RADIUS	= 4;
	private final static int		DEFAULT_ELEMENTAL_DAMAGE = 5;
	private final static int		DEFAULT_ELEMENTAL_FIRE_TICKS = 200;

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
        cover.addAction(new BurnAction());
        ActionHandler handler = new ActionHandler();
        handler.loadAction(cover);
        handler.initialize(parameters);
        registerForUndo();
        return handler.start(getCurrentCast(), parameters);
	}
}
