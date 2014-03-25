package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Effect Bubbles", key="wand_effect_bubbles", priority = 5)
public class WandEffectBubblesFlag extends NBTItemFlag {
	
	public WandEffectBubblesFlag(String key) {
		super(key, "wand", "effect_bubbles");
	}
}
