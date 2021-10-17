package com.elmakers.mine.bukkit.utility.platform.v1_15;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class Platform extends com.elmakers.mine.bukkit.utility.platform.v1_14.Platform {

    public Platform(MageController controller) {
        super(controller);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils createCompatibilityUtils() {
        return new CompatibilityUtils(this);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils createDeprecatedUtils() {
        return new DeprecatedUtils(this);
    }
}
