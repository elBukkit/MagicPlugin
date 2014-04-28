package com.elmakers.mine.bukkit.effect.builtin;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.effect.EffectRepeating;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class EffectTrail extends EffectRepeating {

	protected Double length;
	
	// State
	protected double size;
	protected Vector direction;

	public EffectTrail() {
		
	}
	
	public EffectTrail(Plugin plugin) {
		super(plugin);
	}
	
	@Override
	public void load(Plugin plugin, ConfigurationSection configuration) {
		// Different default for a trail, more iterations are generally needed.
		iterations = 8;
		
		super.load(plugin, configuration);
		length = ConfigurationUtils.getDouble(configuration, "length", length);
	}
	
	@Override
	public void play() {
		if (length != null) {
			size = length;
		} else if (target != null) {
			size = origin.distance(target);
		} else {
			size = 0;
		}
		
		// Don't bother playing if it's right in front of us.
		if (size < 1) {
			stop();
			return;
		}
		
		direction = getDirection();
		super.play();
	}
	
	public void iterate() {
		Vector delta = direction.clone();
		Location current = origin.clone();
		current.add(delta.multiply(scale(size) + 1));
		playEffect(current);
	}
}
