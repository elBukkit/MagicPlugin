package com.elmakers.mine.bukkit.utility;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import org.bukkit.Art;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Painting;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
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
        /*
         * Sin = Opp / Hyp
         * Cos = Adj / Hyp
         * Tan = Opp / Adj
         *
         * x = -Opp
         * z = Adj
         */
        final double _2PI = 2 * Math.PI;
        final double x = vector.getX();
        final double z = vector.getZ();

        if (x == 0 && z == 0) {
            location.setPitch(vector.getY() > 0 ? -90 : 90);
            return location;
        }

        double theta = Math.atan2(-x, z);
        location.setYaw((float) Math.toDegrees((theta + _2PI) % _2PI));

        double x2 = NumberConversions.square(x);
        double z2 = NumberConversions.square(z);
        double xz = Math.sqrt(x2 + z2);
        location.setPitch((float) Math.toDegrees(Math.atan(-vector.getY() / xz)));

        return location;
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

    public static boolean hasMetadata(ItemStack itemStack, Plugin plugin, String key) {
        Object handle = getHandle(itemStack);
        if (handle == null) return false;
        Object tag = getTag(handle);
        if (tag == null) return false;

        Object bukkitRoot = getNode(tag, "bukkit");
        if (bukkitRoot == null) return false;
        Object pluginsRoot = getNode(bukkitRoot, "plugins");
        if (pluginsRoot == null) return false;
        String pluginName = plugin.getName();
        Object pluginRoot = getNode(pluginsRoot, pluginName);

        return pluginRoot != null && containsNode(pluginRoot, key);
    }

    public static String getMetadata(ItemStack itemStack, Plugin plugin, String key) {
        Object handle = getHandle(itemStack);
        if (handle == null) return null;
        Object tag = getTag(handle);
        if (tag == null) return null;

        Object bukkitRoot = getNode(tag, "bukkit");
        if (bukkitRoot == null) return null;
        Object pluginsRoot = getNode(bukkitRoot, "plugins");
        if (pluginsRoot == null) return null;
        String pluginName = plugin.getName();
        Object pluginRoot = getNode(pluginsRoot, pluginName);
        if (pluginRoot == null) return null;
        return getMeta(pluginRoot, key);
    }

    public static boolean setMetadata(ItemStack itemStack, Plugin plugin, String key, String value) {
        Object handle = getHandle(itemStack);
        if (handle == null) return false;
        Object tag = getTag(handle);
        if (tag == null) return false;

        Object bukkitRoot = createNode(tag, "bukkit");
        if (bukkitRoot == null) return false;
        Object pluginsRoot = createNode(bukkitRoot, "plugins");
        if (pluginsRoot == null) return false;
        String pluginName = plugin.getName();
        Object pluginRoot = createNode(pluginsRoot, pluginName);
        if (pluginRoot == null) return false;
        setMeta(pluginRoot, key, value);
        return true;
    }

    public static boolean removeMetadata(ItemStack itemStack, Plugin plugin, String key) {
        Object handle = getHandle(itemStack);
        if (handle == null) return false;
        Object tag = getTag(handle);
        if (tag == null) return false;

        Object bukkitRoot = createNode(tag, "bukkit");
        if (bukkitRoot == null) return false;
        Object pluginsRoot = createNode(bukkitRoot, "plugins");
        if (pluginsRoot == null) return false;
        String pluginName = plugin.getName();
        Object pluginRoot = createNode(pluginsRoot, pluginName);
        if (pluginRoot == null) return false;
        removeMeta(pluginRoot, key);
        return true;
    }

    public static Object getMetadataNode(ItemStack itemStack, Plugin plugin, String key) {
        Object handle = getHandle(itemStack);
        if (handle == null) return null;
        Object tag = getTag(handle);
        if (tag == null) return null;

        Object bukkitRoot = getNode(tag, "bukkit");
        if (bukkitRoot == null) return null;
        Object pluginsRoot = getNode(bukkitRoot, "plugins");
        if (pluginsRoot == null) return null;
        String pluginName = plugin.getName();
        Object pluginRoot = getNode(pluginsRoot, pluginName);
        return getNode(pluginRoot, key);
    }

    public static Object createMetadataNode(ItemStack itemStack, Plugin plugin, String key) {
        Object handle = getHandle(itemStack);
        if (handle == null) return null;
        Object tag = getTag(handle);
        if (tag == null) return null;

        Object bukkitRoot = createNode(tag, "bukkit");
        if (bukkitRoot == null) return null;
        Object pluginsRoot = createNode(bukkitRoot, "plugins");
        if (pluginsRoot == null) return null;
        String pluginName = plugin.getName();
        Object pluginRoot = createNode(pluginsRoot, pluginName);
        return createNode(pluginRoot, key);
    }

    public static void removeCustomData(ItemStack itemStack) {
        Object handle = getHandle(itemStack);
        if (handle == null) return;
        Object tag = getTag(handle);
        if (tag == null) return;

        removeMeta(tag, "bukkit");

        // Magic-specific legacy tags
        removeMeta(tag, "wand");
        removeMeta(tag, "spell");
        removeMeta(tag, "brush");
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

        try {
            final Object loreList = class_NBTTagList.newInstance();

            Method addMethod = class_NBTTagList.getMethod("add", class_NBTBase);
            Constructor stringContructor = class_NBTTagString.getConstructor(String.class);
            for (String value : lore) {
                Object nbtString = stringContructor.newInstance(value);
                addMethod.invoke(loreList, nbtString);
            }

            Method setMethod = class_NBTTagCompound.getMethod("set", String.class, class_NBTBase);
            setMethod.invoke(displayNode, "Lore", loreList);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public static Inventory createInventory(InventoryHolder holder, final int size, final String name) {
        Inventory inventory = null;
        try {
            Constructor<?> inventoryConstructor = class_CraftInventoryCustom.getConstructor(InventoryHolder.class, Integer.TYPE, String.class);
            inventory = (Inventory)inventoryConstructor.newInstance(holder, size, ChatColor.translateAlternateColorCodes('&', name));
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return inventory;
    }

    public static void addPotionEffect(LivingEntity entity, Color color) {
        addPotionEffect(entity, color.asRGB());
    }

    public static void clearPotionEffect(LivingEntity entity) {
        addPotionEffect(entity, 0);
    }

    public static void setInvulnerable(Entity entity) {
        try {
            Object handle = getHandle(entity);
            Field invulnerableField = class_Entity.getDeclaredField("invulnerable");
            invulnerableField.setAccessible(true);
            invulnerableField.set(handle, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void removePotionEffect(LivingEntity entity) {
        addPotionEffect(entity, 0); // ?
    }

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
                addEntity.invoke(worldHandle, newEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                Entity bukkitEntity = getBukkitEntity(newEntity);
                if (bukkitEntity == null || !(bukkitEntity instanceof Painting)) return null;

                newPainting = (Painting)bukkitEntity;
                newPainting.setArt(art, true);
                newPainting.setFacingDirection(facing, true);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return newPainting;
    }

    public static ItemFrame spawnItemFrame(Location location, BlockFace facing, ItemStack item)
    {
        ItemFrame newItemFrame = null;
        try {
            // entity = new EntityItemFrame(world, (int) x, (int) y, (int) z, dir);
            Constructor<?> itemFrameConstructor = class_EntityItemFrame.getConstructor(class_World, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            Method addEntity = class_World.getMethod("addEntity", class_Entity, CreatureSpawnEvent.SpawnReason.class);

            Object worldHandle = getHandle(location.getWorld());
            Object newEntity = itemFrameConstructor.newInstance(worldHandle, location.getBlockX(), location.getBlockY(), location.getBlockZ(), getFacing(facing));
            if (newEntity != null) {
                addEntity.invoke(worldHandle, newEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                Entity bukkitEntity = getBukkitEntity(newEntity);
                if (bukkitEntity == null || !(bukkitEntity instanceof ItemFrame)) return null;

                newItemFrame = (ItemFrame)bukkitEntity;
                newItemFrame.setItem(getCopy(item));
                newItemFrame.setFacingDirection(facing, true);
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
            Method watchMethod = class_DataWatcher.getMethod("watch", Integer.TYPE, Object.class);
            watchMethod.invoke(dataWatcher, key, data);
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
        watch(entity, 7, color);
    }

    public static List<Entity> getNearbyEntities(Location location, double x, double y, double z) {
        Object worldHandle = getHandle(location.getWorld());
        try {
            Method getEntitiesMethod = class_World.getMethod("getEntities", class_Entity, class_AxisAlignedBB);
            Method getBukkitEntityMethod = class_Entity.getMethod("getBukkitEntity");

            // :( No way to create an AABB with unobfuscated non-protected methods.
            Method createBBMethod = class_AxisAlignedBB.getMethod("a", Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE);

            Object bb = createBBMethod.invoke(null, location.getX() - x, location.getY() - y, location.getZ() - z,
                    location.getX() + x, location.getY() + y, location.getZ() + z);

            // The input entity is only used for equivalency testing, so this "null" should be ok.
            List<? extends Object> entityList = (List<? extends Object>)getEntitiesMethod.invoke(worldHandle, null, bb);
            List<Entity> bukkitEntityList = new java.util.ArrayList<org.bukkit.entity.Entity>(entityList.size());

            for (Object entity : entityList) {
                bukkitEntityList.add((Entity)getBukkitEntityMethod.invoke(entity));
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
}
