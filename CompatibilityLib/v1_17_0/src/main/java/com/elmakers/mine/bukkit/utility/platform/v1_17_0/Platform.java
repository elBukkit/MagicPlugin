package com.elmakers.mine.bukkit.utility.platform.v1_17_0;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.platform.base.PlatformBase;

public class Platform extends PlatformBase {

    public Platform(Plugin plugin, Logger logger) {
        super(plugin, logger);
        compatibilityUtils = new CompatibilityUtils(this);

        // valid = true;
    }
}
