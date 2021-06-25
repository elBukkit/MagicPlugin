package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;

public class EntityAnimationAction extends BaseSpellAction {
    private enum AnimationType {
        SWING_MAIN_HAND,
        SWING_OFF_HAND
    }

    private AnimationType animationType;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        String animationTypeKey = parameters.getString("animation", "swing_main_hand");
        try {
            animationType = AnimationType.valueOf(animationTypeKey.toUpperCase());
        } catch (Exception ex) {
            context.getLogger().warning("Invalid animation type: " + animationTypeKey);
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        Entity target = context.getTargetEntity();
        if (!(target instanceof LivingEntity)) {
            return SpellResult.LIVING_ENTITY_REQUIRED;
        }

        CompatibilityUtils compatibilityUtils = CompatibilityLib.getCompatibilityUtils();
        LivingEntity targetEntity = (LivingEntity)target;
        switch (animationType) {
            case SWING_MAIN_HAND:
                compatibilityUtils.swingMainHand(targetEntity);
                break;
            case SWING_OFF_HAND:
                compatibilityUtils.swingOffhand(targetEntity);
                break;
        }
        return SpellResult.CAST;
    }

    @Override
    public boolean isUndoable() {
        return false;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }
}
