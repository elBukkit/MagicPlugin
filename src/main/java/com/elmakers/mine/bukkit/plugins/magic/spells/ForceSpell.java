package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.InventoryUtils;
import com.elmakers.mine.bukkit.utilities.Target;
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
				Location location = getLocation();
				World targetWorld = targetEntity.getWorld();
				if (!targetEntity.isValid() || targetEntity.isDead())
				{
					releaseTarget();
				} 
				else if (targetWorld == null || location == null || !targetWorld.getName().equals(location.getWorld().getName())) 
				{
					releaseTarget();
				} 
				else if (location.distanceSquared(targetEntity.getLocation()) > getMaxRangeSquared())
				{
					releaseTarget();
				}
				
				// Check for protected Mages
				if (targetEntity != null && targetEntity instanceof Player) {
					Mage targetMage = controller.getMage((Player)targetEntity);
					// Check for protected players (admins, generally...)
					if (targetMage.isSuperProtected()) {
						releaseTarget();
					}
				}
			}
		}
		
		if (targetEntity == null) {
			Target target = getTarget();

			if (target == null || !target.hasTarget() || !target.isEntity() || !(target.getEntity() instanceof LivingEntity))
			{
				targetEntity = null;
				return SpellResult.NO_TARGET;
			}

			targetEntity = (LivingEntity)target.getEntity();
			
			// Check for protected Mages
			if (targetEntity instanceof Player) {
				Mage targetMage = controller.getMage((Player)targetEntity);
				// Check for protected players (admins, generally...)
				if (targetMage.isSuperProtected()) {
					return SpellResult.NO_TARGET;
				}
			}

			effectColor = mage.getEffectColor();
			if (effectColor == null) {
				effectColor = Color.fromRGB(Integer.parseInt(parameters.getString("effect_color", "FF0000"), 16));
			}

			if (effectColor != null) {
				InventoryUtils.addPotionEffect(targetEntity, effectColor);
			}
			return SpellResult.TARGET_SELECTED;
		}

		double multiplier = parameters.getDouble("size", 1);

		int magnitude = parameters.getInt("entity_force", DEFAULT_MAGNITUDE);
		forceEntity(targetEntity, multiplier, magnitude);
		return SpellResult.CAST;
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
			castMessage("Released target");

            releaseTarget();
			return true;
		}
		
		return false;
	}
}
