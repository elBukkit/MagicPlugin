package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Effect Sound Pitch", key="wand_effect_sound_pitch", priority = 5)
public class WandEffectSoundPitchAttr extends NBTItemAttr {
	
	public WandEffectSoundPitchAttr(String key) {
		super(key, "wand", "effect_sound_pitch");
	}
}
