package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class CastResultAction extends BaseSpellAction {
    private SpellResult result = SpellResult.CANCELLED;

    @Override
    public void prepare(CastContext context, ConfigurationSection configuration) {
        String spellResultString = configuration.getString("result");
        if (spellResultString != null && !spellResultString.isEmpty()) {
            try {
                result = SpellResult.valueOf(spellResultString.toUpperCase());
            } catch (Exception ex) {
                context.getLogger().warning("Invalid cast result: " + spellResultString);
            }
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        return result;
    }
}
