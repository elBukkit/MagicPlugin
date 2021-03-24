package com.elmakers.mine.bukkit.entity;

import java.util.List;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class MobTargeting {
    private final MageController controller;
    private final List<String> allowedPermissions;
    private final List<String> deniedPermissions;

    private MobTargeting(MageController controller, ConfigurationSection parameters) {
        this.controller = controller;
        allowedPermissions = ConfigurationUtils.getStringList(parameters, "allowed_permissions");
        deniedPermissions = ConfigurationUtils.getStringList(parameters, "denied_permissions");
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
        if (allowedPermissions != null) {
            boolean hasAny = false;
            for (String permission : allowedPermissions) {
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
}
