package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;

public class HealAction extends BaseSpellAction
{
    private double percentage;
    private double amount;
    private double maxDistanceSquared;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        percentage = parameters.getDouble("percentage", 0);
        amount = parameters.getDouble("amount", 20);
        double maxDistance = parameters.getDouble("heal_max_distance");
        maxDistanceSquared = maxDistance * maxDistance;
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Entity entity = context.getTargetEntity();
        if (!(entity instanceof LivingEntity))
        {
            return SpellResult.NO_TARGET;
        }

        LivingEntity targetEntity = (LivingEntity)entity;
        double maxHealth = DeprecatedUtils.getMaxHealth(targetEntity);
        if (targetEntity.getHealth() == maxHealth || targetEntity.isDead())
        {
            return SpellResult.NO_TARGET;
        }

        double healAmount = amount;
        if (percentage > 0)
        {
            healAmount = maxHealth * percentage;
        }

        if (maxDistanceSquared > 0) {
            double distanceSquared = context.getLocation().distanceSquared(entity.getLocation());
            if (distanceSquared > maxDistanceSquared) {
                return SpellResult.NO_TARGET;
            }
            if (distanceSquared > 0) {
                healAmount = healAmount * (1 - distanceSquared / maxDistanceSquared);
            }
        }

        EntityRegainHealthEvent event = new EntityRegainHealthEvent(targetEntity, healAmount, RegainReason.CUSTOM);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return SpellResult.CANCELLED;
        }
        healAmount = event.getAmount();
        if (healAmount == 0)
        {
            return SpellResult.NO_TARGET;
        }

        context.registerModified(targetEntity);
        targetEntity.setHealth(Math.min(targetEntity.getHealth() + healAmount, maxHealth));

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
        parameters.add("percentage");
        parameters.add("amount");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("percentage")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_PERCENTAGES)));
        } else if (parameterKey.equals("amount")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
