package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.event.entity.EntityDamageEvent;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellEventType;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class InvincibleSpell extends Spell 
{
	protected float protectAmount = 0;
	protected int amount = 100;

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		amount = parameters.getInt("amount", amount);

		if (protectAmount != 0)
		{
			deactivate();
		}
		else
		{
			activate();
		}

		return SpellResult.SUCCESS;
	}

	@Override
	public void onDeactivate()
	{
		sendMessage("You feel normal.");
		spells.unregisterEvent(SpellEventType.PLAYER_DAMAGE, this); 
		protectAmount = 0;
	}
	
	@Override
	public void onActivate()
	{
		spells.registerEvent(SpellEventType.PLAYER_DAMAGE, this);

		if (amount >= 100)
		{
			castMessage("You feel invincible!");
		}
		else
		{
			castMessage("You feel strong!");
		}

		protectAmount = (float)amount / 100;
	}
	
	@Override
	public void onPlayerDamage(EntityDamageEvent event)
	{
		if (protectAmount > 0)
		{
			if (protectAmount >= 1)
			{
				event.setCancelled(true);
			}
			else
			{
				int newDamage = (int)Math.floor((1.0f - protectAmount) * event.getDamage());
				if (newDamage == 0) newDamage = 1;
				event.setDamage(newDamage);
			}
		}
	}

	@Override
	public void onLoadTemplate(ConfigurationNode node)
	{
		disableTargeting();
	}
}
