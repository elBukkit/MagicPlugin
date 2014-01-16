package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
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

public class LevitateSpell extends Spell
{
	private long levitateEnded;
	private final long safetyLength = 10000;

    private final static int effectSpeed = 2;
    private final static int effectPeriod = 2;
    private final static int maxEffectRange = 16;
    private final static int maxRingEffectRange = 4;
    private final static int ringEffectAmount = 6;
	
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		if (getPlayer().getAllowFlight()) {
			deactivate();
			return SpellResult.COST_FREE;
		}
		activate();

		return SpellResult.SUCCESS;
	}
	
	@Override
	public void onDeactivate() {
		getPlayer().setFlying(false);
		getPlayer().setAllowFlight(false);
		
		// Prevent the player from death by fall
		controller.registerEvent(SpellEventType.PLAYER_DAMAGE, this);
		levitateEnded = System.currentTimeMillis();
		
		castMessage("You feel heavier");
	}
	
	@Override
	public void onActivate() {
		// Visual effect
		int effectRange = Math.min(getMaxRange(), maxEffectRange / effectSpeed);
		Location effectLocation = getPlayer().getEyeLocation();
		Vector effectDirection = new Vector(0, 1, 0);
		
		EffectTrail effect = new EffectTrail(controller.getPlugin(), effectLocation, effectDirection, effectRange);
		effect.setParticleType(ParticleType.SPELL);
		effect.setParticleCount(3);
		Color effectColor = mage.getEffectColor();
		effect.setEffectData(effectColor != null ? effectColor.asRGB() : 16711935);
		effect.setParticleOffset(4.0f, 4.0f, 4.0f);
		effect.setSpeed(effectSpeed);
		effect.setPeriod(effectPeriod);
		effect.start();
				
		Vector velocity = getPlayer().getVelocity();
		velocity.setY(velocity.getY() + 2);
		getPlayer().setVelocity(velocity);
		Bukkit.getScheduler().scheduleSyncDelayedTask(controller.getPlugin(), new Runnable() {
			public void run() {
				getPlayer().setAllowFlight(true);
				getPlayer().setFlying(true);
				
			}
		}, 2);

		castMessage("You feel lighter");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onPlayerDamage(EntityDamageEvent event)
	{
		if (event.getCause() != DamageCause.FALL) return;

		controller.unregisterEvent(SpellEventType.PLAYER_DAMAGE, this);

		if (levitateEnded == 0) return;

		if (levitateEnded + safetyLength > System.currentTimeMillis())
		{
			event.setCancelled(true);
			levitateEnded = 0;;
			
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
