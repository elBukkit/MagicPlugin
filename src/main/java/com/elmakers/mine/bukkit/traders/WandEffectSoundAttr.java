package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Effect Sound Type", key="wand_effect_sound", priority = 5)
public class WandEffectSoundAttr extends NBTItemAttr {
	
	public WandEffectSoundAttr(String key) {
		super(key, "wand", "effect_sound");
	}
}
