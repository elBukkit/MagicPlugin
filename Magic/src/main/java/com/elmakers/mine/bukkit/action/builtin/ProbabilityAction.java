package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class ProbabilityAction extends CompoundAction
{
    private double probability;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        probability = parameters.getDouble("probability", 0.5);
    }

    @Override
    public SpellResult step(CastContext context)
    {
        if (context.getRandom().nextDouble() > probability) {
            return SpellResult.NO_ACTION;
        }

        return startActions();
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("probability");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("probability")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_PERCENTAGES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
