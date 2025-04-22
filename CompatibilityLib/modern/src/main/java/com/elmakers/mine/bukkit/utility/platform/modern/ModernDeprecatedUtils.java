package com.elmakers.mine.bukkit.utility.platform.modern;

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

public class ModernDeprecatedUtils extends DeprecatedUtilsBase {
    public ModernDeprecatedUtils(Platform platform) {
        super(platform);
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

    @Override
    public Biome getBiome(Location location) {
        return location.getWorld().getBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public short getMapId(MapView mapView) {
        // MapView id is now an int- we proabably should update our own code
        // and change this to an int
        return (short)mapView.getId();
    }
}
