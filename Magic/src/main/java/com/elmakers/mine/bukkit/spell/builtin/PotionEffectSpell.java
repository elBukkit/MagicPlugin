package com.elmakers.mine.bukkit.spell.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.UndoableSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.Target;

@Deprecated
public class PotionEffectSpell extends UndoableSpell
{
    private static final PotionEffectType[] _negativeEffects =
            {PotionEffectType.BLINDNESS, PotionEffectType.CONFUSION, PotionEffectType.HARM,
                    PotionEffectType.HUNGER, PotionEffectType.POISON, PotionEffectType.SLOW,
                    PotionEffectType.SLOW_DIGGING, PotionEffectType.WEAKNESS, PotionEffectType.WITHER};
    protected static final Set<PotionEffectType> negativeEffects = new HashSet<>(Arrays.asList(_negativeEffects));

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        Target target = getTarget();
        if (!target.hasTarget())
        {
            return SpellResult.NO_TARGET;
        }

        List<LivingEntity> targetEntities = new ArrayList<>();

        Entity targetedEntity = target.getEntity();
        if (target.hasEntity() && targetedEntity instanceof LivingEntity) {
            targetEntities.add((LivingEntity)targetedEntity);
        }

        int radius = parameters.getInt("radius", 0);
        radius = (int)(mage.getRadiusMultiplier() * radius);

        if (radius > 0) {
            Collection<Entity> entities = CompatibilityUtils.getNearbyEntities(target.getLocation(), radius, radius, radius);
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity && entity != targetedEntity && entity != mage.getEntity()) {
                    targetEntities.add((LivingEntity)entity);
                }
            }
        }

        if (targetEntities.size() == 0) {
            return SpellResult.NO_TARGET;
        }

        int fallProtection = parameters.getInt("fall_protection", 0);

        Integer duration = null;
        if (parameters.contains("duration")) {
            duration = parameters.getInt("duration");
        }
        Collection<PotionEffect> effects = getPotionEffects(parameters, duration);
        for (LivingEntity targetEntity : targetEntities) {
            Mage targetMage = controller.isMage(targetEntity) ? controller.getMage(targetEntity) : null;

            if (targetMage != null && fallProtection > 0) {
                targetMage.enableFallProtection(fallProtection);
            }
            if (targetEntity != mage.getEntity()) {
                // Check for superprotected mages
                if (targetMage != null) {
                    // Check for protected players
                    if (isSuperProtected(targetMage)) {
                        continue;
                    }

                    if (parameters.getBoolean("deactivate_target_mage")) {
                        targetMage.deactivateAllSpells(true, false);
                    }
                }
            }

            if (targetEntity instanceof Player && parameters.getBoolean("feed", false)) {
                Player p = (Player)targetEntity;
                p.setExhaustion(0);
                p.setFoodLevel(20);
            }
            if (parameters.getBoolean("cure", false)) {
                Collection<PotionEffect> currentEffects = targetEntity.getActivePotionEffects();
                for (PotionEffect effect : currentEffects) {
                    if (negativeEffects.contains(effect.getType())) {
                        targetEntity.removePotionEffect(effect.getType());
                    }
                }
            }

            if (parameters.contains("heal")) {
                registerModified(targetEntity);
                double health = targetEntity.getHealth() + parameters.getDouble("heal");
                targetEntity.setHealth(Math.min(health, targetEntity.getMaxHealth()));
            } else if (parameters.contains("heal_percentage")) {
                registerModified(targetEntity);
                double health = targetEntity.getHealth() + targetEntity.getMaxHealth() * parameters.getDouble("heal_percentage");
                targetEntity.setHealth(Math.min(health, targetEntity.getMaxHealth()));
            } else if (parameters.contains("damage")) {
                registerModified(targetEntity);
                CompatibilityUtils.magicDamage(targetEntity, parameters.getDouble("damage") * mage.getDamageMultiplier(), mage.getEntity());
            } else {
                registerPotionEffects(targetEntity);
            }

            if (parameters.contains("fire")) {
                registerModified(targetEntity);
                targetEntity.setFireTicks(parameters.getInt("fire"));
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
