package com.elmakers.mine.bukkit.entity;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.Art;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

/**
 * This class stores information about an Entity.
 *
 */
public class EntityData implements com.elmakers.mine.bukkit.api.entity.EntityData, Cloneable {
    protected static Map<UUID, WeakReference<Entity>> respawned = new HashMap<UUID, WeakReference<Entity>>();

    protected WeakReference<Entity> entity = null;
    protected UUID uuid = null;
    protected EntityExtraData extraData;
    protected Location location;
    protected Vector relativeLocation;
    protected boolean hasMoved = false;
    protected boolean isTemporary = false;
    protected String name = null;
    protected EntityType type;
    protected Art art;
    protected BlockFace facing;
    protected Rotation rotation;
    protected ItemStack item;
    protected double health = 1;
    protected boolean isBaby;
    protected int fireTicks;
    protected int airLevel;
    protected DyeColor dyeColor;
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
        setEntity(entity);
        this.isTemporary = entity.hasMetadata("temporary");
        this.isLiving = entity instanceof LivingEntity;
        this.isHanging = entity instanceof Hanging;
        this.isProjectile = entity instanceof Projectile;
        this.type = entity.getType();
        this.location = location;
        this.fireTicks = entity.getFireTicks();

        // This can sometimes throw an exception on an invalid
        // entity velocity!
        try {
            this.velocity = entity.getVelocity();
        } catch (Exception ex) {
            this.velocity = null;
        }

        if (entity instanceof Hanging) {
            Hanging hanging = (Hanging)entity;
            try {
                facing = hanging.getFacing();
            } catch (Exception ex) {
                org.bukkit.Bukkit.getLogger().log(Level.WARNING, "Error reading HangingEntity " + entity + " of type " + (entity == null ? "null" : entity.getType()), ex);
            }
        }

        if (entity instanceof LivingEntity) {
            LivingEntity li = (LivingEntity)entity;
            name = li.getCustomName();
            this.health = li.getHealth();
            this.potionEffects = li.getActivePotionEffects();
            this.airLevel = li.getRemainingAir();
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
            this.rotation = ((ItemFrame)entity).getRotation();
        } else if (entity instanceof Item) {
            Item droppedItem = (Item)entity;
            item = droppedItem.getItemStack();
        } else if (entity instanceof Horse) {
            extraData = new EntityHorseData((Horse)entity);
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
        } else if (entity instanceof ArmorStand) {
            extraData = new EntityArmorStandData((ArmorStand)entity);
        }
    }

    private EntityData(EntityType type) {
        this.type = type;
    }

    public static EntityData loadPainting(Vector location, Art art, BlockFace direction) {
        EntityData data = new EntityData(EntityType.PAINTING);
        data.facing = direction;
        data.relativeLocation = location.clone();
        data.art = art;
        return data;
    }

    public static EntityData loadItemFrame(Vector location, ItemStack item, BlockFace direction, Rotation rotation) {
        EntityData data = new EntityData(EntityType.ITEM_FRAME);
        data.facing = direction;
        data.relativeLocation = location.clone();
        data.rotation = rotation;
        data.item = item;
        return data;
    }

    public void setEntity(Entity entity) {
        this.entity = entity == null ? null : new WeakReference<Entity>(entity);
        this.uuid = entity == null ? null : entity.getUniqueId();
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
                spawned = CompatibilityUtils.spawnPainting(location, facing, art);
            break;
            case ITEM_FRAME:
                spawned = CompatibilityUtils.spawnItemFrame(location, facing, rotation, item);
                break;
            case DROPPED_ITEM:
                spawned = location.getWorld().dropItem(location, item);
                break;
            default:
                spawned = location.getWorld().spawnEntity(location, type);
            }
        } catch (Exception ex) {
            org.bukkit.Bukkit.getLogger().log(Level.WARNING, "Error restoring entity type " + getType() + " at " + getLocation(), ex);
        }
        return spawned;
    }

    @Override
    public EntityData getRelativeTo(Location center) {
        EntityData copy = this.clone();
        if (copy != null)
        {
            if (relativeLocation != null) {
                copy.location = center.clone().add(relativeLocation);
            } else if (location != null) {
                copy.location = location.clone();
            }
        }
        return copy;
    }

    @Override
    public Entity spawn() {
        if (location == null) return null;
        Entity spawned = trySpawn();
        if (spawned != null) {
            modify(spawned);
        }

        return spawned;
    }

    @Override
    public Entity undo() {
        Entity entity = this.getEntity();

        // Re-spawn if dead or missing
        if (!isTemporary && uuid != null && (entity == null || !entity.isValid() || entity.isDead()) && !(entity instanceof Player)) {
            // Avoid re-re-spawning an entity
            WeakReference<Entity> respawnedEntity = respawned.get(uuid);
            if (respawnedEntity != null) {
                entity = respawnedEntity.get();
            } else {
                entity = trySpawn();
                if (entity != null) {
                    respawned.put(uuid, new WeakReference<Entity>(entity));
                }
            }
            setEntity(entity);
        }

        modify(entity);
        return entity;
    }

    @Override
    public boolean modify(Entity entity) {
        if (entity == null || entity.getType() != type) return false;

        if (extraData != null) {
            extraData.apply(entity);
        }

        entity.setFireTicks(fireTicks);
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
        } else if (entity instanceof Item) {
            Item droppedItem = (Item)entity;
            droppedItem.setItemStack(item);
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
                li.setRemainingAir(Math.min(airLevel, li.getRemainingAir()));
            } catch (Throwable ex) {
            }
        }

        if (hasMoved && location != null) {
            entity.teleport(location);
        }

        if (hasVelocity && velocity != null) {
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

    public Entity getEntity() {
        return entity == null ? null : entity.get();
    }

    public EntityData clone() {
        try {
            return (EntityData)super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void removed(Entity entity) {
        if (extraData != null) {
            extraData.removed(entity);
        }
    }
}
