package com.elmakers.mine.bukkit.utility;

import com.google.common.io.BaseEncoding;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Witch;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.logging.Level;

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
    public static boolean USE_MAGIC_DAMAGE = true;
    public static int BLOCK_BREAK_RANGE = 64;
    public final static int MAX_ENTITY_RANGE = 72;
    private final static Map<World.Environment, Integer> maxHeights = new HashMap<>();
    public static Map<Integer, Material> materialIdMap;
    private static ItemStack dummyItem;

    private static final EnteredStateTracker DAMAGING = new EnteredStateTracker();

    public static boolean isDamaging() {
        return DAMAGING.isInside();
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
                    break;
                } else if (currentEffect.getAmplifier() > effect.getAmplifier() || effect.getDuration() > Integer.MAX_VALUE / 4) {
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
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(displayName);
        itemStack.setItemMeta(meta);
        return true;
    }

    public static boolean setLore(ItemStack itemStack, List<String> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return true;
    }

    public static Inventory createInventory(InventoryHolder holder, int size, final String name) {
        size = (int)(Math.ceil((double)size / 9) * 9);
        size = Math.min(size, 54);

        String shorterName = name;
        if (shorterName.length() > 32) {
            shorterName = shorterName.substring(0, 31);
        }
        shorterName = ChatColor.translateAlternateColorCodes('&', shorterName);

        // TODO: Is this even still necessary?
        if (class_CraftInventoryCustom_constructor == null) {
            return Bukkit.createInventory(holder, size, shorterName);
        }
        Inventory inventory = null;
        try {
            inventory = (Inventory)class_CraftInventoryCustom_constructor.newInstance(holder, size, shorterName);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return inventory;
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

    public static void setSilent(Entity entity, boolean flag) {
        if (class_Entity_setSilentMethod == null) return;
        try {
            Object handle = getHandle(entity);
            class_Entity_setSilentMethod.invoke(handle, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean isSilent(Entity entity) {
        if (class_Entity_isSilentMethod == null) return false;
        try {
            Object handle = getHandle(entity);
            return (boolean)class_Entity_isSilentMethod.invoke(handle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static void setSilent(Object nmsEntity, boolean flag) {
        if (class_Entity_setSilentMethod == null) return;
        try {
            class_Entity_setSilentMethod.invoke(nmsEntity, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Thanks you, Chilinot!
     * @param loc
     * @param art
     * @param facing
     * @return
     */
    private static Location getPaintingOffset(Location loc, BlockFace facing, Art art) {
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
            case SUNSET:    // Use same as 4x3

            // 4x3
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
            case WITHER:    // Use same as 4x2

            // 4x2
            case FIGHTERS:  // Use same as 4x4

            // 4x4
            case PIGSCENE:
            case POINTER:
                if(facing == BlockFace.WEST)
                    return loc.getBlock().getLocation().add(0, -1, -1);
                else if(facing == BlockFace.SOUTH)
                    return loc.getBlock().getLocation().add(-1, -1, 0);
                else
                    return loc.add(0, -1, 0);

            default:
                // Special cases for name changes...
                switch (art.name()) {
                    case "BURNINGSKULL":
                    case "BURNING_SKULL":
                        if(facing == BlockFace.WEST)
                            return loc.getBlock().getLocation().add(0, -1, -1);
                        else if(facing == BlockFace.SOUTH)
                            return loc.getBlock().getLocation().add(-1, -1, 0);
                        else
                            return loc.add(0, -1, 0);
                    case "DONKEYKONG":
                    case "DONKEY_KONG":
                        if(facing == BlockFace.WEST)
                            return loc.getBlock().getLocation().add(0, 0, -1);
                        else if(facing == BlockFace.SOUTH)
                            return loc.getBlock().getLocation().add(-1, 0, 0);
                        else
                            return loc;
                }

                // Unsupported artwork
                return loc;
        }
    }

    public static Painting createPainting(Location location, BlockFace facing, Art art)
    {
        Painting newPainting = null;
        try {
            location = getPaintingOffset(location, facing, art);
            Object worldHandle = getHandle(location.getWorld());
            Object newEntity = null;
            @SuppressWarnings("unchecked")
            Enum<?> directionEnum = Enum.valueOf(class_EnumDirection, facing.name());
            Object blockLocation = class_BlockPosition_Constructor.newInstance(location.getX(), location.getY(), location.getZ());
            newEntity = class_EntityPaintingConstructor.newInstance(worldHandle, blockLocation, directionEnum);
            if (newEntity != null) {
                Entity bukkitEntity = getBukkitEntity(newEntity);
                if (bukkitEntity == null || !(bukkitEntity instanceof Painting)) return null;

                newPainting = (Painting)bukkitEntity;
                newPainting.setFacingDirection(facing, true);
                newPainting.setArt(art, true);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            newPainting = null;
        }
        return newPainting;
    }

    public static ItemFrame createItemFrame(Location location, BlockFace facing, Rotation rotation, ItemStack item)
    {
        ItemFrame newItemFrame = null;
        try {
            Object worldHandle = getHandle(location.getWorld());
            Object newEntity = null;
            @SuppressWarnings("unchecked")
            Enum<?> directionEnum = Enum.valueOf(class_EnumDirection, facing.name());
            Object blockLocation = class_BlockPosition_Constructor.newInstance(location.getX(), location.getY(), location.getZ());
            newEntity = class_EntityItemFrameConstructor.newInstance(worldHandle, blockLocation, directionEnum);
            if (newEntity != null) {
                Entity bukkitEntity = getBukkitEntity(newEntity);
                if (bukkitEntity == null || !(bukkitEntity instanceof ItemFrame)) return null;

                newItemFrame = (ItemFrame)bukkitEntity;
                newItemFrame.setItem(getCopy(item));
                newItemFrame.setFacingDirection(facing, true);
                newItemFrame.setRotation(rotation);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return newItemFrame;
    }

    public static ArmorStand createArmorStand(Location location)
    {
        return (ArmorStand)createEntity(location, EntityType.ARMOR_STAND);
    }

    public static Entity createEntity(Location location, EntityType entityType)
    {
        Entity bukkitEntity = null;
        try {
            Class<? extends Entity> entityClass = entityType.getEntityClass();
            Object newEntity = class_CraftWorld_createEntityMethod.invoke(location.getWorld(), location, entityClass);
            if (newEntity != null) {
                bukkitEntity = getBukkitEntity(newEntity);
                if (bukkitEntity == null || !entityClass.isAssignableFrom(bukkitEntity.getClass())) return null;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return bukkitEntity;
    }

    public static boolean addToWorld(World world, Entity entity, CreatureSpawnEvent.SpawnReason reason)
    {
        try {
            Object worldHandle = getHandle(world);
            Object entityHandle = getHandle(entity);
            class_World_addEntityMethod.invoke(worldHandle, entityHandle, reason);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public static List<Entity> getNearbyEntities(Location location, double x, double y, double z) {
        if (location == null) return null;
        Object worldHandle = getHandle(location.getWorld());
        try {
            x = Math.min(x, CompatibilityUtils.MAX_ENTITY_RANGE);
            z = Math.min(z, CompatibilityUtils.MAX_ENTITY_RANGE);
            Object bb = class_AxisAlignedBB_Constructor.newInstance(location.getX() - x, location.getY() - y, location.getZ() - z,
                    location.getX() + x, location.getY() + y, location.getZ() + z);

            // The input entity is only used for equivalency testing, so this "null" should be ok.
            @SuppressWarnings("unchecked")
            List<? extends Object> entityList = (List<? extends Object>)class_World_getEntitiesMethod.invoke(worldHandle, null, bb);
            List<Entity> bukkitEntityList = new java.util.ArrayList<>(entityList.size());

            for (Object entity : entityList) {
                Entity bukkitEntity = (Entity)class_Entity_getBukkitEntityMethod.invoke(entity);
                if (bukkitEntity instanceof ComplexLivingEntity) {
                    ComplexLivingEntity complex = (ComplexLivingEntity)bukkitEntity;
                    Set<ComplexEntityPart> parts = complex.getParts();
                    for (ComplexEntityPart part : parts) {
                        bukkitEntityList.add(part);
                    }
                } else {
                    bukkitEntityList.add(bukkitEntity);
                }
            }
            return bukkitEntityList;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Minecart spawnCustomMinecart(Location location, Material material, short data, int offset)
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
                // TODO: Unbreak this maybe one day?
                /*
                int materialId = (display.getMaterial().getId() & 0xFFFF) | (display.getData() << 16);
                watch(newEntity, 20, materialId);

                // Set the tile offset
                watch(newEntity, 21, offset);

                // Finalize custom display tile
                watch(newEntity, 22, (byte)1);
                */

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

    @SuppressWarnings("unchecked")
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

    public static void damage(Damageable target, double amount, Entity source) {
        if (target == null || target.isDead()) return;
        while (target instanceof ComplexEntityPart) {
            target = ((ComplexEntityPart)target).getParent();
        }
        if (USE_MAGIC_DAMAGE && target.getType() == EntityType.ENDER_DRAGON) {
            magicDamage(target, amount, source);
            return;
        }

        try (AutoCloseable b = DAMAGING.enter()) {
            if (target instanceof ArmorStand) {
                double newHealth = Math.max(0, target.getHealth() - amount);
                if (newHealth <= 0) {
                    EntityDeathEvent deathEvent = new EntityDeathEvent((ArmorStand)target, new ArrayList<ItemStack>());
                    Bukkit.getPluginManager().callEvent(deathEvent);
                    target.remove();
                } else {
                    target.setHealth(newHealth);
                }
            } else {
                target.damage(amount, source);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void damage(Damageable target, double amount, Entity source, String damageType) {
        if (target == null || target.isDead()) return;
        if (damageType.equalsIgnoreCase("magic")) {
            magicDamage(target, amount, source);
            return;
        }
        Object damageSource = (damageSources == null) ? null : damageSources.get(damageType.toUpperCase());
        if (damageSource == null || class_EntityLiving_damageEntityMethod == null) {
            magicDamage(target, amount, source);
            return;
        }

        try (AutoCloseable d = DAMAGING.enter()) {
            Object targetHandle = getHandle(target);
            if (targetHandle == null) return;

            class_EntityLiving_damageEntityMethod.invoke(targetHandle, damageSource, (float) amount);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void magicDamage(Damageable target, double amount, Entity source) {
        try {
            if (target == null || target.isDead()) return;

            if (class_EntityLiving_damageEntityMethod == null || object_magicSource == null || class_DamageSource_getMagicSourceMethod == null) {
                damage(target, amount, source);
                return;
            }

            // Special-case for witches .. witches are immune to magic damage :\
            // And endermen are immune to indirect damage .. or something.
            // Also armor stands suck.
            // Might need to config-drive this, or just go back to defaulting to normal damage
            if (!USE_MAGIC_DAMAGE || target instanceof Witch || target instanceof Enderman || target instanceof ArmorStand || !(target instanceof LivingEntity))
            {
                damage(target, amount, source);
                return;
            }

            Object targetHandle = getHandle(target);
            if (targetHandle == null) return;

            Object sourceHandle = getHandle(source);

            // Bukkit won't allow magic damage from anything but a potion..
            if (sourceHandle != null && source instanceof LivingEntity) {
                Location location = target.getLocation();

                ThrownPotion potion = getOrCreatePotionEntity(location);
                potion.setShooter((LivingEntity)source);

                Object potionHandle = getHandle(potion);
                Object damageSource = class_DamageSource_getMagicSourceMethod.invoke(null, potionHandle, sourceHandle);

                // This is a bit of hack that lets us damage the ender dragon, who is a weird and annoying collection
                // of various non-living entity pieces.
                if (class_EntityDamageSource_setThornsMethod != null) {
                    class_EntityDamageSource_setThornsMethod.invoke(damageSource);
                }

                try (AutoCloseable b = DAMAGING.enter()) {
                    class_EntityLiving_damageEntityMethod.invoke(
                            targetHandle,
                            damageSource,
                            (float) amount);
                }
            } else {
                try (AutoCloseable b = DAMAGING.enter()) {
                    class_EntityLiving_damageEntityMethod.invoke(
                            targetHandle,
                            object_magicSource,
                            (float) amount);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static final Map<World, WeakReference<ThrownPotion>> POTION_PER_WORLD = new WeakHashMap<>();

    /**
     * Lazily creates potion entities that can be used when damaging players.
     *
     * @param location The location the potion should be placed at.
     * @return A potion entity placed ad the given location.
     */
    private static ThrownPotion getOrCreatePotionEntity(Location location) {
        World world = location.getWorld();

        // Maintain a separate potion entity for every world so that
        // potion.getWorld() reports the correct result.
        WeakReference<ThrownPotion> ref = POTION_PER_WORLD.get(world);
        ThrownPotion potion = ref == null ? null : ref.get();

        if (potion == null) {
            potion = (ThrownPotion) world.spawnEntity(
                    location,
                    EntityType.SPLASH_POTION);
            potion.remove();

            ref = new WeakReference<>(potion);
            POTION_PER_WORLD.put(world, ref);
        } else {
            // TODO: Make sure this actually works?
            potion.teleport(location);
        }

        return potion;
    }

    public static Location getEyeLocation(Entity entity)
    {
        if (entity instanceof LivingEntity)
        {
            return ((LivingEntity)entity).getEyeLocation();
        }

        return entity.getLocation();
    }

    public static ConfigurationSection loadConfiguration(String fileName) throws IOException, InvalidConfigurationException
    {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(fileName);
        } catch (FileNotFoundException fileNotFound) {

        }
        return configuration;
    }

    public static YamlConfiguration loadConfiguration(InputStream stream) throws IOException, InvalidConfigurationException
    {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(new InputStreamReader(stream, "UTF-8"));
        } catch (FileNotFoundException fileNotFound) {

        }
        return configuration;
    }

    public static ConfigurationSection loadConfiguration(File file) throws IOException, InvalidConfigurationException
    {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(file);
        } catch (FileNotFoundException fileNotFound) {

        }
        return configuration;
    }

    public static void setTNTSource(TNTPrimed tnt, LivingEntity source)
    {
        try {
            Object tntHandle = getHandle(tnt);
            Object sourceHandle = getHandle(source);
            class_EntityTNTPrimed_source.set(tntHandle, sourceHandle);
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.WARNING, "Unable to set TNT source", ex);
        }
    }

    public static void setEntityMotion(Entity entity, Vector motion) {
        try {
            Object handle = getHandle(entity);
            if (class_Entity_motField != null) {
                Object vec = class_Vec3D_constructor.newInstance(motion.getX(), motion.getY(), motion.getZ());
                class_Entity_motField.set(handle, vec);
            } else if (class_Entity_motXField != null) {
                class_Entity_motXField.set(handle, motion.getX());
                class_Entity_motYField.set(handle, motion.getY());
                class_Entity_motZField.set(handle, motion.getZ());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Vector getNormal(Block block, Location intersection)
    {
        double x = intersection.getX() - (block.getX() + 0.5);
        double y = intersection.getY() - (block.getY() + 0.5);
        double z = intersection.getZ() - (block.getZ() + 0.5);
        double ax = Math.abs(x);
        double ay = Math.abs(y);
        double az = Math.abs(z);
        if (ax > ay && ax > az) {
            return new Vector(Math.signum(x), 0, 0);
        } else if (ay > ax && ay > az) {
            return new Vector(0, Math.signum(y), 0);
        }

        return new Vector(0, 0, Math.signum(z));
    }

    public static boolean setLock(Block block, String lockName)
    {
        if (class_ChestLock_Constructor == null) return false;
        if (class_TileEntityContainer_setLock == null && class_TileEntityContainer_lock == null) return false;
        Object tileEntity = getTileEntity(block.getLocation());
        if (tileEntity == null) return false;
        if (!class_TileEntityContainer.isInstance(tileEntity)) return false;
        try {
            Object lock = class_ChestLock_Constructor.newInstance(lockName);
            if (class_TileEntityContainer_lock != null) {
                class_TileEntityContainer_lock.set(tileEntity, lock);
            } else {
                class_TileEntityContainer_setLock.invoke(tileEntity, lock);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean clearLock(Block block)
    {
        if (class_TileEntityContainer_setLock == null && class_TileEntityContainer_lock == null) return false;
        Object tileEntity = getTileEntity(block.getLocation());
        if (tileEntity == null) return false;
        if (!class_TileEntityContainer.isInstance(tileEntity)) return false;
        try {
            if (class_TileEntityContainer_lock != null) {
                class_TileEntityContainer_lock.set(tileEntity, null);
            } else {
                class_TileEntityContainer_setLock.invoke(tileEntity, new Object[] {null});
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean isLocked(Block block)
    {
        if (class_TileEntityContainer_getLock == null && class_TileEntityContainer_lock == null) return false;
        Object tileEntity = getTileEntity(block.getLocation());
        if (tileEntity == null) return false;
        if (!class_TileEntityContainer.isInstance(tileEntity)) return false;
        try {
            Object lock = class_TileEntityContainer_lock != null ? class_TileEntityContainer_lock.get(tileEntity) :
                class_TileEntityContainer_getLock.invoke(tileEntity);
            if (lock == null) return false;
            String key = class_ChestLock_key != null ? (String)class_ChestLock_key.get(lock) :
                (String)class_ChestLock_getString.invoke(lock);
            return key != null && !key.isEmpty();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static String getLock(Block block)
    {
        if (class_ChestLock_getString == null && class_ChestLock_key == null) return null;
        if (class_TileEntityContainer_getLock == null && class_TileEntityContainer_lock == null) return null;
        Object tileEntity = getTileEntity(block.getLocation());
        if (tileEntity == null) return null;
        if (!class_TileEntityContainer.isInstance(tileEntity)) return null;
        try {
            Object lock = class_TileEntityContainer_lock != null ? class_TileEntityContainer_lock.get(tileEntity) :
                class_TileEntityContainer_getLock.invoke(tileEntity);
            if (lock == null) return null;
            return class_ChestLock_key != null ? (String)class_ChestLock_key.get(lock) :
                (String)class_ChestLock_getString.invoke(lock);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void setFallingBlockDamage(FallingBlock entity, float fallHurtAmount, int fallHurtMax)
    {
        Object entityHandle = getHandle(entity);
        if (entityHandle == null) return;
        try {
            class_EntityFallingBlock_hurtEntitiesField.set(entityHandle, true);
            class_EntityFallingBlock_fallHurtAmountField.set(entityHandle, fallHurtAmount);
            class_EntityFallingBlock_fallHurtMaxField.set(entityHandle, fallHurtMax);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void configureMaxHeights(ConfigurationSection config) {
        maxHeights.clear();
        Collection<String> keys = config.getKeys(false);
        for (String key : keys) {
            try {
                World.Environment worldType = World.Environment.valueOf(key.toUpperCase());
                maxHeights.put(worldType, config.getInt(key));
            } catch (Exception ex) {
                org.bukkit.Bukkit.getLogger().log(Level.WARNING, "Invalid environment type: " + key, ex);
            }
        }
    }

    public static int getMaxHeight(World world) {
        Integer maxHeight = maxHeights.get(world.getEnvironment());
        if (maxHeight == null) {
            maxHeight = world.getMaxHeight();
        }
        return maxHeight;
    }

    public static void setInvisible(ArmorStand armorStand, boolean invisible) {
        try {
            Object handle = getHandle(armorStand);
            class_ArmorStand_setInvisible.invoke(handle, invisible);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setGravity(ArmorStand armorStand, boolean gravity) {
        if (class_Entity_setNoGravity == null && class_ArmorStand_setGravity == null) return;
        try {
            Object handle = getHandle(armorStand);
            if (class_Entity_setNoGravity != null) {
                class_Entity_setNoGravity.invoke(handle, !gravity);
            } else {
                class_ArmorStand_setGravity.invoke(handle, gravity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setGravity(Entity entity, boolean gravity) {
        if (class_Entity_setNoGravity == null) return;
        try {
            Object handle = getHandle(entity);
            class_Entity_setNoGravity.invoke(handle, !gravity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setDisabledSlots(ArmorStand armorStand, int disabledSlots) {
        if (class_EntityArmorStand_disabledSlotsField == null) return;
        try {
            Object handle = getHandle(armorStand);
            class_EntityArmorStand_disabledSlotsField.set(handle, disabledSlots);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static int getDisabledSlots(ArmorStand armorStand) {
        if (class_EntityArmorStand_disabledSlotsField == null) return 0;
        try {
            Object handle = getHandle(armorStand);
            return (int)class_EntityArmorStand_disabledSlotsField.get(handle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public static void setYawPitch(Entity entity, float yaw, float pitch) {
        try {
            Object handle = getHandle(entity);
            class_Entity_setYawPitchMethod.invoke(handle, yaw, pitch);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setLocation(Entity entity, double x, double y, double z, float yaw, float pitch) {
        try {
            Object handle = getHandle(entity);
            class_Entity_setLocationMethod.invoke(handle, x, y, z, yaw, pitch);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void addFlightExemption(Player player, int ticks) {
        if (class_PlayerConnection_floatCountField == null) return;
        try {
            Object handle = getHandle(player);
            Object connection = class_EntityPlayer_playerConnectionField.get(handle);
            class_PlayerConnection_floatCountField.set(connection, -ticks);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean isValidProjectileClass(Class<?> projectileType) {
        return projectileType != null
                && (class_EntityArrow.isAssignableFrom(projectileType)
                || class_EntityProjectile.isAssignableFrom(projectileType)
                || class_EntityFireball.isAssignableFrom(projectileType));
    }

    public static Projectile spawnProjectile(Class<?> projectileType, Location location, Vector direction, ProjectileSource source, float speed, float spread, float spreadLocations, Random random) {
        Constructor<? extends Object> constructor = null;
        Method shootMethod = null;
        Method setPositionRotationMethod = null;
        Field projectileSourceField = null;
        Field dirXField = null;
        Field dirYField = null;
        Field dirZField = null;
        Object nmsWorld = getHandle(location.getWorld());
        Projectile projectile = null;
        try {
            Object entityType = null;
            if (entityTypes != null) {
                constructor = projectileType.getConstructor(class_entityTypes, class_World);
                entityType = entityTypes.get(projectileType.getSimpleName());
                if (entityType == null) {
                    throw new Exception("Failed to find entity type for projectile class " + projectileType.getName());
                }
            } else {
                constructor = projectileType.getConstructor(class_World);
            }

            if (class_EntityFireball.isAssignableFrom(projectileType)) {
                dirXField = projectileType.getField("dirX");
                dirYField = projectileType.getField("dirY");
                dirZField = projectileType.getField("dirZ");
            }

            if (class_EntityProjectile.isAssignableFrom(projectileType) || class_EntityArrow.isAssignableFrom(projectileType)) {
                shootMethod = projectileType.getMethod("shoot", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);
            }

            setPositionRotationMethod = projectileType.getMethod("setPositionRotation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);
            projectileSourceField = projectileType.getField("projectileSource");

            Object nmsProjectile = null;
            try {
                nmsProjectile = entityType == null ? constructor.newInstance(nmsWorld) : constructor.newInstance(entityType, nmsWorld);
            } catch (Exception ex) {
                nmsProjectile = null;
                Bukkit.getLogger().log(Level.WARNING, "Error spawning projectile of class " + projectileType.getName(), ex);
            }

            if (nmsProjectile == null) {
                throw new Exception("Failed to spawn projectile of class " + projectileType.getName());
            }

            // Set position and rotation, and potentially velocity (direction)
            // Velocity must be set manually- EntityFireball.setDirection applies a crazy-wide gaussian distribution!
            if (dirXField != null && dirYField != null && dirZField != null) {
                // Taken from EntityArrow
                double spreadWeight = Math.min(0.4f,  spread * 0.007499999832361937D);

                double dx = speed * (direction.getX() + (random.nextGaussian() * spreadWeight));
                double dy = speed * (direction.getY() + (random.nextGaussian() * spreadWeight));
                double dz = speed * (direction.getZ() + (random.nextGaussian() * spreadWeight));

                dirXField.set(nmsProjectile, dx * 0.1D);
                dirYField.set(nmsProjectile, dy * 0.1D);
                dirZField.set(nmsProjectile, dz * 0.1D);
            }
            Vector modifiedLocation = location.toVector().clone();
            if (class_EntityFireball.isAssignableFrom(projectileType) && spreadLocations > 0) {
                modifiedLocation.setX(modifiedLocation.getX() + direction.getX() + (random.nextGaussian() * spread / 5));
                modifiedLocation.setY(modifiedLocation.getY() + direction.getY() + (random.nextGaussian() * spread / 5));
                modifiedLocation.setZ(modifiedLocation.getZ() + direction.getZ() + (random.nextGaussian() * spread / 5));
            }
            setPositionRotationMethod.invoke(nmsProjectile, modifiedLocation.getX(), modifiedLocation.getY(), modifiedLocation.getZ(), location.getYaw(), location.getPitch());

            if (shootMethod != null) {
                shootMethod.invoke(nmsProjectile, direction.getX(), direction.getY(), direction.getZ(), speed, spread);
            }

            Entity entity = NMSUtils.getBukkitEntity(nmsProjectile);
            if (entity == null || !(entity instanceof Projectile)) {
                throw new Exception("Got invalid bukkit entity from projectile of class " + projectileType.getName());
            }

            projectile = (Projectile)entity;
            if (source != null) {
                projectile.setShooter(source);
                projectileSourceField.set(nmsProjectile, source);
            }

            class_World_addEntityMethod.invoke(nmsWorld, nmsProjectile, CreatureSpawnEvent.SpawnReason.DEFAULT);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }

        return projectile;
    }

    public static void setDamage(Projectile projectile, double damage) {
        if (class_EntityArrow_damageField == null) return;
        try {
            Object handle = getHandle(projectile);
            class_EntityArrow_damageField.set(handle, damage);
        } catch (Exception ex) {
            ex.printStackTrace();;
        }
    }

    public static void decreaseLifespan(Projectile projectile, int ticks) {
        if (class_EntityArrow_lifeField == null) return;
        try {
            Object handle = getHandle(projectile);
            int currentLife = (Integer) class_EntityArrow_lifeField.get(handle);
            if (currentLife < ticks) {
                class_EntityArrow_lifeField.set(handle, ticks);
            }
        } catch (Exception ex) {
            ex.printStackTrace();;
        }
    }

    public static Entity spawnEntity(Location target, EntityType entityType, CreatureSpawnEvent.SpawnReason spawnReason)
    {
        if (class_CraftWorld_spawnMethod == null) {
            return target.getWorld().spawnEntity(target, entityType);
        }
        Entity entity = null;
        try {
            World world = target.getWorld();
            try {
                if (!class_CraftWorld_spawnMethod_isLegacy) {
                    entity = (Entity)class_CraftWorld_spawnMethod.invoke(world, target, entityType.getEntityClass(), null, spawnReason);
                } else {
                    entity = (Entity)class_CraftWorld_spawnMethod.invoke(world, target, entityType.getEntityClass(), spawnReason);
                }
            } catch (Exception ex) {
                entity = target.getWorld().spawnEntity(target, entityType);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return entity;
    }
    
    public static String getResourcePack(Server server) {
        String rp = null;
        try {
            Object minecraftServer = getHandle(server);
            if (minecraftServer != null) {
                rp = (String)class_MinecraftServer_getResourcePackMethod.invoke(minecraftServer);   
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rp;
    }

    public static boolean setResourcePack(Player player, String rp, byte[] hash) {
        // TODO: Player.setResourcePack in 1.11+
        try {
            String hashString = BaseEncoding.base16().lowerCase().encode(hash);
            class_EntityPlayer_setResourcePackMethod.invoke(getHandle(player), rp, hashString);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }


    // Taken from CraftBukkit code.
    private static String toMinecraftAttribute(Attribute attribute) {
        String bukkit = attribute.name();
        int first = bukkit.indexOf('_');
        int second = bukkit.indexOf('_', first + 1);

        StringBuilder sb = new StringBuilder(bukkit.toLowerCase(java.util.Locale.ENGLISH));

        sb.setCharAt(first, '.');
        if (second != -1) {
            sb.deleteCharAt(second);
            sb.setCharAt(second, bukkit.charAt(second + 1));
        }

        return sb.toString();
    }

    public static boolean removeItemAttribute(ItemStack item, Attribute attribute) {
        try {
            Object handle = getHandle(item);
            if (handle == null) return false;
            Object tag = getTag(handle);
            if (tag == null) return false;

            String attributeName = toMinecraftAttribute(attribute);
            Object attributesNode = getNode(tag, "AttributeModifiers");
            if (attributesNode == null) {
                return false;
            }
            int size = (Integer)class_NBTTagList_sizeMethod.invoke(attributesNode);
            for (int i = 0; i < size; i++) {
                Object candidate = class_NBTTagList_getMethod.invoke(attributesNode, i);
                String key = getMetaString(candidate, "AttributeName");
                if (key.equals(attributeName)) {
                    if (size == 1) {
                        removeMeta(tag, "AttributeModifiers");
                    } else {
                        class_NBTTagList_removeMethod.invoke(attributesNode, i);
                    }
                    return true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean setItemAttribute(ItemStack item, Attribute attribute, double value, String slot) {
        return setItemAttribute(item, attribute, value, slot, 0);
    }
    
    public static boolean setItemAttribute(ItemStack item, Attribute attribute, double value, String slot, int attributeOperation) {
        try {
            Object handle = getHandle(item);
            if (handle == null) {
                return false;
            }
            Object tag = getTag(handle);
            if (tag == null) return false;
            
            Object attributesNode = getNode(tag, "AttributeModifiers");
            Object attributeNode = null;
            UUID attributeUUID = UUID.randomUUID();

            String attributeName = toMinecraftAttribute(attribute);
            if (attributesNode == null) {
                attributesNode = class_NBTTagList_constructor.newInstance();
                class_NBTTagCompound_setMethod.invoke(tag, "AttributeModifiers", attributesNode);
            } else {
                int size = (Integer)class_NBTTagList_sizeMethod.invoke(attributesNode);
                for (int i = 0; i < size; i++) {
                    Object candidate = class_NBTTagList_getMethod.invoke(attributesNode, i);
                    String key = getMetaString(candidate, "AttributeName");
                    if (key.equals(attributeName)) {
                        attributeNode = candidate;
                        break;
                    }
                }
            }
            if (attributeNode == null) {
                attributeNode = class_NBTTagCompound_constructor.newInstance();
                setMeta(attributeNode, "AttributeName", attributeName);
                setMeta(attributeNode, "Name", "Equipment Modifier");
                setMetaInt(attributeNode, "Operation", attributeOperation);
                setMetaLong(attributeNode, "UUIDMost", attributeUUID.getMostSignificantBits());
                setMetaLong(attributeNode, "UUIDLeast", attributeUUID.getLeastSignificantBits());
                if (slot != null) {
                    setMeta(attributeNode, "Slot", slot);
                }

                addToList(attributesNode, attributeNode);
            }
            setMetaDouble(attributeNode, "Amount", value);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    
    public static void sendExperienceUpdate(Player player, float experience, int level) {
        try {
            Object packet = class_PacketPlayOutExperience_Constructor.newInstance(experience, player.getTotalExperience(), level);
            sendPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Object getEntityData(Entity entity) {
        if (class_Entity_saveMethod == null) return null;
        
        Object data = null;
        try {
            Object nmsEntity = getHandle(entity);
            if (nmsEntity != null) {
                data = class_NBTTagCompound_constructor.newInstance();
                class_Entity_saveMethod.invoke(nmsEntity, data);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return data;
    }
    
    public static String getEntityType(Entity entity) {
        if (class_Entity_getTypeMethod == null) return null;
        String entityType = null;
        try {
            Object nmsEntity = getHandle(entity);
            if (nmsEntity != null) {
                entityType = (String)class_Entity_getTypeMethod.invoke(nmsEntity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return entityType;
    }
    
    public static void applyItemData(ItemStack item, Block block) {
        try {
            Object entityDataTag = getNode(item, "BlockEntityTag");
            if (entityDataTag == null) return;
            NMSUtils.setTileEntityData(block.getLocation(), entityDataTag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void swingOffhand(Entity entity, int range) {
        int rangeSquared = range * range;
        String worldName = entity.getWorld().getName();
        Location center = entity.getLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getName().equals(worldName) || player.getLocation().distanceSquared(center) > rangeSquared) {
                continue;
            }
            swingOffhand(player, entity);
        }
    }
    
    public static void swingOffhand(Player sendToPlayer, Entity entity) {
        try {
            Object packet = class_PacketPlayOutAnimation_Constructor.newInstance(getHandle(entity), 3);
            sendPacket(sendToPlayer, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public static void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        // TODO: New Player.sendTitle in 1.11
        player.sendTitle(title, subTitle);
    }

    public static boolean sendActionBar(Player player, String message) {
        if (class_PacketPlayOutChat == null) return false;
        try {
            Object chatComponent = class_ChatComponentText_constructor.newInstance(message);
            Object packet;
            if (enum_ChatMessageType_GAME_INFO == null) {
                packet = class_PacketPlayOutChat_constructor.newInstance(chatComponent, (byte)2);
            } else {
                packet = class_PacketPlayOutChat_constructor.newInstance(chatComponent, enum_ChatMessageType_GAME_INFO);
            }
            sendPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public static float getDurability(Material material) {
        if (class_Block_durabilityField == null || class_CraftMagicNumbers_getBlockMethod == null) return 0.0f;
        try {
            Object block = class_CraftMagicNumbers_getBlockMethod.invoke(null, material);
            if (block == null) {
                return 0.0f;
            }
            return (float)class_Block_durabilityField.get(block);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0.0f;
    }

    private static void sendBreaking(Player player, long id, Location location, int breakAmount) {
        try {
            Object blockPosition = class_BlockPosition_Constructor.newInstance(location.getX(), location.getY(), location.getZ());
            Object packet = class_PacketPlayOutBlockBreakAnimation_Constructor.newInstance((int)id, blockPosition, breakAmount);
            sendPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static int getBlockEntityId(Block block) {
        // There will be some overlap here, but these effects are very localized so it should be OK.
        return   ((block.getX() & 0xFFF) << 20)
               | ((block.getZ() & 0xFFF) << 8)
               | (block.getY() & 0xFF);
    }

    public static void clearBreaking(Block block) {
        setBreaking(block, 10, BLOCK_BREAK_RANGE);
    }

    public static void setBreaking(Block block, double percentage) {
        // Block break states are 0 - 9
        int breakState = (int)Math.ceil(9 * percentage);
        setBreaking(block, breakState, BLOCK_BREAK_RANGE);
    }

    public static void setBreaking(Block block, int breakAmount) {
        setBreaking(block, breakAmount, BLOCK_BREAK_RANGE);
    }

    public static void setBreaking(Block block, int breakAmount, int range) {
        String worldName = block.getWorld().getName();
        Location location = block.getLocation();
        int rangeSquared = range * range;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getName().equals(worldName) || player.getLocation().distanceSquared(location) > rangeSquared) {
                continue;
            }
            sendBreaking(player, getBlockEntityId(block), location, breakAmount);
        }
    }

    public static Set<String> getTags(Entity entity) {
        // TODO: Use Entity.getScoreboardTags in a future version.
        return null;
    }

    public static boolean isJumping(LivingEntity entity) {
        if (class_Entity_jumpingField == null) return false;
        try {
            return (boolean)class_Entity_jumpingField.get(getHandle(entity));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static float getForwardMovement(LivingEntity entity) {
        if (class_Entity_moveForwardField == null) return 0.0f;
        try {
            return (float)class_Entity_moveForwardField.get(getHandle(entity));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0.0f;
    }

    public static float getStrafeMovement(LivingEntity entity) {
        if (class_Entity_moveStrafingField == null) return 0.0f;
        try {
            return (float)class_Entity_moveStrafingField.get(getHandle(entity));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0.0f;
    }

    public static boolean setBlockFast(Block block, Material material, int data) {
        return setBlockFast(block.getChunk(), block.getX(), block.getY(), block.getZ(), material, data);
    }

    public static boolean setBlockFast(Chunk chunk, int x, int y, int z, Material material, int data) {
        if (class_Block_fromLegacyData == null || class_CraftMagicNumbers_getBlockMethod == null || class_Chunk_setBlockMethod == null || class_BlockPosition_Constructor == null) {
            DeprecatedUtils.setTypeAndData(chunk.getWorld().getBlockAt(x, y, z), material, (byte)data, false);
            return true;
        }
        try {
            Object chunkHandle = getHandle(chunk);
            Object nmsBlock = class_CraftMagicNumbers_getBlockMethod.invoke(null, material);
            nmsBlock = class_Block_fromLegacyData.invoke(nmsBlock, data);
            Object blockLocation = class_BlockPosition_Constructor.newInstance(x, y, z);
            class_Chunk_setBlockMethod.invoke(chunkHandle, blockLocation, nmsBlock);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean setPickupStatus(Arrow arrow, String pickupStatus) {
        if (arrow == null || pickupStatus == null || class_Arrow_setPickupStatusMethod == null || class_PickupStatus == null) return false;

        try {
            Enum enumValue = Enum.valueOf(class_PickupStatus, pickupStatus.toUpperCase());
            class_Arrow_setPickupStatusMethod.invoke(arrow, enumValue);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public static Block getHitBlock(ProjectileHitEvent event) {
        if (class_ProjectileHitEvent_getHitBlockMethod == null) return null;
        try {
            return (Block) class_ProjectileHitEvent_getHitBlockMethod.invoke(event);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Entity getEntity(World world, UUID uuid) {
        try {
            Object worldHandle = getHandle(world);
            final Map<UUID, Entity> entityMap = (Map<UUID, Entity>)class_WorldServer_entitiesByUUIDField.get(worldHandle);
            if (entityMap != null) {
                Object nmsEntity = entityMap.get(uuid);
                if (nmsEntity != null) {
                    return getBukkitEntity(nmsEntity);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Entity getEntity(UUID uuid) {
        if (class_Server_getEntityMethod != null) {
            try {
                return (Entity)class_Server_getEntityMethod.invoke(Bukkit.getServer(), uuid);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        for (World world : Bukkit.getWorlds()) {
            Entity found = getEntity(world, uuid);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    public static ShapedRecipe createShapedRecipe(Plugin plugin, String key, ItemStack item) {
        if (class_NamespacedKey == null) {
            return new ShapedRecipe(item);
        }

        try {
            Object namespacedKey = class_NamespacedKey_constructor.newInstance(plugin, key.toLowerCase());
            return (ShapedRecipe)class_ShapedRecipe_constructor.newInstance(namespacedKey, item);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ShapedRecipe(item);
        }
    }

    public static double getMaxHealth(Damageable li) {
        // return li.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        return li.getMaxHealth();
    }

    public static void setMaxHealth(Damageable li, double maxHealth) {
        // li.getAttribute(Attribute.GENERIC_MAX_HEALTH).setValue(maxHealth);
        li.setMaxHealth(maxHealth);
    }

    @SuppressWarnings("deprecation")
    public static Material fromLegacy(org.bukkit.material.MaterialData materialData) {
        if (class_UnsafeValues_fromLegacyDataMethod != null) {
            try {
                Material converted = (Material)class_UnsafeValues_fromLegacyDataMethod.invoke(DeprecatedUtils.getUnsafe(), materialData);
                if (converted == Material.AIR) {
                    materialData.setData((byte)0);
                    converted = (Material)class_UnsafeValues_fromLegacyDataMethod.invoke(DeprecatedUtils.getUnsafe(), materialData);
                }
                // Converting legacy signs doesn't seem to work
                // This fixes them, but the direction is wrong, and restoring text causes internal errors
                // So I guess it's best to just let signs be broken for now.
                /*
                if (converted == Material.AIR) {
                    String typeKey = materialData.getItemType().name();
                    if (typeKey.equals("LEGACY_WALL_SIGN")) return Material.WALL_SIGN;
                    if (typeKey.equals("LEGACY_SIGN_POST")) return Material.SIGN_POST;
                    if (typeKey.equals("LEGACY_SIGN")) return Material.SIGN;
                }
                */
                return converted;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return materialData.getItemType();
    }

    @SuppressWarnings("deprecation")
    public static Material getMaterial(int id, byte data) {
        Material material = getMaterial(id);
        if (class_UnsafeValues_fromLegacyDataMethod != null) {
            if (material != null) {
                material = fromLegacy(new org.bukkit.material.MaterialData(material, data));
            }
        }
        if (material == null) {
            material = Material.AIR;
        }
        return material;
    }

    @SuppressWarnings("deprecation")
    public static Material getMaterial(int id) {
        if (materialIdMap == null) {
            materialIdMap = new HashMap<>();

            Object[] allMaterials = Material.AIR.getDeclaringClass().getEnumConstants();
            for (Object o : allMaterials) {
                Material material = (Material)o;
                if (isLegacy(material)) {
                    materialIdMap.put(material.getId(), material);
                }
            }
        }
        return materialIdMap.get(id);
    }

    public static boolean isLegacy(Material material) {
        if (class_Material_isLegacyMethod == null) {
            return false;
        }
        try {
            return (boolean)class_Material_isLegacyMethod.invoke(material);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static Material getLegacyMaterial(String materialName) {
        if (class_Material_getLegacyMethod != null) {
            try {
                return (Material)class_Material_getLegacyMethod.invoke(null, materialName, true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return Material.getMaterial(materialName);
    }

    @SuppressWarnings("deprecation")
    public static Material migrateMaterial(Material material, byte data) {
        return fromLegacy(new org.bukkit.material.MaterialData(material, data));
    }

    @SuppressWarnings("deprecation")
    public static String migrateMaterial(String materialKey) {
        if (materialKey == null || materialKey.isEmpty()) return materialKey;
        byte data = 0;
        String[] pieces = StringUtils.split(materialKey, ':');
        String textData = "";
        if (pieces.length > 1) {
            textData = pieces[1];
            try {
                data = Byte.parseByte(pieces[1]);
                textData = "";
            } catch (Exception ex) {
            }
        }

        String materialName = pieces[0].toUpperCase();
        Material material = Material.getMaterial(materialName);
        if (material != null && data == 0) {
            return material.name().toLowerCase();
        }

        Material legacyMaterial = getLegacyMaterial(materialName);
        if (legacyMaterial != null) {
            org.bukkit.material.MaterialData materialData = new org.bukkit.material.MaterialData(legacyMaterial, data);
            legacyMaterial = fromLegacy(materialData);
            if (legacyMaterial != null) {
                material = legacyMaterial;
            }
        }

        if (material != null) {
            materialKey = material.name().toLowerCase();;
            // This mainly covers player skulls, but .. maybe other things? Maps?
            if (!textData.isEmpty()) {
                materialKey += ":" + textData;
            }
        }
        return materialKey;
    }

    public static Enum<?> getParrotVariant(Entity entity) {
        if (class_Parrot == null) return null;
        Enum<?> variant = null;
        try {
            variant = (Enum<?>)class_Parrot_getVariantMethod.invoke(entity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return variant;
    }

    @SuppressWarnings("unchecked")
    public static Enum<?> getParrotVariant(String variantName) {
        if (class_ParrotVariant == null) return null;
        Enum<?> variant = null;
        try {
            variant = Enum.valueOf(class_ParrotVariant, variantName.toUpperCase());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return variant;
    }

    public static boolean setParrotVariant(Entity entity, Enum<?> variant) {
        if (class_Parrot == null || variant == null) return false;
        try {
            class_Parrot_setVariantMethod.invoke(entity, variant);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean checkSingleChunk(Chunk chunk, boolean load) {
        if (!chunk.isLoaded()) {
            if (load) {
                chunk.load();
            }
            return false;
        }
        return isReady(chunk);
    }

    public static boolean checkChunk(Chunk chunk) {
        return checkChunk(chunk, true, true);
    }

    public static boolean checkChunk(Chunk chunk, boolean load, boolean checkNeighbors) {
        if (!checkSingleChunk(chunk, load)) {
            return false;
        }

        if (!checkNeighbors) {
            return true;
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                // Already checked this one
                if (x == 0 && z == 0) continue;

                Chunk checkChunk = chunk.getWorld().getChunkAt(chunk.getX() + x, chunk.getZ() + z);
                if (!checkSingleChunk(checkChunk, load)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean applyBonemeal(Location location) {
        if (class_ItemDye_bonemealMethod == null) return false;

        if (dummyItem == null) {
             dummyItem = new ItemStack(Material.DIRT, 64);
             dummyItem = makeReal(dummyItem);
        }
        dummyItem.setAmount(64);

        try {
            Object world = getHandle(location.getWorld());
            Object itemStack = getHandle(dummyItem);
            Object blockPosition = class_BlockPosition_Constructor.newInstance(location.getX(), location.getY(), location.getZ());
            Object result = class_ItemDye_bonemealMethod.invoke(null, itemStack, world, blockPosition);
            return (Boolean)result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static int[] getServerVersion() {
        String versionString = getVersionPrefix();
        int[] version = new int[2];
        // Format:  v1_12_R1
        versionString = versionString.substring(1);
        try {
            String[] pieces = StringUtils.split(versionString, '_');
            if (pieces.length > 0) {
                version[0] = Integer.parseInt(pieces[0]);
            }
            if (pieces.length > 1) {
                version[1] = Integer.parseInt(pieces[1]);
            }
        } catch (Exception ex) {

        }
        return version;
    }

    public static Color getColor(PotionMeta meta) {
        Color color = Color.BLACK;
        if (class_PotionMeta_getColorMethod != null) {
            try {
                color = (Color)class_PotionMeta_getColorMethod.invoke(meta);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return color;
    }

    public static boolean setColor(PotionMeta meta, Color color) {
        if (class_PotionMeta_setColorMethod != null) {
            try {
                class_PotionMeta_setColorMethod.invoke(meta, color);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public static String getBlockData(Material material, byte data) {
        if (class_UnsafeValues_fromLegacyMethod == null) return null;
        try {
            Object blockData = class_UnsafeValues_fromLegacyMethod.invoke(DeprecatedUtils.getUnsafe(), material, data);
            if (blockData != null) {
                return (String)class_BlockData_getAsStringMethod.invoke(blockData);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getBlockData(Block block) {
        if (class_Block_getBlockDataMethod == null) return null;
        try {
            Object blockData = class_Block_getBlockDataMethod.invoke(block);
            if (blockData == null) {
                return null;
            }
            return (String)class_BlockData_getAsStringMethod.invoke(blockData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean setBlockData(Server server, Block block, String data) {
        if (class_Block_getBlockDataMethod == null) return false;
        try {
            Object blockData = class_Server_createBlockDataMethod.invoke(server, data);
            class_Block_setBlockDataMethod.invoke(block, blockData);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean applyPhysics(Block block) {
        if (class_World_setTypeAndDataMethod == null || class_World_getTypeMethod == null || class_BlockPosition_Constructor == null) return false;
        try {
            Object worldHandle = getHandle(block.getWorld());
            Object blockLocation = class_BlockPosition_Constructor.newInstance(block.getX(), block.getY(), block.getZ());
            Object blockType = class_World_getTypeMethod.invoke(worldHandle, blockLocation);
            clearItems(block.getLocation());
            DeprecatedUtils.setTypeAndData(block, Material.AIR, (byte)0, false);
            return (boolean)class_World_setTypeAndDataMethod.invoke(worldHandle, blockLocation, blockType, 3);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static ItemStack getKnowledgeBook() {
        ItemStack book = null;
        try {
            Material bookMaterial = Material.valueOf("KNOWLEDGE_BOOK");
            book = new ItemStack(bookMaterial);
        } catch (Exception ignore) {

        }
        return book;
    }

    public static boolean addRecipeToBook(ItemStack book, Plugin plugin, String recipeKey) {
        if (class_NamespacedKey_constructor == null || class_KnowledgeBookMeta_addRecipeMethod == null) return false;
        ItemMeta meta = book.getItemMeta();
        if (!class_KnowledgeBookMeta.isAssignableFrom(meta.getClass())) return false;
        try {
            Object namespacedKey = class_NamespacedKey_constructor.newInstance(plugin, recipeKey.toLowerCase());
            Object array = Array.newInstance(class_NamespacedKey, 1);
            Array.set(array, 0, namespacedKey);
            class_KnowledgeBookMeta_addRecipeMethod.invoke(meta, array);
            book.setItemMeta(meta);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }
}
