package com.elmakers.mine.bukkit.utility;

import java.util.UUID;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;

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

    public static void setTypeAndData(Block block, Material material, byte data, boolean applyPhysics) {
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

    public static void sendBlockChange(Player player, Location location, Material material, byte data) {
        player.sendBlockChange(location, material, data);
    }

    public static void sendBlockChange(Player player, Block block) {
        player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
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

    public static int getTypeId(Block block) {
        // @deprecated Magic value
        return block.getType().getId();
    }

    public static MapView getMap(int id) {
        // @deprecated Magic value
        // TODO: Replace this with an API call in future versions.
        return CompatibilityUtils.getMapById(id);
    }

    public static short getMapId(MapView mapView) {
        return (short)mapView.getId();
    }

    public static String getName(EntityType entityType) {
        // @deprecated Magic value
        String name = entityType.getName();
        return WordUtils.capitalize(name);
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

    public static OfflinePlayer getOfflinePlayer(String name) {
        return Bukkit.getOfflinePlayer(name);
    }

    public static Player getPlayer(String name) {
        // @deprecated Use {@link #getPlayer(UUID)} as player names are no
        // longer guaranteed to be unique
        return Bukkit.getPlayer(name);
    }

    public static Player getPlayerExact(String name) {
        return Bukkit.getPlayerExact(name);
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

    public static DyeColor getBaseColor(BannerMeta banner) {
        return banner.getBaseColor();
    }

    public static void setBaseColor(BannerMeta banner, DyeColor color) {
        banner.setBaseColor(color);
    }

    public static void setSkullOwner(final ItemStack itemStack, String ownerName, final SkullLoadedCallback callback) {
        SkinUtils.fetchProfile(ownerName, new SkinUtils.ProfileCallback() {
            @Override
            public void result(SkinUtils.ProfileResponse response) {
                if (response != null) {
                    Object gameProfile = response.getGameProfile();
                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta instanceof SkullMeta) {
                        InventoryUtils.setSkullProfile(meta, gameProfile);
                        itemStack.setItemMeta(meta);
                    }
                }
                if (callback != null) {
                    callback.updated(itemStack);
                }
            }
        });
    }

    public static void setSkullOwner(final ItemStack itemStack, UUID ownerUUID, final SkullLoadedCallback callback) {
        SkinUtils.fetchProfile(ownerUUID, new SkinUtils.ProfileCallback() {
            @Override
            public void result(SkinUtils.ProfileResponse response) {
                if (response != null) {
                    Object gameProfile = response.getGameProfile();
                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta instanceof SkullMeta) {
                        InventoryUtils.setSkullProfile(meta, gameProfile);
                        itemStack.setItemMeta(meta);
                    }
                }
                if (callback != null) {
                    callback.updated(itemStack);
                }
            }
        });
    }

    public static void setOwner(final Skull skull, UUID uuid) {
        SkinUtils.fetchProfile(uuid, new SkinUtils.ProfileCallback() {
            @Override
            public void result(SkinUtils.ProfileResponse response) {
                if (response != null) {
                    Object gameProfile = response.getGameProfile();
                    InventoryUtils.setSkullProfile(skull, gameProfile);
                }
                skull.update(true, false);
            }
        });
    }

    public static void setOwner(final Skull skull, String ownerName) {
        SkinUtils.fetchProfile(ownerName, new SkinUtils.ProfileCallback() {
            @Override
            public void result(SkinUtils.ProfileResponse response) {
                if (response != null) {
                    Object gameProfile = response.getGameProfile();
                    InventoryUtils.setSkullProfile(skull, gameProfile);
                }
                skull.update(true, false);
            }
        });
    }

    public static void showPlayer(Plugin plugin, Player toPlayer, Player showPlayer) {
        // TODO: Use Plugin
        toPlayer.showPlayer(showPlayer);
    }

    public static void hidePlayer(Plugin plugin, Player fromPlayer, Player hidePlayer) {
        // TODO: Use Plugin
        fromPlayer.hidePlayer(hidePlayer);
    }

    public static int[] getExpLevelCostsOffered(PrepareItemEnchantEvent event) {
        // TODO: Use getOffers
        return event.getExpLevelCostsOffered();
    }

    public static Entity getPassenger(Entity mount) {
        // TODO: Use getPassengers, refactor to search through list
        return mount.getPassenger();
    }

    public static void setPassenger(Entity mount, Entity passenger) {
        // TODO: Use addPassenger
        mount.setPassenger(passenger);
    }

    public static org.bukkit.UnsafeValues getUnsafe() {
        return Bukkit.getUnsafe();
    }
}
