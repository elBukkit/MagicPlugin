package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class DeployElytraAction extends BaseSpellAction
{
	@Override
	public SpellResult perform(CastContext context)
	{
        Entity targetEntity = context.getTargetEntity();
		if (!(targetEntity instanceof Player))
		{
			return SpellResult.NO_TARGET;
		}

        Player player = (Player)targetEntity;
        CompatibilityUtils.deployElytra(player);

		return SpellResult.CAST;
	}

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
