package com.elmakers.mine.bukkit.spell.builtin;

import java.util.Collection;

import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.UndoableSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
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

@Deprecated
public class FlingSpell extends UndoableSpell implements Listener
{
	private long safetyLength = 20000;
	private long lastFling = 0;

	protected int defaultMaxSpeedAtElevation = 64;
	protected double defaultMinMagnitude = 1.5;
	protected double defaultMaxMagnitude = 4; 

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
        safetyLength = parameters.getLong("safety", safetyLength);
		
		while (height < maxSpeedAtElevation && playerBlock.getType() == Material.AIR)
		{
			playerBlock = playerBlock.getRelative(BlockFace.DOWN);
			height++;
		}

		double heightModifier = maxSpeedAtElevation > 0 ? ((double)height / maxSpeedAtElevation) : 1;
		double magnitude = (minMagnitude + ((maxMagnitude - minMagnitude) * heightModifier));

		Vector velocity = getDirection();
		if (mage.getLocation().getBlockY() >= 256)
		{
			velocity.setY(0);
		}

		velocity.multiply(magnitude);

        registerVelocity(entity);
        registerMoved(entity);
		CompatibilityUtils.setVelocity(entity, velocity);
        if (safetyLength > 0) {
            mage.registerEvent(SpellEventType.PLAYER_DAMAGE, this);
        }
		lastFling = System.currentTimeMillis();
        registerForUndo();
		return SpellResult.CAST;
	}

	@Override
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
            int ringEffectRange = (int)Math.ceil(((double)maxRingEffectRange - minRingEffectRange) * event.getDamage() / maxDamageAmount + minRingEffectRange);
            ringEffectRange = Math.min(maxRingEffectRange, ringEffectRange);
            playEffects("land", ringEffectRange);
		}
	}

	@Override
	public void getParameters(Collection<String> parameters)
	{
		super.getParameters(parameters);
		parameters.add("cruising_altitude");
		parameters.add("min_speed");
		parameters.add("max_speed");
        parameters.add("safety");
	}

    @Override
    public com.elmakers.mine.bukkit.api.block.MaterialAndData getEffectMaterial()
    {
        Block block = mage.getEntity().getLocation().getBlock();
        block = block.getRelative(BlockFace.DOWN);
        return new MaterialAndData(block);
    }
}
