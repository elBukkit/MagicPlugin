package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.core.exceptions.InvalidItemException;
import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeInvalidValueException;
import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeValueNotFoundException;
import net.dandielo.citizens.traders_v3.utils.items.ItemAttr;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;
import com.elmakers.mine.bukkit.utility.InventoryUtils;

public abstract class NBTItemAttr extends ItemAttr 
{
	private static final boolean DEBUG = false;
	private String tagName;
	private String subtagName;
	private String defaultValue = "";
	private String defaultValue2 = "";
	private String tagData = "";
	
	public NBTItemAttr(String key, String tag, String subtag) 
	{
		super(key);
		tagName = tag;
		subtagName = subtag;
	}
	
	public NBTItemAttr(String key, String tag, String subtag, String def) 
	{
		super(key);
		tagName = tag;
		subtagName = subtag;
		defaultValue = def;
	}
	
	public NBTItemAttr(String key, String tag, String subtag, String def, String def2) 
	{
		super(key);
		tagName = tag;
		subtagName = subtag;
		defaultValue = def;
		defaultValue2 = def2;
	}
	
	public String getData()
	{
		return tagData;
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
		
		if (tagData == null || tagData.length() == 0 || tagData.equals(defaultValue) || tagData.equals(defaultValue2)) {
			throw new AttributeValueNotFoundException();
		}
		if (DEBUG) Bukkit.getLogger().info("NBTItem.onFactorize: " + tagName + "." + subtagName + "=" + tagData);
	}

	@Override
	public void onLoad(String itemKey) throws AttributeInvalidValueException 
	{
		if (itemKey == null || itemKey.length() == 0 || itemKey.equals(defaultValue) || itemKey.equals(defaultValue2)) {
			throw new AttributeInvalidValueException(this.info, "No data: " + defaultValue);
		}
		tagData = itemKey;
		tagData = tagData.replace("|", ":");
		if (DEBUG) Bukkit.getLogger().info("NBTItem.onLoad data: " + tagName + "=" + tagData);
	}

	@Override
	public String onSave() 
	{
		if (tagData == null) tagData = "";
		if (DEBUG) Bukkit.getLogger().info("NBTItem.onSave for data " + tagName + "=" + tagData);
		return tagData.replace(":", "|");
	}
	
	@Override
	public void onAssign(ItemStack itemStack) throws InvalidItemException
	{
		if (itemStack == null) throw new InvalidItemException();
		
		if (tagData.length() > 0) 
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
	public ItemStack onReturnAssign(ItemStack itemStack, boolean endItem) throws InvalidItemException
	{
		if (itemStack == null) throw new InvalidItemException();
		
		if (tagData != null && tagData.length() > 0) 
		{
			itemStack = InventoryUtils.makeReal(itemStack);
			onAssign(itemStack);
		
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
	public boolean equalsWeak(ItemAttr other)
	{
		// Bukkit.getLogger().info(" *TRADER: equalsWeak " + this.getKey() + ":" + this.tagName + "=" + this.tagData);
		
		if (other instanceof NBTItemAttr) 
		{
			// Bukkit.getLogger().info("   compare to : " + ((NBTItemAttr)other).tagData + " = " + tagData.equalsIgnoreCase(((NBTItemAttr)other).tagData));
			return tagData.equalsIgnoreCase(((NBTItemAttr)other).tagData);
		}
		
		// Bukkit.getLogger().info("   NOT an NBT item!");
		return false;
	}

	/**
	 * Called when a strong equality is needed. Values are compared strict.
	 * @return
	 *    true when equal, false instead 
	 */
	public boolean equalsStrong(ItemAttr other)
	{
		// Bukkit.getLogger().info(" *TRADER: equalsStrong " + this.getKey() + ":" + this.tagName + "=" + this.tagData);
		
		if (other instanceof NBTItemAttr) 
		{
			// Bukkit.getLogger().info("   compare to : " + ((NBTItemAttr)other).tagData + " = " + tagData.equalsIgnoreCase(((NBTItemAttr)other).tagData));
			
			return tagData.equalsIgnoreCase(((NBTItemAttr)other).tagData);
		}
		// Bukkit.getLogger().info("   NOT an NBT item!");
		return false;
	}
}
