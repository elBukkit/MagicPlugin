package com.elmakers.mine.bukkit.traders;

import java.util.UUID;

import net.dandielo.citizens.traders_v3.core.exceptions.InvalidItemException;
import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeValueNotFoundException;
import net.dandielo.citizens.traders_v3.utils.items.Attribute;
import net.dandielo.citizens.traders_v3.utils.items.ItemFlag;

import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;
import com.elmakers.mine.bukkit.utilities.InventoryUtils;

@Attribute(name = "Magic Wand", key = ".wand")
public class WandFlag extends ItemFlag {

	public WandFlag(String key)
	{
		super(key);
	}

	@Override
	public void onAssign(ItemStack itemStack) throws InvalidItemException
	{
		if (itemStack == null) throw new InvalidItemException();
		
		// We'll generate a new id each time.
		Object wandNode = InventoryUtils.createNode(itemStack, "wand");
		if (wandNode != null) {
			InventoryUtils.setMeta(wandNode, "id", UUID.randomUUID().toString());
		}
	}

	// TODO Have to wait for dtlTraders 3.1.0 for this!
	// I'm hoping it still works right with the snapshot, in the meantime.
	// @Override
	public ItemStack onReturnAssign(ItemStack itemStack, boolean endItem) throws InvalidItemException
	{
		if (itemStack == null) throw new InvalidItemException();
		itemStack = InventoryUtils.makeReal(itemStack);
		onAssign(itemStack);
		return itemStack;
	}
	
	@Override
	public void onFactorize(ItemStack item) throws AttributeValueNotFoundException
	{
		if (!Wand.isWand(item)) {
			throw new AttributeValueNotFoundException();
		}
		
		// Make sure these don't stack!
		this.item.addFlag(".nostack");
	}
}
