package com.elmakers.mine.bukkit.utility.platform;

import java.util.UUID;

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
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.SkullLoadedCallback;

@SuppressWarnings("deprecation")
public interface DeprecatedUtils {
    void updateInventory(Player player);

    void setTypeAndData(Block block, Material material, byte data, boolean applyPhysics);

    MapView getMap(int id);

    int getMapId(MapView mapView);

    String getName(EntityType entityType);

    String getDisplayName(Entity entity);

    OfflinePlayer getOfflinePlayer(String name);

    Player getPlayer(String name);

    Player getPlayerExact(String name);

    void setSkullOwner(ItemStack itemStack, String ownerName, SkullLoadedCallback callback);

    void setSkullOwner(ItemStack itemStack, UUID ownerUUID, SkullLoadedCallback callback);

    void setOwner(Skull skull, UUID uuid);

    void setOwner(Skull skull, String ownerName);

    void showPlayer(Plugin plugin, Player toPlayer, Player showPlayer);

    void hidePlayer(Plugin plugin, Player fromPlayer, Player hidePlayer);

    int[] getExpLevelCostsOffered(PrepareItemEnchantEvent event);

    Entity getPassenger(Entity mount);

    void setPassenger(Entity mount, Entity passenger);

    org.bukkit.UnsafeValues getUnsafe();

    boolean isTransparent(Material material);

    void setItemDamage(ItemStack itemStack, short damage);

    short getItemDamage(ItemStack itemStack);

    Biome getBiome(Location location);

    ItemStack createItemStack(Material material, int amount, short legacyData);

    void setSkullType(Skull skullBlock, short skullType);

    short getSkullType(Skull skullBlock);
}
