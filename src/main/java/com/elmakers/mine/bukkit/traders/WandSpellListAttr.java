package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Spells", key="wand_spells", priority = 5)
public class WandSpellListAttr extends InventoryItemAttr {
	
	public WandSpellListAttr(String key) {
		super(key, "wand", "spells");
	}
}
