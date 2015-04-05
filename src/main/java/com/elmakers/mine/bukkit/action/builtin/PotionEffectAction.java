package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PotionEffectAction extends BaseSpellAction
{
    private List<String> removeEffects;
    private Collection<PotionEffect> addEffects;
    private Integer duration;

    @Override
    public void initialize(ConfigurationSection parameters)
    {
        super.initialize(parameters);
        if (parameters.contains("remove_effects"))
        {
            removeEffects = parameters.getStringList("remove_effects");
        }
        else
        {
            removeEffects = null;
        }

        if (parameters.contains("duration"))
        {
            duration = parameters.getInt("duration");
        }
        else
        {
            duration = null;
        }
        addEffects = BaseSpell.getPotionEffects(parameters, duration);
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        if (parameters.contains("duration"))
        {
            int durationOverride = parameters.getInt("duration");
            if (duration == null || durationOverride != duration)
            {
                addEffects = BaseSpell.getPotionEffects(parameters, durationOverride);
            }
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
        if (addEffects != null)
        {
            CompatibilityUtils.applyPotionEffects(targetEntity, addEffects);
        }

        if (removeEffects != null)
        {
            for (String removeKey : removeEffects)
            {
                PotionEffectType removeType = PotionEffectType.getByName(removeKey);
                targetEntity.removePotionEffect(removeType);
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
