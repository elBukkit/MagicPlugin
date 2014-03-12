package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeInvalidValueException;
import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeValueNotFoundException;
import net.dandielo.citizens.traders_v3.utils.items.Attribute;
import net.dandielo.citizens.traders_v3.utils.items.ItemAttr;

import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;

@Attribute(name="Wand", key="wand", priority = 5)
public class WandItem extends ItemAttr {
	private String wandKey;
	
	public WandItem(String key) {
		super(key);
	}

	@Override
	public void onFactorize(ItemStack itemStack)
			throws AttributeValueNotFoundException {
		if (Wand.isWand(itemStack)) {
			Wand wand = new Wand(TradersController.getController(), itemStack);
			wandKey = wand.getTemplate();

			// Bukkit.getLogger().info("WandItem.onFactorize for key "+ wandKey + " with wand template " + wand.getTemplate());
		}
	}

	@Override
	public void onLoad(String itemKey) throws AttributeInvalidValueException {
		wandKey = itemKey;
		
		// Bukkit.getLogger().info("WandItem.onLoad itemKey: " + itemKey + " for key "+ wandKey);
	}

	@Override
	public String onSave() {
		// Bukkit.getLogger().info("WandItem.onSave for key "+ wandKey);
		return wandKey;
	}
	
	@Override
	public ItemStack onReturnAssign(ItemStack itemStack, boolean endItem)
	{
		if (wandKey != null && wandKey.length() > 0) {
			Wand wand = Wand.createWand(TradersController.getController(), wandKey);
			itemStack = wand.getItem();
			
			// Bukkit.getLogger().info("WandItem.onReturnAssign for key "+ wandKey);
		} 
		
		return itemStack;
	}
}
