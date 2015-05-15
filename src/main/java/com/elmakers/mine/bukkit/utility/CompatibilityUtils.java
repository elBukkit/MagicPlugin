package com.elmakers.mine.bukkit.utility;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Rotation;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
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
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Witch;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    public final static int MAX_ENTITY_RANGE = 72;
    private final static Map<EntityType, BoundingBox> hitboxes = new HashMap<EntityType, BoundingBox>();
    private final static Map<World.Environment, Integer> maxHeights = new HashMap<World.Environment, Integer>();
    private static double hitboxScale = 1.0;
    private static BoundingBox defaultHitbox;

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
            String shorterName = name;
            if (shorterName.length() > 32) {
                shorterName = shorterName.substring(0, 31);
            }
            inventory = (Inventory)class_CraftInventoryCustom_constructor.newInstance(holder, size, ChatColor.translateAlternateColorCodes('&', shorterName));
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

    public static Painting spawnPainting(Location location, BlockFace facing, Art art)
    {
        Painting newPainting = null;
        try {
            location = getPaintingOffset(location, facing, art);
            Object worldHandle = getHandle(location.getWorld());
            Object newEntity = null;
            Enum<?> directionEnum = Enum.valueOf(class_EnumDirection, facing.name());
            Object blockLocation = class_BlockPositionConstructor.newInstance(location.getX(), location.getY(), location.getZ());
            newEntity = class_EntityPaintingConstructor.newInstance(worldHandle, blockLocation, directionEnum);
            if (newEntity != null) {
                Entity bukkitEntity = getBukkitEntity(newEntity);
                if (bukkitEntity == null || !(bukkitEntity instanceof Painting)) return null;

                newPainting = (Painting)bukkitEntity;
                newPainting.setFacingDirection(facing, true);
                newPainting.setArt(art, true);
                class_World_addEntityMethod.invoke(worldHandle, newEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            newPainting = null;
        }
        return newPainting;
    }

    public static ItemFrame spawnItemFrame(Location location, BlockFace facing, Rotation rotation, ItemStack item)
    {
        ItemFrame newItemFrame = null;
        try {
            Object worldHandle = getHandle(location.getWorld());
            Object newEntity = null;
            Enum<?> directionEnum = Enum.valueOf(class_EnumDirection, facing.name());
            Object blockLocation = class_BlockPositionConstructor.newInstance(location.getX(), location.getY(), location.getZ());
            newEntity = class_EntityItemFrameConstructor.newInstance(worldHandle, blockLocation, directionEnum);
            if (newEntity != null) {
                Entity bukkitEntity = getBukkitEntity(newEntity);
                if (bukkitEntity == null || !(bukkitEntity instanceof ItemFrame)) return null;

                newItemFrame = (ItemFrame)bukkitEntity;
                newItemFrame.setItem(getCopy(item));
                newItemFrame.setFacingDirection(facing, true);
                newItemFrame.setRotation(rotation);

                // This will fail sometimes ... the entity is already tracked?
                try {
                    class_World_addEntityMethod.invoke(worldHandle, newEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                } catch (Exception ex) {
                    newItemFrame = null;
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
        if (location == null) return null;
        Object worldHandle = getHandle(location.getWorld());
        try {
            x = Math.min(x, CompatibilityUtils.MAX_ENTITY_RANGE);
            z = Math.min(z, CompatibilityUtils.MAX_ENTITY_RANGE);
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

    private static WeakReference<ThrownPotion> potionReference = null;

    public static void magicDamage(LivingEntity target, double amount, Entity source) {
        try {
            if (target == null || target.isDead()) return;

            // Special-case for witches .. witches are immune to magic damage :\
            // And endermen are immune to indirect damage .. or something.
            // Might need to config-drive this, or just go back to defaulting to normal damage
            if (!USE_MAGIC_DAMAGE || target instanceof Witch || target instanceof Enderman)
            {
                target.damage(amount, source);
                return;
            }

            Object targetHandle = getHandle(target);
            if (targetHandle == null) return;

            Object sourceHandle = getHandle(source);

            // Bukkit won't allow magic damage from anything but a potion..
            if (sourceHandle != null && source instanceof LivingEntity) {
                ThrownPotion potion = potionReference == null ? null : potionReference.get();
                if (potion == null) {
                    Location location = target.getLocation();
                    potion = (ThrownPotion) location.getWorld().spawnEntity(location, EntityType.SPLASH_POTION);
                    potion.remove();
                    potionReference = new WeakReference<ThrownPotion>(potion);
                }
                potion.setShooter((LivingEntity)source);
                Object potionHandle = getHandle(potion);
                Object damageSource = class_DamageSource_getMagicSourceMethod.invoke(null, potionHandle, sourceHandle);

                // This is a bit of hack that lets us damage the ender dragon, who is a weird and annoying collection
                // of various non-living entity pieces.
                class_EntityDamageSource_setThornsMethod.invoke(damageSource);

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

    public static Object getSkullProfile(Skull state)
    {
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

    public static ConfigurationSection loadConfiguration(String fileName) throws IOException, InvalidConfigurationException
    {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(fileName);
        } catch (FileNotFoundException fileNotFound) {

        }
        return configuration;
    }

    public static ConfigurationSection loadConfiguration(InputStream stream) throws IOException, InvalidConfigurationException
    {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(stream);
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
            class_Entity_motXField.set(handle, motion.getX());
            class_Entity_motYField.set(handle, motion.getY());
            class_Entity_motZField.set(handle, motion.getZ());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static BoundingBox getHitbox(Entity entity)
    {
        if (entity == null)
        {
            return null;
        }
        BoundingBox hitbox = hitboxes.get(entity.getType());
        if (hitbox != null)
        {
            return hitbox.center(entity.getLocation().toVector());
        }

        try {
            Object entityHandle = getHandle(entity);
            Object aabb = class_Entity_getBoundingBox.invoke(entityHandle);
            if (aabb == null) {
                return defaultHitbox.center(entity.getLocation().toVector());
            }
            return new BoundingBox(
                    class_AxisAlignedBB_minXField.getDouble(aabb),
                    class_AxisAlignedBB_maxXField.getDouble(aabb),
                    class_AxisAlignedBB_minYField.getDouble(aabb),
                    class_AxisAlignedBB_maxYField.getDouble(aabb),
                    class_AxisAlignedBB_minZField.getDouble(aabb),
                    class_AxisAlignedBB_maxZField.getDouble(aabb)
            ).scale(hitboxScale);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return defaultHitbox.center(entity.getLocation().toVector());
    }

    public static void setHitboxScale(double scale) {
        hitboxScale = scale;
    }

    public static void configureHitboxes(ConfigurationSection config) {
        hitboxes.clear();
        Collection<String> keys = config.getKeys(false);
        for (String key : keys) {
            try {
                Vector bounds = ConfigurationUtils.getVector(config, key);
                String upperKey = key.toUpperCase();
                double halfX = bounds.getX() / 2;
                double halfZ = bounds.getZ() / 2;
                BoundingBox bb = new BoundingBox(-halfX, halfX, 0, bounds.getY(), -halfZ, halfZ).scale(hitboxScale);
                if (upperKey.equals("DEFAULT")) {
                    defaultHitbox = bb;
                    continue;
                }
                EntityType entityType = EntityType.valueOf(upperKey);
                if (bounds != null && entityType != null)
                {
                    hitboxes.put(entityType, bb);
                }
            } catch (Exception ex) {
                org.bukkit.Bukkit.getLogger().log(Level.WARNING, "Invalid entity type: " + key, ex);
            }
        }
    }

    public static boolean setLock(Location location, String lockName)
    {
        Object tileEntity = getTileEntity(location);
        if (tileEntity == null) return false;
        try {
            Object lock = class_ChestLock_Constructor.newInstance(lockName);
            class_TileEntityContainer_setLock.invoke(tileEntity, lock);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean clearLock(Location location)
    {
        Object tileEntity = getTileEntity(location);
        if (tileEntity == null) return false;
        try {
            class_TileEntityContainer_setLock.invoke(tileEntity, new Object[] {null});
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean isLocked(Location location)
    {
        Object tileEntity = getTileEntity(location);
        if (tileEntity == null) return false;
        try {
            Object lock = class_TileEntityContainer_getLock.invoke(tileEntity);
            if (lock == null) return false;
            return !(Boolean)class_ChestLock_isEmpty.invoke(lock);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
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
                if (worldType != null)
                {
                    maxHeights.put(worldType, config.getInt(key));
                }
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

    public static void setMarker(ArmorStand armorStand, boolean marker) {
        try {
            Object handle = getHandle(armorStand);
            class_ArmorStand_setMarker.invoke(handle, marker);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setGravity(ArmorStand armorStand, boolean marker) {
        try {
            Object handle = getHandle(armorStand);
            class_ArmorStand_setGravity.invoke(handle, marker);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setSmall(ArmorStand armorStand, boolean marker) {
        try {
            Object handle = getHandle(armorStand);
            class_ArmorStand_setSmall.invoke(handle, marker);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
