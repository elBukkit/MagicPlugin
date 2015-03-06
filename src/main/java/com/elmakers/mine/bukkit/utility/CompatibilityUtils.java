package com.elmakers.mine.bukkit.utility;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

/**
 * A generic place to put compatibility-based utilities.
 * 
 * These are generally here when there is a new method added
 * to the Bukkti API we'd like to use, but aren't quite
 * ready to give up backwards compatibility.
 * 
 * The easy solution to this problem is to shamelessly copy
 * Bukkit's code in here, mark it as deprecated and then
 * switch everything over once the new Bukkit method is in an
 * official release.
 */
public class CompatibilityUtils extends NMSUtils {

    /**
     * This is shamelessly copied from org.bukkit.Location.setDirection.
     *
     * It's only here for 1.6 backwards compatibility.
     *
     * This will be removed once there is an RB for 1.7 or 1.8.
     *
     * @param location The Location to set the direction of
     * @param vector the vector to use for the new direction
     * @return Location the resultant Location (same as location)
     */
    @Deprecated
    public static Location setDirection(Location location, Vector vector) {
        return location.setDirection(vector);
    }

    public static void applyPotionEffects(LivingEntity entity, Collection<PotionEffect> effects) {
        for (PotionEffect effect: effects) {
            applyPotionEffect(entity, effect);
        }
    }

    public static void applyPotionEffect(LivingEntity entity, PotionEffect effect) {
        // Avoid nerfing existing effects
        boolean applyEffect = true;
        Collection<PotionEffect> currentEffects = entity.getActivePotionEffects();
        for (PotionEffect currentEffect : currentEffects) {
            if (currentEffect.getType().equals(effect.getType())) {
                if (effect.getAmplifier() < 0) {
                    applyEffect = false;
                    entity.removePotionEffect(effect.getType());
                    break;
                } else if (currentEffect.getAmplifier() > effect.getAmplifier()) {
                    applyEffect = false;
                    break;
                }
            }
        }
        if (applyEffect) {
            entity.addPotionEffect(effect, true);
        }
    }

    public static boolean setDisplayName(ItemStack itemStack, String displayName) {
        Object handle = getHandle(itemStack);
        if (handle == null) return false;
        Object tag = getTag(handle);
        if (tag == null) return false;

        Object displayNode = createNode(tag, "display");
        if (displayNode == null) return false;
        setMeta(displayNode, "Name", displayName);
        return true;
    }

    public static boolean setLore(ItemStack itemStack, Collection<String> lore) {
        Object handle = getHandle(itemStack);
        if (handle == null) return false;
        Object tag = getTag(handle);
        if (tag == null) return false;

        Object displayNode = createNode(tag, "display");
        if (displayNode == null) return false;
        final Object loreList = setStringList(displayNode, "Lore", lore);
        return loreList != null;
    }

