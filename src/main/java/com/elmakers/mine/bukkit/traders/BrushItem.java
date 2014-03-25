package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;

@Attribute(name="Material Brush", key="brush", priority = 5)
public class BrushItem extends NBTItem {
	
	public BrushItem(String key) {
		super(key, "brush", "key");
	}

	@Override
	protected boolean isItem(ItemStack itemStack) {
		return Wand.isBrush(itemStack);
	}
}
