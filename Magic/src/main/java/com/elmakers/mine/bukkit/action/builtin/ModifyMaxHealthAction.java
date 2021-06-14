package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class ModifyMaxHealthAction extends BaseSpellAction implements Listener {
    /**
     * The max health to be applied with this action.
     */
    private double health = 0.0;
    private double healthScale = 0.0;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        health = parameters.getDouble("max_health", 0.0);
        healthScale = parameters.getDouble("health_scale", 0.0);
    }

    @Override
    public SpellResult perform(CastContext context) {
        Entity target = context.getTargetEntity();

        if (target == null || !(target instanceof LivingEntity)) {
            return SpellResult.NO_TARGET;
        }
        if (health <= 0) {
            return SpellResult.FAIL;
        }

        LivingEntity li = (LivingEntity)target;
        if (li.getHealth() > health) {
            li.setHealth(health);
        }
        CompatibilityLib.getCompatibilityUtils().setMaxHealth(li, health);
        if (li instanceof Player && healthScale > 0) {
            Player player = (Player)li;
            player.setHealthScale(healthScale);
        }

        return SpellResult.CAST;
    }

    @Override
    public boolean isUndoable()
    {
        return false;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }


    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("max_health");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("max_health")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
