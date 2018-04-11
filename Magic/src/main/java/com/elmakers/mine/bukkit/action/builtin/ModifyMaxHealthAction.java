package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class ModifyMaxHealthAction extends BaseSpellAction implements Listener {
    /**
     * The max health to be applied with this action.
     */
    private double health = 0.0;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters) {
        super.initialize(spell, parameters);
        health = parameters.getDouble("health", 0.0);
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
        li.setMaxHealth(health);

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
}
