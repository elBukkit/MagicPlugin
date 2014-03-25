package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Hunger Regeneration", key="wand_hunger_regeneration", priority = 5)
public class WandHungerRegenerationAttr extends FloatItemAttr {
	
	public WandHungerRegenerationAttr(String key) {
		super(key, "wand", "hunger_regeneration");
	}
}
