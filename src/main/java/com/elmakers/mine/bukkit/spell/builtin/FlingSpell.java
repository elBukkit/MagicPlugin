package com.elmakers.mine.bukkit.spell.builtin;

import java.util.Collection;

import com.elmakers.mine.bukkit.api.effect.ParticleType;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.spell.SpellEventType;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.effect.builtin.EffectRing;
import com.elmakers.mine.bukkit.spell.TargetingSpell;

public class FlingSpell extends TargetingSpell implements Listener
{
	private final long safetyLength = 20000;
	private long lastFling = 0;

	protected int defaultMaxSpeedAtElevation = 64;
	protected double defaultMinMagnitude = 1.5;
	protected double defaultMaxMagnitude = 4; 

    private final static int effectSpeed = 1;
    private final static int effectPeriod = 3;
    private final static int minRingEffectRange = 2;
    private final static int maxRingEffectRange = 15;
    private final static int maxDamageAmount = 200;

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		int height = 0;
		Block playerBlock = getLocation().getBlock();

        LivingEntity entity = mage.getLivingEntity();
        if (entity == null) {
            return SpellResult.LIVING_ENTITY_REQUIRED;
        }

		int maxSpeedAtElevation = parameters.getInt("cruising_altitude", defaultMaxSpeedAtElevation);
		double minMagnitude = parameters.getDouble("min_speed", defaultMinMagnitude);
		double maxMagnitude = parameters.getDouble("max_speed", defaultMaxMagnitude);
		
		while (height < maxSpeedAtElevation && playerBlock.getType() == Material.AIR)
		{
			playerBlock = playerBlock.getRelative(BlockFace.DOWN);
			height++;
		}

		double heightModifier = maxSpeedAtElevation > 0 ? ((double)height / maxSpeedAtElevation) : 1;
		double magnitude = (minMagnitude + (((double)maxMagnitude - minMagnitude) * heightModifier));

		Vector velocity = mage.getLocation().getDirection();
		if (mage.getLocation().getBlockY() >= 256)
		{
			velocity.setY(0);
		}

		velocity.multiply(magnitude);
		entity.setVelocity(velocity);

		mage.registerEvent(SpellEventType.PLAYER_DAMAGE, this);
		lastFling = System.currentTimeMillis();
		return SpellResult.CAST;
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event)
	{
		if (event.getCause() != DamageCause.FALL) return;

		mage.unregisterEvent(SpellEventType.PLAYER_DAMAGE, this);

		if (lastFling == 0) return;

		if (lastFling + safetyLength > System.currentTimeMillis())
		{
			event.setCancelled(true);
			lastFling = 0;
			
			// Visual effect
			// TODO: Data-drive?
			Location effectLocation = event.getEntity().getLocation();
			Block block = event.getEntity().getLocation().getBlock();
			block = block.getRelative(BlockFace.DOWN);
			int ringEffectRange = (int)Math.ceil(((double)maxRingEffectRange - minRingEffectRange) * event.getDamage() / maxDamageAmount + minRingEffectRange);
			int effectRange = Math.min(maxRingEffectRange, ringEffectRange);
			effectRange = Math.min(getMaxRange(), effectRange / effectSpeed);
			
			EffectRing effect = new EffectRing(controller.getPlugin());
			effect.setRadius(effectRange);
            effect.setEffect(Effect.STEP_SOUND);
			//effect.setParticleType(ParticleType.BLOCK_BREAKING);
			effect.setMaterial(block);
			effect.setPeriod(effectPeriod);
			effect.start(effectLocation, null);
		}
	}

	@Override
	public void getParameters(Collection<String> parameters)
	{
		super.getParameters(parameters);
		parameters.add("cruising_altitude");
		parameters.add("min_speed");
		parameters.add("max_speed");
	}
}
