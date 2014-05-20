package com.elmakers.mine.bukkit.api.wand;

import java.util.Collection;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.CostReducer;

/**
 * Represents a Wand that a Mage may use to cast a Spell.
 * 
 * Every Wand has an inventory of Spell keys and Material brush keys that it may cast and use.
 * 
 * A Wand may also have a variety of properties, including effects, an XP ("Mana") pool for
 * casting Spells with an XP-based CastingCost, and various boosts and protections.
 * 
 * Each Wand is backed by an ItemStack, and the Wand stores its data in the ItemStack. A Wand
 * is otherwise not tracked or persistent, other than via the Mage.getActiveWand() method, or
 * via a tracked LostWand record, if the ItemStack can be found.
 *
 */
public interface Wand extends CostReducer {
    public String getName();
    public void closeInventory();
    public void activate(Mage mage);
    public void deactivate();
    public void organizeInventory(Mage mage);
    public ItemStack getItem();
    public void makeUpgrade();
    public Collection<String> getSpells();
    public Collection<String> getBrushes();
    public void describe(CommandSender sender);
    public void unenchant();
    public Wand duplicate();
    public boolean hasSpell(String key);
    public boolean hasBrush(String key);
    public boolean isLocked();
    public boolean canUse(Player player);
    public boolean fill(Player player);
    public boolean add(Wand other);
    public boolean configure(Map<String, Object> properties);
    public boolean upgrade(Map<String, Object> properties);
    public boolean addBrush(String key);
    public boolean addSpell(String key);
    public boolean removeBrush(String key);
    public boolean removeSpell(String key);
    public void setActiveBrush(String key);
    public void setActiveSpell(String key);
    public void setName(String name);
    public void setDescription(String description);

    public LostWand makeLost(Location location);
    public boolean isLost();
    public boolean isLost(LostWand wand);
}
