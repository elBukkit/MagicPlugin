package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeInvalidValueException;
import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeValueNotFoundException;
import net.dandielo.citizens.traders_v3.utils.items.ItemAttr;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;
import com.elmakers.mine.bukkit.utilities.InventoryUtils;

public abstract class NBTItem extends ItemAttr {
	private static final boolean DEBUG = false;
	private String nbtData;
	
	public NBTItem(String key) {
		super(key);
	}
	
	protected abstract boolean isItem(ItemStack itemStack);

	@Override
	public void onFactorize(ItemStack itemStack) throws AttributeValueNotFoundException {
		if (isItem(itemStack)) {
			nbtData = InventoryUtils.serialize(itemStack);
			if (nbtData == null || nbtData.length() == 0) {
				Bukkit.getLogger().warning("Failed to serialize item data for " + this.getClass());
				nbtData = "";
			}
			
			if (DEBUG) Bukkit.getLogger().info("NBTItem.onFactorize from " + Wand.isWand(itemStack) + ": " + nbtData);
		}
	}

	@Override
	public void onLoad(String itemKey) throws AttributeInvalidValueException {
		nbtData = itemKey;
		if (nbtData == null) nbtData = "";
		if (DEBUG) Bukkit.getLogger().info("NBTItem.onLoad data: " + nbtData);
	}

	@Override
	public String onSave() {
		if (nbtData == null) nbtData = "";
		if (DEBUG) Bukkit.getLogger().info("NBTItem.onSave for data " + nbtData);
		return nbtData;
	}
	
	@Override
	public ItemStack onReturnAssign(ItemStack itemStack, boolean endItem)
	{
		if (nbtData != null && nbtData.length() > 0 && itemStack != null) {
			itemStack = InventoryUtils.getCopy(itemStack);
			if (!InventoryUtils.deserialize(itemStack, nbtData)) {
				Bukkit.getLogger().info("Failed to deserialize item data for " + nbtData + " as " + itemStack);
			}
		}
		
		if (DEBUG) Bukkit.getLogger().info("NBTItem.onReturnAssign for data " + nbtData + " as " + itemStack 
		  + " wand: "+ Wand.isWand(itemStack) + ", spell: " + Wand.isSpell(itemStack) 
		  + ", brush: " + Wand.isBrush(itemStack));
		
		return itemStack;
	}
}
