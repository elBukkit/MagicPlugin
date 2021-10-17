package com.elmakers.mine.bukkit.utility.platform.v1_16;

import org.bukkit.plugin.PluginManager;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.EntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.platform.v1_16.event.ResourcePackListener;

public class Platform extends com.elmakers.mine.bukkit.utility.platform.v1_15.Platform {

    public Platform(MageController controller) {
        super(controller);
    }

    @Override
    protected EntityMetadataUtils createEntityMetadataUtils() {
        return new PersistentEntityMetadataUtils(this.getPlugin());
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils createCompatibilityUtils() {
        return new CompatibilityUtils(this);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.EntityUtils createEntityUtils() {
        return new EntityUtils(this);
    }

    @Override
    public void registerEvents(PluginManager pm) {
        // Note that TimeListener is handled separately for .. reasons
        // Also note that the ModernPlatform does not inherit from here, so
        // we need to copy this registration there.
        super.registerEvents(pm);
        ResourcePackListener timeListener = new ResourcePackListener(controller);
        pm.registerEvents(timeListener, controller.getPlugin());
    }
}
