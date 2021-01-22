package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class AirSupplyAction extends BaseSpellAction
{
    private int air;
    private boolean max;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters)
    {
        super.processParameters(context, parameters);
        max = parameters.getString("air", "").equalsIgnoreCase("max");
        if (!max) {
            air = parameters.getInt("air", 0);
        }
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Entity entity = context.getTargetEntity();

        if (entity == null || !(entity instanceof LivingEntity)) {
            return SpellResult.NO_TARGET;
        }
        LivingEntity livingEntity = (LivingEntity)entity;

        int airLevel = air;
        if (max) {
            airLevel = livingEntity.getMaximumAir();
        } else if (airLevel > livingEntity.getMaximumAir()) {
            airLevel = livingEntity.getMaximumAir();
        }
        context.registerModified(livingEntity);
        livingEntity.setRemainingAir(airLevel);
        return SpellResult.CAST;
    }

    @Override
    public boolean isUndoable()
    {
        return true;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("air");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("air")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
