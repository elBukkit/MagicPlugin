package com.elmakers.mine.bukkit.utilities;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class EffectTrail implements Runnable {
	
	protected final Location start;
	protected final Vector direction;
	protected final Plugin plugin;
	protected final int length;
	protected Effect effect = null;
	protected int period = 1;
	protected int data = 0;

	protected Location current;
	protected int played = 0;
	
	public EffectTrail(Plugin plugin, Location start, Vector direction, int length) {
		this.start = start.clone();
		this.direction = direction.clone();
		this.direction.normalize();
		this.plugin = plugin;
		this.length = length;
	}
	
	public void setEffect(Effect effect) {
		this.effect = effect;
	}
	
	public void setSpeed(float speed) {
		this.direction.normalize();
		this.direction.multiply(speed);
	}
	
	public void start() {
		current = start.clone();
		schedule();
	}
	
	protected void schedule() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, period);
	}
	
	public void setPeriod(int period) {
		this.period = period;
	}
	
	public void setData(int data) {
		this.data = data;
	}
	
	public void run() {
		if (effect != null) {
			current.getWorld().playEffect(current, effect, data);
		}
		current.add(direction);
		played++;
		if (played < length) {
			schedule();
		}
	}
}
