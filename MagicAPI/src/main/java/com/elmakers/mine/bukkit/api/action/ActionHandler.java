package com.elmakers.mine.bukkit.api.action;

import com.elmakers.mine.bukkit.api.spell.SpellResult;

public interface ActionHandler {
    SpellResult perform(CastContext context);
    void finish(CastContext context);
    int size();
}
