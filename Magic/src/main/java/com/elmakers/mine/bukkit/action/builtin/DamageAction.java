package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;

public class DamageAction extends BaseSpellAction
{
    protected double entityDamage;
    protected double playerDamage;
    protected double elementalDamage;
    private boolean magicDamage;
    private boolean magicEntityDamage;
    private Double percentage;
    private Double knockbackResistance;
    private Double damageMultiplier;
    private double maxDistanceSquared;
    private String damageType;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        double damage = parameters.getDouble("damage", 1);
        entityDamage = parameters.getDouble("entity_damage", damage);
        playerDamage = parameters.getDouble("player_damage", damage);
        elementalDamage = parameters.getDouble("elemental_damage", damage);
        if (parameters.contains("damage_multiplier")) {
            damageMultiplier = parameters.getDouble("damage_multiplier");
        } else {
            damageMultiplier = null;
        }
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
        damageType = parameters.getString("damage_type");
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
                    damage = percentage * DeprecatedUtils.getMaxHealth(targetEntity);
                } else if (targetEntity instanceof Player) {
                    damage = playerDamage;
                } else {
                    damage = entityDamage;
                }
                String multiplierType = damageType;
                if (multiplierType == null) {
                    multiplierType = magicDamage ? "magic" : "physical";
                }
                double mageMultiplier = mage.getDamageMultiplier(multiplierType);
                damage *= mageMultiplier;
                if (maxDistanceSquared > 0) {
                    double distanceSquared = context.getLocation().distanceSquared(entity.getLocation());
                    if (distanceSquared > maxDistanceSquared) {
                        return SpellResult.NO_TARGET;
                    }
                    if (distanceSquared > 0) {
                        damage = damage * (1 - distanceSquared / maxDistanceSquared);
                    }
                }
                if (damageMultiplier != null) {
                    damage *= damageMultiplier;
                    mageMultiplier *= damageMultiplier;
                }
                if (damageType != null) {
                    Mage targetMage = controller.getRegisteredMage(targetEntity);
                    String targetAnnotation = "";
                    if (targetMage != null) {
                        targetMage.setLastDamageType(damageType);
                    } else  {
                        targetAnnotation = "*";
                    }
                    mage.sendDebugMessage(ChatColor.RED + "Damaging (" + ChatColor.DARK_RED + damageType + ChatColor.RED + ") x " + ChatColor.DARK_RED + mageMultiplier + ChatColor.RED + " to " + ChatColor.BLUE + targetEntity.getType() + targetAnnotation + ": " + ChatColor.RED + damage, 5);

                    // Have to do magic damage to preserve the source, it seems like this is only important for player
                    // mages since other plugins may be tracking kills.
                    if (mage.isPlayer() && controller.getDamageTypes().contains(damageType)) {
                        CompatibilityUtils.magicDamage(targetEntity, damage, mage.getEntity());
                    } else {
                        CompatibilityUtils.damage(targetEntity, damage, mage.getEntity(), damageType);
                    }
                } else if (magicDamage && (magicEntityDamage || targetEntity instanceof Player)) {
                    mage.sendDebugMessage(ChatColor.RED + "Damaging (Magic) x " +  ChatColor.DARK_RED + mageMultiplier + ChatColor.RED + " to " + ChatColor.BLUE + targetEntity.getType() + ": " + damage, 5);
                    CompatibilityUtils.magicDamage(targetEntity, damage, mage.getEntity());
                } else {
                    mage.sendDebugMessage(ChatColor.RED + "Damaging x " + ChatColor.DARK_RED + mageMultiplier + ChatColor.RED + " to " + ChatColor.BLUE + targetEntity.getType() + ": " + damage, 5);
                    CompatibilityUtils.damage(targetEntity, damage, mage.getEntity());
                }

                if (damage == (int)damage) {
                    context.addMessageParameter("damage", Integer.toString((int)damage));
                } else {
                    context.addMessageParameter("damage", Double.toString(damage));
                }
                if (damageType != null && !damageType.isEmpty()) {
                    String typeDescription = context.getController().getMessages().get("damage_types." + damageType, damageType);
                    context.addMessageParameter("damage_type", typeDescription);
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
