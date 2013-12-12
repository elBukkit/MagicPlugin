package com.elmakers.mine.bukkit.utilities;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public abstract class EffectPlayer implements Runnable {
	
	protected final Plugin plugin;
	protected Effect effect = null;
	protected int period = 1;
	protected int data = 0;
	protected FireworkEffect fireworkEffect = null;
	protected int power = 0;
	
	public EffectPlayer(Plugin plugin) {
		this.plugin = plugin;
	}
	
	public void setEffect(Effect effect) {
		this.effect = effect;
	}
	
	public void setFireworkEffect(FireworkEffect fireworkEffect, int power) {
		this.fireworkEffect = fireworkEffect;
		this.power = power;
	}
	
	public void start() {
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
	
	protected void playEffect(Location location) {
		if (effect != null) {
			location.getWorld().playEffect(location, effect, data);
		}
		if (fireworkEffect != null) {
			FireworkUtils.spawnFireworkEffect(location, fireworkEffect, power);
		}
	}
	
	public abstract void setSpeed(float speed);
}
