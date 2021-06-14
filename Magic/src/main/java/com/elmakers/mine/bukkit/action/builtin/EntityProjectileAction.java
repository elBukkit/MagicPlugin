package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;
import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.SafetyUtils;
import com.elmakers.mine.bukkit.utility.metadata.EntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;

public class EntityProjectileAction extends CustomProjectileAction {
    private boolean noTarget = true;
    private boolean doTeleport = false;
    private boolean doVelocity = false;
    private boolean orient = false;
    private Vector velocityOffset;
    private Vector locationOffset;
    protected CreatureSpawnEvent.SpawnReason spawnReason = CreatureSpawnEvent.SpawnReason.CUSTOM;
    private Collection<PotionEffect> projectileEffects;

    private EntityData entityData;
    protected String variantName;

    protected Entity entity = null;
    protected Plugin plugin = null;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters) {
        super.initialize(spell, parameters);
        projectileEffects = ConfigurationUtils.getPotionEffects(parameters, "projectile_potion_effects", Integer.MAX_VALUE);
    }

    protected boolean teleportByDefault() {
        return false;
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        plugin = context.getPlugin();
        doVelocity = parameters.getBoolean("apply_velocity", true);
        doTeleport = parameters.getBoolean("teleport", teleportByDefault());
        noTarget = parameters.getBoolean("no_target", true);
        orient = parameters.getBoolean("orient", false);
        velocityOffset = ConfigurationUtils.getVector(parameters, "velocity_offset");
        locationOffset = ConfigurationUtils.getVector(parameters, "location_offset");

        if (parameters.contains("spawn_reason")) {
            String reasonText = parameters.getString("spawn_reason").toUpperCase();
            try {
                spawnReason = CreatureSpawnEvent.SpawnReason.valueOf(reasonText);
            } catch (Exception ex) {
                context.getMage().sendMessage("Unknown spawn reason: " + reasonText);
            }
        }

        String mobType = parameters.getString("type");
        if (mobType == null || mobType.isEmpty()) {
            context.getLogger().warning("EntityProjectileAction missing type parameter");
        } else {
            entityData = context.getController().getMob(parameters);
        }

        variantName = parameters.getString("variant");
        if (variantName != null && variantName.isEmpty()) {
            variantName = null;
        }
    }

    protected Entity setEntity(MageController controller, @Nonnull Entity entity) {
        this.entity = entity;
        if (noTarget) {
            EntityMetadataUtils.instance().setBoolean(entity, MagicMetaKeys.NO_TARGET, true);
        }
        CompatibilityUtils.setPersist(entity, false);

        if (entity instanceof LivingEntity) {
            CompatibilityUtils.setMaxHealth(((LivingEntity) entity), 1000.0);
            ((LivingEntity) entity).setHealth(1000.0);
        }

        if (projectileEffects != null && entity instanceof LivingEntity) {
            CompatibilityUtils.applyPotionEffects((LivingEntity)entity, projectileEffects);
        }
        targeting.ignoreEntity(entity);
        return entity;
    }

    @Override
    public SpellResult start(CastContext context) {
        if (entity == null) {
            // Specific for falling blocks
            MaterialAndData brush = context.getBrush();
            if (brush != null && entityData.getMaterial() == null) {
                entityData.setMaterial(brush);
            }

            Location location = adjustLocation(sourceLocation.getLocation(context));
            Entity spawned = entityData.spawn(location, spawnReason);
            if (spawned != null) {
                context.registerForUndo(spawned);
                setEntity(context.getController(), spawned);
            }
        }
        if (entity == null) {
            return SpellResult.FAIL;
        }
        return super.start(context);
    }

    protected Location adjustLocation(Location target) {
        // TODO: locationOffset and velocityOffset should be made relative
        if (locationOffset != null) {
            target = target.clone().add(locationOffset);
        }
        return target;
    }

    @Override
    public SpellResult step(CastContext context) {
        SpellResult result = super.step(context);
        if (entity == null) {
            return SpellResult.CAST;
        }

        // Note that in testing it somehow doesn't seem to matter if we adjust the location here
        // I really have no idea why, but it seems to work OK if we adjust it on spawn.
        Location target = adjustLocation(actionContext.getTargetLocation());
        if (doTeleport) {
            if (orient) {
                target.setDirection(velocity);
            }
            entity.teleport(target);
        }
        if (doVelocity) {
            Vector velocity = this.velocity.clone().multiply(distanceTravelledThisTick);
            if (velocityOffset != null) {
                velocity = velocity.add(velocityOffset);
            }
            SafetyUtils.setVelocity(entity, velocity);
        }
        return result;
    }

    @Override
    public void finishEffects() {
        super.finishEffects();
        if (entity != null) {
            entity.remove();
            entity = null;
        }
    }
}
