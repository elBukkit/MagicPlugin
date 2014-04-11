package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Effect Particle Data", key="wand_effect_particle_data", priority = 5)
public class WandEffectParticleDataAttr extends FloatItemAttr {
	
	public WandEffectParticleDataAttr(String key) {
		super(key, "wand", "effect_particle_data");
	}
}
