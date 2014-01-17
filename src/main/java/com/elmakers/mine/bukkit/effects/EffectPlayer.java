package com.elmakers.mine.bukkit.effects;

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
	protected ParticleType particleType = null;
	protected String particleSubType = "";
	protected float xOffset = 0;
	protected float yOffset = 0;
	protected float zOffset = 0;
	protected float effectData = 20f;
	protected int particleCount = 10;
	
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
	
	public void setParticleType(ParticleType particleType) {
		this.particleType = particleType;
	}
	
	public void setParticleSubType(String particleSubType) {
		this.particleSubType = particleSubType;
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
			EffectUtils.spawnFireworkEffect(location, fireworkEffect, power);
		}
		if (particleType != null) {
			EffectUtils.playEffect(location, particleType, particleSubType, xOffset, yOffset, zOffset, effectData, particleCount);
		}
	}
	
	public abstract void setSpeed(float speed);

	public float getEffectData() {
		return effectData;
	}

	public void setEffectData(float effectData) {
		this.effectData = effectData;
	}

	public int getParticleCount() {
		return particleCount;
	}

	public void setParticleCount(int particleCount) {
		this.particleCount = particleCount;
	}
	
	public void setParticleOffset(float xOffset, float yOffset, float zOffset) {
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.zOffset = zOffset;
	}
}
