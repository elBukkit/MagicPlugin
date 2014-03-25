package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Power", key="wand_power", priority = 5)
public class WandPowerAttr extends FloatItemAttr {
	
	public WandPowerAttr(String key) {
		super(key, "wand", "power");
	}
}
