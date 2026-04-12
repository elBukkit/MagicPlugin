package com.elmakers.mine.bukkit.utility.platform.base;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.ProfileCallback;
import com.elmakers.mine.bukkit.utility.ProfileResponse;
import com.elmakers.mine.bukkit.utility.SkullLoadedCallback;
import com.elmakers.mine.bukkit.utility.WordUtils;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;

@SuppressWarnings("deprecation")
public class DeprecatedUtilsBase implements DeprecatedUtils {
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
    public MapView getMap(int id) {
        // @deprecated Magic value
        return platform.getCompatibilityUtils().getMapById(id);
    }

    @Override
    public int getMapId(MapView mapView) {
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
    public void setSkullOwner(final ItemStack itemStack, String ownerName, final SkullLoadedCallback callback) {
        platform.getSkinUtils().fetchProfile(ownerName, new ProfileCallback() {
            @Override
            public void result(ProfileResponse response) {
                if (response != null) {
                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta instanceof SkullMeta) {
                        ((SkullMeta)meta).setOwnerProfile(response.getPlayerProfile());
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
                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta instanceof SkullMeta) {
                        ((SkullMeta)meta).setOwnerProfile(response.getPlayerProfile());
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
                    skull.setOwnerProfile(response.getPlayerProfile());
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
                    skull.setOwnerProfile(response.getPlayerProfile());
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
        if (getItemDamage(itemStack) != damage) {
            itemStack.setDurability(damage);
        }
    }

    @Override
    public short getItemDamage(ItemStack itemStack) {
        return itemStack.getDurability();
    }

    @Override
    public Biome getBiome(Location location) {
        return location.getWorld().getBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public ItemStack createItemStack(Material material, int amount, short legacyData) {
        return new ItemStack(material, amount, legacyData);
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
