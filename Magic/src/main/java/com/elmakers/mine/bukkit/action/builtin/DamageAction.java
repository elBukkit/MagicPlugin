package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.Location;
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
import com.elmakers.mine.bukkit.magic.SourceLocation;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class DamageAction extends BaseSpellAction
{
    protected double entityDamage;
    protected double playerDamage;
    protected double elementalDamage;
    private boolean magicDamage;
    private boolean magicEntityDamage;
    private boolean invertDistance;
    private Double percentage;
    private Double knockbackResistance;
    private Double damageMultiplier;
    private double maxDistanceSquared;
    private double minDistanceSquared;
    private double minDamage;
    private String damageType;
    private SourceLocation damageSourceLocation;
    private double criticalProbability;
    private double criticalMultiplier;
    private int noDamageTicks;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        noDamageTicks = parameters.getInt("no_damage_ticks", -1);
        double damage = parameters.getDouble("damage", 1);
        entityDamage = parameters.getDouble("entity_damage", damage);
        playerDamage = parameters.getDouble("player_damage", damage);
        elementalDamage = parameters.getDouble("elemental_damage", damage);
        invertDistance = parameters.getBoolean("invert_distance", false);
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
        double minDistance = parameters.getDouble("damage_min_distance", 0);
        minDistanceSquared = minDistance * minDistance;
        minDamage = parameters.getDouble("min_damage", 0);
        damageType = parameters.getString("damage_type");
        damageSourceLocation = new SourceLocation(parameters.getString("damage_source_location", "BODY"), false);
        criticalProbability = parameters.getDouble("critical_probability", 0);
        criticalMultiplier = parameters.getDouble("critical_damage_multiplier", 0);
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
                if (targetEntity instanceof LivingEntity && noDamageTicks >= 0) {
                    LivingEntity li = (LivingEntity)targetEntity;
                    li.setNoDamageTicks(noDamageTicks);
                }
                if (percentage != null) {
                    damage = percentage * CompatibilityLib.getCompatibilityUtils().getMaxHealth(targetEntity);
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
                    Location entityLocation = damageSourceLocation.getLocation(context);
                    double distanceSquared = context.getLocation().distanceSquared(entityLocation);
                    if (distanceSquared > maxDistanceSquared) {
                        if (invertDistance) {
                            distanceSquared = maxDistanceSquared;
                        } else {
                            return SpellResult.NO_TARGET;
                        }
                    }
                    double distanceRange = maxDistanceSquared - minDistanceSquared;
                    double distanceScale = Math.min(1, Math.max(0, (distanceSquared - minDistanceSquared)) / distanceRange);
                    if (!invertDistance) {
                        distanceScale = 1 - distanceScale;
                    }
                    damage = damage * distanceScale;
                }
                if (damageMultiplier != null) {
                    damage *= damageMultiplier;
                    mageMultiplier *= damageMultiplier;
                }
                if (criticalProbability > 0 && criticalMultiplier > 0 && context.getRandom().nextDouble() <= criticalProbability) {
                    damage *= criticalMultiplier;
                }
                damage = Math.max(minDamage, damage);
                if (damageType != null) {
                    mage.setLastDamageDealtType(damageType);
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
                        CompatibilityLib.getCompatibilityUtils().magicDamage(targetEntity, damage, mage.getEntity());
                    } else {
                        CompatibilityLib.getCompatibilityUtils().damage(targetEntity, damage, mage.getEntity(), damageType);
                    }
                } else if (magicDamage && (magicEntityDamage || targetEntity instanceof Player)) {
                    mage.sendDebugMessage(ChatColor.RED + "Damaging (Magic) x " +  ChatColor.DARK_RED + mageMultiplier + ChatColor.RED + " to " + ChatColor.BLUE + targetEntity.getType() + ": " + damage, 5);
                    CompatibilityLib.getCompatibilityUtils().magicDamage(targetEntity, damage, mage.getEntity());
                } else {
                    mage.sendDebugMessage(ChatColor.RED + "Damaging x " + ChatColor.DARK_RED + mageMultiplier + ChatColor.RED + " to " + ChatColor.BLUE + targetEntity.getType() + ": " + damage, 5);
                    CompatibilityLib.getCompatibilityUtils().damage(targetEntity, damage, mage.getEntity());
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
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else if (parameterKey.equals("magic_damage")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else if (parameterKey.equals("percentage")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_PERCENTAGES));
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
