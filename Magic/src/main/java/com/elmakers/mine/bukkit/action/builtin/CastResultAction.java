package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class CastResultAction extends BaseSpellAction {
    private SpellResult result = SpellResult.CANCELLED;
    private boolean endResult = false;

    @Override
    public void processParameters(CastContext context, ConfigurationSection configuration) {
        endResult = configuration.getBoolean("end_result", false);
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
        while (endResult && context != null) {
            context.setResult(result);
            context.setInitialResult(result);
            if (context == context.getBaseContext()) break;
            context = context.getBaseContext();
        }
        return result;
    }
}
