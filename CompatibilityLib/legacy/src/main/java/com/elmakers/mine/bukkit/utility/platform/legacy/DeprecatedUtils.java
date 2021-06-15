package com.elmakers.mine.bukkit.utility.platform.legacy;

import org.bukkit.Material;
import org.bukkit.block.Block;

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
    public void setTypeAndData(Block block, Material material, byte data, boolean applyPhysics) {
        // @deprecated Magic value
        if (NMSUtils.class_Block_setTypeIdAndDataMethod != null) {
            try {
                NMSUtils.class_Block_setTypeIdAndDataMethod.invoke(block, material.getId(), data, applyPhysics);
            } catch (Exception ex) {
                block.setType(material, applyPhysics);
                ex.printStackTrace();
            }
        } else {
            block.setType(material, applyPhysics);
        }
    }
}
