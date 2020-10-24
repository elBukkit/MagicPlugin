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

import com.elmakers.mine.bukkit.magic.MagicController;

public class WarpController {
    private final MagicController controller;
    private CommandBookWarps commandBook;
    private EssentialsWarps essentials;
    private final Map<String, MagicWarp> warps = new HashMap<>();

    public WarpController(MagicController controller) {
        this.controller = controller;
    }

    public void load(ConfigurationSection warpData) {
        warps.clear();
        Set<String> keys = warpData.getKeys(false);
        for (String key : keys) {
            MagicWarp warp = new MagicWarp(key, warpData);
            warps.put(key, warp);
            warp.checkMarker(controller);
        }
    }

    public void save(ConfigurationSection warpData) {
        for (MagicWarp warp : warps.values()) {
            warp.save(warpData);
        }
    }

    public Collection<String> getCustomWarps() {
        return warps.keySet();
    }

    public boolean hasCustomWarp(String warpName) {
        return warps.containsKey(warpName);
    }

    public void setWarp(String warpName, Location location) {
        MagicWarp warp = warps.get(warpName);
        if (warp == null) {
            warps.put(warpName, new MagicWarp(warpName, location));
        } else {
            warp.setLocation(location);
            warp.checkMarker(controller);
        }
    }

    public boolean removeWarp(String warpName) {
        return warps.remove(warpName) != null;
    }

    public int importWarps() {
        if (commandBook != null) {
            for (Map.Entry<String, Location> warpEntry : commandBook.getWarps().entrySet()) {
                String key = warpEntry.getKey();
                warps.put(key, new MagicWarp(key, warpEntry.getValue()));
            }
        }
        if (essentials != null) {
            for (Map.Entry<String, Location> warpEntry : essentials.getWarps().entrySet()) {
                String key = warpEntry.getKey();
                warps.put(key, new MagicWarp(key, warpEntry.getValue()));
            }
        }
        return warps.size();
    }

    @Nullable
    public MagicWarp getMagicWarp(String warpName) {
        return warps.get(warpName);
    }

    public Collection<MagicWarp> getMagicWarps() {
        return warps.values();
    }

    @Nullable
    public Location getWarp(String warpName) {
        Location warp = null;
        MagicWarp customWarp = warps.get(warpName);
        if (customWarp != null) {
            warp = customWarp.getLocation();
        }
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
