package com.elmakers.mine.bukkit.entity;

import java.util.Collection;

import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

/**
 * This class stores information about an Entity.
 *
 */
public class EntityData implements com.elmakers.mine.bukkit.api.entity.EntityData {
    protected Location location;
    protected boolean hasMoved = false;
    protected String name = null;
    protected EntityType type;
    protected Art art;
    protected BlockFace facing;
    protected Rotation rotation;
    protected ItemStack item;
    protected double health = 1;
    protected boolean isBaby;
    protected int fireTicks;
    protected DyeColor dyeColor;
    protected Horse.Color horseColor;
    protected Horse.Variant horseVariant;
    protected Horse.Style horseStyle;
    protected SkeletonType skeletonType;
    protected Ocelot.Type ocelotType;
    protected Villager.Profession villagerProfession;
    protected Collection<PotionEffect> potionEffects = null;
    protected boolean hasPotionEffects = false;
    protected Vector velocity = null;
    protected boolean hasVelocity = false;
    protected boolean isHanging = false;
    protected boolean isLiving = false;
    protected boolean isProjectile = false;

    public EntityData(Entity entity) {
        this(entity.getLocation(), entity);
    }

    public EntityData(Location location, Entity entity) {
        this.isLiving = entity instanceof LivingEntity;
        this.isHanging = entity instanceof Hanging;
        this.isProjectile = entity instanceof Projectile;
        this.type = entity.getType();
        this.location = location;
        this.fireTicks = entity.getFireTicks();
        this.velocity = entity.getVelocity();

        if (entity instanceof Hanging) {
            Hanging hanging = (Hanging)entity;
            facing = hanging.getFacing();
            this.location = location.getBlock().getLocation();
        }
        if (entity instanceof ItemFrame) {
            this.rotation = ((ItemFrame)entity).getRotation();
        }

        if (entity instanceof LivingEntity) {
            LivingEntity li = (LivingEntity)entity;
            name = li.getCustomName();
            this.health = li.getHealth();
            this.potionEffects = li.getActivePotionEffects();
        }

        if (entity instanceof Ageable) {
            Ageable ageable = (Ageable)entity;
            this.isBaby = !ageable.isAdult();
        }

        if (entity instanceof Colorable) {
            Colorable colorable = (Colorable)entity;
            dyeColor = colorable.getColor();
        }

        if (entity instanceof Painting) {
            Painting painting = (Painting)entity;
            art = painting.getArt();
        } else if (entity instanceof ItemFrame) {
            ItemFrame itemFrame = (ItemFrame)entity;
            item = itemFrame.getItem();
        } else if (entity instanceof Horse) {
            Horse horse = (Horse)entity;
            horseVariant = horse.getVariant();
            horseColor = horse.getColor();
            horseStyle = horse.getStyle();
        } else if (entity instanceof Skeleton) {
            Skeleton skeleton = (Skeleton)entity;
            skeletonType = skeleton.getSkeletonType();
        } else if (entity instanceof Villager) {
            Villager villager = (Villager)entity;
            villagerProfession = villager.getProfession();
        } else if (entity instanceof Wolf) {
            Wolf wolf = (Wolf)entity;
            dyeColor = wolf.getCollarColor();
        } else if (entity instanceof Ocelot) {
            Ocelot ocelot = (Ocelot)entity;
            ocelotType = ocelot.getCatType();
        }
    }

    /**
     * Thanks you, Chilinot!
     * @param loc
     * @param art
     * @param facing
     * @return
     */
    private Location getPaintingOffset(Location loc, Art art, BlockFace facing) {
        switch(art) {

            // 1x1
            case ALBAN:
            case AZTEC:
            case AZTEC2:
            case BOMB:
            case KEBAB:
            case PLANT:
            case WASTELAND:
                return loc; // No calculation needed.

            // 1x2
            case GRAHAM:
            case WANDERER:
                return loc.getBlock().getLocation().add(0, -1, 0);

            // 2x1
            case CREEBET:
            case COURBET:
            case POOL:
            case SEA:
            case SUNSET:	// Use same as 4x3

                // 4x3
            case DONKEYKONG:
            case SKELETON:
                if(facing == BlockFace.WEST)
                    return loc.getBlock().getLocation().add(0, 0, -1);
                else if(facing == BlockFace.SOUTH)
                    return loc.getBlock().getLocation().add(-1, 0, 0);
                else
                    return loc;

                // 2x2
            case BUST:
            case MATCH:
            case SKULL_AND_ROSES:
            case STAGE:
            case VOID:
            case WITHER:	// Use same as 4x2

                // 4x2
            case FIGHTERS:  // Use same as 4x4

                // 4x4
            case BURNINGSKULL:
            case PIGSCENE:
            case POINTER:
                if(facing == BlockFace.WEST)
                    return loc.getBlock().getLocation().add(0, -1, -1);
                else if(facing == BlockFace.SOUTH)
                    return loc.getBlock().getLocation().add(-1, -1, 0);
                else
                    return loc.add(0, -1, 0);

                // Unsupported artwork
            default:
                return loc;
        }
    }

