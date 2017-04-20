package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class EntityProjectileAction extends CustomProjectileAction {
    private boolean noTarget = true;
    private boolean doTeleport = false;
    private boolean doVelocity = false;
    private boolean orient = false;
    private Vector velocityOffset;
    private Vector locationOffset;
    private EntityType entityType;
    protected CreatureSpawnEvent.SpawnReason spawnReason = CreatureSpawnEvent.SpawnReason.CUSTOM;

    // To do .. use EntityData for all of this
    private String customName;
    protected String variantName;
    boolean isBaby;

    protected Entity entity = null;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        doVelocity = parameters.getBoolean("apply_velocity", true);
        doTeleport = parameters.getBoolean("teleport", true);
        noTarget = parameters.getBoolean("no_target", true);
        orient = parameters.getBoolean("orient", false);
        velocityOffset = ConfigurationUtils.getVector(parameters, "velocity_offset");
        locationOffset = ConfigurationUtils.getVector(parameters, "location_offset");

        try {
            String entityTypeName = parameters.getString("type", "");
            if (!entityTypeName.isEmpty())
            {
                entityType = EntityType.valueOf(entityTypeName.toUpperCase());
            }
        } catch(Exception ex) {
            entityType = null;
        }

        if (parameters.contains("spawn_reason")) {
            String reasonText = parameters.getString("spawn_reason").toUpperCase();
            try {
                spawnReason = CreatureSpawnEvent.SpawnReason.valueOf(reasonText);
            } catch (Exception ex) {
                context.getMage().sendMessage("Unknown spawn reason: " + reasonText);
            }
        }

        customName = parameters.getString("name");
        isBaby =  parameters.getBoolean("baby", false);
        variantName = parameters.getString("variant");
        if (variantName != null && variantName.isEmpty()) {
            variantName = null;
        }
    }

    protected Entity setEntity(MageController controller, Entity entity) {
        this.entity = entity;
        if (noTarget) {
            entity.setMetadata("notarget", new FixedMetadataValue(controller.getPlugin(), true));
        }
        if (customName != null) {
            entity.setCustomName(customName);
            entity.setCustomNameVisible(true);
        }

        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).setMaxHealth(1000.0);
            ((LivingEntity) entity).setHealth(1000.0);
        }
        if (entity instanceof Slime) {
            ((Slime)entity).setSize(1);
        }

        if (entity instanceof Ageable) {
            if (isBaby) {
                ((Ageable) entity).setBaby();
            } else {
                ((Ageable) entity).setAdult();
            }
        } else if (entity instanceof Zombie) {
            ((Zombie) entity).setBaby(isBaby);
        } else if (entity instanceof PigZombie) {
            ((PigZombie) entity).setBaby(isBaby);
        } else if (entity instanceof Slime && isBaby) {
            Slime slime = (Slime) entity;
            slime.setSize(0);
        }

        if (entity instanceof Ocelot) {
            Ocelot ocelot = (Ocelot) entity;
            Ocelot.Type variant = Ocelot.Type.WILD_OCELOT;
            if (variantName != null) {
                try {
                    variant = Ocelot.Type.valueOf(variantName.toUpperCase());
                } catch (Exception ex) {
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
                } catch (Exception ex){

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
                } catch (Exception ex){

                }
                wolf.setCollarColor(color);
            }
        }
        targeting.ignoreEntity(entity);
        return entity;
    }

    protected Location adjustLocation(Location target) {
        // TODO: locationOffset and velocityOffset should be made relative
        if (locationOffset != null) {
            target = target.clone().add(locationOffset);
        }
        return target;
    }

    protected Entity spawnEntity(Location location) {
        if (entityType != null) {
            return CompatibilityUtils.spawnEntity(location, entityType, spawnReason);
        }
        return null;
    }

    @Override
    public SpellResult step(CastContext context) {
        SpellResult result = super.step(context);
        Location target = adjustLocation(actionContext.getTargetLocation());

        if (entity == null) {
            Location location = adjustLocation(target);
            Entity spawned = spawnEntity(location);
            if (spawned == null) {
                return SpellResult.FAIL;
            }
            setEntity(context.getController(), spawned);
            return result;
        }

        // Note that in testing it somehow doesn't seem to matter if we adjust the location here
        // I really have no idea why, but it seems to work OK if we adjust it on spawn.
        if (doVelocity) {
            Vector velocity = this.velocity.clone().multiply(distanceTravelledThisTick);
            if (velocityOffset != null) {
                velocity = velocity.add(velocityOffset);
            }
            entity.setVelocity(velocity);
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
    public void finish(CastContext context) {
        super.finish(context);
        if (entity != null) {
            entity.remove();
            entity = null;
        }
    }
}
