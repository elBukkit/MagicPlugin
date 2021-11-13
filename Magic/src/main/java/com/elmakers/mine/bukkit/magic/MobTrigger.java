package com.elmakers.mine.bukkit.magic;

import javax.annotation.Nonnull;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class MobTrigger extends CustomTrigger {
    boolean requiresTarget = false;
    boolean swingArm = false;

    public MobTrigger(@Nonnull MageController controller, @Nonnull String key, @Nonnull ConfigurationSection configuration) {
        super(controller, key, configuration);
        requiresTarget = configuration.getBoolean("requires_target");
        swingArm = configuration.getBoolean("swing_arm", spells != null && !spells.isEmpty());
    }

    @Override
    public boolean isValid(Mage mage) {
        if (!super.isValid(mage)) return false;
        if (requiresTarget) {
            Entity entity = mage.getEntity();
            if (entity instanceof Creature) {
                return ((Creature)entity).getTarget() != null;
            }
        }
        return true;
    }

    @Override
    protected boolean cast(Mage mage, String castSpell, ConfigurationSection parameters) {
        boolean success = super.cast(mage, castSpell, parameters);
        if (success && swingArm) {
            CompatibilityLib.getCompatibilityUtils().swingMainHand(mage.getEntity());
        }
        return success;
    }
}
