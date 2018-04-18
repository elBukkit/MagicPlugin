package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class RestrictedAction extends BaseSpellAction {
    private final String message;

    public RestrictedAction(String message) {
        this.message = message;
    }

    @Override
    public SpellResult perform(CastContext context) {
        context.getMage().sendMessage(message);
        return SpellResult.FAIL;
    }
}
