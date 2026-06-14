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

public class FreezeEntityAction extends BaseSpellAction
{
    private int freezeTicks;
    private boolean additive;
    private boolean reduce;
    private boolean lock;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        freezeTicks = parameters.getInt("duration", 0) * 20 / 1000;
        additive = parameters.getBoolean("additive", false);
        reduce = parameters.getBoolean("reduce", false);
        lock = parameters.getBoolean("lock", false);
    }

    @Override
    public SpellResult perform(CastContext context) {
        Entity targetEntity = context.getTargetEntity();
        if (targetEntity == null) {
            return SpellResult.ENTITY_REQUIRED;
        }
        int newFreezeTicks = freezeTicks;
        int currentFreezeTicks = targetEntity.getFreezeTicks();
        if (additive) {
            newFreezeTicks += Math.max(0, currentFreezeTicks);
        } else if (!reduce) {
            newFreezeTicks = Math.max(newFreezeTicks, currentFreezeTicks);
        }
        targetEntity.setFreezeTicks(newFreezeTicks);
        if (lock) {
            CompatibilityLib.getCompatibilityUtils().lockFreezeTicks(targetEntity, true);
        }

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
