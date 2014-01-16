package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellEventType;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.EffectRing;
import com.elmakers.mine.bukkit.utilities.EffectTrail;
import com.elmakers.mine.bukkit.utilities.ParticleType;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class FlingSpell extends Spell
{
	private final long safetyLength = 20000;
	private long lastFling = 0;

	protected int defaultMaxSpeedAtElevation = 64;
	protected double defaultMinMagnitude = 1.5;
	protected double defaultMaxMagnitude = 6; 

    private final static int effectSpeed = 2;
    private final static int effectPeriod = 2;
    private final static int maxEffectRange = 16;
    private final static int maxRingEffectRange = 4;
    private final static int ringEffectAmount = 6;

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		int height = 0;
		Block playerBlock = getPlayer().getLocation().getBlock();

		int maxSpeedAtElevation = parameters.getInt("cruising_altitude", defaultMaxSpeedAtElevation);
		double minMagnitude = parameters.getDouble("min_speed", defaultMinMagnitude);
		double maxMagnitude = parameters.getDouble("max_speed", defaultMaxMagnitude);
		double yOffset = parameters.getDouble("yo", 0);
		Double yValue = parameters.getDouble("dy", null);
		Double xValue = parameters.getDouble("dx", null);
		Double zValue = parameters.getDouble("dz", null);
		
		while (height < maxSpeedAtElevation && playerBlock.getType() == Material.AIR)
		{
			playerBlock = playerBlock.getRelative(BlockFace.DOWN);
			height++;
		}

		double heightModifier = maxSpeedAtElevation > 0 ? ((double)height / maxSpeedAtElevation) : 1;
		double magnitude = (minMagnitude + (((double)maxMagnitude - minMagnitude) * heightModifier));

		Vector velocity = getAimVector();
		if (yValue != null) {
			velocity.setY(yValue);
		} else if (yOffset > 0) {
			velocity.setY(velocity.getY() + yOffset);
		}
		if (xValue != null) {
			velocity.setX(xValue);
		}
		if (zValue != null) {
			velocity.setZ(zValue);
		}

		if (getPlayer().getLocation().getBlockY() >= 256)
		{
			velocity.setY(0);
		}

		// Visual effect
		int effectRange = Math.min(getMaxRange(), maxEffectRange / effectSpeed);
		Location effectLocation = getPlayer().getEyeLocation();
		Vector effectDirection = effectLocation.getDirection();
		
		EffectTrail effect = new EffectTrail(controller.getPlugin(), effectLocation, effectDirection, effectRange);
		effect.setParticleType(ParticleType.SPELL);
		effect.setParticleCount(3);
		Color effectColor = mage.getEffectColor();
		effect.setEffectData(effectColor != null ? effectColor.asRGB() : 255);
		effect.setParticleOffset(2.0f, 2.0f, 2.0f);
		effect.setSpeed(effectSpeed);
		effect.setPeriod(effectPeriod);
		effect.start();
		
		velocity.multiply(magnitude);
		getPlayer().setVelocity(velocity);
		castMessage("Whee!");

		controller.registerEvent(SpellEventType.PLAYER_DAMAGE, this);
		lastFling = System.currentTimeMillis();
		return SpellResult.SUCCESS;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onPlayerDamage(EntityDamageEvent event)
	{
		if (event.getCause() != DamageCause.FALL) return;

		controller.unregisterEvent(SpellEventType.PLAYER_DAMAGE, this);

		if (lastFling == 0) return;

		if (lastFling + safetyLength > System.currentTimeMillis())
		{
			event.setCancelled(true);
			lastFling = 0;
			
			// Visual effect
			Location effectLocation = event.getEntity().getLocation();
			Block block = event.getEntity().getLocation().getBlock();
			block = block.getRelative(BlockFace.DOWN);
			int effectRange = Math.min(getMaxRange(), maxRingEffectRange / effectSpeed);
			EffectRing effect = new EffectRing(controller.getPlugin(), effectLocation, effectRange, ringEffectAmount);
			effect.setEffect(Effect.STEP_SOUND);
			effect.setData(block.getTypeId());
			effect.setSpeed(effectSpeed);
			effect.setPeriod(effectPeriod);
			effect.start();
		}
	}
}
