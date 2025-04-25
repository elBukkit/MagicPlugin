package com.elmakers.mine.bukkit.utility.platform;

import java.net.URL;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utility.CurrencyAmount;

public interface InventoryUtils {
    CurrencyAmount getCurrencyAmount(ItemStack item);

    boolean configureSkillItem(ItemStack skillItem, String skillClass, boolean quickCast, ConfigurationSection skillConfig);

    ItemStack setSkullURL(ItemStack itemStack, String url);

    ItemStack setSkullURL(ItemStack itemStack, URL url, UUID id);

    ItemStack setSkullURL(ItemStack itemStack, URL url, UUID id, String name);

    String getSkullURL(ItemStack skull);

    boolean isSkull(ItemStack item);

    void wrapText(String text, Collection<String> list);

    void wrapText(String text, String prefix, Collection<String> list);

    void wrapText(String text, int maxLength, Collection<String> list);

    void wrapText(String text, String prefix, int maxLength, Collection<String> list);

    boolean hasItem(Inventory inventory, String itemName);

    ItemStack getItem(Inventory inventory, String itemName);

    void makeKeep(ItemStack itemStack);

    boolean isKeep(ItemStack itemStack);

    void applyAttributes(ItemStack item, ConfigurationSection attributeConfig, String slot);

    void applyEnchantments(ItemStack item, ConfigurationSection enchantConfig);

    boolean addEnchantments(ItemStack item, ConfigurationSection enchantConfig);

    String describeProperty(Object property);

    String describeProperty(Object property, int maxLength);

    @SuppressWarnings("EqualsReference")
    boolean isSameInstance(ItemStack one, ItemStack two);

    int getMapId(ItemStack mapItem);

    void setMapId(ItemStack mapItem, int id);

    ItemStack createMap(Material material, int id);
}
