package com.elmakers.mine.bukkit.warp;

import javax.annotation.Nonnull;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class MagicWarp {
    @Nonnull
    private final String key;
    @Nonnull
    private Location location;
    private String icon;
    private String name;
    private String description;
    private String markerIcon;
    private String markerSet;

    public MagicWarp(String key, ConfigurationSection warps) {
        this.key = key;
        if (warps.isConfigurationSection(key)) {
            ConfigurationSection configuration = warps.getConfigurationSection(key);
            location = ConfigurationUtils.getLocation(configuration, "location");
            icon = configuration.getString("icon");
            name = configuration.getString("name");
            description = configuration.getString("description");
            markerIcon = configuration.getString("marker_icon");
            markerSet = configuration.getString("marker_set");
        } else {
            // Legacy warp file format
            location = ConfigurationUtils.getLocation(warps, key);
        }
    }

    public MagicWarp(String key, Location location) {
        this.key = key;
        this.location = location;
    }

    public void save(ConfigurationSection warps) {
        ConfigurationSection warpConfig = warps.createSection(key);
        warpConfig.set("location", ConfigurationUtils.fromLocation(location));
        warpConfig.set("icon", icon);
        warpConfig.set("name", name);
        warpConfig.set("description", description);
        warpConfig.set("marker_set", markerSet);
        warpConfig.set("marker_icon", markerIcon);
    }

    @Nonnull
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void checkMarker(MagicController controller) {
        if (markerIcon != null) {
            controller.addMarker("warp-" + key, markerIcon, getMarkerSet(), getName(), location, description);
        } else {
            removeMarker(controller);
        }
    }

    public void removeMarker(MagicController controller) {
        controller.removeMarker("warp-" + key, getMarkerSet());
    }

    public static String keyToName(String key) {
        String converted = key.replace("_", " ").replace("-", " ");
        return WordUtils.capitalizeFully(converted);
    }

    @Nonnull
    public String getName() {
        if (name != null) {
            return name;
        }
        return keyToName(key);
    }

    @Nonnull
    public String getMarkerSet() {
        return markerSet != null ? markerSet : "magic";
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMarkerIcon(String markerIcon) {
        this.markerIcon = markerIcon;
    }

    public void setMarkerSet(String markerSet) {
        this.markerSet = markerSet;
    }
}
