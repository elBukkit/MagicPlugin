package com.elmakers.mine.bukkit.spell.builtin;

import com.elmakers.mine.bukkit.block.MaterialAndData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.Target;

public class DisintegrateSpell extends BlockSpell
{
	private final static int             DEFAULT_PLAYER_DAMAGE = 1;
	private final static int             DEFAULT_ENTITY_DAMAGE = 100;

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target target = getTarget();
		
		int playerDamage = parameters.getInt("player_damage", DEFAULT_PLAYER_DAMAGE);
		int entityDamage = parameters.getInt("entity_damage", DEFAULT_ENTITY_DAMAGE);

		if (target.hasEntity())
		{
			Entity targetEntity = target.getEntity();
			if (controller.isElemental(targetEntity))
			{
				int elementalDamage = parameters.getInt("elemental_damage", DEFAULT_ENTITY_DAMAGE);
				controller.damageElemental(targetEntity, elementalDamage, 0, mage.getCommandSender());
				return SpellResult.CAST;
			}
			else if (targetEntity instanceof LivingEntity)
			{
				// Register for undo in advance to catch entity death.
				registerForUndo();
				
				LivingEntity li = (LivingEntity)targetEntity;
				if (li instanceof Player)
				{
					li.damage(mage.getDamageMultiplier() * playerDamage, mage.getEntity());
				}
				else
				{
					li.damage(mage.getDamageMultiplier() * entityDamage, mage.getEntity());
					if (li.isDead()) {
						registerModified(li);
						registerForUndo();
					}
				}
				return SpellResult.CAST;
			}
		}

		if (!target.hasTarget())
		{
			return SpellResult.NO_TARGET;
		}
		
		Block targetBlock = target.getBlock();
		if (!hasBuildPermission(targetBlock)) 
		{
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		if (mage.isIndestructible(targetBlock)) 
		{
			return SpellResult.NO_TARGET;
		}

		registerForUndo(targetBlock);

        // This makes $target messaging work properly, otherwise
        // it always displays air or water
        MaterialAndData targetMaterial = new MaterialAndData(targetBlock);
        setTargetName(targetMaterial.getName());
		
		if (isUnderwater())
		{
			targetBlock.setType(Material.STATIONARY_WATER);
		}
		else
		{
			targetBlock.setType(Material.AIR);
		}
		
		registerForUndo();
		return SpellResult.CAST;
	}
}
