package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Effect Sound Volume", key="wand_effect_sound_volume", priority = 5)
public class WandEffectSoundVolumeAttr extends NBTItemAttr {
	
	public WandEffectSoundVolumeAttr(String key) {
		super(key, "wand", "effect_sound_volume");
	}
}