    /**
     * API Implementation
     */

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public EntityType getType() {
        return type;
    }

    @Override
    public Art getArt() {
        return art;
    }

    @Override
    public BlockFace getFacing() {
        return facing;
    }

    @Override
    public ItemStack getItem() {
        return item;
    }

    @Override
    public double getHealth() {
        return health;
    }

    protected Entity trySpawn() {
        Entity spawned = null;
        try {
            switch (type) {
            case PLAYER:
                // Nope!
            break;
            case PAINTING:
                Location attach = location.getBlock().getRelative(facing.getOppositeFace()).getLocation();
                spawned = location.getWorld().spawn(attach, Painting.class);
                Painting painting = (Painting)spawned;

                // Not sure why the double-offset is needed here.. but it seems to work! :P
                Location offset = getPaintingOffset(getPaintingOffset(location, art, facing), art, facing);

                painting.teleport(offset);
                painting.setFacingDirection(facing, true);
                painting.setArt(art, true);
            break;
            case ITEM_FRAME:
                Location frameAttach = location.getBlock().getRelative(facing.getOppositeFace()).getLocation();
                spawned = CompatibilityUtils.spawnItemFrame(frameAttach, facing, rotation, item);
                break;
            case DROPPED_ITEM:
                // TODO: Handle this, would need to store item data.
                spawned = null;
                break;
            default:
                spawned = location.getWorld().spawnEntity(location, type);
            }
        } catch (Exception ex) {

        }
        return spawned;
    }

    @Override
    public Entity spawn() {
        Entity spawned = trySpawn();
        if (spawned != null) {
            modify(spawned);
        }

        return spawned;
    }

    @Override
    public boolean modify(Entity entity) {
        if (entity == null || entity.getType() != type) return false;

        entity.setFireTicks(fireTicks);

        // Re-spawn if dead
        if (!entity.isValid() && !(entity instanceof Player)) {
            Entity tryEntity = trySpawn();
            if (tryEntity != null) {
                entity = tryEntity;
            }
        }

        if (entity instanceof Ageable) {
            Ageable ageable = (Ageable)entity;
            if (isBaby) {
                ageable.setBaby();
            } else {
                ageable.setAdult();
            }
        }

        if (entity instanceof Colorable) {
            Colorable colorable = (Colorable)entity;
            colorable.setColor(dyeColor);
        }

        if (entity instanceof Painting) {
            Painting painting = (Painting) entity;
            painting.setArt(art, true);
            painting.setFacingDirection(facing, true);
        }
        else if (entity instanceof ItemFrame) {
            ItemFrame itemFrame = (ItemFrame)entity;
            itemFrame.setItem(item);
            itemFrame.setFacingDirection(facing, true);
        } else if (entity instanceof Horse) {
            Horse horse = (Horse)entity;
            horse.setVariant(horseVariant);
            horse.setStyle(horseStyle);
            horse.setColor(horseColor);
        } else if (entity instanceof Skeleton) {
            Skeleton skeleton = (Skeleton)entity;
            skeleton.setSkeletonType(skeletonType);
        } else if (entity instanceof Villager) {
            Villager villager = (Villager)entity;
            villager.setProfession(villagerProfession);
        } else if (entity instanceof Wolf) {
            Wolf wolf = (Wolf)entity;
            wolf.setCollarColor(dyeColor);
        } else if (entity instanceof Ocelot) {
            Ocelot ocelot = (Ocelot)entity;
            ocelot.setCatType(ocelotType);
        }

        if (entity instanceof LivingEntity) {
            LivingEntity li = (LivingEntity)entity;
            if (hasPotionEffects) {
                Collection<PotionEffect> currentEffects = li.getActivePotionEffects();
                for (PotionEffect effect : currentEffects) {
                    li.removePotionEffect(effect.getType());
                }
                if (potionEffects != null) {
                    for (PotionEffect effect : potionEffects) {
                        li.addPotionEffect(effect);
                    }
                }
            }

            if (name != null && name.length() > 0) {
                li.setCustomName(name);
            }

            try {
                li.setHealth(Math.min(health, li.getMaxHealth()));
            } catch (Throwable ex) {
            }
        }

        if (hasMoved) {
            entity.teleport(location);
        }

        if (hasVelocity) {
            entity.setVelocity(velocity);
        }

        return true;
    }

    public void setHasMoved(boolean moved) {
        this.hasMoved = moved;
    }

    public void setHasPotionEffects(boolean changed) {
        this.hasPotionEffects = changed;
    }

    public void setHasVelocity(boolean hasVelocity) {
        this.hasVelocity = hasVelocity;
    }

    public boolean isHanging() {
        return isHanging;
    }

    public boolean isLiving() {
        return isLiving;
    }

    public boolean isProjectile() {
        return isProjectile;
    }
}
