package com.elmakers.mine.bukkit.utility.platform.legacy;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.map.MapView;

import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.DeprecatedUtilsBase;

/**
 * Makes deprecation warnings useful again by suppressing all bukkit 'magic
 * number' deprecations.
 *
 */
@SuppressWarnings("deprecation")
public class DeprecatedUtils extends DeprecatedUtilsBase {
    public DeprecatedUtils(Platform platform) {
        super(platform);
    }

    @Override
    public short getMapId(MapView mapView) {
        // MapView id is now an int- we proabably should update our own code
        // and change this to an int
        return (short)mapView.getId();
    }

    @Override
    public DyeColor getBaseColor(BannerMeta banner) {
        return DyeColor.WHITE;
    }

    @Override
    public void setBaseColor(BannerMeta banner, DyeColor color) {
        // Can't actually do this anymore, different banner colors are different materials
    }

    @Override
    public Biome getBiome(Location location) {
        return location.getWorld().getBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public void setTypeAndData(Block block, Material material, byte data, boolean applyPhysics) {
        block.setType(material, applyPhysics);
    }

    @Override
    public void setSkullType(Skull skullBlock, short skullType) {
    }

    @Override
    public short getSkullType(Skull skullBlock) {
        return 0;
    }
}
