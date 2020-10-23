package com.elmakers.mine.bukkit.warp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class MagicWarp {
    @Nonnull
    private final String key;
    @Nonnull
    private String locationDescriptor;
    @Nullable
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
            locationDescriptor = configuration.getString("location");
            icon = configuration.getString("icon");
            name = configuration.getString("name");
            description = configuration.getString("description");
            markerIcon = configuration.getString("marker_icon");
            markerSet = configuration.getString("marker_set");
        } else {
            // Legacy warp file format
            locationDescriptor = warps.getString(key);
        }
    }

    public MagicWarp(String key, Location location) {
        this.key = key;
        setLocation(location);
    }

    public void save(ConfigurationSection warps) {
        ConfigurationSection warpConfig = warps.createSection(key);
        warpConfig.set("location", locationDescriptor);
        warpConfig.set("icon", icon);
        warpConfig.set("name", name);
        warpConfig.set("description", description);
        warpConfig.set("marker_set", markerSet);
        warpConfig.set("marker_icon", markerIcon);
    }

    @Nullable
    public Location getLocation() {
        if (location == null || location.getWorld() == null) {
            location = ConfigurationUtils.toLocation(this.locationDescriptor);
        }
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
        this.locationDescriptor = ConfigurationUtils.fromLocation(location);
    }

    public void checkMarker(MagicController controller) {
        if (markerIcon != null) {
            controller.addMarker("warp-" + key, markerIcon, getMarkerSet(), getName(), getLocation(), description);
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
