package com.elmakers.mine.bukkit.effects;

import org.bukkit.plugin.Plugin;

public class EffectSingle extends EffectPlayer {
	
	public EffectSingle() {
		
	}
	
	public EffectSingle(Plugin plugin) {
		super(plugin);
	}
	
	public void play() {
		if (playAtOrigin) {
			playEffect(origin);
		}
		if (playAtTarget && target != null) {
			playEffect(target);
		}
	}
}
