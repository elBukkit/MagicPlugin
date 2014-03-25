package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Effect Particle Count", key="wand_effect_particle_count", priority = 5)
public class WandEffectParticleCountAttr extends NBTItemAttr {
	
	public WandEffectParticleCountAttr(String key) {
		super(key, "wand", "effect_particle_count");
	}
}
