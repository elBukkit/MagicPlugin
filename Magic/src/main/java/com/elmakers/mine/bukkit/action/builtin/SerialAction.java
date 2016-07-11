package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class SerialAction extends CompoundAction
{
    @Override
    public SpellResult step(CastContext context) {
        return startActions();
    }
}
