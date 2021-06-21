package com.elmakers.mine.bukkit.utility.platform.v1_15;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

public class Platform extends com.elmakers.mine.bukkit.utility.platform.v1_14.Platform {

    public Platform(Plugin plugin, Logger logger) {
        super(plugin, logger);
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
