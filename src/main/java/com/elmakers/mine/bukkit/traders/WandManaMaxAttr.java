package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Max Mana", key="wand_xp_max", priority = 5)
public class WandManaMaxAttr extends IntegerItemAttr {
	
	public WandManaMaxAttr(String key) {
		super(key, "wand", "xp_max");
	}
}
