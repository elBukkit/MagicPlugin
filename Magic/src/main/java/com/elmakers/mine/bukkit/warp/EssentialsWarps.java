package com.elmakers.mine.bukkit.warp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Warps;

/**
 * Encapsulates Essentials warps
 */
public class EssentialsWarps {
    private final Warps warps;

    private EssentialsWarps(Warps warps) {
        this.warps = warps;
    }

    @Nullable
    public static EssentialsWarps create(Plugin plugin) {
        if (plugin instanceof Essentials) {
            Essentials essentials = (Essentials)plugin;
            Warps warps = essentials.getWarps();
            return warps == null ? null : new EssentialsWarps(warps);
        }

        return null;
    }

    @Nullable
    public Location getWarp(String warpName) {
        try {
            return warps.getWarp(warpName);
        } catch (Exception ignored) {
        }
        return null;
    }

    public Map<String, Location> getWarps() {
        Map<String, Location> all = new HashMap<>();
        Collection<String> names = warps.getList();
        for (String name : names) {
            try {
                all.put(name, warps.getWarp(name));
            } catch (Exception ignored) {
            }
        }
        return all;
    }
}
