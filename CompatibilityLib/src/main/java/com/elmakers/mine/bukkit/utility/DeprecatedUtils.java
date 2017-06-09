package com.elmakers.mine.bukkit.utility;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
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

    public static byte getWoolData(DyeColor color) {
        // @deprecated Magic value
        return color.getWoolData();
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

    public static Player getPlayer(String name) {
        // @deprecated Use {@link #getPlayer(UUID)} as player names are no
        // longer guaranteed to be unique
        return Bukkit.getPlayer(name);
    }

    public static void setData(Block block, byte data) {
        // @deprecated Magic value
        block.setData(data);
    }

    public static FallingBlock spawnFallingBlock(Location location,
            Material material, byte data) {
        // @deprecated Magic value
        return location.getWorld().spawnFallingBlock(location, material, data);
    }

    public static byte getRawData(BlockState state) {
        // @deprecated Magic value
        return state.getRawData();
    }

    public static Material getMaterial(int id) {
        // @deprecated Magic value
        return Material.getMaterial(id);
    }
}
