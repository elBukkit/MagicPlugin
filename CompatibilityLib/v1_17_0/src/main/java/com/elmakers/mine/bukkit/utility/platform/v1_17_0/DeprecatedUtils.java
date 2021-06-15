package com.elmakers.mine.bukkit.utility.platform.v1_17_0;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.DeprecatedUtilsBase;

public class DeprecatedUtils extends DeprecatedUtilsBase {
    public DeprecatedUtils(Platform platform) {
        super(platform);
    }

    @Override
    public void setTypeAndData(Block block, Material material, byte data, boolean applyPhysics) {
        block.setType(material, applyPhysics);
    }
}
