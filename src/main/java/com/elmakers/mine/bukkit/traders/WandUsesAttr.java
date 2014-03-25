package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Uses Remaining", key="wand_uses", priority = 5)
public class WandUsesAttr extends IntegerItemAttr {
	
	public WandUsesAttr(String key) {
		super(key, "wand", "uses");
	}
}
