package com.elmakers.mine.bukkit.api.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public interface MaterialAndData {
	public void updateFrom(MaterialAndData other);
	public void setMaterial(Material material, byte data);
	public void setMaterial(Material material);
	public void updateFrom(Block block);
	public void modify(Block block);
	public byte getData();
	public Material getMaterial();
	public String getKey();
	public String getName();
	public boolean is(Block block);
	public boolean isDifferent(Block block);
	public ItemStack getItemStack(int amount);
	public boolean isValid();
}
