package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Effect Particle Type", key="wand_effect_particle", priority = 5)
public class WandEffectParticleAttr extends NBTItemAttr {
	
	public WandEffectParticleAttr(String key) {
		super(key, "wand", "effect_particle");
	}
}
