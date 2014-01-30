package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Color;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.InventoryUtils;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class ForceSpell extends Spell
{
	LivingEntity targetEntity = null;
	private Color effectColor = null;
	
	final private static int DEFAULT_MAGNITUDE = 3;
	
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		if (targetEntity != null)
		{
			if (targetEntity instanceof LivingEntity)
			{
				if (!targetEntity.isValid() || targetEntity.isDead())
				{
					releaseTarget();
				}
				if (targetEntity != null && getPlayer().getLocation().distanceSquared(targetEntity.getLocation()) > getMaxRangeSquared())
				{
					releaseTarget();
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

			effectColor = mage.getEffectColor();
			if (effectColor == null) {
				effectColor = Color.fromRGB(Integer.parseInt(parameters.getString("effect_color", "FF0000"), 16));
			}

			targetEntity = (LivingEntity)target.getEntity();
			if (effectColor != null) {
				InventoryUtils.addPotionEffect(targetEntity, effectColor);
			}
			return SpellResult.COST_FREE;
		}

		double multiplier = parameters.getDouble("size", 1);

		int magnitude = parameters.getInt("entity_force", DEFAULT_MAGNITUDE);
		forceEntity(targetEntity, multiplier, magnitude);
		return SpellResult.SUCCESS;
	}

	protected void forceEntity(Entity target, double multiplier, int magnitude)
	{
		magnitude = (int)((double)magnitude * multiplier);
		Vector forceVector = mage.getLocation().getDirection();
		forceVector.normalize();
		forceVector.multiply(magnitude);
		target.setVelocity(forceVector);
	}
	
	protected void releaseTarget() {
		if (targetEntity != null && effectColor != null) {
			InventoryUtils.clearPotionEffect(targetEntity);
		}
		targetEntity = null;
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
            &&      getPlayer().getLocation().distanceSquared(targetEntity.getLocation()) > getMaxRangeSquared()
            )
            {
                castMessage("Released target");
            }

            releaseTarget();
			return true;
		}
		
		return false;
	}
}
