package com.elmakers.mine.bukkit.utility.platform.base;

import java.util.UUID;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
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

import com.elmakers.mine.bukkit.utility.ProfileCallback;
import com.elmakers.mine.bukkit.utility.ProfileResponse;
import com.elmakers.mine.bukkit.utility.SkullLoadedCallback;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;

@SuppressWarnings("deprecation")
public abstract class DeprecatedUtilsBase implements DeprecatedUtils {
    protected final Platform platform;

    protected DeprecatedUtilsBase(final Platform platform) {
        this.platform = platform;
    }

    @Override
    public void updateInventory(Player player) {
        // @deprecated This method should not be relied upon as it is a
        // temporary work-around for a larger, more complicated issue.
        player.updateInventory();
    }

    @Override
    public void sendBlockChange(Player player, Location location, Material material, byte data) {
        player.sendBlockChange(location, material, data);
    }

    @Override
    public void sendBlockChange(Player player, Block block) {
        player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
    }

    @Override
    public byte getData(Block block) {
        // @deprecated Magic value
        return block.getData();
    }

    @Override
    public byte getWoolData(DyeColor color) {
        // @deprecated Magic value
        return color.getWoolData();
    }

    @Override
    public int getId(Material material) {
        // @deprecated Magic value
        return material.getId();
    }

    @Override
    public int getTypeId(Block block) {
        // @deprecated Magic value
        return block.getType().getId();
    }

    @Override
    public MapView getMap(int id) {
        // @deprecated Magic value
        return platform.getCompatibilityUtils().getMapById(id);
    }

    @Override
    public short getMapId(MapView mapView) {
        return mapView.getId();
    }

    @Override
    public String getName(EntityType entityType) {
        // @deprecated Magic value
        String name = entityType.getName();
        return WordUtils.capitalize(name);
    }

    @Override
    public String getDisplayName(Entity entity) {
        if (entity instanceof Player) {
            return ((Player)entity).getDisplayName();
        }
        String customName = entity.getCustomName();
        if (customName != null && !customName.isEmpty()) {
            return customName;
        }
        return getName(entity.getType());
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String name) {
        return Bukkit.getOfflinePlayer(name);
    }

    @Override
    public Player getPlayer(String name) {
        // @deprecated Use {@link #getPlayer(UUID)} as player names are no
        // longer guaranteed to be unique
        return Bukkit.getPlayer(name);
    }

    @Override
    public Player getPlayerExact(String name) {
        return Bukkit.getPlayerExact(name);
    }

    @Override
    public FallingBlock spawnFallingBlock(Location location,
                                          Material material, byte data) {
        // @deprecated Magic value
        return location.getWorld().spawnFallingBlock(location, material, data);
    }

    @Override
    public byte getRawData(BlockState state) {
        // @deprecated Magic value
        return state.getRawData();
    }

    @Override
    public DyeColor getBaseColor(BannerMeta banner) {
        return banner.getBaseColor();
    }

    @Override
    public void setBaseColor(BannerMeta banner, DyeColor color) {
        banner.setBaseColor(color);
    }

    @Override
    public void setSkullOwner(final ItemStack itemStack, String ownerName, final SkullLoadedCallback callback) {
        platform.getSkinUtils().fetchProfile(ownerName, new ProfileCallback() {
            @Override
            public void result(ProfileResponse response) {
                if (response != null) {
                    Object gameProfile = response.getGameProfile();
                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta instanceof SkullMeta) {
                        platform.getInventoryUtils().setSkullProfile(meta, gameProfile);
                        itemStack.setItemMeta(meta);
                    }
                }
                if (callback != null) {
                    callback.updated(itemStack);
                }
            }
        });
    }

    @Override
    public void setSkullOwner(final ItemStack itemStack, UUID ownerUUID, final SkullLoadedCallback callback) {
        platform.getSkinUtils().fetchProfile(ownerUUID, new ProfileCallback() {
            @Override
            public void result(ProfileResponse response) {
                if (response != null) {
                    Object gameProfile = response.getGameProfile();
                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta instanceof SkullMeta) {
                        platform.getInventoryUtils().setSkullProfile(meta, gameProfile);
                        itemStack.setItemMeta(meta);
                    }
                }
                if (callback != null) {
                    callback.updated(itemStack);
                }
            }
        });
    }

    @Override
    public void setOwner(final Skull skull, UUID uuid) {
        platform.getSkinUtils().fetchProfile(uuid, new ProfileCallback() {
            @Override
            public void result(ProfileResponse response) {
                if (response != null) {
                    Object gameProfile = response.getGameProfile();
                    platform.getInventoryUtils().setSkullProfile(skull, gameProfile);
                }
                skull.update(true, false);
            }
        });
    }

    @Override
    public void setOwner(final Skull skull, String ownerName) {
        platform.getSkinUtils().fetchProfile(ownerName, new ProfileCallback() {
            @Override
            public void result(ProfileResponse response) {
                if (response != null) {
                    Object gameProfile = response.getGameProfile();
                    platform.getInventoryUtils().setSkullProfile(skull, gameProfile);
                }
                skull.update(true, false);
            }
        });
    }

    @Override
    public void showPlayer(Plugin plugin, Player toPlayer, Player showPlayer) {
        // TODO: Use Plugin
        toPlayer.showPlayer(showPlayer);
    }

    @Override
    public void hidePlayer(Plugin plugin, Player fromPlayer, Player hidePlayer) {
        // TODO: Use Plugin
        fromPlayer.hidePlayer(hidePlayer);
    }

    @Override
    public int[] getExpLevelCostsOffered(PrepareItemEnchantEvent event) {
        // TODO: Use getOffers
        return event.getExpLevelCostsOffered();
    }

    @Override
    public Entity getPassenger(Entity mount) {
        // TODO: Use getPassengers, refactor to search through list
        return mount.getPassenger();
    }

    @Override
    public void setPassenger(Entity mount, Entity passenger) {
        // TODO: Use addPassenger
        mount.setPassenger(passenger);
    }

    @Override
    public org.bukkit.UnsafeValues getUnsafe() {
        return Bukkit.getUnsafe();
    }

    @Override
    public boolean isTransparent(Material material) {
        return material.isTransparent();
    }

    @Override
    public void setItemDamage(ItemStack itemStack, short damage) {
        itemStack.setDurability(damage);
    }

    @Override
    public short getItemDamage(ItemStack itemStack) {
        return itemStack.getDurability();
    }

    @Override
    public Biome getBiome(Location location) {
        return location.getWorld().getBiome(location.getBlockX(), location.getBlockZ());
    }

    @Override
    public ItemStack createItemStack(Material material, int amount, short legacyData) {
        return new ItemStack(material, amount, legacyData);
    }
}
