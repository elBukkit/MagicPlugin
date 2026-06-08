package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class BurnEntityAction extends BaseSpellAction
{
    private int burnTicks;
    private boolean additive;
    private boolean reduce;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        burnTicks = parameters.getInt("duration", 0) * 20 / 1000;
        additive = parameters.getBoolean("additive", false);
        reduce = parameters.getBoolean("reduce", false);
    }

    @Override
    public SpellResult perform(CastContext context) {
        Entity targetEntity = context.getTargetEntity();
        if (targetEntity == null) {
            return SpellResult.ENTITY_REQUIRED;
        }
        int newBurnTicks = burnTicks;
        int currentBurnTicks = targetEntity.getFireTicks();
        if (additive) {
            newBurnTicks += Math.max(0, currentBurnTicks);
        } else if (!reduce) {
            newBurnTicks = Math.max(newBurnTicks, currentBurnTicks);
        }
        targetEntity.setFireTicks(newBurnTicks);

        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("duration");
        parameters.add("additive");
        parameters.add("reduce");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("duration")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_DURATIONS));
        } else if (parameterKey.equals("additive") || parameterKey.equals("reduce")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
