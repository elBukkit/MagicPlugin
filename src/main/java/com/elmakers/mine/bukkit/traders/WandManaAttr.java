package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Mana", key="wand_mana", priority = 5)
public class WandManaAttr extends IntegerItemAttr {
	
	public WandManaAttr(String key) {
		super(key, "wand", "xp");
	}
}
