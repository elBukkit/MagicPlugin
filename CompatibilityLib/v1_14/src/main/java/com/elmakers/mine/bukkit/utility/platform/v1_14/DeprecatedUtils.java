package com.elmakers.mine.bukkit.utility.platform.v1_14;

import org.bukkit.DyeColor;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.map.MapView;

import com.elmakers.mine.bukkit.utility.platform.Platform;

public class DeprecatedUtils extends com.elmakers.mine.bukkit.utility.platform.legacy.DeprecatedUtils {

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
}
