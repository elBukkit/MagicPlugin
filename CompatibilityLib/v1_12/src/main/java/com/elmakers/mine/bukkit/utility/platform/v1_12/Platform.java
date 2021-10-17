package com.elmakers.mine.bukkit.utility.platform.v1_12;

import org.bukkit.plugin.PluginManager;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.v1_12.listener.EntityPickupListener;

public class Platform extends com.elmakers.mine.bukkit.utility.platform.v1_11.Platform {

    public Platform(MageController controller) {
        super(controller);
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
    protected void registerPickupEvent(PluginManager pm) {
        pm.registerEvents(new EntityPickupListener(controller), controller.getPlugin());
    }
}
