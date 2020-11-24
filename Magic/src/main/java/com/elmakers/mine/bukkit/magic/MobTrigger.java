package com.elmakers.mine.bukkit.magic;

import javax.annotation.Nonnull;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class MobTrigger extends CustomTrigger {
    boolean requiresTarget = false;

    public MobTrigger(@Nonnull MageController controller, @Nonnull String key, @Nonnull ConfigurationSection configuration) {
        super(controller, key, configuration);
        requiresTarget = configuration.getBoolean("requires_target");
    }

    public boolean isValid(Mage mage) {
        if (!isValid(mage)) return false;
        if (requiresTarget) {
            Entity entity = mage.getEntity();
            if (entity instanceof Creature) {
                return ((Creature)entity).getTarget() != null;
            }
        }
        return true;
    }
}
