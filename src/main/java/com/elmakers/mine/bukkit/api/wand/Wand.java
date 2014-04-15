package com.elmakers.mine.bukkit.api.wand;

import java.util.Collection;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.Mage;

public interface Wand {
	public String getId();
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
}