    public static Inventory createInventory(InventoryHolder holder, final int size, final String name) {
        Inventory inventory = null;
        try {
            inventory = (Inventory)class_CraftInventoryCustom_constructor.newInstance(holder, size, ChatColor.translateAlternateColorCodes('&', name));
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return inventory;
    }

    public static void addPotionEffect(LivingEntity entity, Color color) {
        addPotionEffect(entity, color.asRGB());
    }

    public static void setInvulnerable(Entity entity) {
        setInvulnerable(entity, true);
    }

    public static void setInvulnerable(Entity entity, boolean flag) {
        try {
            Object handle = getHandle(entity);
            class_Entity_invulnerableField.set(handle, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void removePotionEffect(LivingEntity entity) {
        watch(entity, 7, 0);
    }

    @Deprecated
    public static Painting spawnPainting(Location location, BlockFace facing, Art art)
    {
        Painting newPainting = null;
        try {
            //                entity = new EntityPainting(world, (int) x, (int) y, (int) z, dir);
            Constructor<?> paintingConstructor = class_EntityPainting.getConstructor(class_World, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            Method addEntity = class_World.getMethod("addEntity", class_Entity, CreatureSpawnEvent.SpawnReason.class);

            Object worldHandle = getHandle(location.getWorld());
            Object newEntity = paintingConstructor.newInstance(worldHandle, location.getBlockX(), location.getBlockY(), location.getBlockZ(), getFacing(facing));
            if (newEntity != null) {
                Entity bukkitEntity = getBukkitEntity(newEntity);
                if (bukkitEntity == null || !(bukkitEntity instanceof Painting)) return null;

                newPainting = (Painting)bukkitEntity;
                newPainting.setFacingDirection(facing, true);
                newPainting.setArt(art, true);
                addEntity.invoke(worldHandle, newEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return newPainting;
    }

    @Deprecated
    public static ItemFrame spawnItemFrame(Location location, BlockFace facing, Rotation rotation, ItemStack item)
    {
        ItemFrame newItemFrame = null;
        try {
            // entity = new EntityItemFrame(world, (int) x, (int) y, (int) z, dir);
            Constructor<?> itemFrameConstructor = class_EntityItemFrame.getConstructor(class_World, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            Method addEntity = class_World.getMethod("addEntity", class_Entity, CreatureSpawnEvent.SpawnReason.class);

            Object worldHandle = getHandle(location.getWorld());
            Object newEntity = itemFrameConstructor.newInstance(worldHandle, location.getBlockX(), location.getBlockY(), location.getBlockZ(), getFacing(facing));
            if (newEntity != null) {
                Entity bukkitEntity = getBukkitEntity(newEntity);
                if (bukkitEntity == null || !(bukkitEntity instanceof ItemFrame)) return null;

                newItemFrame = (ItemFrame)bukkitEntity;
                newItemFrame.setItem(getCopy(item));
                newItemFrame.setFacingDirection(facing, true);
                newItemFrame.setRotation(rotation);

                // This will fail sometimes ... the entity is already tracked?
                try {
                    addEntity.invoke(worldHandle, newEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                } catch (Exception ex) {

                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return newItemFrame;
    }

    public static void watch(Object entityHandle, int key, Object data) {
        try {
            Method getDataWatcherMethod = class_Entity.getMethod("getDataWatcher");
            Object dataWatcher = getDataWatcherMethod.invoke(entityHandle);
            class_DataWatcher_watchMethod.invoke(dataWatcher, key, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void watch(Entity entity, int key, Object data) {
        try {
            Method geHandleMethod = entity.getClass().getMethod("getHandle");
            watch(geHandleMethod.invoke(entity), key, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void addPotionEffect(LivingEntity entity, int color) {
        // Hacky safety check
        if (color == 0) {
            color = 0x010101;
        }
        watch(entity, 7, color);
    }

    public static List<Entity> getNearbyEntities(Location location, double x, double y, double z) {
        Object worldHandle = getHandle(location.getWorld());
        try {
            Object bb = class_AxisAlignedBB_createBBMethod.invoke(null, location.getX() - x, location.getY() - y, location.getZ() - z,
                    location.getX() + x, location.getY() + y, location.getZ() + z);

            // The input entity is only used for equivalency testing, so this "null" should be ok.
            List<? extends Object> entityList = (List<? extends Object>)class_World_getEntitiesMethod.invoke(worldHandle, null, bb);
            List<Entity> bukkitEntityList = new java.util.ArrayList<org.bukkit.entity.Entity>(entityList.size());

            for (Object entity : entityList) {
                bukkitEntityList.add((Entity)class_Entity_getBukkitEntityMethod.invoke(entity));
            }
            return bukkitEntityList;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Minecart spawnCustomMinecart(Location location, MaterialAndData display, int offset)
    {
        Minecart newMinecart = null;
        try {
            Constructor<?> minecartConstructor = class_EntityMinecartRideable.getConstructor(class_World, Double.TYPE, Double.TYPE, Double.TYPE);
            Method addEntity = class_World.getMethod("addEntity", class_Entity, CreatureSpawnEvent.SpawnReason.class);
            Method setPositionRotationMethod = class_Entity.getMethod("setPositionRotation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);

            Object worldHandle = getHandle(location.getWorld());
            Object newEntity = minecartConstructor.newInstance(worldHandle, location.getX(), location.getY(), location.getZ());
            if (newEntity != null) {
                // Set initial rotation
                setPositionRotationMethod.invoke(newEntity, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

                // Set tile material id, pack into NMS 3-byte format
                int materialId = (display.getMaterial().getId() & 0xFFFF) | (display.getData() << 16);
                watch(newEntity, 20, materialId);

                // Set the tile offset
                watch(newEntity, 21, offset);

                // Finalize custom display tile
                watch(newEntity, 22, (byte)1);

                addEntity.invoke(worldHandle, newEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                Entity bukkitEntity = getBukkitEntity(newEntity);
                if (bukkitEntity == null || !(bukkitEntity instanceof Minecart)) return null;

                newMinecart = (Minecart)bukkitEntity;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return newMinecart;
    }

    public static Class<? extends Runnable> getTaskClass(BukkitTask task) {
        Class<? extends Runnable> taskClass = null;
        try {
            Method getTaskClassMethod = class_CraftTask.getDeclaredMethod("getTaskClass");
            getTaskClassMethod.setAccessible(true);
            taskClass = (Class<? extends Runnable>)getTaskClassMethod.invoke(task);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return taskClass;
    }

    public static Runnable getTaskRunnable(BukkitTask task) {
        Runnable runnable = null;
        try {
            Field taskField = class_CraftTask.getDeclaredField("task");
            taskField.setAccessible(true);
            runnable = (Runnable)taskField.get(task);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return runnable;
    }

    public static void ageItem(Item item, int ticksToAge)
    {
        try {
            Class<?> itemClass = fixBukkitClass("net.minecraft.server.EntityItem");
            Object handle = getHandle(item);
            Field ageField = itemClass.getDeclaredField("age");
            ageField.setAccessible(true);
            ageField.set(handle, ticksToAge);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static ThrownPotion potion = null;

    public static void magicDamage(LivingEntity target, double amount, Entity source) {
        try {

            if (target == null) return;
            Object targetHandle = getHandle(target);
            if (targetHandle == null) return;

            Object sourceHandle = getHandle(source);

            // Special-case for mobs .. witches are immune to magic damage :\
            if (!(target instanceof Player))
            {
                target.damage(amount, source);
                return;
            }

            // Bukkit won't allow magic damage from anything but a potion..
            if (sourceHandle != null && source instanceof LivingEntity) {
                if (potion == null) {
                    Location location = target.getLocation();
                    potion = (ThrownPotion) location.getWorld().spawnEntity(location, EntityType.SPLASH_POTION);
                    potion.remove();
                }
                potion.setShooter((LivingEntity)source);
                Object potionHandle = getHandle(potion);
                Object damageSource = class_DamageSource_getMagicSourceMethod.invoke(null, potionHandle, sourceHandle);
                class_EntityLiving_damageEntityMethod.invoke(targetHandle, damageSource, (float)amount);
            } else {
                Object magicSource = class_DamageSource_MagicField.get(null);
                class_EntityLiving_damageEntityMethod.invoke(targetHandle, magicSource, (float) amount);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Deprecated
    public static void setTarget(LivingEntity entity, Location target)
    {
        // This never seemed to work, and is not compatible with 1.8, so removing it.
    }

    public static Location getEyeLocation(Entity entity)
    {
        if (entity instanceof LivingEntity)
        {
            return ((LivingEntity)entity).getEyeLocation();
        }

        return entity.getLocation();
    }

    public static BoundingBox getHitbox(Entity entity)
    {
        // TODO: Config-driven
        return new BoundingBox(entity.getLocation().toVector(), -0.75, 0.75, 0, 2, -0.75, 0.75);
    }

    public static Object getSkullProfile(Skull state)
    {
        if (isLegacy) return null;
        Object profile = null;
        try {
            if (state == null || !class_CraftSkull.isInstance(state)) return false;
            profile = class_CraftSkull_profile.get(state);
        } catch (Exception ex) {

        }
        return profile;
    }

    public static boolean setSkullProfile(Skull state, Object data)
    {
        if (isLegacy) return false;
        try {
            if (state == null || !class_CraftSkull.isInstance(state)) return false;
            class_CraftSkull_profile.set(state, data);
            return true;
        } catch (Exception ex) {

        }

        return false;
    }

    public static boolean setSkullOwner(Skull state, String playerName, UUID playerId)
    {
        // TODO: This could be done directly, but is kind of tricky.
        ItemStack skullItem = InventoryUtils.getPlayerSkull(playerName, playerId);
        if (skullItem == null) {
            return false;
        }

        return setSkullProfile(state, InventoryUtils.getSkullProfile(skullItem.getItemMeta()));

    }

    public static boolean setSkullOwner(Skull state, Player owner)
    {
        return setSkullOwner(state, owner.getName(), owner.getUniqueId());
    }

    public static Object getBannerPatterns(BlockState state)
    {
        if (isLegacy) return null;
        Object data = null;
        try {
            if (state == null || !class_CraftBanner.isInstance(state)) return null;
            data = class_CraftBanner_getPatternsMethod.invoke(state);
        } catch (Exception ex) {

        }
        return data;
    }

    public static DyeColor getBannerBaseColor(BlockState state)
    {
        if (isLegacy) return null;
        DyeColor color = null;
        try {
            if (state == null || !class_CraftBanner.isInstance(state)) return null;
            color = (DyeColor)class_CraftBanner_getBaseColorMethod.invoke(state);
        } catch (Exception ex) {

        }
        return color;
    }

    public static boolean setBannerPatterns(BlockState state, Object patterns)
    {
        if (isLegacy || patterns == null) return false;
        Object data = null;
        try {
            if (state == null || !class_CraftBanner.isInstance(state)) return false;
            data = class_CraftBanner_setPatternsMethod.invoke(state, patterns);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public static boolean setBannerBaseColor(BlockState state, DyeColor color)
    {
        if (isLegacy || color == null) return false;
        try {
            if (state == null || !class_CraftBanner.isInstance(state)) return false;
            color = (DyeColor)class_CraftBanner_setBaseColorMethod.invoke(state, color);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
}
