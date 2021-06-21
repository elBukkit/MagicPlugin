package com.elmakers.mine.bukkit.utility.platform.v1_12;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.platform.legacy.EntityUtils;

public class Platform extends com.elmakers.mine.bukkit.utility.platform.v1_11.Platform {

    public Platform(Plugin plugin, Logger logger) {
        super(plugin, logger);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.EntityUtils createEntityUtils() {
        return new EntityUtils(this);
    }
}
