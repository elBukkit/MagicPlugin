package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.core.exceptions.InvalidItemException;
import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeValueNotFoundException;
import net.dandielo.citizens.traders_v3.utils.items.Attribute;
import net.dandielo.citizens.traders_v3.utils.items.ItemFlag;

import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utility.InventoryUtils;

@Attribute(name = "Glow", key = ".glow")
public class GlowFlag extends ItemFlag {

	public GlowFlag(String key)
	{
		super(key);
	}

	@Override
	public void onAssign(ItemStack item) throws InvalidItemException
	{
		// Don't overwrite any existing enchantments!
		if (!InventoryUtils.hasMeta(item, "ench")) {
			InventoryUtils.addGlow(item);
		}
	}

	@Override
	public void onFactorize(ItemStack item) throws AttributeValueNotFoundException
	{
		if (!InventoryUtils.hasMeta(item, "ench")) {
			throw new AttributeValueNotFoundException();
		}
	}
}
