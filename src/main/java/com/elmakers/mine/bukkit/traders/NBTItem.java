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
	private String tagData;
	private String tagName;
	private String subtagName;
	
	public NBTItem(String key, String tag, String subtag) {
		super(key);
		tagName = tag;
		subtagName = subtag;
	}
	
	protected abstract boolean isItem(ItemStack itemStack);

	@Override
	public void onFactorize(ItemStack itemStack) throws AttributeValueNotFoundException {
		if (itemStack == null || !InventoryUtils.hasMeta(itemStack, tagName)) {
			throw new AttributeValueNotFoundException();
		}
			
		Object tagNode = InventoryUtils.getNode(itemStack, tagName);
		if (tagNode != null) {
			tagData = InventoryUtils.getMeta(tagNode, subtagName);
		}
		tagData = tagData == null ? "" : tagData;
		
		if (DEBUG) Bukkit.getLogger().info("NBTItem.onFactorize: " + tagName + "." + subtagName + "=" + tagData);
	}

	@Override
	public void onLoad(String itemKey) throws AttributeInvalidValueException {
		tagData = itemKey;
		if (tagData == null) tagData = "";
		
		if (DEBUG) Bukkit.getLogger().info("NBTItem.onLoad data: " + tagName + "=" + tagData);
	}

	@Override
	public String onSave() {
		if (tagData == null) tagData = "";
		if (DEBUG) Bukkit.getLogger().info("NBTItem.onSave for data " + tagName + "=" + tagData);
		return tagData;
	}
	
	@Override
	public void onAssign(ItemStack itemStack)
	{
		if (tagData != null && tagData.length() > 0 && itemStack != null) {
			InventoryUtils.addGlow(itemStack);
		}
	}
	
	@Override
	public ItemStack onReturnAssign(ItemStack itemStack, boolean endItem)
	{
		if (tagData != null && tagData.length() > 0 && itemStack != null) {
			itemStack = InventoryUtils.getCopy(itemStack);
			Object subNode = InventoryUtils.createNode(itemStack, tagName);
			InventoryUtils.setMeta(subNode, subtagName, tagData);
			InventoryUtils.addGlow(itemStack);
			if (DEBUG) Bukkit.getLogger().info("NBTItem.onAssign for data " + tagName + "." + subtagName + "=" + tagData + " as " + itemStack 
					  + " wand: "+ Wand.isWand(itemStack) + ", spell: " + Wand.isSpell(itemStack) 
					  + ", brush: " + Wand.isBrush(itemStack));
		}
		
		return itemStack;
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
		
		// if (!(attr instanceof NBTItem)) return false;
		// return nbtData.equals(((NBTItem)attr).nbtData);
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
		
		// if (!(attr instanceof NBTItem)) return false;
		// return nbtData.equals(((NBTItem)attr).nbtData);
	}
}
