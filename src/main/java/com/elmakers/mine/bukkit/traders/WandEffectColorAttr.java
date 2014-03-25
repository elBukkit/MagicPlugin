package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Effect Color", key="wand_effect_color", priority = 5)
public class WandEffectColorAttr extends NBTItemAttr {
	
	public WandEffectColorAttr(String key) {
		super(key, "wand", "effect_color");
	}
}
