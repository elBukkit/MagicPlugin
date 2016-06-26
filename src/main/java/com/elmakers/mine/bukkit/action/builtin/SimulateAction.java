package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class SimulateAction extends BaseSpellAction {
	public static boolean DEBUG = false;

    @Override
	public void reset(CastContext context) {
        super.reset(context);
    }

    @Override
    public SpellResult perform(CastContext context) {
        return SpellResult.CAST;
    }
}
