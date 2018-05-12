package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;

public class PotionEffectAction extends BaseSpellAction
{
    private Set<PotionEffectType> removeEffects;
    private Collection<PotionEffect> addEffects;
    private Integer duration;
    private boolean ambient = true;
    private boolean particles = true;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);
        ambient = parameters.getBoolean("effects_ambient", true);
        particles = parameters.getBoolean("effects_particles", true);
        if (parameters.contains("remove_effects"))
        {
            removeEffects = new HashSet<>();
            Collection<String> removeEffectKeys = parameters.getStringList("remove_effects");
            for (String removeKey : removeEffectKeys)
            {
                try {
                    PotionEffectType removeType = PotionEffectType.getByName(removeKey);
                    if (removeType != null) {
                        removeEffects.add(removeType);
                    }
                } catch (Exception ex) {
                    spell.getController().getLogger().log(Level.WARNING, "Invalid potion effect type: " + removeKey, ex);
                }
            }
        }
        else
        {
            removeEffects = null;
        }

        if (parameters.contains("duration"))
        {
            duration = parameters.getInt("duration");
            if (parameters.contains("duration_multiplier")) {
                duration = (int)Math.ceil(parameters.getDouble("duration_multiplier") * duration);
            }
        }
        else
        {
            duration = null;
        }
        addEffects = BaseSpell.getPotionEffects(parameters, duration, ambient, particles);
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        ambient = parameters.getBoolean("effects_ambient", true);
        particles = parameters.getBoolean("effects_particles", true);
        if (parameters.contains("duration"))
        {
            int durationOverride = parameters.getInt("duration");
            if (parameters.contains("duration_multiplier")) {
                durationOverride = (int)Math.ceil(parameters.getDouble("duration_multiplier") * durationOverride);
            }
            if (duration == null || durationOverride != duration)
            {
                addEffects = BaseSpell.getPotionEffects(parameters, durationOverride, ambient, particles);
            }
        }
        if (addEffects == null) {
            addEffects = Collections.emptyList();
        }
        Collection<PotionEffect> mappedEffects = getMappedPotionEffects(parameters);
        if (!mappedEffects.isEmpty()) {
            Collection<PotionEffect> newEffects = new ArrayList<>(addEffects.size() + mappedEffects.size());
            newEffects.addAll(addEffects);
            newEffects.addAll(mappedEffects);
            addEffects = newEffects;
        }
    }

    private Collection<PotionEffect> getMappedPotionEffects(ConfigurationSection parameters) {
        ConfigurationSection section = parameters.getConfigurationSection("potion_effects");
        if (section == null) {
            section = parameters.getConfigurationSection("add_effects");
        }
        if (section != null) {
            Collection<String> keys = section.getKeys(false);
            Collection<PotionEffect> effects = new ArrayList<>(keys.size());
            int ticks = parameters.getInt("duration", 500) / 50;
            for (String key : keys) {
                int strength = section.getInt(key, 0);
                PotionEffectType type = PotionEffectType.getByName(key);
                if (type != null) {
                    effects.add(new PotionEffect(type, type.isInstant() ? 1 : ticks, strength, ambient, particles));
                }
            }
            return effects;
        } else {
            return Collections.emptyList();
        }
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
        context.registerPotionEffects(targetEntity);
        boolean effected = false;
        if (addEffects != null && addEffects.size() > 0)
        {
            effected = true;
            CompatibilityUtils.applyPotionEffects(targetEntity, addEffects);
        }

        if (removeEffects != null)
        {
            Collection<PotionEffect> currentEffects = targetEntity.getActivePotionEffects();
            for (PotionEffect effect : currentEffects)
            {
                PotionEffectType removeType = effect.getType();
                if (removeEffects.contains(removeType) && effect.getDuration() < Integer.MAX_VALUE / 4)
                {
                    targetEntity.removePotionEffect(removeType);
                    effected = true;
                }
            }
        }

        return effected ? SpellResult.CAST : SpellResult.NO_TARGET;
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
        parameters.add("duration");
        PotionEffectType[] effectTypes = PotionEffectType.values();
        for (PotionEffectType effectType : effectTypes) {
            if (effectType == null) continue;
            parameters.add("effect_" + effectType.getName().toLowerCase());
        }
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("duration")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_DURATIONS)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
