package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;

public class DamageAction extends BaseSpellAction
{
    protected double entityDamage;
    protected double playerDamage;
    protected double elementalDamage;
    private boolean magicDamage;
	private boolean magicEntityDamage;
	private Double percentage;
	private Double knockbackResistance;
	private double maxDistanceSquared;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        double damage = parameters.getDouble("damage", 1);
        entityDamage = parameters.getDouble("entity_damage", damage);
        playerDamage = parameters.getDouble("player_damage", damage);
        elementalDamage = parameters.getDouble("elemental_damage", damage);
		if (parameters.contains("percentage")) {
			percentage = parameters.getDouble("percentage");
		} else {
			percentage = null;
		}
        magicDamage = parameters.getBoolean("magic_damage", true);
		magicEntityDamage = parameters.getBoolean("magic_entity_damage", magicDamage);
        if (parameters.contains("knockback_resistance")) {
            knockbackResistance = parameters.getDouble("knockback_resistance");
        } else {
            knockbackResistance = null;
        }
		double maxDistance = parameters.getDouble("damage_max_distance");
		maxDistanceSquared = maxDistance * maxDistance;
    }

	@Override
	public SpellResult perform(CastContext context)
	{
        Entity entity = context.getTargetEntity();
		if (entity == null || !(entity instanceof Damageable) || entity.isDead())
		{
			return SpellResult.NO_TARGET;
		}

		double damage = 1;

		Damageable targetEntity = (Damageable)entity;
		LivingEntity livingTarget = (entity instanceof LivingEntity) ? (LivingEntity)entity : null;
        context.registerDamaged(targetEntity);
		Mage mage = context.getMage();
		MageController controller = context.getController();

		double previousKnockbackResistance = 0D;
		try {
			if (knockbackResistance != null && livingTarget != null) {
				AttributeInstance knockBackAttribute = livingTarget.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
				previousKnockbackResistance = knockBackAttribute.getBaseValue();
				knockBackAttribute.setBaseValue(knockbackResistance); 
			}
			if (controller.isElemental(entity)) {
				damage = elementalDamage;
				controller.damageElemental(entity, damage * mage.getDamageMultiplier(), 0, mage.getCommandSender());
			} else {
				if (percentage != null) {
					damage = percentage * targetEntity.getMaxHealth();
				} else if (targetEntity instanceof Player) {
					damage = playerDamage;
				} else {
					damage = entityDamage;
				}
				damage *= mage.getDamageMultiplier();
				if (maxDistanceSquared > 0) {
					double distanceSquared = context.getLocation().distanceSquared(entity.getLocation());
					if (distanceSquared > maxDistanceSquared) {
						return SpellResult.NO_TARGET;
					}
					if (distanceSquared > 0) {
						damage = damage * (1 - distanceSquared / maxDistanceSquared);
					}
				}
				if (magicDamage && (magicEntityDamage || targetEntity instanceof Player)) {
					CompatibilityUtils.magicDamage(targetEntity, damage, mage.getEntity());
				} else {
					CompatibilityUtils.damage(targetEntity, damage, mage.getEntity());
				}
			}
		} finally {
			if (knockbackResistance != null && livingTarget != null) {
				AttributeInstance knockBackAttribute = livingTarget.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
				knockBackAttribute.setBaseValue(previousKnockbackResistance);
			}
		}

		return SpellResult.CAST;
	}

	@Override
	public boolean isUndoable()
	{
		return true;
	}

	@Override
	public void getParameterNames(Spell spell, Collection<String> parameters) {
		super.getParameterNames(spell, parameters);
		parameters.add("damage");
		parameters.add("player_damage");
		parameters.add("entity_damage");
		parameters.add("elemental_damage");
        parameters.add("magic_damage");
		parameters.add("percentage");
	}

	@Override
	public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
		if (parameterKey.equals("damage") || parameterKey.equals("player_damage")
			|| parameterKey.equals("entity_damage") || parameterKey.equals("elemental_damage")) {
			examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
		} else if (parameterKey.equals("magic_damage")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else if (parameterKey.equals("percentage")) {
			examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_PERCENTAGES)));
		} else {
			super.getParameterOptions(spell, parameterKey, examples);
		}
	}

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
