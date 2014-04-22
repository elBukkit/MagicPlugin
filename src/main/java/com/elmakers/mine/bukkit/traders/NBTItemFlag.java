package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.core.exceptions.InvalidItemException;
import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeValueNotFoundException;
import net.dandielo.citizens.traders_v3.utils.items.ItemFlag;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utilities.InventoryUtils;

public abstract class NBTItemFlag extends ItemFlag
{
	private static final boolean DEBUG = false;
	private String tagName;
	private String subtagName;
	
	public NBTItemFlag(String key, String tag, String subtag) 
	{
		super(key);
		tagName = tag;
		subtagName = subtag;
	}
	
	@Override
	public void onFactorize(ItemStack itemStack) throws AttributeValueNotFoundException 
	{
		if (itemStack == null || !InventoryUtils.hasMeta(itemStack, tagName)) 
		{
			throw new AttributeValueNotFoundException();
		}
		String testData = null;
		if (subtagName != null && subtagName.length() > 0) 
		{
			Object tagNode = InventoryUtils.getNode(itemStack, tagName);
			if (tagNode == null) 
			{
				throw new AttributeValueNotFoundException();
			}
			
			testData = InventoryUtils.getMeta(tagNode, subtagName);
		} 
		else 
		{
			testData = InventoryUtils.getMeta(itemStack, tagName);
		}
		
		if (!parseBoolean(testData)) {
			throw new AttributeValueNotFoundException();
		}
		if (DEBUG) Bukkit.getLogger().info("NBTFlag.onFactorize: " + tagName + "." + subtagName + "=" + testData + ")");
	}
	
	protected boolean parseBoolean(String s)
	{
		return s != null && (s.equalsIgnoreCase("true") || s.equals("1"));
	}
	
	@Override
	public ItemStack onReturnAssign(ItemStack itemStack, boolean endItem) throws InvalidItemException
	{
		if (itemStack == null) throw new InvalidItemException();
		itemStack = InventoryUtils.makeReal(itemStack);
		onAssign(itemStack);
		return itemStack;
	}
	
	@Override
	public void onAssign(ItemStack itemStack) throws InvalidItemException
	{
		if (itemStack == null) throw new InvalidItemException();
		
		if (subtagName != null && subtagName.length() > 0) 
		{
			Object tagNode = InventoryUtils.createNode(itemStack, tagName);
			if (tagNode != null) 
			{
				InventoryUtils.setMeta(tagNode, subtagName, "true");
			}
		} 
		else 
		{
			InventoryUtils.setMeta(itemStack, tagName, "true");
		}
	}
}
