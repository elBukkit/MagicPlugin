package com.elmakers.mine.bukkit.utility;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.map.MapView;
import org.bukkit.material.MaterialData;

/**
 * Makes deprecation warnings useful again by suppressing all bukkit 'magic
 * number' deprecations.
 *
 */
@SuppressWarnings("deprecation")
public class DeprecatedUtils {
    public static void updateInventory(Player player) {
        // @deprecated This method should not be relied upon as it is a
        // temporary work-around for a larger, more complicated issue.
        player.updateInventory();
    }

    public static void setTypeIdAndData(Block block, int id, byte data,
            boolean applyPhysics) {
        // @deprecated Magic value
        block.setTypeIdAndData(id, data, applyPhysics);
    }

    public static MaterialData newMaterialData(Material material, byte data) {
        // @deprecated Magic value
        return new MaterialData(material, data);
    }

    public static byte getData(Block block) {
        // @deprecated Magic value
        return block.getData();
    }

    public static byte getData(DyeColor color) {
        // @deprecated The name is misleading. It would imply
        // {@link Material#INK_SACK} but uses {@link Material#WOOL}
        return color.getData();
    }

    public static int getId(Material material) {
        // @deprecated Magic value
        return material.getId();
    }

    public static byte getBlockData(FallingBlock falling) {
        // @deprecated Magic value
        return falling.getBlockData();
    }

    public static int getTypeId(Block block) {
        // @deprecated Magic value
        return block.getTypeId();
    }

    public static MapView getMap(short id) {
        // @deprecated Magic value
        return Bukkit.getMap(id);
    }

    public static String getName(EntityType entityType) {
        // @deprecated Magic value
        return entityType.getName();
    }
}
