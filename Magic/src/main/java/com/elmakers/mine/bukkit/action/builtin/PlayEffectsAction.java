package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;

import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

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

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("particle");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("particle")) {
            for (Particle particle : Particle.values()) {
                examples.add(particle.name().toLowerCase());
            }
        }  else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
