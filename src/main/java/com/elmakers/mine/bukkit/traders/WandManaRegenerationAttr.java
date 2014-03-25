package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Mana Regeneration", key="wand_xp_regeneration", priority = 5)
public class WandManaRegenerationAttr extends IntegerItemAttr {
	
	public WandManaRegenerationAttr(String key) {
		super(key, "wand", "xp_regeneration");
	}
}
