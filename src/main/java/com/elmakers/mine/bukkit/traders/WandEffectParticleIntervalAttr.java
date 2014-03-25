package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Effect Particle Interval", key="wand_effect_particle_interval", priority = 5)
public class WandEffectParticleIntervalAttr extends NBTItemAttr {
	
	public WandEffectParticleIntervalAttr(String key) {
		super(key, "wand", "effect_particle_interval");
	}
}
