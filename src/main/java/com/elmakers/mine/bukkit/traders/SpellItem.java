package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;

@Attribute(name="Spell", key="spell", priority = 5)
public class SpellItem extends NBTItem {
	
	public SpellItem(String key) {
		super(key);
	}

	@Override
	protected boolean isItem(ItemStack itemStack) {
		return Wand.isSpell(itemStack);
	}
}
