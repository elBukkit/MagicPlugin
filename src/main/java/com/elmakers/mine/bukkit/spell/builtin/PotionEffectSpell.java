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
		Player player = getPlayer();
		if (targetEntity != player) {
			// Check for superprotected mages
			if (targetEntity instanceof Player) {
				Player targetPlayer = (Player)targetEntity;
				Mage targetMage = controller.getMage(targetPlayer);
				
				// Check for protected players
				if (targetMage.isSuperProtected()) {
					return SpellResult.NO_TARGET;
				}
				
				if (parameters.getBoolean("deactivate_target_mage")) {
					targetMage.deactivateAllSpells();
				}
			}
		}
		
		Collection<PotionEffect> effects = getPotionEffects(parameters);
		registerPotionEffects(targetEntity);
		CompatibilityUtils.applyPotionEffects(targetEntity, effects);
		registerForUndo();
		return SpellResult.CAST;
	}
}
