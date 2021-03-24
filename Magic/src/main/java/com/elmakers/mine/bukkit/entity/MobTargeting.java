package com.elmakers.mine.bukkit.entity;

import java.util.List;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class MobTargeting {
    private final MageController controller;
    private final List<String> requiredParameters;
    private final List<String> deniedPermissions;
    private final double radius;

    private MobTargeting(MageController controller, ConfigurationSection parameters) {
        this.controller = controller;
        requiredParameters = ConfigurationUtils.getStringList(parameters, "required_permissions");
        deniedPermissions = ConfigurationUtils.getStringList(parameters, "denied_permissions");
        radius = parameters.getDouble("radius");
    }

    @Nullable
    public static MobTargeting getFromMobConfig(MageController controller, ConfigurationSection mobConfiguration) {
        ConfigurationSection targetingParameters = mobConfiguration.getConfigurationSection("targeting");
        if (targetingParameters == null) {
            return null;
        }
        return new MobTargeting(controller, targetingParameters);
    }

    public boolean canTarget(Entity target) {
        if (deniedPermissions != null) {
            for (String permission : deniedPermissions) {
                if (controller.hasPermission(target, permission)) {
                    return false;
                }
            }
        }
        if (requiredParameters != null) {
            boolean hasAny = false;
            for (String permission : requiredParameters) {
                if (controller.hasPermission(target, permission)) {
                    hasAny = true;
                    break;
                }
            }
            if (!hasAny) {
                return false;
            }
        }
        return true;
    }

    public void tick(Mage mage) {
        if (radius <= 0) {
            return;
        }
        Entity mageEntity = mage.getEntity();
        if (mageEntity == null || !(mageEntity instanceof Creature)) {
            return;
        }
        Creature mageCreature = (Creature)mageEntity;
        Entity currentTarget = mageCreature.getTarget();
        if (currentTarget != null && currentTarget.isValid() && canTarget(currentTarget)) {
            return;
        }

        // TODO: Line of sight checks
        Location location = mageEntity.getLocation();
        List<Entity> nearby = mageEntity.getNearbyEntities(radius, radius, radius);
        double closestDistance = Double.MAX_VALUE;
        Entity closestEntity = null;
        for (Entity checkTarget : nearby) {
            if (!checkTarget.isValid() || !canTarget(checkTarget)) continue;
            if (checkTarget == mageEntity) continue;
            double distanceTo = location.distanceSquared(checkTarget.getLocation());
            if (distanceTo > closestDistance) continue;
            closestDistance = distanceTo;
            closestEntity = checkTarget;
        }
        mage.setTarget(closestEntity);
    }
}
