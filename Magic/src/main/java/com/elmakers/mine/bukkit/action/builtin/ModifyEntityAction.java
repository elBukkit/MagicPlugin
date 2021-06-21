package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class ModifyEntityAction extends BaseSpellAction
{
    private CreatureSpawnEvent.SpawnReason spawnReason = CreatureSpawnEvent.SpawnReason.EGG;

    private boolean noDrops = false;
    private boolean force = false;
    private boolean tamed = false;
    private boolean setOwner = true;

    private Vector direction = null;
    private double speed;
    private double dyOffset;

    private EntityData entityData;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        noDrops = parameters.getBoolean("no_drops", false);
        force = parameters.getBoolean("force", false);
        tamed = parameters.getBoolean("tamed", false);
        speed = parameters.getDouble("speed", 0);
        direction = ConfigurationUtils.getVector(parameters, "direction");
        dyOffset = parameters.getDouble("dy_offset", 0);

        String disguiseTarget = parameters.getString("disguise_target");
        if (disguiseTarget != null) {
            Entity targetEntity = disguiseTarget.equals("target") ? context.getTargetEntity() : context.getEntity();
            if (targetEntity != null) {
                ConfigurationSection disguiseConfig = parameters.createSection("disguise");
                disguiseConfig.set("type", targetEntity.getType().name().toLowerCase());
                if (targetEntity instanceof Player) {
                    Player targetPlayer = (Player)targetEntity;
                    disguiseConfig.set("name", targetPlayer.getName());
                    disguiseConfig.set("skin", targetPlayer.getName());
                }
            }
        }

        entityData = context.getController().getMob(parameters);
        if (parameters.contains("reason")) {
            String reasonText = parameters.getString("reason").toUpperCase();
            try {
                spawnReason = CreatureSpawnEvent.SpawnReason.valueOf(reasonText);
            } catch (Exception ex) {
                spawnReason = CreatureSpawnEvent.SpawnReason.EGG;
            }
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        Entity targetEntity = context.getTargetEntity();
        EntityType newType = entityData.getType();
        if (newType != null && !targetEntity.getType().equals(newType)) {
            return replaceEntity(targetEntity, context);
        }

        Collection<EffectPlayer> entityEffects = context.getEffects("modified");
        for (EffectPlayer effectPlayer : entityEffects) {
            effectPlayer.start(targetEntity.getLocation(), targetEntity, null, null);
        }
        context.registerModified(targetEntity);
        entityData.modify(targetEntity);
        return modify(context, targetEntity);
    }


    private SpellResult modify(CastContext context, Entity entity) {
        if (entity == null) {
            return SpellResult.FAIL;
        }

        MageController controller = context.getController();
        // Special check to assign ownership
        if (entity instanceof AreaEffectCloud) {
            ((AreaEffectCloud)entity).setSource(context.getLivingEntity());
        } else if (entity instanceof Projectile) {
            ((Projectile)entity).setShooter(context.getLivingEntity());
        }

        if (noDrops && ! (entity instanceof Player)) {
            CompatibilityLib.getEntityMetadataUtils().setBoolean(entity, MagicMetaKeys.NO_DROPS, true);
        }
        if (speed > 0)
        {
            Vector motion = direction;
            if (motion == null)
            {
                motion = context.getDirection();
            }
            else
            {
                motion = motion.clone();
            }

            if (dyOffset != 0) {
                motion.setY(motion.getY() + dyOffset);
            }
            motion.normalize();
            motion.multiply(speed);
            CompatibilityLib.getCompatibilityUtils().setEntityMotion(entity, motion);
        }
        if (setOwner && entity instanceof Creature) {
            CompatibilityLib.getEntityMetadataUtils().setString(entity, MagicMetaKeys.OWNER, context.getMage().getId());
        }
        LivingEntity shooter = context.getLivingEntity();
        if (shooter != null) {
            if (entity instanceof Projectile) {
                ((Projectile)entity).setShooter(shooter);
            } else if (entity instanceof AreaEffectCloud) {
                ((AreaEffectCloud)entity).setSource(shooter);
            }
        }
        if (tamed && entity instanceof Tameable) {
            Tameable tameable = (Tameable)entity;
            tameable.setTamed(true);
            Player owner = context.getMage().getPlayer();
            if (owner != null) {
                tameable.setOwner(owner);
            }
        }
        return SpellResult.CAST;
    }

    private SpellResult replaceEntity(Entity targetEntity, CastContext context) {
        context.registerModified(targetEntity);
        MageController controller = context.getController();
        Entity spawnedEntity = controller.replaceMob(targetEntity, entityData, force, spawnReason);
        if (spawnedEntity == null) {
            return SpellResult.FAIL;
        }

        Collection<EffectPlayer> entityEffects = context.getEffects("spawned");
        for (EffectPlayer effectPlayer : entityEffects) {
            effectPlayer.start(spawnedEntity.getLocation(), spawnedEntity, null, null);
        }
        context.registerForUndo(spawnedEntity);
        return modify(context, spawnedEntity);
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("loot");
        parameters.add("baby");
        parameters.add("name");
        parameters.add("type");
        parameters.add("speed");
        parameters.add("reason");
        parameters.add("villager_profession");
        parameters.add("skeleton_type");
        parameters.add("ocelot_type");
        parameters.add("rabbit_type");
        parameters.add("horse_variant");
        parameters.add("horse_style");
        parameters.add("horse_color");
        parameters.add("color");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("type")) {
            for (EntityType type : EntityType.values()) {
                examples.add(type.name().toLowerCase());
            }
        } else if (parameterKey.equals("reason")) {
            for (CreatureSpawnEvent.SpawnReason type : CreatureSpawnEvent.SpawnReason.values()) {
                examples.add(type.name().toLowerCase());
            }
        } else if (parameterKey.equals("villager_profession")) {
            for (Villager.Profession profession : Villager.Profession.values()) {
                examples.add(profession.name().toLowerCase());
            }
        } else if (parameterKey.equals("rabbit_type")) {
            for (Rabbit.Type type : Rabbit.Type.values()) {
                examples.add(type.name().toLowerCase());
            }
        } else if (parameterKey.equals("horse_style")) {
            for (Horse.Style type : Horse.Style.values()) {
                examples.add(type.name().toLowerCase());
            }
        } else if (parameterKey.equals("horse_color")) {
            for (Horse.Color type : Horse.Color.values()) {
                examples.add(type.name().toLowerCase());
            }
        } else if (parameterKey.equals("color")) {
            for (DyeColor type : DyeColor.values()) {
                examples.add(type.name().toLowerCase());
            }
        } else if (parameterKey.equals("loot") || parameterKey.equals("baby") || parameterKey.equals("repeat_random")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else if (parameterKey.equals("name")) {
            examples.add("Philbert");
        } else if (parameterKey.equals("speed")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
