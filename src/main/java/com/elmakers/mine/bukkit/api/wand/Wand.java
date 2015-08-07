package com.elmakers.mine.bukkit.api.wand;

import java.util.Collection;
import java.util.Map;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
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
    public long getWorth();
    public void closeInventory();
    public void activate(Mage mage);
    public void deactivate();
    public void organizeInventory(Mage mage);
    public void alphabetizeInventory();
    public ItemStack getItem();
    public void makeUpgrade();
    public Collection<String> getSpells();
    public Collection<String> getBrushes();
    public void describe(CommandSender sender);
    public void unenchant();
    public void unlock();
    public Wand duplicate();
    public boolean hasSpell(String key);
    public boolean hasBrush(String key);
    public boolean isLocked();
    public boolean canUse(Player player);
    public boolean fill(Player player);
    public boolean fill(Player player, int maxLevel);
    public boolean add(Wand other);
    public boolean add(Wand other, Mage mage);
    public boolean addItem(ItemStack item);
    public boolean configure(Map<String, Object> properties);
    public boolean upgrade(Map<String, Object> properties);
    public boolean addBrush(String key);
    public boolean addSpell(String key);
    public boolean removeBrush(String key);
    public boolean removeSpell(String key);
    public String getActiveBrushKey();
    public String getActiveSpellKey();
    public Spell getActiveSpell();
    public void setActiveBrush(String key);
    public void setActiveSpell(String key);
    public void setName(String name);
    public void setDescription(String description);
    public void setPath(String path);

    public LostWand makeLost(Location location);
    public boolean isLost();
    public boolean isLost(LostWand wand);
    public int enchant(int levels);
    public int enchant(int levels, Mage mage);

    public Map<String, String> getOverrides();
    public void setOverrides(Map<String, String> overrides);
    public void removeOverride(String key);
    public void setOverride(String key, String value);
    public SpellTemplate getBaseSpell(String spellKey);

    public boolean isSuperProtected();
    public boolean isSuperPowered();
    public boolean isCostFree();
    public boolean isCooldownFree();
    public float getPower();
    public float getHealthRegeneration();
    public float getHungerRegeneration();
    public float getCooldownReduction();
    public float getCostReduction();
    public WandUpgradePath getPath();
    public MageController getController();
    public boolean showCastMessages();
    public boolean showMessages();
    public String getTemplateKey();
    public boolean isIndestructible();
    public void playEffects(String key);
    public boolean cast();
    public boolean isBound();
    public void damageDealt(double damage, Entity target);
}
