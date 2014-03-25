package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.core.exceptions.InvalidItemException;
import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeValueNotFoundException;
import net.dandielo.citizens.traders_v3.utils.items.Attribute;
import net.dandielo.citizens.traders_v3.utils.items.ItemFlag;

import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;

@Attribute(name = "Wand", key = ".wand")
public class WandFlag extends ItemFlag {

	public WandFlag(String key)
	{
		super(key);
	}

	@Override
	public void onAssign(ItemStack item) throws InvalidItemException
	{
		// TODO, not sure, maybe control lore? Maybe nothing.
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
