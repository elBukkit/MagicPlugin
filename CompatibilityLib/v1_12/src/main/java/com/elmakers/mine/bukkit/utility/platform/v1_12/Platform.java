package com.elmakers.mine.bukkit.utility.platform.v1_12;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.v1_12.listener.EntityPickupListener;

public class Platform extends com.elmakers.mine.bukkit.utility.platform.v1_11.Platform {

    public Platform(Plugin plugin, Logger logger) {
        super(plugin, logger);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.EntityUtils createEntityUtils() {
        return new EntityUtils(this);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils createCompatibilityUtils() {
        return new CompatibilityUtils(this);
    }

    @Override
    protected void registerPickupEvent(MageController controller, PluginManager pm) {
        pm.registerEvents(new EntityPickupListener(controller), controller.getPlugin());
    }
}
