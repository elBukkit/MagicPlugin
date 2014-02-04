package com.elmakers.mine.bukkit.effects;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;


public class EffectRing extends EffectPlayer {
	
	protected final Location start;
	protected final int length;
	protected final int count;
	protected boolean invert = false;
	
	protected int played = 0;
	protected float speed = 1;
	
	public EffectRing(Plugin plugin, Location start, int length, int count) {
		super(plugin);
		this.start = start.clone();
		this.length = length;
		this.count = count;
	}
	
	@Override
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	public void setInvert(boolean invert) {
		this.invert = true;
	}
	
	public void run() {
		int iteration = played + 1;
		float radius = invert ? (float)(1 + length - iteration) * speed : (float)iteration * speed;
		float fullRing = (1 + radius * 2) * (1 + radius * 2);
		int scaledCount = Math.min((int)Math.ceil(fullRing), count);
		for (int i = 0; i < scaledCount; i++) {
			double radians = (double)i / scaledCount * Math.PI * 2;
			Vector direction = new Vector(Math.cos(radians) * radius, 0, Math.sin(radians) * radius);
			Location loc = start.clone();
			loc.add(direction);
			playEffect(loc);
		}
		played++;
		if (played < length) {
			schedule();
		}
	}
}
