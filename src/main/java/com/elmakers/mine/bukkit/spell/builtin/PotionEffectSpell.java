package com.elmakers.mine.bukkit.spell.builtin;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.UndoableSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.Target;

public class PotionEffectSpell extends UndoableSpell
{
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target target = getTarget();
		if (!target.hasEntity() || !(target.getEntity() instanceof LivingEntity))
		{
			return SpellResult.NO_TARGET;
		}
		LivingEntity targetEntity = (LivingEntity)target.getEntity();
		if (targetEntity != mage.getEntity()) {
			// Check for superprotected mages
			if (controller.isMage(targetEntity)) {
				Mage targetMage = controller.getMage(targetEntity);
				
				// Check for protected players
				if (targetMage.isSuperProtected()) {
					return SpellResult.NO_TARGET;
				}
				
				if (parameters.getBoolean("deactivate_target_mage")) {
					targetMage.deactivateAllSpells();
				}
			}
		}

        Integer duration = null;
        if (parameters.contains("duration")) {
            duration = parameters.getInt("duration");
        }
		Collection<PotionEffect> effects = getPotionEffects(parameters, duration);
		registerPotionEffects(targetEntity);
		CompatibilityUtils.applyPotionEffects(targetEntity, effects);
		registerForUndo();
		return SpellResult.CAST;
	}
}
