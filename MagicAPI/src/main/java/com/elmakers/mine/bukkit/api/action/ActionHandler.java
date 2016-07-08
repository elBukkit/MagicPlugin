package com.elmakers.mine.bukkit.api.action;

import com.elmakers.mine.bukkit.api.spell.SpellResult;

public interface ActionHandler {
    public SpellResult perform(CastContext context);
    public void finish(CastContext context);
    public int size();
}
