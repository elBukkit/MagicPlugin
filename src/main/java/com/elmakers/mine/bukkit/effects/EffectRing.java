package com.elmakers.mine.bukkit.effects;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;



public class EffectRing extends EffectRepeating {

	protected int size = 8;
	protected float radius = 1;

	public EffectRing() {
		
	}
	
	public EffectRing(Plugin plugin) {
		super(plugin);
	}
	
	@Override
	public void load(Plugin plugin, ConfigurationSection configuration) {
		super.load(plugin, configuration);
		
		radius = (float)configuration.getDouble("radius", radius);
		size = configuration.getInt("size", size);
	}
	
	public void setRadius(float radius) {
		this.radius = radius;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public void iterate() {
		float currentRadius = scale(radius) + 1;
		
		// Randomization
		double startRadians = Math.random() * Math.PI * 2;
		
		for (int i = 0; i < size; i++) {
			double radians = (double)i / size * Math.PI * 2 + startRadians;
			Vector direction = new Vector(Math.cos(radians) * currentRadius, 0, Math.sin(radians) * currentRadius);
			if (playAtOrigin) {
				Location loc = origin.clone();
				loc.add(direction);
				playEffect(loc);
			}
			if (playAtTarget && target != null) {
				Location loc = target.clone();
				loc.add(direction);
				playEffect(loc);
			}
		}
	}
}
