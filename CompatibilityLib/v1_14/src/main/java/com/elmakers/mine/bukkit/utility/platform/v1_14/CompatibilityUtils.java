package com.elmakers.mine.bukkit.utility.platform.v1_14;

import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.utility.platform.Platform;

public class CompatibilityUtils extends com.elmakers.mine.bukkit.utility.platform.v1_13.CompatibilityUtils {

    public CompatibilityUtils(Platform platform) {
        super(platform);
    }

    @Override
    public boolean isArrow(Entity projectile) {
        return projectile instanceof AbstractArrow;
    }
}
