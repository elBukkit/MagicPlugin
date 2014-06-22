package com.elmakers.mine.bukkit.spell.builtin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.UndoableSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectSpell extends UndoableSpell
{
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target target = getTarget();
		if (!target.hasTarget())
		{
			return SpellResult.NO_TARGET;
		}

        List<LivingEntity> targetEntities = new ArrayList<LivingEntity>();

        Entity targetedEntity = target.getEntity();
        if (target.hasEntity() && targetedEntity instanceof LivingEntity) {
            targetEntities.add((LivingEntity)targetedEntity);
        }

        int radius = parameters.getInt("radius", 0);
        radius = (int)(mage.getRadiusMultiplier() * radius);

        if (radius > 0) {
            List<Entity> entities = CompatibilityUtils.getNearbyEntities(location, radius, radius, radius);
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity && entity != targetedEntity) {
                    targetEntities.add((LivingEntity)entity);
                }
            }
        }

        Integer duration = null;
        if (parameters.contains("duration")) {
            duration = parameters.getInt("duration");
        }
		Collection<PotionEffect> effects = getPotionEffects(parameters, duration);
        for (LivingEntity targetEntity : targetEntities) {
            if (targetEntity != mage.getEntity()) {
                // Check for superprotected mages
                if (controller.isMage(targetEntity)) {
                    Mage targetMage = controller.getMage(targetEntity);

                    // Check for protected players
                    if (isSuperProtected(targetMage)) {
                        continue;
                    }

                    if (parameters.getBoolean("deactivate_target_mage")) {
                        targetMage.deactivateAllSpells();
                    }
                }
            }

            if (parameters.contains("damage")) {
                registerModified(targetEntity);
                targetEntity.damage(parameters.getDouble("damage") * mage.getDamageMultiplier());
            } else {
                registerPotionEffects(targetEntity);
            }

            CompatibilityUtils.applyPotionEffects(targetEntity, effects);

            if (parameters.contains("remove_effects")) {
                List<String> removeKeys = parameters.getStringList("remove_effects");
                for (String removeKey : removeKeys) {
                    PotionEffectType removeType = PotionEffectType.getByName(removeKey);
                    targetEntity.removePotionEffect(removeType);
                }
            }
        }
		registerForUndo();
		return SpellResult.CAST;
	}
}
