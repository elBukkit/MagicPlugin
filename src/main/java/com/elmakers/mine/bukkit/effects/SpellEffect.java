package com.elmakers.mine.bukkit.effects;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Mage;

public class SpellEffect {
	
	// TODO: Make these private once they're config-driven.
    public int speed = 2;
    public int period = 2;
    public int ringSize = 8;
    public int range = 16;
    public float particleOffsetX = 0.5f;
    public float particleOffsetY = 0.5f;
    public float particleOffsetZ = 0.5f;
    public ParticleType particleType = ParticleType.SPELL;
    public Effect effect = null;
    public int particleCount = 8;
    public int data = 0;
    
    // private int ringRange = 6;
    //private boolean reverse = false;
    private Color color1 = Color.PURPLE;
    //private Color color2 = Color.FUCHSIA;
    	
    public EffectPlayer startTrailEffect(Mage mage, Location location, Location target) {
    	Vector toTarget = target.toVector().subtract(location.toVector());
    	int length = (int)Math.ceil(toTarget.length());
    	return startTrailEffect(mage, location, toTarget, length);
	}
    
    public EffectPlayer startTrailEffect(Mage mage, Location playerLocation) {
    	return startTrailEffect(mage, playerLocation, playerLocation.getDirection());
	}
    
	public EffectPlayer startTrailEffect(Mage mage, Location location, Vector direction) {
		return startTrailEffect(mage, location, direction, range);
	}
    
	protected EffectPlayer startTrailEffect(Mage mage, Location location, Vector direction, int length) {
		EffectPlayer effect = new EffectTrail(mage.getController().getPlugin(), location, direction, length);
		startEffect(mage, effect);
		return effect;
	}
	
	public EffectPlayer startRingEffect(Mage mage, Location location) {
		EffectPlayer effect = new EffectRing(mage.getController().getPlugin(), location, range, ringSize);
		startEffect(mage, effect);
		return effect;
	}
		
	protected void startEffect(Mage mage, EffectPlayer effectPlayer) {
		effectPlayer.setEffect(effect);
		effectPlayer.setData(data);
		effectPlayer.setParticleType(particleType);
		effectPlayer.setParticleCount(particleCount);
		Color effectColor = mage.getEffectColor();
		effectColor = effectColor == null ? color1 : effectColor;
		effectPlayer.setEffectData(effectColor.asRGB());
		effectPlayer.setParticleOffset(particleOffsetX, particleOffsetY, particleOffsetZ);
		effectPlayer.setSpeed(speed);
		effectPlayer.setPeriod(period);
		effectPlayer.start();
	}
}
