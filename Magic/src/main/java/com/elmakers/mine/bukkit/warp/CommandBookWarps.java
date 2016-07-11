package com.elmakers.mine.bukkit.warp;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.commandbook.locations.NamedLocation;
import com.sk89q.commandbook.locations.RootLocationManager;
import com.sk89q.commandbook.locations.WarpsComponent;
import com.zachsthings.libcomponents.ComponentManager;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

/**
 * Encapsulates CommandBook warps
 */
public class CommandBookWarps {
    private final RootLocationManager<NamedLocation> locationManager;

    private CommandBookWarps(RootLocationManager<NamedLocation> locationManager)
    {
        this.locationManager = locationManager;
    }

    public static CommandBookWarps create(Plugin plugin)
    {
        if (plugin instanceof CommandBook) {
            ComponentManager<BukkitComponent> componentManager = ((CommandBook)plugin).getComponentManager();
            WarpsComponent component = componentManager.getComponent(WarpsComponent.class);
            if (component == null) return null;

            RootLocationManager<NamedLocation> locationManager = component.getManager();
            return locationManager != null ? new CommandBookWarps(locationManager) : null;
        }

        return null;
    }

    public Location getWarp(String warpName) {
        if (locationManager == null) return null;
        NamedLocation location = locationManager.get(null, warpName);
        if (location == null) return null;
        return location.getLocation();
    }
}
