package com.elmakers.mine.bukkit.utility.platform.v1_15;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

public class Platform extends com.elmakers.mine.bukkit.utility.platform.v1_14.Platform {

    public Platform(Plugin plugin, Logger logger) {
        super(plugin, logger);
    }

    @Override
    protected void createCompatibilityUtils() {
        compatibilityUtils = new CompatibilityUtils(this);
    }
}
