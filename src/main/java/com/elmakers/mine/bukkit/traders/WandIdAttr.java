package com.elmakers.mine.bukkit.traders;

import java.util.UUID;

import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeValueNotFoundException;
import net.dandielo.citizens.traders_v3.utils.items.Attribute;
import net.dandielo.citizens.traders_v3.utils.items.ItemAttr;

import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utilities.InventoryUtils;

@Attribute(name="Wand Unique Id", key="wand_id", priority = 5)
public class WandIdAttr extends ItemAttr {
	
	public WandIdAttr(String key) {
		super(key);
	}
	
	public void onFactorize(ItemStack itemStack) throws AttributeValueNotFoundException 
	{
		if (itemStack == null || !InventoryUtils.hasMeta(itemStack, "wand")) 
		{
			throw new AttributeValueNotFoundException();
		}
		Object tagNode = InventoryUtils.getNode(itemStack, "wand");
		if (tagNode == null) 
		{
			throw new AttributeValueNotFoundException();
		}
		String tagData = InventoryUtils.getMeta(tagNode, "id");
		if (tagData == null || tagData.length() == 0) {
			throw new AttributeValueNotFoundException();
		}
	}

	
	public void onLoad(String itemKey)
	{
		// Do nothing
	}
	
	public String onSave() 
	{
		// TODO: Is there a way to not even have this save to the trader at all
		// but not lose the information? I guess the wand would auto-create a new
		// id if it was wiped, but I don't want to rely on that.
		// So for now, we'll make an empty attribute, which seems hacky.
		return "";
	}
	
	public boolean equalsWeak(ItemAttr other)
	{
		return other instanceof WandIdAttr;
	}

	public boolean equalsStrong(ItemAttr other)
	{
		return other instanceof WandIdAttr;
	}

	@Override
	public void onAssign(ItemStack itemStack)
	{
		// We'll generate a new id each time.
		Object wandNode = InventoryUtils.createNode(itemStack, "wand");
		InventoryUtils.setMeta(wandNode, "id", UUID.randomUUID().toString());
	}
	
	@Override
	public ItemStack onReturnAssign(ItemStack itemStack, boolean endItem)
	{
		if (itemStack != null) 
		{
			if (endItem) {
				itemStack = InventoryUtils.makeReal(itemStack);
				onAssign(itemStack);
			}
		}
		
		return itemStack;
	}
}
