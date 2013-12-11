package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class ForceSpell extends Spell
{
	int magnitude = 3;
	LivingEntity targetEntity = null;
	
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		if (targetEntity != null)
		{
			if (targetEntity instanceof LivingEntity)
			{
				if (!targetEntity.isValid() || targetEntity.isDead())
				{
					targetEntity = null;
				}
				if (targetEntity != null && player.getLocation().distanceSquared(targetEntity.getLocation()) > getMaxRangeSquared())
				{
					targetEntity = null;
				}
			}
		}
		
		if (targetEntity == null) {
			targetEntity(LivingEntity.class);
			Target target = getTarget();

			if (target == null || !target.hasTarget() || !target.isEntity() || !(target.getEntity() instanceof LivingEntity))
			{
				targetEntity = null;
				return SpellResult.NO_TARGET;
			}
			
			targetEntity = (LivingEntity)target.getEntity();
			return SpellResult.COST_FREE;
		}

		double multiplier = parameters.getDouble("size", 1);
		forceEntity(targetEntity, multiplier);
		return SpellResult.SUCCESS;
	}

	protected void forceEntity(Entity target, double multiplier)
	{
		magnitude = (int)((double)magnitude * multiplier);
		Vector forceVector = getAimVector();
		forceVector.normalize();
		forceVector.multiply(magnitude);
		target.setVelocity(forceVector);
	}

	@Override
	public boolean onCancel()
	{
		if (targetEntity != null)
		{
            if 
            (
                    (targetEntity instanceof LivingEntity) 
            &&      !targetEntity.isDead() 
            &&      player.getLocation().distanceSquared(targetEntity.getLocation()) > getMaxRangeSquared()
            )
            {
                castMessage("Released target");
            }

            targetEntity = null;
			return true;
		}
		
		return false;
	}

	@Override
	public void onLoad(ConfigurationNode properties)  
	{
		magnitude = properties.getInt("entity_force", magnitude);
	}
}
