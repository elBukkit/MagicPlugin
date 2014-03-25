package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.core.exceptions.InvalidItemException;
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
	private String serializedData;
	private static final boolean DEBUG = false;
	
	public WandItem(String key) {
		super(key);
	}
	
	@Override
	public void onFactorize(ItemStack itemStack) throws AttributeValueNotFoundException {
		if (!Wand.isWand(itemStack)) {
			throw new AttributeValueNotFoundException();
		}
			
		try {
			Wand wand = new Wand (TradersController.getController(), itemStack);
			serializedData = wand.export();
			if (serializedData == null || serializedData.length() == 0) {
				Bukkit.getLogger().warning("Failed to serialize item data for " + this.getClass());
				serializedData = "";
			}
			
			if (DEBUG) Bukkit.getLogger().info("WandItem.onFactorize: " + serializedData);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void onLoad(String data) throws AttributeInvalidValueException {
		serializedData = data;
		if (serializedData == null) serializedData = "";
		
		if (DEBUG) Bukkit.getLogger().info("WandItem.onLoad data: " + serializedData);
	}

	@Override
	public String onSave() {
		if (serializedData == null) serializedData = "";
		if (DEBUG) Bukkit.getLogger().info("WandItem.onSave for data " + serializedData);
		return serializedData;
	}
	
	protected ItemStack updateItem(ItemStack itemStack)
	{
		try {
			if (serializedData != null && serializedData.length() > 0 && itemStack != null) {
				itemStack = InventoryUtils.getCopy(itemStack);
				Wand wand = new Wand(TradersController.getController(), itemStack, serializedData);
				itemStack = wand.getItem();
				
				if (DEBUG) Bukkit.getLogger().info("WandItem.onAssign for data " + serializedData + " as " + itemStack + " wand: "+ Wand.isWand(itemStack));
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		
		return itemStack;
	}
	
	@Override
	public void onAssign(ItemStack itemStack) throws InvalidItemException
	{
		updateItem(itemStack);
	}
	
	@Override
	public ItemStack onReturnAssign(ItemStack itemStack, boolean endItem)
	{
		return updateItem(itemStack);
	}

	/**
	 * Called when a week equality is needed. Allows sometimes a value to be in range of another value, used for priority requests
	 * @return
	 *    true when equal, false instead 
	 */
	public boolean equalsWeak(ItemAttr attr)
	{
		// Hacked right now to just assume these are all unique.
		return false;
		
		// if (!(attr instanceof WandItem)) return false;
		// return nbtData.equals(((WandItem)attr).nbtData);
	}

	/**
	 * Called when a strong equality is needed. Values are compared strict.
	 * @return
	 *    true when equal, false instead 
	 */
	public boolean equalsStrong(ItemAttr attr)
	{
		// Hacked right now to just assume these are all unique.
		return false;
		
		// if (!(attr instanceof WandItem)) return false;
		// return nbtData.equals(((WandItem)attr).nbtData);
	}
}
