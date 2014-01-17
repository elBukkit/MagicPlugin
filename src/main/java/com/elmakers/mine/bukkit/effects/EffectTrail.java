package com.elmakers.mine.bukkit.effects;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;


public class EffectTrail extends EffectPlayer {
	
	protected final Location start;
	protected final Vector direction;
	protected final int length;
	
	protected Location current;
	protected int played = 0;
	
	public EffectTrail(Plugin plugin, Location start, Vector direction, int length) {
		super(plugin);
		this.start = start.clone();
		this.direction = direction.clone();
		this.direction.normalize();
		this.length = length;
		current = start.clone();
	}
	
	@Override
	public void setSpeed(float speed) {
		this.direction.normalize();
		this.direction.multiply(speed);
	}
	
	public void run() {
		playEffect(current);
		current.add(direction);
		played++;
		if (played < length) {
			schedule();
		}
	}
}
