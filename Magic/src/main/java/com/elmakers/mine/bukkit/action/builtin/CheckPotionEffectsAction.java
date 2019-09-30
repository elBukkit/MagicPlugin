package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

import com.elmakers.mine.bukkit.action.CheckAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class CheckPotionEffectsAction extends CheckAction {
    private Collection<PotionEffectType> required;
    private Collection<PotionEffectType> blocked;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        required = getEffects(context, parameters, "required");
        blocked = getEffects(context, parameters, "blocked");
    }

    @Nullable
    private Collection<PotionEffectType> getEffects(CastContext context, ConfigurationSection section, String key) {
        List<String> list = ConfigurationUtils.getStringList(section, key);
        if (list == null || list.isEmpty()) return null;

        List<PotionEffectType> effects = new ArrayList<>();
        for (String effectKey : list) {
            try {
                PotionEffectType effectType = PotionEffectType.getByName(effectKey.toUpperCase());
                effects.add(effectType);
            } catch (Exception ex) {
                context.getLogger().warning("Invalid potion effect type: " + effectKey);
            }
        }
        return effects;
    }

    @Override
    protected boolean isAllowed(CastContext context) {
        Entity targetEntity = context.getTargetEntity();
        if (targetEntity == null || !(targetEntity instanceof LivingEntity)) return false;
        LivingEntity living = (LivingEntity)targetEntity;

        if (blocked != null) {
            for (PotionEffectType check : blocked) {
                if (living.hasPotionEffect(check)) {
                    return false;
                }
            }
        }

        if (required != null) {
            for (PotionEffectType check : required) {
                if (living.hasPotionEffect(check)) {
                    return true;
                }
            }
        }

        return required == null || required.isEmpty();
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
        parameters.add("required");
        parameters.add("blocked");
    }
}
