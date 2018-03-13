package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.configuration.ConfigurationSection;

public class PlayEffectsAction extends BaseSpellAction
{
	private String effectKey;

	@Override
	public SpellResult perform(CastContext context)
	{
        if (effectKey == null || effectKey.isEmpty()) {
            return SpellResult.FAIL;
        }
        context.playEffects(effectKey, 1.0f, context.getTargetBlock());
		return SpellResult.CAST;
	}

	@Override
	public void prepare(CastContext context, ConfigurationSection parameters) {
		super.prepare(context, parameters);
        effectKey = parameters.getString("effect");
		effectKey = parameters.getString("effects", effectKey);
	}
}
