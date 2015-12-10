package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class StopAction extends BaseSpellAction
{
	@Override
	public SpellResult perform(CastContext context)
	{
        return SpellResult.STOP;
	}
}
