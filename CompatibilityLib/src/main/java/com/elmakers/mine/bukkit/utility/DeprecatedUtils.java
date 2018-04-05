package com.elmakers.mine.bukkit.utility;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.map.MapView;

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

    public static MapView getMap(short id) {
        // @deprecated Magic value
        return Bukkit.getMap(id);
    }

    public static String getName(EntityType entityType) {
        // @deprecated Magic value
        return entityType.getName();
    }

    public static String getDisplayName(Entity entity) {
        if (entity instanceof Player) {
            return ((Player)entity).getDisplayName();
        }
        String customName = entity.getCustomName();
        if (customName != null && !customName.isEmpty()) {
            return customName;
        }
        return getName(entity.getType());
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

    public static void setSkullOwner(SkullMeta skull, String ownerName) {
        skull.setOwner(ownerName);
    }

    public static double getMaxHealth(Damageable li) {
        // return li.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        return li.getMaxHealth();
    }

    public static void setMaxHealth(Damageable li, double maxHealth) {
        // li.getAttribute(Attribute.GENERIC_MAX_HEALTH).setValue(maxHealth);
        li.setMaxHealth(maxHealth);
    }
}
