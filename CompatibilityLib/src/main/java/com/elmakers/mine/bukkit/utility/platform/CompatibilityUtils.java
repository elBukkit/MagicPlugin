package com.elmakers.mine.bukkit.utility.platform;

import com.elmakers.mine.bukkit.utility.BoundingBox;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.EnteredStateTracker;
import com.elmakers.mine.bukkit.utility.EnteredStateTracker.Touchable;

import com.elmakers.mine.bukkit.utility.TeleportPassengerTask;
import com.google.common.io.BaseEncoding;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
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
import org.bukkit.entity.Hanging;
import org.bukkit.entity.HumanEntity;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;
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
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Consumer;
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
@SuppressWarnings("deprecation")
public class CompatibilityUtils {
    // This is really here to prevent infinite loops, but sometimes these requests legitimately come in many time
    // (for instance when undoing a spell in an unloaded chunk that threw a ton of different falling blocks)
    // So putting some lower number on this will trigger a lot of false-positives.
    public static final int MAX_CHUNK_LOAD_TRY = 10000;
    public boolean USE_MAGIC_DAMAGE = true;
    public int BLOCK_BREAK_RANGE = 64;
    public final static int MAX_ENTITY_RANGE = 72;
    private final static Map<World.Environment, Integer> maxHeights = new HashMap<>();
    public Map<Integer, Material> materialIdMap;
    private ItemStack dummyItem;
    public static final UUID emptyUUID = new UUID(0L, 0L);
    private static final Map<LoadingChunk, Integer> loadingChunks = new HashMap<>();
    private boolean hasDumpedStack = false;
    private boolean teleporting = false;

    public void applyPotionEffects(LivingEntity entity, Collection<PotionEffect> effects) {
        for (PotionEffect effect : effects) {
            CompatibilityUtils.this.applyPotionEffect(entity, effect);
        }
    }

    private static final EnteredStateTracker DAMAGING = new EnteredStateTracker();

    public boolean isDamaging() {
        return DAMAGING.isInside();
    }

    public Inventory createInventory(InventoryHolder holder, int size, final String name) {
        size = (int) (Math.ceil((double) size / 9) * 9);
        size = Math.min(size, 54);

        String shorterName = name;
        if (shorterName.length() > 32) {
            shorterName = shorterName.substring(0, 31);
        }
        shorterName = ChatColor.translateAlternateColorCodes('&', shorterName);

        // TODO: Is this even still necessary?
        if (NMSUtils.class_CraftInventoryCustom_constructor == null) {
            return Bukkit.createInventory(holder, size, shorterName);
        }
        Inventory inventory = null;
        try {
            inventory = (Inventory) NMSUtils.class_CraftInventoryCustom_constructor.newInstance(holder, size, shorterName);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return inventory;
    }

    public boolean applyPotionEffect(LivingEntity entity, PotionEffect effect) {
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
        return applyEffect;
    }

    public boolean setDisplayNameRaw(ItemStack itemStack, String displayName) {
        Object handle = CompatibilityLib.getItemUtils().getHandle(itemStack);
        if (handle == null) return false;
        Object tag = CompatibilityLib.getItemUtils().getTag(handle);
        if (tag == null) return false;

        Object displayNode = CompatibilityLib.getNBTUtils().createNode(tag, "display");
        if (displayNode == null) return false;
        CompatibilityLib.getNBTUtils().setMeta(displayNode, "Name", displayName);
        return true;
    }

    public boolean setDisplayName(ItemStack itemStack, String displayName) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(displayName);
        itemStack.setItemMeta(meta);
        return true;
    }

    public boolean setLore(ItemStack itemStack, List<String> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return true;
    }

