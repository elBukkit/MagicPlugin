package com.elmakers.mine.bukkit.api.magic;

import org.bukkit.inventory.ItemStack;

public interface Wand {
	public String getId();
	public String getName();
	public void closeInventory();
	public void activate(Mage mage);
	public ItemStack getItem();
	public void makeUpgrade();
}
