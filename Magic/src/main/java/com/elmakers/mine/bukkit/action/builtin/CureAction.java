package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

@Deprecated
public class CureAction extends BaseSpellAction
{
    @Override
    public SpellResult perform(CastContext context)
    {
        Entity entity = context.getTargetEntity();
        if (entity == null || !(entity instanceof LivingEntity))
        {
            return SpellResult.NO_TARGET;
        }

        LivingEntity targetEntity = (LivingEntity)entity;
        Collection<PotionEffect> currentEffects = targetEntity.getActivePotionEffects();
        Set<PotionEffectType> negativeEffects = CompatibilityLib.getCompatibilityUtils().getNegativeEffects();
        for (PotionEffect effect : currentEffects)
        {
            if (negativeEffects.contains(effect.getType()))
            {
                context.registerPotionEffects(targetEntity);
                targetEntity.removePotionEffect(effect.getType());
            }
        }
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
}
