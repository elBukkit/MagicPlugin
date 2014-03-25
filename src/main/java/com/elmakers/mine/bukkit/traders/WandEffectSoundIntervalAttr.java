package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Effect Sound Interval", key="wand_effect_sound_interval", priority = 5)
public class WandEffectSoundIntervalAttr extends IntegerItemAttr {
	
	public WandEffectSoundIntervalAttr(String key) {
		super(key, "wand", "effect_sound_interval");
	}
}