    public boolean isInvulnerable(Entity entity) {
        if (NMSUtils.class_Entity_invulnerableField == null) return false;
        try {
            Object handle = NMSUtils.getHandle(entity);
            return (boolean) NMSUtils.class_Entity_invulnerableField.get(handle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void setInvulnerable(Entity entity) {
        CompatibilityUtils.this.setInvulnerable(entity, true);
    }

    public void setInvulnerable(Entity entity, boolean flag) {
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_invulnerableField.set(handle, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isSilent(Entity entity) {
        if (NMSUtils.class_Entity_isSilentMethod == null) return false;
        try {
            Object handle = NMSUtils.getHandle(entity);
            return (boolean) NMSUtils.class_Entity_isSilentMethod.invoke(handle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void setSilent(Entity entity, boolean flag) {
        if (NMSUtils.class_Entity_setSilentMethod == null) return;
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_setSilentMethod.invoke(handle, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isPersist(Entity entity) {
        if (NMSUtils.class_Entity_persistField == null) return false;
        try {
            Object handle = NMSUtils.getHandle(entity);
            return (boolean) NMSUtils.class_Entity_persistField.get(handle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void setRemoveWhenFarAway(Entity entity, boolean flag) {
        if (NMSUtils.class_LivingEntity_setRemoveWhenFarAway == null || !(entity instanceof LivingEntity)) return;
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_LivingEntity_setRemoveWhenFarAway.invoke(entity, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setPersist(Entity entity, boolean flag) {
        if (NMSUtils.class_Entity_persistField == null) return;
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_persistField.set(handle, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isSitting(Entity entity) {
        if (NMSUtils.class_Sittable == null) return false;
        if (!NMSUtils.class_Sittable.isAssignableFrom(entity.getClass())) return false;
        try {
            return (boolean) NMSUtils.class_Sitting_isSittingMethod.invoke(entity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void setSitting(Entity entity, boolean flag) {
        if (NMSUtils.class_Sittable == null) return;
        if (!NMSUtils.class_Sittable.isAssignableFrom(entity.getClass())) return;
        try {
            NMSUtils.class_Sitting_setSittingMethod.invoke(entity, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Painting createPainting(Location location, BlockFace facing, Art art) {
        Painting newPainting = null;
        try {
            Object worldHandle = NMSUtils.getHandle(location.getWorld());
            Object newEntity = null;
            @SuppressWarnings("unchecked")
            Enum<?> directionEnum = Enum.valueOf(NMSUtils.class_EnumDirection, facing.name());
            Object blockLocation = NMSUtils.class_BlockPosition_Constructor.newInstance(location.getX(), location.getY(), location.getZ());
            newEntity = NMSUtils.class_EntityPaintingConstructor.newInstance(worldHandle, blockLocation, directionEnum);
            if (newEntity != null) {
                if (NMSUtils.class_EntityPainting_art != null) {
                    Object notchArt = NMSUtils.class_CraftArt_NotchToBukkitMethod.invoke(null, art);
                    NMSUtils.class_EntityPainting_art.set(newEntity, notchArt);
                }
                Entity bukkitEntity = CompatibilityUtils.this.getBukkitEntity(newEntity);
                if (bukkitEntity == null || !(bukkitEntity instanceof Painting)) return null;

                newPainting = (Painting) bukkitEntity;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            newPainting = null;
        }
        return newPainting;
    }

    public void setSilent(Object nmsEntity, boolean flag) {
        if (NMSUtils.class_Entity_setSilentMethod == null) return;
        try {
            NMSUtils.class_Entity_setSilentMethod.invoke(nmsEntity, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ItemFrame createItemFrame(Location location, BlockFace facing, Rotation rotation, ItemStack item) {
        ItemFrame newItemFrame = null;
        try {
            Object worldHandle = NMSUtils.getHandle(location.getWorld());
            Object newEntity = null;
            @SuppressWarnings("unchecked")
            Enum<?> directionEnum = Enum.valueOf(NMSUtils.class_EnumDirection, facing.name());
            Object blockLocation = NMSUtils.class_BlockPosition_Constructor.newInstance(location.getX(), location.getY(), location.getZ());
            newEntity = NMSUtils.class_EntityItemFrameConstructor.newInstance(worldHandle, blockLocation, directionEnum);
            if (newEntity != null) {
                Entity bukkitEntity = CompatibilityUtils.this.getBukkitEntity(newEntity);
                if (bukkitEntity == null || !(bukkitEntity instanceof ItemFrame)) return null;

                newItemFrame = (ItemFrame) bukkitEntity;
                newItemFrame.setItem(CompatibilityLib.getItemUtils().getCopy(item));
                newItemFrame.setRotation(rotation);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return newItemFrame;
    }

    public ArmorStand createArmorStand(Location location) {
        return (ArmorStand) CompatibilityUtils.this.createEntity(location, EntityType.ARMOR_STAND);
    }

    public Entity createEntity(Location location, EntityType entityType) {
        Entity bukkitEntity = null;
        try {
            Class<? extends Entity> entityClass = entityType.getEntityClass();
            Object newEntity = NMSUtils.class_CraftWorld_createEntityMethod.invoke(location.getWorld(), location, entityClass);
            if (newEntity != null) {
                bukkitEntity = CompatibilityUtils.this.getBukkitEntity(newEntity);
                if (bukkitEntity == null || !entityClass.isAssignableFrom(bukkitEntity.getClass())) return null;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return bukkitEntity;
    }

    public boolean addToWorld(World world, Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        try {
            Object worldHandle = NMSUtils.getHandle(world);
            Object entityHandle = NMSUtils.getHandle(entity);
            NMSUtils.class_World_addEntityMethod.invoke(worldHandle, entityHandle, reason);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public List<Entity> getNearbyEntities(Location location, double x, double y, double z) {
        if (location == null) return null;
        Object worldHandle = NMSUtils.getHandle(location.getWorld());
        try {
            x = Math.min(x, CompatibilityUtils.MAX_ENTITY_RANGE);
            z = Math.min(z, CompatibilityUtils.MAX_ENTITY_RANGE);
            Object bb = NMSUtils.class_AxisAlignedBB_Constructor.newInstance(location.getX() - x, location.getY() - y, location.getZ() - z,
                    location.getX() + x, location.getY() + y, location.getZ() + z);

            // The input entity is only used for equivalency testing, so this "null" should be ok.
            @SuppressWarnings("unchecked")
            List<? extends Object> entityList = (List<? extends Object>) NMSUtils.class_World_getEntitiesMethod.invoke(worldHandle, null, bb);
            List<Entity> bukkitEntityList = new java.util.ArrayList<>(entityList.size());

            for (Object entity : entityList) {
                Entity bukkitEntity = (Entity) NMSUtils.class_Entity_getBukkitEntityMethod.invoke(entity);
                if (bukkitEntity instanceof ComplexLivingEntity) {
                    ComplexLivingEntity complex = (ComplexLivingEntity) bukkitEntity;
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

    public Minecart spawnCustomMinecart(Location location, Material material, short data, int offset) {
        Minecart newMinecart = null;
        try {
            Constructor<?> minecartConstructor = NMSUtils.class_EntityMinecartRideable.getConstructor(NMSUtils.class_World, Double.TYPE, Double.TYPE, Double.TYPE);
            Method addEntity = NMSUtils.class_World.getMethod("addEntity", NMSUtils.class_Entity, CreatureSpawnEvent.SpawnReason.class);
            Method setPositionRotationMethod = NMSUtils.class_Entity.getMethod("setPositionRotation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);

            Object worldHandle = NMSUtils.getHandle(location.getWorld());
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
                Entity bukkitEntity = CompatibilityUtils.this.getBukkitEntity(newEntity);
                if (bukkitEntity == null || !(bukkitEntity instanceof Minecart)) return null;

                newMinecart = (Minecart) bukkitEntity;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return newMinecart;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Runnable> getTaskClass(BukkitTask task) {
        Class<? extends Runnable> taskClass = null;
        try {
            Method getTaskClassMethod = NMSUtils.class_CraftTask.getDeclaredMethod("getTaskClass");
            getTaskClassMethod.setAccessible(true);
            taskClass = (Class<? extends Runnable>) getTaskClassMethod.invoke(task);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return taskClass;
    }

    public Runnable getTaskRunnable(BukkitTask task) {
        Runnable runnable = null;
        try {
            Field taskField = NMSUtils.class_CraftTask.getDeclaredField("task");
            taskField.setAccessible(true);
            runnable = (Runnable) taskField.get(task);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return runnable;
    }

    public void ageItem(Item item, int ticksToAge) {
        try {
            Class<?> itemClass = NMSUtils.fixBukkitClass("net.minecraft.server.EntityItem");
            Object handle = NMSUtils.getHandle(item);
            Field ageField = itemClass.getDeclaredField("age");
            ageField.setAccessible(true);
            ageField.set(handle, ticksToAge);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void damage(Damageable target, double amount, Entity source) {
        if (target == null || target.isDead()) return;
        while (target instanceof ComplexEntityPart) {
            target = ((ComplexEntityPart) target).getParent();
        }
        if (CompatibilityUtils.this.USE_MAGIC_DAMAGE && target.getType() == EntityType.ENDER_DRAGON) {
            CompatibilityUtils.this.magicDamage(target, amount, source);
            return;
        }

        try (Touchable damaging = DAMAGING.enter()) {
            damaging.touch();
            if (target instanceof ArmorStand) {
                double newHealth = Math.max(0, target.getHealth() - amount);
                if (newHealth <= 0) {
                    EntityDeathEvent deathEvent = new EntityDeathEvent((ArmorStand) target, new ArrayList<ItemStack>());
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

    public void magicDamage(Damageable target, double amount, Entity source) {
        try {
            if (target == null || target.isDead()) return;

            if (NMSUtils.class_EntityLiving_damageEntityMethod == null || NMSUtils.object_magicSource == null || NMSUtils.class_DamageSource_getMagicSourceMethod == null) {
                CompatibilityUtils.this.damage(target, amount, source);
                return;
            }

            // Special-case for witches .. witches are immune to magic damage :\
            // And endermen are immune to indirect damage .. or something.
            // Also armor stands suck.
            // Might need to config-drive this, or just go back to defaulting to normal damage
            if (!CompatibilityUtils.this.USE_MAGIC_DAMAGE || target instanceof Witch || target instanceof Enderman || target instanceof ArmorStand || !(target instanceof LivingEntity)) {
                CompatibilityUtils.this.damage(target, amount, source);
                return;
            }

            Object targetHandle = NMSUtils.getHandle(target);
            if (targetHandle == null) return;

            Object sourceHandle = NMSUtils.getHandle(source);

            // Bukkit won't allow magic damage from anything but a potion..
            if (sourceHandle != null && source instanceof LivingEntity) {
                Location location = target.getLocation();

                ThrownPotion potion = CompatibilityUtils.this.getOrCreatePotionEntity(location);
                potion.setShooter((LivingEntity) source);

                Object potionHandle = NMSUtils.getHandle(potion);
                Object damageSource = NMSUtils.class_DamageSource_getMagicSourceMethod.invoke(null, potionHandle, sourceHandle);

                // This is a bit of hack that lets us damage the ender dragon, who is a weird and annoying collection
                // of various non-living entity pieces.
                if (NMSUtils.class_EntityDamageSource_setThornsMethod != null) {
                    NMSUtils.class_EntityDamageSource_setThornsMethod.invoke(damageSource);
                }

                try (Touchable damaging = DAMAGING.enter()) {
                    damaging.touch();
                    NMSUtils.class_EntityLiving_damageEntityMethod.invoke(
                            targetHandle,
                            damageSource,
                            (float) amount);
                }
            } else {
                try (Touchable damaging = DAMAGING.enter()) {
                    damaging.touch();
                    NMSUtils.class_EntityLiving_damageEntityMethod.invoke(
                            targetHandle,
                            NMSUtils.object_magicSource,
                            (float) amount);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void damage(Damageable target, double amount, Entity source, String damageType) {
        if (target == null || target.isDead()) return;
        if (damageType.equalsIgnoreCase("direct")) {
            double health = target.getHealth() - amount;
            target.setHealth(Math.max(health, 0));
            return;
        }
        if (damageType.equalsIgnoreCase("magic")) {
            CompatibilityUtils.this.magicDamage(target, amount, source);
            return;
        }
        Object damageSource = (NMSUtils.damageSources == null) ? null : NMSUtils.damageSources.get(damageType.toUpperCase());
        if (damageSource == null || NMSUtils.class_EntityLiving_damageEntityMethod == null) {
            CompatibilityUtils.this.magicDamage(target, amount, source);
            return;
        }

        try (Touchable damaging = DAMAGING.enter()) {
            damaging.touch();
            Object targetHandle = NMSUtils.getHandle(target);
            if (targetHandle == null) return;

            NMSUtils.class_EntityLiving_damageEntityMethod.invoke(targetHandle, damageSource, (float) amount);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Location getEyeLocation(Entity entity) {
        if (entity instanceof LivingEntity) {
            return ((LivingEntity) entity).getEyeLocation();
        }

        return entity.getLocation();
    }

    private static final Map<World, WeakReference<ThrownPotion>> POTION_PER_WORLD = new WeakHashMap<>();

    /**
     * Lazily creates potion entities that can be used when damaging players.
     *
     * @param location The location the potion should be placed at.
     * @return A potion entity placed ad the given location.
     */
    private ThrownPotion getOrCreatePotionEntity(Location location) {
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

    public ConfigurationSection loadConfiguration(String fileName) throws IOException, InvalidConfigurationException {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(fileName);
        } catch (FileNotFoundException fileNotFound) {

        }
        return configuration;
    }

    public YamlConfiguration loadBuiltinConfiguration(String fileName) throws IOException, InvalidConfigurationException {
        Plugin plugin = CompatibilityLib.getPlugin();
        return CompatibilityUtils.this.loadConfiguration(plugin.getResource(fileName), fileName);
    }

    public YamlConfiguration loadConfiguration(InputStream stream, String fileName) throws IOException, InvalidConfigurationException
    {
        YamlConfiguration configuration = new YamlConfiguration();
        if (stream == null) {
            CompatibilityLib.getLogger().log(Level.SEVERE, "Could not find builtin configuration file '" + fileName + "'");
            return configuration;
        }
        try {
            configuration.load(new InputStreamReader(stream, "UTF-8"));
        } catch (FileNotFoundException fileNotFound) {

        }
        return configuration;
    }

    // Here to support older versions of MagicWorlds
    @Deprecated
    public boolean isDone(Chunk chunk) {
        return CompatibilityUtils.this.isReady(chunk);
    }

    public boolean isReady(Chunk chunk) {
        if (NMSUtils.class_Chunk_isReadyMethod == null) return true;

        Object chunkHandle = NMSUtils.getHandle(chunk);
        boolean ready = true;
        try {
            ready = (Boolean) NMSUtils.class_Chunk_isReadyMethod.invoke(chunkHandle);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return ready;
    }

    public boolean createExplosion(Entity entity, World world, double x, double y, double z, float power, boolean setFire, boolean breakBlocks) {
        boolean result = false;
        if (world == null) return false;
        if (NMSUtils.class_World_explodeMethod == null) {
            return world.createExplosion(x, y, z, power, setFire, breakBlocks);
        }
        try {
            Object worldHandle = NMSUtils.getHandle(world);
            if (worldHandle == null) return false;
            Object entityHandle = entity == null ? null : NMSUtils.getHandle(entity);

            Object explosion = NMSUtils.class_EnumExplosionEffect != null ?
                    NMSUtils.class_World_explodeMethod.invoke(worldHandle, entityHandle, x, y, z, power, setFire, breakBlocks ? NMSUtils.enum_ExplosionEffect_BREAK : NMSUtils.enum_ExplosionEffect_NONE) :
                    NMSUtils.class_World_explodeMethod.invoke(worldHandle, entityHandle, x, y, z, power, setFire, breakBlocks);
            Field cancelledField = explosion.getClass().getDeclaredField("wasCanceled");
            result = (Boolean)cancelledField.get(explosion);
        } catch (Throwable ex) {
            ex.printStackTrace();
            result = false;
        }
        return result;
    }

    public Object getTileEntityData(Location location) {
       if (NMSUtils.class_TileEntity_saveMethod == null) return null;
        Object tileEntity = CompatibilityUtils.this.getTileEntity(location);
        if (tileEntity == null) return null;
        Object data = null;
        try {
            data = NMSUtils.class_NBTTagCompound_constructor.newInstance();
            NMSUtils.class_TileEntity_saveMethod.invoke(tileEntity, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return data;
    }

    public Object getTileEntity(Location location) {
        if (NMSUtils.class_World_getTileEntityMethod != null) {
            Object tileEntity = null;
            try {
                World world = location.getWorld();
                Object blockLocation = NMSUtils.class_BlockPosition_Constructor.newInstance(location.getX(), location.getY(), location.getZ());
                tileEntity = NMSUtils.class_World_getTileEntityMethod.invoke(NMSUtils.getHandle(world), blockLocation);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return tileEntity;
        }

        if (NMSUtils.class_CraftWorld_getTileEntityAtMethod == null) return null;
        Object tileEntity = null;
        try {
            World world = location.getWorld();
            tileEntity = NMSUtils.class_CraftWorld_getTileEntityAtMethod.invoke(world, location.getBlockX(), location.getBlockY(), location.getBlockZ());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return tileEntity;
    }

    public void clearItems(Location location) {
        if (NMSUtils.class_TileEntity_loadMethod == null || NMSUtils.class_TileEntity_updateMethod == null || NMSUtils.class_TileEntity_saveMethod == null) return;
        if (location == null) return;
        Object tileEntity = CompatibilityUtils.this.getTileEntity(location);
        if (tileEntity == null) return;
        try {
            Object entityData = NMSUtils.class_NBTTagCompound_constructor.newInstance();
            NMSUtils.class_TileEntity_saveMethod.invoke(tileEntity, entityData);
            Object itemList = NMSUtils.class_NBTTagCompound_getListMethod.invoke(entityData, "Items", NMSUtils.NBT_TYPE_COMPOUND);
            if (itemList != null) {
                List<?> items = (List<?>) NMSUtils.class_NBTTagList_list.get(itemList);
                items.clear();
            }
            NMSUtils.class_NBTTagCompound_removeMethod.invoke(entityData,"Item");
            if (NMSUtils.class_IBlockData != null) {
                Object worldHandle = NMSUtils.getHandle(location.getWorld());
                Object blockLocation = NMSUtils.class_BlockPosition_Constructor.newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                Object blockType = NMSUtils.class_World_getTypeMethod.invoke(worldHandle, blockLocation);
                NMSUtils.class_TileEntity_loadMethod.invoke(tileEntity, blockType, entityData);
            } else {
                NMSUtils.class_TileEntity_loadMethod.invoke(tileEntity, entityData);
            }
            NMSUtils.class_TileEntity_updateMethod.invoke(tileEntity);

            if (NMSUtils.class_Lootable_setLootTableMethod != null && NMSUtils.class_Lootable != null) {
                Block block = location.getBlock();
                BlockState blockState = block.getState();
                if (NMSUtils.class_Lootable.isAssignableFrom(blockState.getClass())) {
                    NMSUtils.class_Lootable_setLootTableMethod.invoke(blockState, new Object[]{ null });
                    blockState.update();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setTileEntityData(Location location, Object data) {
        if (NMSUtils.class_TileEntity_loadMethod == null || NMSUtils.class_TileEntity_updateMethod == null) return;
        if (location == null || data == null) return;
        Object tileEntity = CompatibilityUtils.this.getTileEntity(location);
        if (tileEntity == null) return;
        try {
            NMSUtils.class_NBTTagCompound_setIntMethod.invoke(data, "x", location.getBlockX());
            NMSUtils.class_NBTTagCompound_setIntMethod.invoke(data, "y", location.getBlockY());
            NMSUtils.class_NBTTagCompound_setIntMethod.invoke(data, "z", location.getBlockZ());

            if (NMSUtils.class_IBlockData != null) {
                Object worldHandle = NMSUtils.getHandle(location.getWorld());
                Object blockLocation = NMSUtils.class_BlockPosition_Constructor.newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                Object blockType = NMSUtils.class_World_getTypeMethod.invoke(worldHandle, blockLocation);
                NMSUtils.class_TileEntity_loadMethod.invoke(tileEntity, blockType, data);
            } else {
                NMSUtils.class_TileEntity_loadMethod.invoke(tileEntity, data);
            }
            NMSUtils.class_TileEntity_updateMethod.invoke(tileEntity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setEnvironment(World world, World.Environment environment) {
        try {
            NMSUtils.class_CraftWorld_environmentField.set(world, environment);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void playCustomSound(Player player, Location location, String sound, float volume, float pitch)
    {
        if (NMSUtils.class_PacketPlayOutCustomSoundEffect_Constructor == null || sound == null) return;
        try {
            Object packet = null;
            if (NMSUtils.class_MinecraftKey_constructor != null) {
                Object key = NMSUtils.class_MinecraftKey_constructor.newInstance(sound);
                Object vec = NMSUtils.class_Vec3D_constructor.newInstance(location.getX(), location.getY(), location.getZ());
                packet = NMSUtils.class_PacketPlayOutCustomSoundEffect_Constructor.newInstance(key, NMSUtils.enum_SoundCategory_PLAYERS, vec, volume, pitch);
            } else {
                packet = NMSUtils.class_PacketPlayOutCustomSoundEffect_Constructor.newInstance(sound, NMSUtils.enum_SoundCategory_PLAYERS, location.getX(), location.getY(), location.getZ(), volume, pitch);
            }
            NMSUtils.sendPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Entity> selectEntities(CommandSender sender, String selector) {
        if (NMSUtils.class_Bukkit_selectEntitiesMethod == null) return null;
        if (!selector.startsWith("@")) return null;
        try {
            return (List<Entity>) NMSUtils.class_Bukkit_selectEntitiesMethod.invoke(null, sender, selector);
        } catch (Throwable ex) {
            CompatibilityLib.getLogger().warning("Invalid selector: " + ex.getMessage());
        }
        return null;
    }

    public int getFacing(BlockFace direction)
    {
        int dir;
        switch (direction) {
        case SOUTH:
        default:
            dir = 0;
            break;
        case WEST:
            dir = 1;
            break;
        case NORTH:
            dir = 2;
            break;
        case EAST:
            dir = 3;
            break;
        }

        return dir;
    }

    public Entity getBukkitEntity(Object entity)
    {
        if (entity == null) return null;
        try {
            Method getMethod = entity.getClass().getMethod("getBukkitEntity");
            Object bukkitEntity = getMethod.invoke(entity);
            if (!(bukkitEntity instanceof Entity)) return null;
            return (Entity)bukkitEntity;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public byte getBlockData(FallingBlock falling) {
        // @deprecated Magic value
        byte data = 0;
        try {
            if (NMSUtils.class_FallingBlock_getBlockDataMethod != null) {
                data = (byte) NMSUtils.class_FallingBlock_getBlockDataMethod.invoke(falling);
            }
        } catch (Exception ignore) {

        }
        return data;
    }

    public MapView getMapById(int id) {
        if (NMSUtils.class_Bukkit_getMapMethod == null) return null;
        try {
            if (NMSUtils.legacyMaps) {
                return (MapView) NMSUtils.class_Bukkit_getMapMethod.invoke(null, (short)id);
            }
            return (MapView) NMSUtils.class_Bukkit_getMapMethod.invoke(null, (short)id);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Map<String, Object> getMap(ConfigurationSection section) {
        return CompatibilityUtils.this.getTypedMap(section);
    }

    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getTypedMap(ConfigurationSection section)
    {
        if (section == null) return null;
        if (section instanceof MemorySection && NMSUtils.class_MemorySection_mapField != null)
        {
            try {
                Object mapObject = NMSUtils.class_MemorySection_mapField.get(section);
                if (mapObject instanceof Map) {
                    return (Map<String, T>)mapObject;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Do it the slow way
        Map<String, T> map = new HashMap<>();
        Set<String> keys = section.getKeys(false);
        for (String key : keys) {
            map.put(key, (T)section.get(key));
        }

        return map;
    }

    public boolean setMap(ConfigurationSection section, Map<String, Object> map)
    {
        if (section == null || NMSUtils.class_MemorySection_mapField == null) return false;
        if (section instanceof MemorySection)
        {
            try {
                NMSUtils.class_MemorySection_mapField.set(section, map);
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }

        return true;
    }

    public Vector getPosition(Object entityData, String tag) {
        if (NMSUtils.class_NBTTagList_getDoubleMethod == null) return null;
        try {
            Object posList = NMSUtils.class_NBTTagCompound_getListMethod.invoke(entityData, tag, NMSUtils.NBT_TYPE_DOUBLE);
            Double x = (Double) NMSUtils.class_NBTTagList_getDoubleMethod.invoke(posList, 0);
            Double y = (Double) NMSUtils.class_NBTTagList_getDoubleMethod.invoke(posList, 1);
            Double z = (Double) NMSUtils.class_NBTTagList_getDoubleMethod.invoke(posList, 2);
            if (x != null && y != null && z != null) {
                return new Vector(x, y, z);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public BlockVector getBlockVector(Object entityData, String tag) {
        if (NMSUtils.class_NBTTagCompound_getIntArrayMethod == null) return null;
        try {
            int[] coords = (int[]) NMSUtils.class_NBTTagCompound_getIntArrayMethod.invoke(entityData, tag);
            if (coords == null || coords.length < 3) return null;
            return new BlockVector(coords[0], coords[1], coords[2]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    static class LoadingChunk {
        private final String worldName;
        private final int chunkX;
        private final int chunkZ;

        public LoadingChunk(Chunk chunk) {
            this(chunk.getWorld().getName(), chunk.getX(), chunk.getX());
        }

        public LoadingChunk(World world, int chunkX, int chunkZ) {
            this(world.getName(), chunkX, chunkZ);
        }

        public LoadingChunk(String worldName, int chunkX, int chunkZ) {
            this.worldName = worldName;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        @Override
        public int hashCode() {
            int worldHashCode = worldName.hashCode();
            return ((worldHashCode & 0xFFF) << 48)
                    | ((chunkX & 0xFFFFFF) << 24)
                    | (chunkX & 0xFFFFFF);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof LoadingChunk)) return false;
            LoadingChunk other = (LoadingChunk) o;
            ;
            return worldName.equals(other.worldName) && chunkX == other.chunkX && chunkZ == other.chunkZ;
        }

        @Override
        public String toString() {
            return worldName + ":" + chunkX + "," + chunkZ;
        }
    }

    public ConfigurationSection loadConfiguration(File file) throws IOException, InvalidConfigurationException
    {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(file);
        } catch (FileNotFoundException fileNotFound) {

        } catch (Throwable ex) {
            CompatibilityLib.getLogger().log(Level.SEVERE, "Error reading configuration file '" + file.getAbsolutePath() + "'");
            throw ex;
        }
        return configuration;
    }

    public void setTNTSource(TNTPrimed tnt, LivingEntity source)
    {
        try {
            Object tntHandle = NMSUtils.getHandle(tnt);
            Object sourceHandle = NMSUtils.getHandle(source);
            NMSUtils.class_EntityTNTPrimed_source.set(tntHandle, sourceHandle);
        } catch (Exception ex) {
            CompatibilityLib.getLogger().log(Level.WARNING, "Unable to set TNT source", ex);
        }
    }

    public void setEntityMotion(Entity entity, Vector motion) {
        try {
            Object handle = NMSUtils.getHandle(entity);
            if (NMSUtils.class_Entity_motField != null) {
                Object vec = NMSUtils.class_Vec3D_constructor.newInstance(motion.getX(), motion.getY(), motion.getZ());
                NMSUtils.class_Entity_motField.set(handle, vec);
            } else if (NMSUtils.class_Entity_motXField != null) {
                NMSUtils.class_Entity_motXField.set(handle, motion.getX());
                NMSUtils.class_Entity_motYField.set(handle, motion.getY());
                NMSUtils.class_Entity_motZField.set(handle, motion.getZ());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Vector getNormal(Block block, Location intersection)
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

    public boolean setLock(Block block, String lockName)
    {
        if (NMSUtils.class_ChestLock_Constructor == null) return false;
        if (NMSUtils.class_TileEntityContainer_setLock == null && NMSUtils.class_TileEntityContainer_lock == null) return false;
        Object tileEntity = CompatibilityUtils.this.getTileEntity(block.getLocation());
        if (tileEntity == null) return false;
        if (!NMSUtils.class_TileEntityContainer.isInstance(tileEntity)) return false;
        try {
            Object lock = NMSUtils.class_ChestLock_Constructor.newInstance(lockName);
            if (NMSUtils.class_TileEntityContainer_lock != null) {
                NMSUtils.class_TileEntityContainer_lock.set(tileEntity, lock);
            } else {
                NMSUtils.class_TileEntityContainer_setLock.invoke(tileEntity, lock);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean clearLock(Block block)
    {
        if (NMSUtils.class_TileEntityContainer_setLock == null && NMSUtils.class_TileEntityContainer_lock == null) return false;
        Object tileEntity = CompatibilityUtils.this.getTileEntity(block.getLocation());
        if (tileEntity == null) return false;
        if (!NMSUtils.class_TileEntityContainer.isInstance(tileEntity)) return false;
        try {
            if (NMSUtils.class_TileEntityContainer_lock != null) {
                if (NMSUtils.object_emptyChestLock == null) {
                    return false;
                }
                NMSUtils.class_TileEntityContainer_lock.set(tileEntity, NMSUtils.object_emptyChestLock);
            } else {
                NMSUtils.class_TileEntityContainer_setLock.invoke(tileEntity, new Object[] {null});
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean isLocked(Block block)
    {
        if (NMSUtils.class_TileEntityContainer_getLock == null && NMSUtils.class_TileEntityContainer_lock == null) return false;
        Object tileEntity = CompatibilityUtils.this.getTileEntity(block.getLocation());
        if (tileEntity == null) return false;
        if (!NMSUtils.class_TileEntityContainer.isInstance(tileEntity)) return false;
        try {
            Object lock = NMSUtils.class_TileEntityContainer_lock != null ? NMSUtils.class_TileEntityContainer_lock.get(tileEntity) :
                NMSUtils.class_TileEntityContainer_getLock.invoke(tileEntity);
            if (lock == null) return false;
            String key = NMSUtils.class_ChestLock_key != null ? (String) NMSUtils.class_ChestLock_key.get(lock) :
                (String) NMSUtils.class_ChestLock_getString.invoke(lock);
            return key != null && !key.isEmpty();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public String getLock(Block block)
    {
        if (NMSUtils.class_ChestLock_getString == null && NMSUtils.class_ChestLock_key == null) return null;
        if (NMSUtils.class_TileEntityContainer_getLock == null && NMSUtils.class_TileEntityContainer_lock == null) return null;
        Object tileEntity = CompatibilityUtils.this.getTileEntity(block.getLocation());
        if (tileEntity == null) return null;
        if (!NMSUtils.class_TileEntityContainer.isInstance(tileEntity)) return null;
        try {
            Object lock = NMSUtils.class_TileEntityContainer_lock != null ? NMSUtils.class_TileEntityContainer_lock.get(tileEntity) :
                NMSUtils.class_TileEntityContainer_getLock.invoke(tileEntity);
            if (lock == null) return null;
            return NMSUtils.class_ChestLock_key != null ? (String) NMSUtils.class_ChestLock_key.get(lock) :
                (String) NMSUtils.class_ChestLock_getString.invoke(lock);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void setFallingBlockDamage(FallingBlock entity, float fallHurtAmount, int fallHurtMax)
    {
        Object entityHandle = NMSUtils.getHandle(entity);
        if (entityHandle == null) return;
        try {
            NMSUtils.class_EntityFallingBlock_hurtEntitiesField.set(entityHandle, true);
            NMSUtils.class_EntityFallingBlock_fallHurtAmountField.set(entityHandle, fallHurtAmount);
            NMSUtils.class_EntityFallingBlock_fallHurtMaxField.set(entityHandle, fallHurtMax);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void configureMaxHeights(ConfigurationSection config) {
        maxHeights.clear();
        if (config == null) return;
        Collection<String> keys = config.getKeys(false);
        for (String key : keys) {
            try {
                World.Environment worldType = World.Environment.valueOf(key.toUpperCase());
                maxHeights.put(worldType, config.getInt(key));
            } catch (Exception ex) {
                CompatibilityLib.getLogger().log(Level.WARNING, "Invalid environment type: " + key, ex);
            }
        }
    }

    public int getMinHeight(World world) {
        if (!CompatibilityLib.isCurrentVersion()) {
            return 0;
        }
        return -64;
    }

    public int getMaxHeight(World world) {
        Integer maxHeight = maxHeights.get(world.getEnvironment());
        if (maxHeight == null) {
            maxHeight = world.getMaxHeight();
        }
        return maxHeight;
    }

    public void setInvisible(ArmorStand armorStand, boolean invisible) {
        try {
            Object handle = NMSUtils.getHandle(armorStand);
            NMSUtils.class_ArmorStand_setInvisible.invoke(handle, invisible);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setGravity(ArmorStand armorStand, boolean gravity) {
        if (NMSUtils.class_Entity_setNoGravity == null && NMSUtils.class_ArmorStand_setGravity == null) return;
        try {
            Object handle = NMSUtils.getHandle(armorStand);
            if (NMSUtils.class_Entity_setNoGravity != null) {
                NMSUtils.class_Entity_setNoGravity.invoke(handle, !gravity);
            } else {
                NMSUtils.class_ArmorStand_setGravity.invoke(handle, gravity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setGravity(Entity entity, boolean gravity) {
        if (NMSUtils.class_Entity_setNoGravity == null) return;
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_setNoGravity.invoke(handle, !gravity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setDisabledSlots(ArmorStand armorStand, int disabledSlots) {
        if (NMSUtils.class_EntityArmorStand_disabledSlotsField == null) return;
        try {
            Object handle = NMSUtils.getHandle(armorStand);
            NMSUtils.class_EntityArmorStand_disabledSlotsField.set(handle, disabledSlots);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int getDisabledSlots(ArmorStand armorStand) {
        if (NMSUtils.class_EntityArmorStand_disabledSlotsField == null) return 0;
        try {
            Object handle = NMSUtils.getHandle(armorStand);
            return (int) NMSUtils.class_EntityArmorStand_disabledSlotsField.get(handle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public void setInvisible(Entity entity, boolean invisible) {
        if (NMSUtils.class_Entity_setInvisible == null) return;
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_setInvisible.invoke(handle, invisible);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Boolean isInvisible(Entity entity) {
        if (NMSUtils.class_Entity_isInvisible == null) return null;
        try {
            Object handle = NMSUtils.getHandle(entity);
            return (boolean) NMSUtils.class_Entity_isInvisible.invoke(handle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean isPersistentInvisible(Entity entity) {
        if (NMSUtils.class_Entity_persistentInvisibilityField == null) return false;
        try {
            Object handle = NMSUtils.getHandle(entity);
            return (boolean) NMSUtils.class_Entity_persistentInvisibilityField.get(handle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void setPersistentInvisible(Entity entity, boolean invisible) {
        if (NMSUtils.class_Entity_persistentInvisibilityField == null) return;
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_persistentInvisibilityField.set(handle, invisible);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setYawPitch(Entity entity, float yaw, float pitch) {
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_setYawPitchMethod.invoke(handle, yaw, pitch);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setLocation(Entity entity, double x, double y, double z, float yaw, float pitch) {
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_setLocationMethod.invoke(handle, x, y, z, yaw, pitch);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addFlightExemption(Player player, int ticks) {
        if (NMSUtils.class_PlayerConnection_floatCountField == null) return;
        try {
            Object handle = NMSUtils.getHandle(player);
            Object connection = NMSUtils.class_EntityPlayer_playerConnectionField.get(handle);
            NMSUtils.class_PlayerConnection_floatCountField.set(connection, -ticks);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isValidProjectileClass(Class<?> projectileType) {
        return projectileType != null
                && (NMSUtils.class_EntityArrow.isAssignableFrom(projectileType)
                || NMSUtils.class_EntityProjectile.isAssignableFrom(projectileType)
                || NMSUtils.class_EntityFireball.isAssignableFrom(projectileType)
                || (NMSUtils.class_IProjectile != null && NMSUtils.class_IProjectile.isAssignableFrom(projectileType))
        );
    }

    public Projectile spawnProjectile(Class<?> projectileType, Location location, Vector direction, ProjectileSource source, float speed, float spread, float spreadLocations, Random random) {
        Constructor<? extends Object> constructor = null;
        Method shootMethod = null;
        Method setPositionRotationMethod = null;
        Field projectileSourceField = null;
        Field dirXField = null;
        Field dirYField = null;
        Field dirZField = null;
        Object nmsWorld = NMSUtils.getHandle(location.getWorld());
        Projectile projectile = null;
        try {
            Object entityType = null;
            if (NMSUtils.entityTypes != null) {
                constructor = projectileType.getConstructor(NMSUtils.class_entityTypes, NMSUtils.class_World);
                entityType = NMSUtils.entityTypes.get(projectileType.getSimpleName());
                if (entityType == null) {
                    throw new Exception("Failed to find entity type for projectile class " + projectileType.getName());
                }
            } else {
                constructor = projectileType.getConstructor(NMSUtils.class_World);
            }

            if (NMSUtils.class_EntityFireball.isAssignableFrom(projectileType)) {
                dirXField = projectileType.getField("dirX");
                dirYField = projectileType.getField("dirY");
                dirZField = projectileType.getField("dirZ");
            }

            if (NMSUtils.class_EntityProjectile.isAssignableFrom(projectileType) || NMSUtils.class_EntityArrow.isAssignableFrom(projectileType)) {
                shootMethod = projectileType.getMethod("shoot", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);
            }

            setPositionRotationMethod = projectileType.getMethod("setPositionRotation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);
            projectileSourceField = projectileType.getField("projectileSource");

            Object nmsProjectile = null;
            try {
                nmsProjectile = entityType == null ? constructor.newInstance(nmsWorld) : constructor.newInstance(entityType, nmsWorld);
            } catch (Exception ex) {
                nmsProjectile = null;
                CompatibilityLib.getLogger().log(Level.WARNING, "Error spawning projectile of class " + projectileType.getName(), ex);
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
            if (NMSUtils.class_EntityFireball.isAssignableFrom(projectileType) && spreadLocations > 0) {
                modifiedLocation.setX(modifiedLocation.getX() + direction.getX() + (random.nextGaussian() * spread / 5));
                modifiedLocation.setY(modifiedLocation.getY() + direction.getY() + (random.nextGaussian() * spread / 5));
                modifiedLocation.setZ(modifiedLocation.getZ() + direction.getZ() + (random.nextGaussian() * spread / 5));
            }
            setPositionRotationMethod.invoke(nmsProjectile, modifiedLocation.getX(), modifiedLocation.getY(), modifiedLocation.getZ(), location.getYaw(), location.getPitch());

            if (shootMethod != null) {
                shootMethod.invoke(nmsProjectile, direction.getX(), direction.getY(), direction.getZ(), speed, spread);
            }

            Entity entity = CompatibilityUtils.this.getBukkitEntity(nmsProjectile);
            if (entity == null || !(entity instanceof Projectile)) {
                throw new Exception("Got invalid bukkit entity from projectile of class " + projectileType.getName());
            }

            projectile = (Projectile)entity;
            if (source != null) {
                projectile.setShooter(source);
                projectileSourceField.set(nmsProjectile, source);
            }

            NMSUtils.class_World_addEntityMethod.invoke(nmsWorld, nmsProjectile, CreatureSpawnEvent.SpawnReason.DEFAULT);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }

        return projectile;
    }

    public void setDamage(Projectile projectile, double damage) {
        if (NMSUtils.class_EntityArrow_damageField == null) return;
        try {
            Object handle = NMSUtils.getHandle(projectile);
            NMSUtils.class_EntityArrow_damageField.set(handle, damage);
        } catch (Exception ex) {
            ex.printStackTrace();;
        }
    }

    public void decreaseLifespan(Projectile projectile, int ticks) {
        if (NMSUtils.class_EntityArrow_lifeField == null) return;
        try {
            Object handle = NMSUtils.getHandle(projectile);
            int currentLife = (Integer) NMSUtils.class_EntityArrow_lifeField.get(handle);
            if (currentLife < ticks) {
                NMSUtils.class_EntityArrow_lifeField.set(handle, ticks);
            }
        } catch (Exception ex) {
            ex.printStackTrace();;
        }
    }

    public Entity spawnEntity(Location target, EntityType entityType, CreatureSpawnEvent.SpawnReason spawnReason)
    {
        if (NMSUtils.class_CraftWorld_spawnMethod == null) {
            return target.getWorld().spawnEntity(target, entityType);
        }
        Entity entity = null;
        try {
            World world = target.getWorld();
            try {
                if (!NMSUtils.class_CraftWorld_spawnMethod_isLegacy) {
                    entity = (Entity) NMSUtils.class_CraftWorld_spawnMethod.invoke(world, target, entityType.getEntityClass(), null, spawnReason);
                } else {
                    entity = (Entity) NMSUtils.class_CraftWorld_spawnMethod.invoke(world, target, entityType.getEntityClass(), spawnReason);
                }
            } catch (Exception ex) {
                entity = target.getWorld().spawnEntity(target, entityType);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return entity;
    }
    
    public String getResourcePack(Server server) {
        String rp = null;
        try {
            Object minecraftServer = NMSUtils.getHandle(server);
            if (minecraftServer != null) {
                rp = (String) NMSUtils.class_MinecraftServer_getResourcePackMethod.invoke(minecraftServer);   
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rp;
    }

    public boolean setResourcePack(Player player, String rp, byte[] hash) {
        // TODO: Player.setResourcePack in 1.11+
        try {
            String hashString = BaseEncoding.base16().lowerCase().encode(hash);
            NMSUtils.class_EntityPlayer_setResourcePackMethod.invoke(NMSUtils.getHandle(player), rp, hashString);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }


    // Taken from CraftBukkit code.
    private String toMinecraftAttribute(Attribute attribute) {
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

    public boolean removeItemAttribute(ItemStack item, Attribute attribute) {
        try {
            Object handle = CompatibilityLib.getItemUtils().getHandle(item);
            if (handle == null) return false;
            Object tag = CompatibilityLib.getItemUtils().getTag(handle);
            if (tag == null) return false;

            String attributeName = CompatibilityUtils.this.toMinecraftAttribute(attribute);
            Object attributesNode = CompatibilityLib.getNBTUtils().getNode(tag, "AttributeModifiers");
            if (attributesNode == null) {
                return false;
            }
            int size = (Integer) NMSUtils.class_NBTTagList_sizeMethod.invoke(attributesNode);
            for (int i = 0; i < size; i++) {
                Object candidate = NMSUtils.class_NBTTagList_getMethod.invoke(attributesNode, i);
                String key = CompatibilityLib.getNBTUtils().getMetaString(candidate, "AttributeName");
                if (key.equals(attributeName)) {
                    if (size == 1) {
                        CompatibilityLib.getNBTUtils().removeMeta(tag, "AttributeModifiers");
                    } else {
                        NMSUtils.class_NBTTagList_removeMethod.invoke(attributesNode, i);
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

    public boolean removeItemAttributes(ItemStack item) {
        try {
            Object handle = CompatibilityLib.getItemUtils().getHandle(item);
            if (handle == null) return false;
            Object tag = CompatibilityLib.getItemUtils().getTag(handle);
            if (tag == null) return false;

            Object attributesNode = CompatibilityLib.getNBTUtils().getNode(tag, "AttributeModifiers");
            if (attributesNode == null) {
                return false;
            }
            CompatibilityLib.getNBTUtils().removeMeta(tag, "AttributeModifiers");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean setItemAttribute(ItemStack item, Attribute attribute, double value, String slot, int attributeOperation) {
        return CompatibilityUtils.this.setItemAttribute(item, attribute, value, slot, attributeOperation, UUID.randomUUID());
    }
    
    public boolean setItemAttribute(ItemStack item, Attribute attribute, double value, String slot, int attributeOperation, UUID attributeUUID) {
        if (NMSUtils.class_ItemMeta_addAttributeModifierMethod != null) {
            try {
                AttributeModifier.Operation operation;
                try {
                     operation = AttributeModifier.Operation.values()[attributeOperation];
                } catch (Throwable ex) {
                    CompatibilityLib.getLogger().warning("[Magic] invalid attribute operation ordinal: " + attributeOperation);
                    return false;
                }
                ItemMeta meta = item.getItemMeta();
                AttributeModifier modifier;
                if (slot != null && !slot.isEmpty()) {
                    EquipmentSlot equipmentSlot;
                    try {
                        if (slot.equalsIgnoreCase("mainhand")) {
                            equipmentSlot = EquipmentSlot.HAND;
                        } else if (slot.equalsIgnoreCase("offhand")) {
                            equipmentSlot = EquipmentSlot.OFF_HAND;
                        } else {
                            equipmentSlot = EquipmentSlot.valueOf(slot.toUpperCase());
                        }
                    } catch (Throwable ex) {
                        CompatibilityLib.getLogger().warning("[Magic] invalid attribute slot: " + slot);
                        return false;
                    }

                    modifier = (AttributeModifier) NMSUtils.class_AttributeModifier_constructor.newInstance(
                        attributeUUID, "Equipment Modifier", value, operation, equipmentSlot);
                } else {
                    modifier = new AttributeModifier(attributeUUID, "Equipment Modifier", value, operation);
                }
                NMSUtils.class_ItemMeta_addAttributeModifierMethod.invoke(meta, attribute, modifier);
                item.setItemMeta(meta);
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
            return true;
        }
        try {
            Object handle = CompatibilityLib.getItemUtils().getHandle(item);
            if (handle == null) {
                return false;
            }
            Object tag = CompatibilityLib.getItemUtils().getTag(handle);
            if (tag == null) return false;
            
            Object attributesNode = CompatibilityLib.getNBTUtils().getNode(tag, "AttributeModifiers");
            Object attributeNode = null;

            String attributeName = CompatibilityUtils.this.toMinecraftAttribute(attribute);
            if (attributesNode == null) {
                attributesNode = NMSUtils.class_NBTTagList_constructor.newInstance();
                NMSUtils.class_NBTTagCompound_setMethod.invoke(tag, "AttributeModifiers", attributesNode);
            } else {
                int size = (Integer) NMSUtils.class_NBTTagList_sizeMethod.invoke(attributesNode);
                for (int i = 0; i < size; i++) {
                    Object candidate = NMSUtils.class_NBTTagList_getMethod.invoke(attributesNode, i);
                    String key = CompatibilityLib.getNBTUtils().getMetaString(candidate, "AttributeName");
                    if (key.equals(attributeName)) {
                        attributeNode = candidate;
                        break;
                    }
                }
            }
            if (attributeNode == null) {
                attributeNode = NMSUtils.class_NBTTagCompound_constructor.newInstance();
                CompatibilityLib.getNBTUtils().setMeta(attributeNode, "AttributeName", attributeName);
                CompatibilityLib.getNBTUtils().setMeta(attributeNode, "Name", "Equipment Modifier");
                CompatibilityLib.getNBTUtils().setMetaInt(attributeNode, "Operation", attributeOperation);
                CompatibilityLib.getNBTUtils().setMetaLong(attributeNode, "UUIDMost", attributeUUID.getMostSignificantBits());
                CompatibilityLib.getNBTUtils().setMetaLong(attributeNode, "UUIDLeast", attributeUUID.getLeastSignificantBits());
                if (slot != null) {
                    CompatibilityLib.getNBTUtils().setMeta(attributeNode, "Slot", slot);
                }

                CompatibilityLib.getNBTUtils().addToList(attributesNode, attributeNode);
            }
            CompatibilityLib.getNBTUtils().setMetaDouble(attributeNode, "Amount", value);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    
    public void sendExperienceUpdate(Player player, float experience, int level) {
        try {
            Object packet = NMSUtils.class_PacketPlayOutExperience_Constructor.newInstance(experience, player.getTotalExperience(), level);
            NMSUtils.sendPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Object getEntityData(Entity entity) {
        if (NMSUtils.class_Entity_saveMethod == null) return null;
        
        Object data = null;
        try {
            Object nmsEntity = NMSUtils.getHandle(entity);
            if (nmsEntity != null) {
                data = NMSUtils.class_NBTTagCompound_constructor.newInstance();
                NMSUtils.class_Entity_saveMethod.invoke(nmsEntity, data);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return data;
    }
    
    public String getEntityType(Entity entity) {
        if (NMSUtils.class_Entity_getTypeMethod == null) return null;
        String entityType = null;
        try {
            Object nmsEntity = NMSUtils.getHandle(entity);
            if (nmsEntity != null) {
                entityType = (String) NMSUtils.class_Entity_getTypeMethod.invoke(nmsEntity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return entityType;
    }
    
    public void applyItemData(ItemStack item, Block block) {
        try {
            Object entityDataTag = CompatibilityLib.getNBTUtils().getNode(item, "BlockEntityTag");
            if (entityDataTag == null) return;
            CompatibilityUtils.this.setTileEntityData(block.getLocation(), entityDataTag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void swingOffhand(Entity entity, int range) {
        int rangeSquared = range * range;
        String worldName = entity.getWorld().getName();
        Location center = entity.getLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getName().equals(worldName) || player.getLocation().distanceSquared(center) > rangeSquared) {
                continue;
            }
            CompatibilityUtils.this.swingOffhand(player, entity);
        }
    }
    
    public void swingOffhand(Player sendToPlayer, Entity entity) {
        try {
            Object packet = NMSUtils.class_PacketPlayOutAnimation_Constructor.newInstance(NMSUtils.getHandle(entity), 3);
            NMSUtils.sendPacket(sendToPlayer, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        // TODO: New Player.sendTitle in 1.11
        player.sendTitle(title, subTitle);
    }

    public boolean sendActionBar(Player player, String message) {
        if (NMSUtils.class_PacketPlayOutChat == null) return false;
        try {
            Object chatComponent = NMSUtils.class_ChatComponentText_constructor.newInstance(message);
            Object packet;
            if (NMSUtils.enum_ChatMessageType_GAME_INFO == null) {
                packet = NMSUtils.class_PacketPlayOutChat_constructor.newInstance(chatComponent, (byte)2);
            } else if (NMSUtils.chatPacketHasUUID) {
                packet = NMSUtils.class_PacketPlayOutChat_constructor.newInstance(chatComponent, NMSUtils.enum_ChatMessageType_GAME_INFO, emptyUUID);
            } else {
                packet = NMSUtils.class_PacketPlayOutChat_constructor.newInstance(chatComponent, NMSUtils.enum_ChatMessageType_GAME_INFO);
            }
            NMSUtils.sendPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public float getDurability(Material material) {
        if (NMSUtils.class_Block_durabilityField == null || NMSUtils.class_CraftMagicNumbers_getBlockMethod == null) return 0.0f;
        try {
            Object block = NMSUtils.class_CraftMagicNumbers_getBlockMethod.invoke(null, material);
            if (block == null) {
                return 0.0f;
            }
            return (float) NMSUtils.class_Block_durabilityField.get(block);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0.0f;
    }

    private void sendBreaking(Player player, long id, Location location, int breakAmount) {
        try {
            Object blockPosition = NMSUtils.class_BlockPosition_Constructor.newInstance(location.getX(), location.getY(), location.getZ());
            Object packet = NMSUtils.class_PacketPlayOutBlockBreakAnimation_Constructor.newInstance((int)id, blockPosition, breakAmount);
            NMSUtils.sendPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int getBlockEntityId(Block block) {
        // There will be some overlap here, but these effects are very localized so it should be OK.
        return   ((block.getX() & 0xFFF) << 20)
               | ((block.getZ() & 0xFFF) << 8)
               | (block.getY() & 0xFF);
    }

    public void clearBreaking(Block block) {
        CompatibilityUtils.this.setBreaking(block, 10, CompatibilityUtils.this.BLOCK_BREAK_RANGE);
    }

    public void setBreaking(Block block, double percentage) {
        // Block break states are 0 - 9
        int breakState = (int)Math.ceil(9 * percentage);
        CompatibilityUtils.this.setBreaking(block, breakState, CompatibilityUtils.this.BLOCK_BREAK_RANGE);
    }

    public void setBreaking(Block block, int breakAmount) {
        CompatibilityUtils.this.setBreaking(block, breakAmount, CompatibilityUtils.this.BLOCK_BREAK_RANGE);
    }

    public void setBreaking(Block block, int breakAmount, int range) {
        String worldName = block.getWorld().getName();
        Location location = block.getLocation();
        int rangeSquared = range * range;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getName().equals(worldName) || player.getLocation().distanceSquared(location) > rangeSquared) {
                continue;
            }
            CompatibilityUtils.this.sendBreaking(player, CompatibilityUtils.this.getBlockEntityId(block), location, breakAmount);
        }
    }

    public Set<String> getTags(Entity entity) {
        // TODO: Use Entity.getScoreboardTags in a future version.
        return null;
    }

    public boolean isJumping(LivingEntity entity) {
        if (NMSUtils.class_Entity_jumpingField == null) return false;
        try {
            return (boolean) NMSUtils.class_Entity_jumpingField.get(NMSUtils.getHandle(entity));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public float getForwardMovement(LivingEntity entity) {
        if (NMSUtils.class_Entity_moveForwardField == null) return 0.0f;
        try {
            return (float) NMSUtils.class_Entity_moveForwardField.get(NMSUtils.getHandle(entity));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0.0f;
    }

    public float getStrafeMovement(LivingEntity entity) {
        if (NMSUtils.class_Entity_moveStrafingField == null) return 0.0f;
        try {
            return (float) NMSUtils.class_Entity_moveStrafingField.get(NMSUtils.getHandle(entity));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0.0f;
    }

    public boolean setBlockFast(Block block, Material material, int data) {
        return CompatibilityUtils.this.setBlockFast(block.getChunk(), block.getX(), block.getY(), block.getZ(), material, data);
    }

    public boolean setBlockFast(Chunk chunk, int x, int y, int z, Material material, int data) {
        if (NMSUtils.class_Block_fromLegacyData == null || NMSUtils.class_CraftMagicNumbers_getBlockMethod == null || NMSUtils.class_Chunk_setBlockMethod == null || NMSUtils.class_BlockPosition_Constructor == null) {
            CompatibilityLib.getDeprecatedUtils().setTypeAndData(chunk.getWorld().getBlockAt(x, y, z), material, (byte)data, false);
            return true;
        }
        try {
            Object chunkHandle = NMSUtils.getHandle(chunk);
            Object nmsBlock = NMSUtils.class_CraftMagicNumbers_getBlockMethod.invoke(null, material);
            nmsBlock = NMSUtils.class_Block_fromLegacyData.invoke(nmsBlock, data);
            Object blockLocation = NMSUtils.class_BlockPosition_Constructor.newInstance(x, y, z);
            NMSUtils.class_Chunk_setBlockMethod.invoke(chunkHandle, blockLocation, nmsBlock);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean setPickupStatus(Arrow arrow, String pickupStatus) {
        if (arrow == null || pickupStatus == null || NMSUtils.class_Arrow_setPickupStatusMethod == null || NMSUtils.class_PickupStatus == null) return false;

        try {
            Enum enumValue = Enum.valueOf(NMSUtils.class_PickupStatus, pickupStatus.toUpperCase());
            NMSUtils.class_Arrow_setPickupStatusMethod.invoke(arrow, enumValue);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public Block getHitBlock(ProjectileHitEvent event) {
        if (NMSUtils.class_ProjectileHitEvent_getHitBlockMethod == null) return null;
        try {
            return (Block) NMSUtils.class_ProjectileHitEvent_getHitBlockMethod.invoke(event);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Entity getEntity(World world, UUID uuid) {
        try {
            Object worldHandle = NMSUtils.getHandle(world);
            final Map<UUID, Entity> entityMap = (Map<UUID, Entity>) NMSUtils.class_WorldServer_entitiesByUUIDField.get(worldHandle);
            if (entityMap != null) {
                Object nmsEntity = entityMap.get(uuid);
                if (nmsEntity != null) {
                    return CompatibilityUtils.this.getBukkitEntity(nmsEntity);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Entity getEntity(UUID uuid) {
        if (NMSUtils.class_Server_getEntityMethod != null) {
            try {
                return (Entity) NMSUtils.class_Server_getEntityMethod.invoke(Bukkit.getServer(), uuid);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        for (World world : Bukkit.getWorlds()) {
            Entity found = CompatibilityUtils.this.getEntity(world, uuid);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    public boolean canRemoveRecipes() {
        return NMSUtils.class_Server_removeRecipeMethod != null;
    }

    public boolean removeRecipe(Plugin plugin, Recipe recipe) {
        if (NMSUtils.class_Keyed == null || NMSUtils.class_Keyed_getKeyMethod == null || NMSUtils.class_Server_removeRecipeMethod == null) {
            return false;
        }
        if (!NMSUtils.class_Keyed.isAssignableFrom(recipe.getClass())) {
            return false;
        }
        try {
            Object namespacedKey = NMSUtils.class_Keyed_getKeyMethod.invoke(recipe);
            return (boolean) NMSUtils.class_Server_removeRecipeMethod.invoke(plugin.getServer(), namespacedKey);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean removeRecipe(Plugin plugin, String key) {
        if (NMSUtils.class_NamespacedKey == null || NMSUtils.class_Server_removeRecipeMethod == null) {
            return false;
        }

        try {
            Object namespacedKey = NMSUtils.class_NamespacedKey_constructor.newInstance(plugin, key.toLowerCase());
            return (boolean) NMSUtils.class_Server_removeRecipeMethod.invoke(plugin.getServer(), namespacedKey);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public ShapedRecipe createShapedRecipe(Plugin plugin, String key, ItemStack item) {
        if (NMSUtils.class_NamespacedKey == null) {
            return new ShapedRecipe(item);
        }

        try {
            Object namespacedKey = NMSUtils.class_NamespacedKey_constructor.newInstance(plugin, key.toLowerCase());
            return (ShapedRecipe) NMSUtils.class_ShapedRecipe_constructor.newInstance(namespacedKey, item);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ShapedRecipe(item);
        }
    }

    public boolean discoverRecipe(HumanEntity entity, Plugin plugin, String key) {
        if (NMSUtils.class_NamespacedKey == null || NMSUtils.class_HumanEntity_discoverRecipeMethod == null) {
            return false;
        }

        try {
            Object namespacedKey = NMSUtils.class_NamespacedKey_constructor.newInstance(plugin, key.toLowerCase());
            return (boolean) NMSUtils.class_HumanEntity_discoverRecipeMethod.invoke(entity, namespacedKey);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean undiscoverRecipe(HumanEntity entity, Plugin plugin, String key) {
        if (NMSUtils.class_NamespacedKey == null || NMSUtils.class_HumanEntity_undiscoverRecipeMethod == null) {
            return false;
        }

        try {
            Object namespacedKey = NMSUtils.class_NamespacedKey_constructor.newInstance(plugin, key.toLowerCase());
            return (boolean) NMSUtils.class_HumanEntity_undiscoverRecipeMethod.invoke(entity, namespacedKey);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public double getMaxHealth(Damageable li) {
        // return li.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        return li.getMaxHealth();
    }

    public void setMaxHealth(Damageable li, double maxHealth) {
        // li.getAttribute(Attribute.GENERIC_MAX_HEALTH).setValue(maxHealth);
        li.setMaxHealth(maxHealth);
    }

    @SuppressWarnings("deprecation")
    public Material fromLegacy(org.bukkit.material.MaterialData materialData) {
        if (NMSUtils.class_UnsafeValues_fromLegacyDataMethod != null) {
            try {
                Material converted = (Material) NMSUtils.class_UnsafeValues_fromLegacyDataMethod.invoke(CompatibilityLib.getDeprecatedUtils().getUnsafe(), materialData);
                if (converted == Material.AIR) {
                    materialData.setData((byte)0);
                    converted = (Material) NMSUtils.class_UnsafeValues_fromLegacyDataMethod.invoke(CompatibilityLib.getDeprecatedUtils().getUnsafe(), materialData);
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
    public Material getMaterial(int id, byte data) {
        Material material = CompatibilityUtils.this.getMaterial(id);
        if (NMSUtils.class_UnsafeValues_fromLegacyDataMethod != null) {
            if (material != null) {
                material = CompatibilityUtils.this.fromLegacy(new org.bukkit.material.MaterialData(material, data));
            }
        }
        if (material == null) {
            material = Material.AIR;
        }
        return material;
    }

    @SuppressWarnings("deprecation")
    public Material getMaterial(int id) {
        if (CompatibilityUtils.this.materialIdMap == null) {
            CompatibilityUtils.this.materialIdMap = new HashMap<>();

            Object[] allMaterials = Material.AIR.getDeclaringClass().getEnumConstants();
            for (Object o : allMaterials) {
                Material material = (Material)o;
                if (!CompatibilityUtils.this.hasLegacyMaterials() || CompatibilityUtils.this.isLegacy(material)) {
                    CompatibilityUtils.this.materialIdMap.put(material.getId(), material);
                }
            }
        }
        return CompatibilityUtils.this.materialIdMap.get(id);
    }

    public Material getMaterial(String blockData) {
        String[] pieces = StringUtils.split(blockData, "[", 2);
        if (pieces.length == 0) return null;
        pieces = StringUtils.split(pieces[0], ":", 2);
        if (pieces.length == 0) return null;
        String materialKey = "";
        if (pieces.length == 2) {
            if (!pieces[0].equals("minecraft")) return null;
            materialKey = pieces[1];
        } else {
            materialKey = pieces[0];
        }
        try {
            return Material.valueOf(materialKey.toUpperCase());
        } catch (Exception ignore) {
        }
        return null;
    }

    public boolean hasLegacyMaterials() {
        return NMSUtils.class_Material_isLegacyMethod != null;
    }

    public boolean isLegacy(Material material) {
        if (NMSUtils.class_Material_isLegacyMethod == null) {
            return false;
        }
        try {
            return (boolean) NMSUtils.class_Material_isLegacyMethod.invoke(material);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public Material getLegacyMaterial(String materialName) {
        if (NMSUtils.class_Material_getLegacyMethod != null) {
            try {
                return (Material) NMSUtils.class_Material_getLegacyMethod.invoke(null, materialName, true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return Material.getMaterial(materialName);
    }

    @SuppressWarnings("deprecation")
    public Material migrateMaterial(Material material, byte data) {
        return CompatibilityUtils.this.fromLegacy(new org.bukkit.material.MaterialData(material, data));
    }

    @SuppressWarnings("deprecation")
    public String migrateMaterial(String materialKey) {
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

        Material legacyMaterial = data == 0 ? CompatibilityUtils.this.getLegacyMaterial(materialName) : Material.getMaterial("LEGACY_" + materialName);
        if (legacyMaterial != null) {
            org.bukkit.material.MaterialData materialData = new org.bukkit.material.MaterialData(legacyMaterial, data);
            legacyMaterial = CompatibilityUtils.this.fromLegacy(materialData);
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

    public boolean isChunkLoaded(Block block) {
        return CompatibilityUtils.this.isChunkLoaded(block.getLocation());
    }

    public boolean isChunkLoaded(Location location) {
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        World world = location.getWorld();
        return world.isChunkLoaded(chunkX, chunkZ);
    }

    public boolean checkChunk(Location location) {
        return CompatibilityUtils.this.checkChunk(location, true);
    }

    /**
     * Take care if setting generate to false, the chunk will load but not show as loaded
     */
    public boolean checkChunk(Location location, boolean generate) {
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        World world = location.getWorld();
        return CompatibilityUtils.this.checkChunk(world, chunkX, chunkZ, generate);
    }

    public boolean checkChunk(World world, int chunkX, int chunkZ) {
        return CompatibilityUtils.this.checkChunk(world, chunkX, chunkZ, true);
    }

    /**
     * Take care if setting generate to false, the chunk will load but not show as loaded
     */
    public boolean checkChunk(World world, int chunkX, int chunkZ, boolean generate) {
        if (!world.isChunkLoaded(chunkX, chunkZ)) {
            CompatibilityUtils.this.loadChunk(world, chunkX, chunkZ, generate);
            return false;
        }
        return CompatibilityUtils.this.isReady(world.getChunkAt(chunkX, chunkZ));
    }

    public boolean applyBonemeal(Location location) {
        if (NMSUtils.class_ItemDye_bonemealMethod == null) return false;

        if (CompatibilityUtils.this.dummyItem == null) {
             CompatibilityUtils.this.dummyItem = new ItemStack(Material.DIRT, 64);
             CompatibilityUtils.this.dummyItem = CompatibilityLib.getItemUtils().makeReal(CompatibilityUtils.this.dummyItem);
        }
        CompatibilityUtils.this.dummyItem.setAmount(64);

        try {
            Object world = NMSUtils.getHandle(location.getWorld());
            Object itemStack = CompatibilityLib.getItemUtils().getHandle(CompatibilityUtils.this.dummyItem);
            Object blockPosition = NMSUtils.class_BlockPosition_Constructor.newInstance(location.getX(), location.getY(), location.getZ());
            Object result = NMSUtils.class_ItemDye_bonemealMethod.invoke(null, itemStack, world, blockPosition);
            return (Boolean)result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public Color getColor(PotionMeta meta) {
        Color color = Color.BLACK;
        if (NMSUtils.class_PotionMeta_getColorMethod != null) {
            try {
                color = (Color) NMSUtils.class_PotionMeta_getColorMethod.invoke(meta);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return color;
    }

    public boolean setColor(PotionMeta meta, Color color) {
        if (NMSUtils.class_PotionMeta_setColorMethod != null) {
            try {
                NMSUtils.class_PotionMeta_setColorMethod.invoke(meta, color);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public String getBlockData(Material material, byte data) {
        if (NMSUtils.class_UnsafeValues_fromLegacyMethod == null) return null;
        try {
            Object blockData = NMSUtils.class_UnsafeValues_fromLegacyMethod.invoke(CompatibilityLib.getDeprecatedUtils().getUnsafe(), material, data);
            if (blockData != null) {
                return (String) NMSUtils.class_BlockData_getAsStringMethod.invoke(blockData);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean hasBlockDataSupport() {
        return NMSUtils.class_Block_getBlockDataMethod != null;
    }

    public boolean isTopBlock(Block block) {
        // Yes this is an ugly way to do it.
        String blockData = CompatibilityUtils.this.getBlockData(block);
        return blockData != null && blockData.contains("type=top");
    }

    public String getBlockData(Block block) {
        if (NMSUtils.class_Block_getBlockDataMethod == null) return null;
        try {
            Object blockData = NMSUtils.class_Block_getBlockDataMethod.invoke(block);
            if (blockData == null) {
                return null;
            }
            return (String) NMSUtils.class_BlockData_getAsStringMethod.invoke(blockData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean setBlockData(Server server, Block block, String data) {
        if (NMSUtils.class_Block_getBlockDataMethod == null) return false;
        try {
            Object blockData = NMSUtils.class_Server_createBlockDataMethod.invoke(server, data);
            NMSUtils.class_Block_setBlockDataMethod.invoke(block, blockData, false);
            return true;
        } catch (Exception ignore) {
            // Ignore issues setting invalid block data
        }
        return false;
    }

    public boolean applyPhysics(Block block) {
        if (NMSUtils.class_World_setTypeAndDataMethod == null || NMSUtils.class_World_getTypeMethod == null || NMSUtils.class_BlockPosition_Constructor == null) return false;
        try {
            Object worldHandle = NMSUtils.getHandle(block.getWorld());
            Object blockLocation = NMSUtils.class_BlockPosition_Constructor.newInstance(block.getX(), block.getY(), block.getZ());
            Object blockType = NMSUtils.class_World_getTypeMethod.invoke(worldHandle, blockLocation);
            CompatibilityUtils.this.clearItems(block.getLocation());
            CompatibilityLib.getDeprecatedUtils().setTypeAndData(block, Material.AIR, (byte)0, false);
            return (boolean) NMSUtils.class_World_setTypeAndDataMethod.invoke(worldHandle, blockLocation, blockType, 3);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public ItemStack getKnowledgeBook() {
        ItemStack book = null;
        try {
            Material bookMaterial = Material.valueOf("KNOWLEDGE_BOOK");
            book = new ItemStack(bookMaterial);
        } catch (Exception ignore) {

        }
        return book;
    }

    public boolean addRecipeToBook(ItemStack book, Plugin plugin, String recipeKey) {
        if (NMSUtils.class_NamespacedKey_constructor == null || NMSUtils.class_KnowledgeBookMeta_addRecipeMethod == null) return false;
        ItemMeta meta = book.getItemMeta();
        if (!NMSUtils.class_KnowledgeBookMeta.isAssignableFrom(meta.getClass())) return false;
        try {
            Object namespacedKey = NMSUtils.class_NamespacedKey_constructor.newInstance(plugin, recipeKey.toLowerCase());
            Object array = Array.newInstance(NMSUtils.class_NamespacedKey, 1);
            Array.set(array, 0, namespacedKey);
            NMSUtils.class_KnowledgeBookMeta_addRecipeMethod.invoke(meta, array);
            book.setItemMeta(meta);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean isPowerable(Block block) {
        if (NMSUtils.class_Powerable == null || NMSUtils.class_Powerable_setPoweredMethod == null || NMSUtils.class_Block_getBlockDataMethod == null) {
            return CompatibilityUtils.this.isPowerableLegacy(block);
        }
        try {
            Object blockData = NMSUtils.class_Block_getBlockDataMethod.invoke(block);
            return blockData != null && NMSUtils.class_Powerable.isAssignableFrom(blockData.getClass());
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    protected boolean isPowerableLegacy(Block block) {
        BlockState blockState = block.getState();
        org.bukkit.material.MaterialData data = blockState.getData();
        return data instanceof org.bukkit.material.Button ||
                data instanceof org.bukkit.material.Lever ||
                data instanceof org.bukkit.material.PistonBaseMaterial ||
                data instanceof org.bukkit.material.PoweredRail;
    }

    public boolean isPowered(Block block) {
        if (NMSUtils.class_Powerable == null || NMSUtils.class_Powerable_setPoweredMethod == null || NMSUtils.class_Block_getBlockDataMethod == null) {
            return CompatibilityUtils.this.isPoweredLegacy(block);
        }
        try {
            Object blockData = NMSUtils.class_Block_getBlockDataMethod.invoke(block);
            if (blockData == null) return false;
            if (!NMSUtils.class_Powerable.isAssignableFrom(blockData.getClass())) return false;
            return (boolean) NMSUtils.class_Powerable_isPoweredMethod.invoke(blockData);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    protected boolean isPoweredLegacy(Block block) {
        BlockState blockState = block.getState();
        org.bukkit.material.MaterialData data = blockState.getData();
        if (data instanceof org.bukkit.material.Button) {
            org.bukkit.material.Button powerData = (org.bukkit.material.Button)data;
            return powerData.isPowered();
        } else if (data instanceof org.bukkit.material.Lever) {
            org.bukkit.material.Lever powerData = (org.bukkit.material.Lever)data;
            return powerData.isPowered();
        } else if (data instanceof org.bukkit.material.PistonBaseMaterial) {
            org.bukkit.material.PistonBaseMaterial powerData = (org.bukkit.material.PistonBaseMaterial)data;
            return powerData.isPowered();
        } else if (data instanceof org.bukkit.material.PoweredRail) {
            org.bukkit.material.PoweredRail powerData = (org.bukkit.material.PoweredRail)data;
            return powerData.isPowered();
        }
        return false;
    }

    public boolean setPowered(Block block, boolean powered) {
        if (NMSUtils.class_Powerable == null || NMSUtils.class_Powerable_setPoweredMethod == null
                || NMSUtils.class_Block_setBlockDataMethod == null || NMSUtils.class_Block_getBlockDataMethod == null) {
            return CompatibilityUtils.this.setPoweredLegacy(block, powered);
        }

        try {
            Object blockData = NMSUtils.class_Block_getBlockDataMethod.invoke(block);
            if (blockData == null) return false;
            if (!NMSUtils.class_Powerable.isAssignableFrom(blockData.getClass())) return false;
            NMSUtils.class_Powerable_setPoweredMethod.invoke(blockData, powered);
            NMSUtils.class_Block_setBlockDataMethod.invoke(block, blockData, true);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean isWaterLoggable(Block block) {
        if (NMSUtils.class_Waterlogged == null || NMSUtils.class_Waterlogged_setWaterloggedMethod == null
            || NMSUtils.class_Block_setBlockDataMethod == null || NMSUtils.class_Block_getBlockDataMethod == null) {
            return false;
        }

        try {
            Object blockData = NMSUtils.class_Block_getBlockDataMethod.invoke(block);
            if (blockData == null) return false;
            return NMSUtils.class_Waterlogged.isAssignableFrom(blockData.getClass());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean setWaterlogged(Block block, boolean waterlogged) {
        if (NMSUtils.class_Waterlogged == null || NMSUtils.class_Waterlogged_setWaterloggedMethod == null
            || NMSUtils.class_Block_setBlockDataMethod == null || NMSUtils.class_Block_getBlockDataMethod == null) {
            return false;
        }

        try {
            Object blockData = NMSUtils.class_Block_getBlockDataMethod.invoke(block);
            if (blockData == null) return false;
            if (!NMSUtils.class_Waterlogged.isAssignableFrom(blockData.getClass())) return false;
            NMSUtils.class_Waterlogged_setWaterloggedMethod.invoke(blockData, waterlogged);
            NMSUtils.class_Block_setBlockDataMethod.invoke(block, blockData, true);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    protected boolean setPoweredLegacy(Block block, boolean powered) {
        BlockState blockState = block.getState();
        org.bukkit.material.MaterialData data = blockState.getData();
        boolean powerBlock = false;
        if (data instanceof org.bukkit.material.Button) {
            org.bukkit.material.Button powerData = (org.bukkit.material.Button)data;
            powerData.setPowered(!powerData.isPowered());
            powerBlock = true;
        } else if (data instanceof org.bukkit.material.Lever) {
            org.bukkit.material.Lever powerData = (org.bukkit.material.Lever)data;
            powerData.setPowered(!powerData.isPowered());
            powerBlock = true;
        } else if (data instanceof org.bukkit.material.PistonBaseMaterial) {
            org.bukkit.material.PistonBaseMaterial powerData = (org.bukkit.material.PistonBaseMaterial)data;
            powerData.setPowered(!powerData.isPowered());
            powerBlock = true;
        } else if (data instanceof org.bukkit.material.PoweredRail) {
            org.bukkit.material.PoweredRail powerData = (org.bukkit.material.PoweredRail)data;
            powerData.setPowered(!powerData.isPowered());
            powerBlock = true;
        }
        if (powerBlock) {
            blockState.update();
        }
        return powerBlock;
    }

    public boolean setTopHalf(Block block) {
        if (NMSUtils.class_Bisected == null) {
            return CompatibilityUtils.this.setTopHalfLegacy(block);
        }
        try {
            Object blockData = NMSUtils.class_Block_getBlockDataMethod.invoke(block);
            if (blockData == null || !NMSUtils.class_Bisected.isAssignableFrom(blockData.getClass())) return false;
            NMSUtils.class_Bisected_setHalfMethod.invoke(blockData, NMSUtils.enum_BisectedHalf_TOP);
            NMSUtils.class_Block_setBlockDataMethod.invoke(block, blockData, false);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    protected boolean setTopHalfLegacy(Block block) {
        byte data = CompatibilityLib.getDeprecatedUtils().getData(block);
        CompatibilityLib.getDeprecatedUtils().setTypeAndData(block, block.getType(), (byte)(data | 8), false);
        return true;
    }

    public Entity getSource(Entity entity) {
        if (entity instanceof Projectile) {
            ProjectileSource source = ((Projectile)entity).getShooter();
            if (source instanceof Entity) {
                entity = (Entity)source;
            }
        }

        return entity;
    }

    public boolean stopSound(Player player, Sound sound) {
        if (NMSUtils.class_Player_stopSoundMethod == null) return false;
        try {
            NMSUtils.class_Player_stopSoundMethod.invoke(player, sound);
            return true;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean stopSound(Player player, String sound) {
        if (NMSUtils.class_Player_stopSoundStringMethod == null) return false;
        try {
            NMSUtils.class_Player_stopSoundStringMethod.invoke(player, sound);
            return true;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean lockChunk(Chunk chunk, Plugin plugin) {
        if (!plugin.isEnabled()) return false;
        if (!chunk.isLoaded()) {
            CompatibilityLib.getLogger().info("Locking unloaded chunk");
        }
        if (NMSUtils.class_Chunk_addPluginChunkTicketMethod == null) return false;
        try {
            NMSUtils.class_Chunk_addPluginChunkTicketMethod.invoke(chunk, plugin);
            return true;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean unlockChunk(Chunk chunk, Plugin plugin) {
        if (!plugin.isEnabled()) return false;
        if (NMSUtils.class_Chunk_removePluginChunkTicketMethod == null) return false;
        try {
            NMSUtils.class_Chunk_removePluginChunkTicketMethod.invoke(chunk, plugin);
            return true;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public Location getHangingLocation(Entity entity) {
        Location location = entity.getLocation();
        if (NMSUtils.class_EntityHanging_blockPosition == null || !(entity instanceof Hanging)) {
            return location;
        }
        Object handle = NMSUtils.getHandle(entity);
        try {
            Object position = NMSUtils.class_EntityHanging_blockPosition.get(handle);
            location.setX((int) NMSUtils.class_BlockPosition_getXMethod.invoke(position));
            location.setY((int) NMSUtils.class_BlockPosition_getYMethod.invoke(position));
            location.setZ((int) NMSUtils.class_BlockPosition_getZMethod.invoke(position));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return location;
    }

    public BlockFace getCCW(BlockFace face) {
        switch (face) {
            case NORTH:
                return BlockFace.WEST;
            case SOUTH:
                return BlockFace.EAST;
            case WEST:
                return BlockFace.SOUTH;
            case EAST:
                return BlockFace.NORTH;
            default:
                throw new IllegalStateException("Unable to get CCW facing of " + face);
        }
    }

    public boolean setRecipeGroup(ShapedRecipe recipe, String group) {
        if (NMSUtils.class_Recipe_setGroupMethod == null) return false;
        try {
            NMSUtils.class_Recipe_setGroupMethod.invoke(recipe, group);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isSameKey(Plugin plugin, String key, Object keyed) {
        if (keyed == null || NMSUtils.class_Keyed == null || !NMSUtils.class_Keyed.isAssignableFrom(keyed.getClass())) {
            return false;
        }
        String namespace = plugin.getName().toLowerCase(Locale.ROOT);
        key = key.toLowerCase(Locale.ROOT);
        try {
            Object namespacedKey = NMSUtils.class_Keyed_getKeyMethod.invoke(keyed);
            Object keyNamespace = NMSUtils.class_NamespacedKey_getNamespaceMethod.invoke(namespacedKey);
            Object keyKey = NMSUtils.class_NamespacedKey_getKeyMethod.invoke(namespacedKey);
            return keyNamespace.equals(namespace) && keyKey.equals(key);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean isLegacyRecipes() {
        return NMSUtils.class_RecipeChoice_ExactChoice == null || NMSUtils.class_NamespacedKey == null;
    }

    public boolean setRecipeIngredient(ShapedRecipe recipe, char key, ItemStack ingredient, boolean ignoreDamage) {
        if (ingredient == null) return false;
        if (NMSUtils.class_RecipeChoice_ExactChoice == null) {
            if (CompatibilityLib.isLegacy()) {
                @SuppressWarnings("deprecation")
                org.bukkit.material.MaterialData material = ingredient == null ? null : ingredient.getData();
                if (material == null) {
                    return false;
                }
                recipe.setIngredient(key, material);
            } else {
                recipe.setIngredient(key, ingredient.getType());
            }
            return true;
        }
        try {
            short maxDurability = ingredient.getType().getMaxDurability();
            if (ignoreDamage && maxDurability > 0) {
                List<ItemStack> damaged = new ArrayList<>();
                for (short damage = 0 ; damage < maxDurability; damage++) {
                    ingredient = ingredient.clone();
                    ingredient.setDurability(damage);
                    damaged.add(ingredient);
                }
                Object exactChoice = NMSUtils.class_RecipeChoice_ExactChoice_List_constructor.newInstance(damaged);
                NMSUtils.class_ShapedRecipe_setIngredientMethod.invoke(recipe, key, exactChoice);
                return true;
            }
            Object exactChoice = NMSUtils.class_RecipeChoice_ExactChoice_constructor.newInstance(ingredient);
            NMSUtils.class_ShapedRecipe_setIngredientMethod.invoke(recipe, key, exactChoice);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean setAutoBlockState(Block block, Location target, BlockFace facing, boolean physics, Player originator) {
        if (NMSUtils.class_CraftBlock == null || block == null || facing == null || target == null) return false;
        try {
            Object nmsBlock = NMSUtils.class_CraftBlock_getNMSBlockMethod.invoke(block);
            if (nmsBlock == null) return false;
            ItemStack blockItem = new ItemStack(block.getType());
            Object originatorHandle = NMSUtils.getHandle(originator);
            Object world = NMSUtils.getHandle(block.getWorld());
            Object item = CompatibilityLib.getItemUtils().getHandle(CompatibilityLib.getItemUtils().makeReal(blockItem));
            if (originatorHandle == null || world == null || item == null) {
                return false;
            }
            Object blockPosition = NMSUtils.class_BlockPosition_Constructor.newInstance(block.getX(), block.getY(), block.getZ());
            Object vec3D = NMSUtils.class_Vec3D_constructor.newInstance(target.getX(), target.getY(), target.getZ());
            Enum<?> directionEnum = Enum.valueOf(NMSUtils.class_EnumDirection, facing.name());
            Object movingObject = NMSUtils.class_MovingObjectPositionBlock_createMethod.invoke(null, vec3D, directionEnum, blockPosition);
            Object actionContext = NMSUtils.class_BlockActionContext_constructor.newInstance(world, originatorHandle, NMSUtils.enum_EnumHand_MAIN_HAND, item, movingObject);
            Object placedState = NMSUtils.class_Block_getPlacedStateMethod.invoke(nmsBlock, actionContext);
            if (placedState == null) return false;
            NMSUtils.class_CraftBlock_setTypeAndDataMethod.invoke(block, placedState, physics);
            // class_World_setTypeAndDataMethod.invoke(world, blockPosition, placedState, 11);
            return true;
         } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean forceUpdate(Block block, boolean physics) {
        if (NMSUtils.class_nms_Block_getBlockDataMethod == null) return false;
        try {
            Object nmsBlock = NMSUtils.class_CraftBlock_getNMSBlockMethod.invoke(block);
            Object blockData = NMSUtils.class_nms_Block_getBlockDataMethod.invoke(nmsBlock);
            Object world = NMSUtils.getHandle(block.getWorld());
            Object blockPosition = NMSUtils.class_BlockPosition_Constructor.newInstance(block.getX(), block.getY(), block.getZ());
            NMSUtils.class_World_setTypeAndDataMethod.invoke(world, blockPosition, blockData, 11);
            return true;
         } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getPhantomSize(Entity entity) {
        if (NMSUtils.class_Phantom == null || entity == null) return 0;
        try {
            if (!NMSUtils.class_Phantom.isAssignableFrom(entity.getClass())) return 0;
            return (int) NMSUtils.class_Phantom_getSizeMethod.invoke(entity);
         } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean setPhantomSize(Entity entity, int size) {
        if (NMSUtils.class_Phantom == null || entity == null) return false;
        try {
            if (!NMSUtils.class_Phantom.isAssignableFrom(entity.getClass())) return false;
            NMSUtils.class_Phantom_setSizeMethod.invoke(entity, size);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public Location getBedSpawnLocation(Player player) {
        if (player == null) {
            return null;
        }
        if (NMSUtils.class_EntityHuman_getBedMethod != null && NMSUtils.class_EntityPlayer_getSpawnDimensionMethod != null) {
            try {
                Object playerHandle = NMSUtils.getHandle(player);
                Object bedLocation = NMSUtils.class_EntityHuman_getBedMethod.invoke(playerHandle);
                Object spawnDimension = NMSUtils.class_EntityPlayer_getSpawnDimensionMethod.invoke(playerHandle);
                if (spawnDimension != null && bedLocation != null) {
                    Object server = NMSUtils.class_EntityPlayer_serverField.get(playerHandle);
                    Object worldServer = server != null ? NMSUtils.class_MinecraftServer_getWorldServerMethod.invoke(server, spawnDimension) : null;
                    World world = worldServer != null ? (World) NMSUtils.class_WorldServer_worldMethod.invoke(worldServer) : null;
                    if (world != null) {
                        int x = (int) NMSUtils.class_BlockPosition_getXMethod.invoke(bedLocation);
                        int y = (int) NMSUtils.class_BlockPosition_getYMethod.invoke(bedLocation);
                        int z = (int) NMSUtils.class_BlockPosition_getZMethod.invoke(bedLocation);
                        return new Location(world, x, y, z);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (NMSUtils.class_EntityHuman_getBedMethod != null && NMSUtils.class_EntityHuman_spawnWorldField != null) {
            try {
                Object playerHandle = NMSUtils.getHandle(player);
                Object bedLocation = NMSUtils.class_EntityHuman_getBedMethod.invoke(playerHandle);
                String spawnWorld = (String) NMSUtils.class_EntityHuman_spawnWorldField.get(playerHandle);
                if (spawnWorld != null && bedLocation != null) {
                    World world = Bukkit.getWorld(spawnWorld);
                    if (world != null) {
                        int x = (int) NMSUtils.class_BlockPosition_getXMethod.invoke(bedLocation);
                        int y = (int) NMSUtils.class_BlockPosition_getYMethod.invoke(bedLocation);
                        int z = (int) NMSUtils.class_BlockPosition_getZMethod.invoke(bedLocation);
                        return new Location(world, x, y, z);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return player.getBedSpawnLocation();
    }

    public void loadChunk(Location location, boolean generate, Consumer<Chunk> consumer) {
        CompatibilityUtils.this.loadChunk(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, generate, consumer);
    }

    public void loadChunk(World world, int x, int z, boolean generate) {
        CompatibilityUtils.this.loadChunk(world, x, z, generate, null);
    }

    /**
     * This will load chunks asynchronously if possible.
     *
     * But note that it will never be truly asynchronous, it is important not to call this in a tight retry loop,
     * the main server thread needs to free up to actually process the async chunk loads.
     */
    public void loadChunk(World world, int x, int z, boolean generate, Consumer<Chunk> consumer) {
        final LoadingChunk loading = new LoadingChunk(world, x, z);
        Integer requestCount = loadingChunks.get(loading);
        if (requestCount != null) {
            requestCount++;
            if (requestCount > MAX_CHUNK_LOAD_TRY) {
                CompatibilityLib.getLogger().warning("Exceeded retry count for asynchronous chunk load, loading synchronously");
                if (!CompatibilityUtils.this.hasDumpedStack) {
                    CompatibilityUtils.this.hasDumpedStack = true;
                    Thread.dumpStack();
                }
                Chunk chunk = world.getChunkAt(x, z);
                chunk.load();
                if (consumer != null) {
                    consumer.accept(chunk);
                }
                loadingChunks.remove(loading);
                return;
            }
            loadingChunks.put(loading, requestCount);
            return;
        }
        if (NMSUtils.class_World_getChunkAtAsyncMethod != null) {
            try {
                loadingChunks.put(loading, 1);
                NMSUtils.class_World_getChunkAtAsyncMethod.invoke(world, x, z, generate, (Consumer<Chunk>) chunk -> {
                    loadingChunks.remove(loading);
                    if (consumer != null) {
                        consumer.accept(chunk);
                    }
                });
                return;
            } catch (Exception ex) {
                CompatibilityLib.getLogger().log(Level.WARNING, "Error loading chunk asynchronously", ex);
                loadingChunks.remove(loading);
            }
        }

        Chunk chunk = world.getChunkAt(x, z);
        chunk.load();
        if (consumer != null) {
            consumer.accept(chunk);
        }
    }

    public Entity getRootVehicle(Entity entity) {
        if (entity == null) {
            return null;
        }
        Entity vehicle = entity.getVehicle();
        while (vehicle != null) {
            entity = vehicle;
            vehicle = entity.getVehicle();
        }
        return entity;
    }

    public void addPassenger(Entity vehicle, Entity passenger) {
        if (NMSUtils.class_Entity_addPassengerMethod != null) {
            try {
                NMSUtils.class_Entity_addPassengerMethod.invoke(vehicle, passenger);
                return;
            } catch (Exception ex) {
                CompatibilityLib.getLogger().log(Level.WARNING, "Error adding entity passenger", ex);
            }
        }
        CompatibilityLib.getDeprecatedUtils().setPassenger(vehicle, passenger);
    }

    @SuppressWarnings("unchecked")
    public List<Entity> getPassengers(Entity entity) {
        if (NMSUtils.class_Entity_getPassengersMethod != null) {
            try {
                return (List<Entity>) NMSUtils.class_Entity_getPassengersMethod.invoke(entity);
            } catch (Exception ex) {
                CompatibilityLib.getLogger().log(Level.WARNING, "Error getting entity passengers", ex);
            }
        }
        List<Entity> passengerList = new ArrayList<>();
        Entity passenger = CompatibilityLib.getDeprecatedUtils().getPassenger(entity);
        if (passenger != null) {
            passengerList.add(passenger);
        }
        return passengerList;
    }

    protected void teleportPassengers(Entity vehicle, Location location, Collection<Entity> passengers) {
        for (Entity passenger : passengers) {
            if (passenger instanceof Player) {
                TeleportPassengerTask task = new TeleportPassengerTask(vehicle, passenger, location);
                Plugin plugin = CompatibilityLib.getPlugin();
                plugin.getServer().getScheduler().runTaskLater(plugin, task, 2);
            } else {
                // TODO: If there is a player midway in a stack of mobs do the mobs need to wait... ?
                // Might have to rig up something weird to test.
                // Otherwise this seems like too complicated of an edge case to worry about
                CompatibilityUtils.this.teleportVehicle(passenger, location);
                CompatibilityUtils.this.addPassenger(vehicle, passenger);
            }
        }
    }

    public void teleportVehicle(Entity vehicle, Location location) {
        List<Entity> passengers = CompatibilityUtils.this.getPassengers(vehicle);
        vehicle.eject();
        vehicle.teleport(location);
        // eject seems to just not work sometimes? (on chunk load, maybe)
        // So let's try to avoid exponentially adding passengers.
        List<Entity> newPassengers = CompatibilityUtils.this.getPassengers(vehicle);
        if (newPassengers.isEmpty()) {
            CompatibilityUtils.this.teleportPassengers(vehicle, location, passengers);
        } else {
            CompatibilityLib.getLogger().warning("Entity.eject failed!");
        }
    }

    public void teleportWithVehicle(Entity entity, Location location) {
        CompatibilityUtils.this.teleporting = true;
        if (entity != null && entity.isValid()) {
            final Entity vehicle = CompatibilityUtils.this.getRootVehicle(entity);
            CompatibilityUtils.this.teleportVehicle(vehicle, location);
        }
        CompatibilityUtils.this.teleporting = false;
    }

    public boolean isTeleporting() {
        return CompatibilityUtils.this.teleporting;
    }

    public boolean openBook(Player player, ItemStack itemStack) {
        if (NMSUtils.class_Player_openBookMethod == null) {
            return false;
        }
        try {
            NMSUtils.class_Player_openBookMethod.invoke(player, itemStack);
            return true;
        } catch (Exception ex) {
            CompatibilityLib.getLogger().log(Level.SEVERE, "Unexpected error showing book", ex);
        }
        return false;
    }

    public boolean isHandRaised(Player player) {
        if (NMSUtils.class_Player_isHandRaisedMethod == null) return false;
        try {
            return (boolean) NMSUtils.class_Player_isHandRaisedMethod.invoke(player);
        } catch (Exception ex) {
            CompatibilityLib.getLogger().log(Level.SEVERE, "Unexpected error checking block status", ex);
        }
        return false;
    }

    public void playRecord(Location location, Material record) {
        if (CompatibilityLib.isLegacy()) {
            location.getWorld().playEffect(location, Effect.RECORD_PLAY,
                    CompatibilityLib.getDeprecatedUtils().getId(record));
        } else {
            location.getWorld().playEffect(location, Effect.RECORD_PLAY, record);
        }
    }

    public Class<?> getProjectileClass(String projectileTypeName) {
        Class<?> projectileType = NMSUtils.getBukkitClass("net.minecraft.server.Entity" + projectileTypeName);
        if (!CompatibilityUtils.this.isValidProjectileClass(projectileType)) {
            return null;
        }
        return projectileType;
    }

    public Entity spawnFireworkEffect(Material fireworkMaterial, Server server, Location location, FireworkEffect effect, int power, Vector direction, Integer expectedLifespan, Integer ticksFlown, boolean silent) {
        Entity entity = null;
        try {
            if (fireworkMaterial == null) {
                return null;
            }
            Object world = NMSUtils.getHandle(location.getWorld());
            ItemStack itemStack = new ItemStack(fireworkMaterial);
            FireworkMeta meta = (FireworkMeta) itemStack.getItemMeta();
            meta.addEffect(effect);
            meta.setPower(power);
            itemStack.setItemMeta(meta);

            Object item = CompatibilityLib.getItemUtils().getHandle(CompatibilityLib.getItemUtils().makeReal(itemStack));
            final Object fireworkHandle = NMSUtils.class_EntityFireworkConstructor.newInstance(world, location.getX(), location.getY(), location.getZ(), item);
            CompatibilityUtils.this.setSilent(fireworkHandle, silent);

            if (direction != null) {
                if (NMSUtils.class_Entity_motField != null) {
                    Object vec = NMSUtils.class_Vec3D_constructor.newInstance(direction.getX(), direction.getY(), direction.getZ());
                    NMSUtils.class_Entity_motField.set(fireworkHandle, vec);
                } else if (NMSUtils.class_Entity_motXField != null) {
                    NMSUtils.class_Entity_motXField.set(fireworkHandle, direction.getX());
                    NMSUtils.class_Entity_motYField.set(fireworkHandle, direction.getY());
                    NMSUtils.class_Entity_motZField.set(fireworkHandle, direction.getZ());
                }
            }

            if (ticksFlown != null) {
                NMSUtils.class_Firework_ticksFlownField.set(fireworkHandle, ticksFlown);
            }
            if (expectedLifespan != null) {
                NMSUtils.class_Firework_expectedLifespanField.set(fireworkHandle, expectedLifespan);
            }

            if (direction == null)
            {
                Object fireworkPacket = NMSUtils.class_PacketSpawnEntityConstructor.newInstance(fireworkHandle, NMSUtils.FIREWORK_TYPE);
                Object fireworkId = NMSUtils.class_Entity_getIdMethod.invoke(fireworkHandle);
                Object watcher = NMSUtils.class_Entity_getDataWatcherMethod.invoke(fireworkHandle);
                Object metadataPacket = NMSUtils.class_PacketPlayOutEntityMetadata_Constructor.newInstance(fireworkId, watcher, true);
                Object statusPacket = NMSUtils.class_PacketPlayOutEntityStatus_Constructor.newInstance(fireworkHandle, (byte)17);

                Constructor<?> packetDestroyEntityConstructor = NMSUtils.class_PacketPlayOutEntityDestroy.getConstructor(int[].class);
                Object destroyPacket = packetDestroyEntityConstructor.newInstance(new int[] {(Integer)fireworkId});

                Collection<? extends Player> players = server.getOnlinePlayers();
                NMSUtils.sendPacket(server, location, players, fireworkPacket);
                NMSUtils.sendPacket(server, location, players, metadataPacket);
                NMSUtils.sendPacket(server, location, players, statusPacket);
                NMSUtils.sendPacket(server, location, players, destroyPacket);
                return null;
            }

            NMSUtils.class_World_addEntityMethod.invoke(world, fireworkHandle, CreatureSpawnEvent.SpawnReason.CUSTOM);
            entity = (Entity) NMSUtils.class_Entity_getBukkitEntityMethod.invoke(fireworkHandle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return entity;
    }

    public boolean loadAllTagsFromNBT(ConfigurationSection tags, Object tag)
    {
        try {
            Set<String> keys = CompatibilityLib.getInventoryUtils().getTagKeys(tag);
            if (keys == null) return false;

            for (String tagName : keys) {
                Object metaBase = NMSUtils.class_NBTTagCompound_getMethod.invoke(tag, tagName);
                if (metaBase != null) {
                    if (NMSUtils.class_NBTTagCompound.isAssignableFrom(metaBase.getClass())) {
                        ConfigurationSection newSection = tags.createSection(tagName);
                        CompatibilityUtils.this.loadAllTagsFromNBT(newSection, metaBase);
                    } else {
                        tags.set(tagName, CompatibilityLib.getInventoryUtils().getTagValue(metaBase));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public BoundingBox getHitbox(Entity entity) {
        if (NMSUtils.class_Entity_getBoundingBox != null) {
            try {
                Object entityHandle = NMSUtils.getHandle(entity);
                Object aabb = NMSUtils.class_Entity_getBoundingBox.invoke(entityHandle);
                if (aabb == null) {
                    return null;
                }
                return new BoundingBox(
                        NMSUtils.class_AxisAlignedBB_minXField.getDouble(aabb),
                        NMSUtils.class_AxisAlignedBB_maxXField.getDouble(aabb),
                        NMSUtils.class_AxisAlignedBB_minYField.getDouble(aabb),
                        NMSUtils.class_AxisAlignedBB_maxYField.getDouble(aabb),
                        NMSUtils.class_AxisAlignedBB_minZField.getDouble(aabb),
                        NMSUtils.class_AxisAlignedBB_maxZField.getDouble(aabb)
                );

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
