package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeInvalidValueException;
import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeValueNotFoundException;
import net.dandielo.citizens.traders_v3.utils.items.ItemAttr;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;
import com.elmakers.mine.bukkit.utilities.InventoryUtils;

public abstract class NBTItemAttr extends ItemAttr 
{
	private static final boolean DEBUG = false;
	private String tagName;
	private String subtagName;
	private String defaultValue = "";
	private String tagData = "";
	private boolean playerOnly;
	
	public NBTItemAttr(String key, String tag, String subtag) 
	{
		super(key);
		tagName = tag;
		subtagName = subtag;
		playerOnly = false;
	}
	
	public NBTItemAttr(String key, String tag, String subtag, String def) 
	{
		super(key);
		tagName = tag;
		subtagName = subtag;
		defaultValue = def;
		playerOnly = false;
	}
	
	public NBTItemAttr(String key, String tag, String subtag, boolean forPlayer) 
	{
		super(key);
		tagName = tag;
		subtagName = subtag;
		playerOnly = forPlayer;
	}

	@Override
	public void onFactorize(ItemStack itemStack) throws AttributeValueNotFoundException 
	{
		tagData = null;
		if (itemStack == null || !InventoryUtils.hasMeta(itemStack, tagName)) 
		{
			throw new AttributeValueNotFoundException();
		}
		if (subtagName != null && subtagName.length() > 0) 
		{
			Object tagNode = InventoryUtils.getNode(itemStack, tagName);
			if (tagNode == null) 
			{
				throw new AttributeValueNotFoundException();
			}
			
			tagData = InventoryUtils.getMeta(tagNode, subtagName);
		} 
		else 
		{
			tagData = InventoryUtils.getMeta(itemStack, tagName);
		}
		
		if (tagData == null || tagData.length() == 0 || tagData.equals(defaultValue)) {
			throw new AttributeValueNotFoundException();
		}
		if (DEBUG) Bukkit.getLogger().info("NBTItem.onFactorize: " + tagName + "." + subtagName + "=" + tagData);
	}

	@Override
	public void onLoad(String itemKey) throws AttributeInvalidValueException 
	{
		if (itemKey == null || itemKey.length() == 0 || itemKey.equals(defaultValue)) {
			throw new AttributeInvalidValueException(this.info, "No data: " + defaultValue);
		}
		tagData = itemKey;
		if (DEBUG) Bukkit.getLogger().info("NBTItem.onLoad data: " + tagName + "=" + tagData);
	}

	@Override
	public String onSave() 
	{
		if (tagData == null) tagData = "";
		if (DEBUG) Bukkit.getLogger().info("NBTItem.onSave for data " + tagName + "=" + tagData);
		return tagData;
	}
	
	@Override
	public void onAssign(ItemStack itemStack)
	{
		if (tagData != null && tagData.length() > 0 && itemStack != null) 
		{
			if (subtagName != null && subtagName.length() > 0) 
			{
				Object tagNode = InventoryUtils.createNode(itemStack, tagName);
				if (tagNode != null) 
				{
					InventoryUtils.setMeta(tagNode, subtagName, tagData);
				}
			} 
			else 
			{
				InventoryUtils.setMeta(itemStack, tagName, tagData);
			}
		}
	}
	
	@Override
	public ItemStack onReturnAssign(ItemStack itemStack, boolean endItem)
	{
		if (tagData != null && tagData.length() > 0 && itemStack != null) 
		{
			if (!playerOnly || endItem) {
				itemStack = InventoryUtils.makeReal(itemStack);
				onAssign(itemStack);
			
				if (DEBUG) Bukkit.getLogger().info("NBTItem.onAssign for data " + tagName + "." + subtagName + "=" + tagData + " as " + itemStack 
					  + " wand: "+ Wand.isWand(itemStack) + ", spell: " + Wand.isSpell(itemStack) 
					  + ", brush: " + Wand.isBrush(itemStack));
			}
		}
		
		return itemStack;
	}

	/**
	 * Called when a week equality is needed. Allows sometimes a value to be in range of another value, used for priority requests
	 * @return
	 *    true when equal, false instead 
	 */
	public boolean equalsWeak(ItemAttr other)
	{
		if (other instanceof NBTItemAttr) 
		{
			return tagData.equalsIgnoreCase(((NBTItemAttr)other).tagData);
		}
		return false;
	}

	/**
	 * Called when a strong equality is needed. Values are compared strict.
	 * @return
	 *    true when equal, false instead 
	 */
	public boolean equalsStrong(ItemAttr other)
	{
		if (other instanceof NBTItemAttr) 
		{
			return tagData.equalsIgnoreCase(((NBTItemAttr)other).tagData);
		}
		return false;
	}
}
