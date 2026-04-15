package com.elmakers.mine.bukkit.utility.platform.v1_15;

import org.bukkit.Location;
import org.bukkit.block.Biome;

import com.elmakers.mine.bukkit.utility.platform.Platform;

public class DeprecatedUtils extends com.elmakers.mine.bukkit.utility.platform.v1_14.DeprecatedUtils {

    public DeprecatedUtils(Platform platform) {
        super(platform);
    }

    @Override
    public Biome getBiome(Location location) {
        return location.getWorld().getBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
