package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellEventType;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class FlingSpell extends Spell
{
	private final long safetyLength = 20000;
	private long lastFling = 0;

	protected int maxSpeedAtElevation = 32;
	protected double minMagnitude = 1.5;
	protected double maxMagnitude = 8; 

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		int height = 0;
		Block playerBlock = player.getLocation().getBlock();

		while (height < maxSpeedAtElevation && playerBlock.getType() == Material.AIR)
		{
			playerBlock = playerBlock.getRelative(BlockFace.DOWN);
			height++;
		}

		double magnitude = (minMagnitude + (((double)maxMagnitude - minMagnitude) * ((double)height / maxSpeedAtElevation)));

		Vector velocity = getAimVector();

		if (player.getLocation().getBlockY() >= 256)
		{
			velocity.setY(0);
		}

		velocity.multiply(magnitude);
		player.setVelocity(velocity);
		castMessage("Whee!");

		spells.registerEvent(SpellEventType.PLAYER_DAMAGE, this);
		lastFling = System.currentTimeMillis();
		return SpellResult.SUCCESS;
	}

	@Override
	public void onPlayerDamage(EntityDamageEvent event)
	{
		if (event.getCause() != DamageCause.FALL) return;

		spells.unregisterEvent(SpellEventType.PLAYER_DAMAGE, this);

		if (lastFling == 0) return;

		if (lastFling + safetyLength > System.currentTimeMillis())
		{
			event.setCancelled(true);
			lastFling = 0;
		}
	}

	@Override
	public void onLoad(ConfigurationNode node)
	{
		disableTargeting();
	}
}
