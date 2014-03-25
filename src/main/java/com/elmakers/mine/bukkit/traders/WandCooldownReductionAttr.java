package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Cooldown Reduction", key="wand_cooldown_reduction", priority = 5)
public class WandCooldownReductionAttr extends NBTItemAttr {
	
	public WandCooldownReductionAttr(String key) {
		super(key, "wand", "cooldown_reduction");
	}
}
