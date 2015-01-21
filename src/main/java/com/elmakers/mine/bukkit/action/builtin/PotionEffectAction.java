package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.EntityAction;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PotionEffectAction extends BaseSpellAction implements EntityAction
{
	@Override
	public SpellResult perform(ConfigurationSection parameters, Entity entity)
	{
		if (!(entity instanceof LivingEntity))
		{
			return SpellResult.NO_TARGET;
		}

        Integer duration = null;
        if (parameters.contains("duration"))
        {
            duration = parameters.getInt("duration");
        }
        LivingEntity targetEntity = (LivingEntity)entity;
        registerPotionEffects(targetEntity);
		Collection<PotionEffect> effects = getPotionEffects(parameters, duration);
        CompatibilityUtils.applyPotionEffects(targetEntity, effects);

        if (parameters.contains("remove_effects")) {
            List<String> removeKeys = parameters.getStringList("remove_effects");
            for (String removeKey : removeKeys) {
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
    public void getParameterNames(Collection<String> parameters) {
        super.getParameterNames(parameters);
        parameters.add("duration");
        PotionEffectType[] effectTypes = PotionEffectType.values();
        for (PotionEffectType effectType : effectTypes) {
            if (effectType == null) continue;
            parameters.add("effect_" + effectType.getName().toLowerCase());
        }
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("duration")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_DURATIONS)));
        } else {
            super.getParameterOptions(examples, parameterKey);
        }
    }
}
