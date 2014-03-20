package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.Collection;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class PotionEffectSpell extends Spell
{
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Target target = getTarget();
		if (!target.isEntity() || !(target.getEntity() instanceof LivingEntity))
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
			}
		}
		
		Collection<PotionEffect> effects = getPotionEffects(parameters);
		targetEntity.addPotionEffects(effects);
		return SpellResult.CAST;
	}
}
