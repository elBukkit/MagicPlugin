package com.elmakers.mine.bukkit.utility.platform.v1_17_1;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.modern.ModernPlatform;
import com.elmakers.mine.bukkit.utility.platform.v1_17_1.event.EntityLoadEventHandler;

public class Platform extends ModernPlatform {
    private Boolean hasEntityLoadEvent;

    public Platform(Plugin plugin, Logger logger) {
        super(plugin, logger);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.NBTUtils createNBTUtils() {
        return new NBTUtils(this);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.ItemUtils createItemUtils() {
        return new ItemUtils(this);
    }

    @Override
    protected com.elmakers.mine.bukkit.utility.platform.InventoryUtils createInventoryUtils() {
        return new InventoryUtils(this);
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
    public boolean hasEntityLoadEvent() {
        if (hasEntityLoadEvent == null) {
            try {
                Class.forName("org.bukkit.event.world.EntitiesLoadEvent");
                hasEntityLoadEvent = true;
            } catch (Exception ex) {
                hasEntityLoadEvent = false;
                getLogger().warning("EntitiesLoadEvent not found, it is recommended that you update your server software");
            }
        }
        return hasEntityLoadEvent;
    }

    @Override
    public boolean hasDeferredEntityLoad() {
        return true;
    }

    @Override
    public void registerEvents(MageController controller, PluginManager pm) {
        super.registerEvents(controller, pm);
        if (hasEntityLoadEvent()) {
            pm.registerEvents(new EntityLoadEventHandler(controller), controller.getPlugin());
        }
    }
}
