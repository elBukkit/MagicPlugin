package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.SafetyUtils;

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

        projectileEffects = ConfigurationUtils.getPotionEffects(parameters.getConfigurationSection("projectile_potion_effects"));
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        plugin = context.getPlugin();
        doVelocity = parameters.getBoolean("apply_velocity", true);
        doTeleport = parameters.getBoolean("teleport", true);
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
            entityData = context.getController().getMob(mobType);
            if (entityData == null) {
                entityData = new com.elmakers.mine.bukkit.entity.EntityData(context.getController(), parameters);
            }
        }

        variantName = parameters.getString("variant");
        if (variantName != null && variantName.isEmpty()) {
            variantName = null;
        }
    }

    protected Entity setEntity(MageController controller, @Nonnull Entity entity) {
        this.entity = entity;
        if (noTarget) {
            entity.setMetadata("notarget", new FixedMetadataValue(controller.getPlugin(), true));
        }

        if (entity instanceof LivingEntity) {
            DeprecatedUtils.setMaxHealth(((LivingEntity) entity), 1000.0);
            ((LivingEntity) entity).setHealth(1000.0);
        }
        if (entity instanceof Slime) {
            ((Slime)entity).setSize(1);
        }

        // Variant name still used here for backwards compatibility
        if (entity instanceof Ocelot) {
            Ocelot ocelot = (Ocelot) entity;
            Ocelot.Type variant = Ocelot.Type.WILD_OCELOT;
            if (variantName != null) {
                try {
                    variant = Ocelot.Type.valueOf(variantName.toUpperCase());
                } catch (Exception ignored) {
                }
            } else {
                variant = Ocelot.Type.WILD_OCELOT;
            }

            ocelot.setCatType(variant);

        }
        if (entity instanceof Sheep) {
            Sheep sheep = (Sheep) entity;
            DyeColor color = DyeColor.WHITE;
            if (variantName != null) {
                try {
                    color = DyeColor.valueOf(variantName.toUpperCase());
                } catch (Exception ignored) {

                }
            }
            sheep.setColor(color);
        }
        if (entity instanceof Wolf) {
            Wolf wolf = (Wolf) entity;
            if (variantName != null) {
                // Only set collar color if a variant is set..
                // this makes it a dog, versus a wolf. Technically.
                DyeColor color = DyeColor.RED;
                try {
                    color = DyeColor.valueOf(variantName.toUpperCase());
                    wolf.setTamed(true);
                } catch (Exception ignored) {
                }
                wolf.setCollarColor(color);
            }
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
            Entity spawned = entityData.spawn(context.getController(), location, spawnReason);
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
        if (doVelocity) {
            Vector velocity = this.velocity.clone().multiply(distanceTravelledThisTick);
            if (velocityOffset != null) {
                velocity = velocity.add(velocityOffset);
            }
            SafetyUtils.setVelocity(entity, velocity);
        }
        if (doTeleport) {
            if (orient) {
                target.setDirection(velocity);
            }
            entity.teleport(target);
        }
        return result;
    }

    @Override
    public void finishEffects() {
        super.finishEffects();
        if (entity != null) {
            if (plugin != null) {
                entity.removeMetadata("notarget", plugin);
            }
            entity.remove();
            entity = null;
        }
    }
}
