package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeInvalidValueException;
import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeValueNotFoundException;
import net.dandielo.citizens.traders_v3.utils.items.Attribute;
import net.dandielo.citizens.traders_v3.utils.items.ItemAttr;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;
import com.elmakers.mine.bukkit.utilities.InventoryUtils;

@Attribute(name="Wand", key="wand", priority = 5)
public class WandItem extends ItemAttr {
	private String wandData;
	
	public WandItem(String key) {
		super(key);
	}

	@Override
	public void onFactorize(ItemStack itemStack)
			throws AttributeValueNotFoundException {
		if (Wand.isWand(itemStack)) {
			Wand wand = new Wand(TradersController.getController(), itemStack);
			wandData = InventoryUtils.serialize(wand.getItem());
			if (wandData == null) {
				Bukkit.getLogger().warning("Failed to serialize wand data");
			}
			
			//Bukkit.getLogger().info("WandItem.onFactorize with wand template " + wand.getTemplate() + " from " + Wand.isWand(itemStack) + ": " + wandData);
		}
	}

	@Override
	public void onLoad(String itemKey) throws AttributeInvalidValueException {
		wandData = itemKey;	
		// Bukkit.getLogger().info("WandItem.onLoad data: " + wandData);
	}

	@Override
	public String onSave() {
		// Bukkit.getLogger().info("WandItem.onSave for data " + wandData);
		return wandData;
	}
	
	@Override
	public ItemStack onReturnAssign(ItemStack itemStack, boolean endItem)
	{
		if (wandData != null && wandData.length() > 0) {
			itemStack = InventoryUtils.getCopy(itemStack);
			if (!InventoryUtils.deserialize(itemStack, wandData)) {
				Bukkit.getLogger().warning("Failed to deserialize wand data");
			}
			
			// Bukkit.getLogger().info("WandItem.onReturnAssign for data " + wandData + " as " + itemStack + ": " + success);
		} 
		
		// Bukkit.getLogger().info("Returning " + itemStack + ": " + Wand.isWand(itemStack));
		
		return itemStack;
	}
}
