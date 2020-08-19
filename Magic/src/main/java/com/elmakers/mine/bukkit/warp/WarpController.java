package com.elmakers.mine.bukkit.warp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class WarpController {
    private CommandBookWarps commandBook;
    private EssentialsWarps essentials;
    private final Map<String, Location> warps = new HashMap<>();

    public void load(ConfigurationSection warpData) {
        warps.clear();
        Set<String> keys = warpData.getKeys(false);
        for (String key : keys) {
            warps.put(key, ConfigurationUtils.getLocation(warpData, key));
        }
    }

    public void save(ConfigurationSection warpData) {
        for (Map.Entry<String, Location> warp : warps.entrySet()) {
            warpData.set(warp.getKey(), ConfigurationUtils.fromLocation(warp.getValue()));
        }
    }

    public Collection<String> getCustomWarps() {
        return warps.keySet();
    }

    public boolean hasCustomWarp(String warpName) {
        return warps.containsKey(warpName);
    }

    public void setWarp(String warpName, Location location) {
        warps.put(warpName, location);
    }

    public boolean removeWarp(String warpName) {
        return warps.remove(warpName) != null;
    }

    public int importWarps() {
        if (commandBook != null) {
            warps.putAll(commandBook.getWarps());
        }
        if (essentials != null) {
            warps.putAll(essentials.getWarps());
        }
        return warps.size();
    }

    @Nullable
    public Location getWarp(String warpName) {
        Location warp = warps.get(warpName);
        if (warp == null && commandBook != null) {
            warp = commandBook.getWarp(warpName);
        }
        if (warp == null && essentials != null) {
            warp = essentials.getWarp(warpName);
        }
        return warp;
    }

    public boolean setCommandBook(Plugin plugin) {
        commandBook = CommandBookWarps.create(plugin);
        return (commandBook != null);
    }

    public boolean setEssentials(Plugin plugin) {
        essentials = EssentialsWarps.create(plugin);
        return (essentials != null);
    }

    public List<String> getWarps() {
        return new ArrayList<>(warps.keySet());
    }
}
