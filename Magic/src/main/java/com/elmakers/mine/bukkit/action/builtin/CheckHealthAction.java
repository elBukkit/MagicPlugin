package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.CheckAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class CheckHealthAction extends CheckAction {
    private Double minHealth;
    private Double maxHealth;
    private boolean fullHealth;
    private boolean percentages;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        if (parameters.contains("min_health")) {
            minHealth = parameters.getDouble("min_health");
        }
        if (parameters.contains("max_health")) {
            maxHealth = parameters.getDouble("max_health");
        }
        fullHealth = parameters.getBoolean("full_health", false);
        percentages = parameters.getBoolean("as_percentages", false);
    }

    @Override
    protected boolean isAllowed(CastContext context) {
        Entity targetEntity = context.getTargetEntity();
        if (targetEntity == null || !(targetEntity instanceof Damageable)) return false;
        Damageable damageable = (Damageable)targetEntity;
        double health = damageable.getHealth();
        if (fullHealth && health < CompatibilityLib.getCompatibilityUtils().getMaxHealth(damageable)) return false;
        if (percentages) {
            health = 100.0 * health / CompatibilityLib.getCompatibilityUtils().getMaxHealth(damageable);
        }
        if (minHealth != null && health < minHealth) return false;
        if (maxHealth != null && health > maxHealth) return false;
        return true;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("min_health");
        parameters.add("max_health");
        parameters.add("full_health");
        parameters.add("as_percentages");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("min_health") && parameterKey.equals("max_health")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_PERCENTAGES));
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else if (parameterKey.equals("full_health") || parameterKey.equals("as_percentages")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
