package com.elmakers.mine.bukkit.utility.platform.base_v1_17_0;

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
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Rotation;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Snow;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.map.MapView;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;
import org.spigotmc.event.entity.EntityDismountEvent;

import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.utility.BoundingBox;
import com.elmakers.mine.bukkit.utility.ChatUtils;
import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.DoorActionType;
import com.elmakers.mine.bukkit.utility.EnteredStateTracker;
import com.elmakers.mine.bukkit.utility.LoadingChunk;
import com.elmakers.mine.bukkit.utility.StringUtils;
import com.elmakers.mine.bukkit.utility.TeleportPassengerTask;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.PaperUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.PlatformInterpreter;
import com.elmakers.mine.bukkit.utility.platform.SpigotUtils;
import com.google.common.io.BaseEncoding;

public class CompatibilityUtilsBase implements CompatibilityUtils {
    // This is really here to prevent infinite loops, but sometimes these requests legitimately come in many time
    // (for instance when undoing a spell in an unloaded chunk that threw a ton of different falling blocks)
    // So putting some lower number on this will trigger a lot of false-positives.
    protected static final int MAX_CHUNK_LOAD_TRY = 10000;
    protected static final int MAX_ENTITY_RANGE = 72;
    public static int OFFHAND_BROADCAST_RANGE = 32;
    protected static boolean USE_MAGIC_DAMAGE = true;
    protected static int BLOCK_BREAK_RANGE = 64;

    protected static final BoundingBox BLOCK_BOUNDING_BOX = new BoundingBox(0, 1, 0, 1, 0, 1);
    protected final List<BoundingBox> blockBoundingBoxes = new ArrayList<>();

    protected final UUID emptyUUID = new UUID(0L, 0L);
    protected final Pattern hexColorPattern = Pattern.compile("&(#[A-Fa-f0-9]{6})");
    protected final WeakReference<Thread> primaryThread;
    protected ItemStack dummyItem;
    protected boolean hasDumpedStack = false;
    protected boolean teleporting = false;
    protected final Map<World.Environment, Integer> maxHeights = new HashMap<>();
    protected final Map<LoadingChunk, Integer> loadingChunks = new HashMap<>();
    protected final EnteredStateTracker isDamaging = new EnteredStateTracker();
    protected final Map<World, WeakReference<ThrownPotion>> worldPotions = new WeakHashMap<>();
    public Map<Integer, Material> materialIdMap;
    protected final Platform platform;

    protected CompatibilityUtilsBase(final Platform platform) {
        this.platform = platform;
        // This will be replaced, but adding it here lets us initialize the list to be the right size
        // There's probably a cleaner way to get a mutable pre-initialized list, but I couldn't figure it ou.
        blockBoundingBoxes.add(BLOCK_BOUNDING_BOX);
        primaryThread = new WeakReference<>(Thread.currentThread());
    }

    @Override
    public boolean isDamaging() {
        return isDamaging.isInside();
    }

    @Override
    public void applyPotionEffects(LivingEntity entity, Collection<PotionEffect> effects) {
        for (PotionEffect effect : effects) {
            applyPotionEffect(entity, effect);
        }
    }

    public boolean applyPotionEffect(LivingEntity entity, PotionEffect effect) {
        // Avoid nerfing existing effects
        boolean applyEffect = true;
        Collection<PotionEffect> currentEffects = entity.getActivePotionEffects();
        for (PotionEffect currentEffect : currentEffects) {
            // Look for the effect type we care about
            if (!currentEffect.getType().equals(effect.getType())) {
                continue;
            }

            int currentAmplifier = currentEffect.getAmplifier();
            int newAmplifier = effect.getAmplifier();
            // Don't replace negative effects, except with more negative effects
            if (currentAmplifier < 0)  {
                applyEffect = newAmplifier < currentAmplifier;
            } else if (newAmplifier > 0 && newAmplifier < currentAmplifier) {
                // Don't replace if the new amplifier is positive but less than the current amplifier
                applyEffect = false;
            }

            // Strictly speaking active effects come from a Map, so there should never be more
            // than one effect of the same type, even though Bukkit exposes it as a List
            break;
        }
        if (applyEffect) {
            entity.addPotionEffect(effect, true);
        }
        return applyEffect;
    }

    @Override
    public void setInvulnerable(Entity entity, boolean flag) {
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_invulnerableField.set(handle, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setInvulnerable(Entity entity) {
        setInvulnerable(entity, true);
    }

    @Override
    public ArmorStand createArmorStand(Location location) {
        return (ArmorStand)createEntity(location, EntityType.ARMOR_STAND);
    }

    @Override
    public Inventory createInventory(InventoryHolder holder, int size, final String name) {
        size = (int) (Math.ceil((double) size / 9) * 9);
        size = Math.min(size, 54);
        String translatedName = translateColors(name);
        return Bukkit.createInventory(holder, size, translatedName);
    }

    @Override
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

    @Override
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

    @Override
    public void setSilent(Entity entity, boolean flag) {
        if (NMSUtils.class_Entity_setSilentMethod == null) return;
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_setSilentMethod.invoke(handle, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setSilent(Object nmsEntity, boolean flag) {
        if (NMSUtils.class_Entity_setSilentMethod == null) return;
        try {
            NMSUtils.class_Entity_setSilentMethod.invoke(nmsEntity, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
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

    @Override
    public void setRemoveWhenFarAway(Entity entity, boolean flag) {
        if (NMSUtils.class_LivingEntity_setRemoveWhenFarAway == null || !(entity instanceof LivingEntity)) return;
        try {
            NMSUtils.class_LivingEntity_setRemoveWhenFarAway.invoke(entity, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setPersist(Entity entity, boolean flag) {
        if (NMSUtils.class_Entity_persistField == null) return;
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_persistField.set(handle, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
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

    @Override
    public void setSitting(Entity entity, boolean flag) {
        if (NMSUtils.class_Sittable == null) return;
        if (!NMSUtils.class_Sittable.isAssignableFrom(entity.getClass())) return;
        try {
            NMSUtils.class_Sitting_setSittingMethod.invoke(entity, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
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
                Entity bukkitEntity = getBukkitEntity(newEntity);
                if (bukkitEntity == null || !(bukkitEntity instanceof Painting)) return null;

                newPainting = (Painting) bukkitEntity;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            newPainting = null;
        }
        return newPainting;
    }

    @Override
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
                Entity bukkitEntity = getBukkitEntity(newEntity);
                if (bukkitEntity == null || !(bukkitEntity instanceof ItemFrame)) return null;

                newItemFrame = (ItemFrame) bukkitEntity;
                newItemFrame.setItem(platform.getItemUtils().getCopy(item));
                newItemFrame.setRotation(rotation);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return newItemFrame;
    }

    @Override
    public Entity createEntity(Location location, EntityType entityType) {
        Entity bukkitEntity = null;
        try {
            Class<? extends Entity> entityClass = entityType.getEntityClass();
            Object newEntity = NMSUtils.class_CraftWorld_createEntityMethod.invoke(location.getWorld(), location, entityClass);
            if (newEntity != null) {
                bukkitEntity = getBukkitEntity(newEntity);
                if (bukkitEntity == null || !entityClass.isAssignableFrom(bukkitEntity.getClass())) return null;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return bukkitEntity;
    }

    @Override
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

    @Override
    public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z) {
        if (location == null) return null;
        Object worldHandle = NMSUtils.getHandle(location.getWorld());
        try {
            x = Math.min(x, MAX_ENTITY_RANGE);
            z = Math.min(z, MAX_ENTITY_RANGE);
            Object bb = NMSUtils.class_AxisAlignedBB_Constructor.newInstance(location.getX() - x, location.getY() - y, location.getZ() - z,
                    location.getX() + x, location.getY() + y, location.getZ() + z);

            // The input entity is only used for equivalency testing, so this "null" should be ok.
            @SuppressWarnings("unchecked")
            List<? extends Object> entityList = (List<? extends Object>) NMSUtils.class_World_getEntitiesMethod.invoke(worldHandle, null, bb);
            List<Entity> bukkitEntityList = new ArrayList<>(entityList.size());

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

    @Override
    public Runnable getTaskRunnable(BukkitTask task) {
        return null;
    }

    @Override
    public void damage(Damageable target, double amount, Entity source, String damageType) {
        if (target == null || target.isDead()) return;
        if (damageType.equalsIgnoreCase("direct")) {
            double health = target.getHealth() - amount;
            target.setHealth(Math.max(health, 0));
            return;
        }
        if (damageType.equalsIgnoreCase("magic")) {
            magicDamage(target, amount, source);
            return;
        }
        Object damageSource = (NMSUtils.damageSources == null) ? null : NMSUtils.damageSources.get(damageType.toUpperCase());
        if (damageSource == null || NMSUtils.class_EntityLiving_damageEntityMethod == null) {
            magicDamage(target, amount, source);
            return;
        }

        try (EnteredStateTracker.Touchable damaging = isDamaging.enter()) {
            damaging.touch();
            Object targetHandle = NMSUtils.getHandle(target);
            if (targetHandle == null) return;

            NMSUtils.class_EntityLiving_damageEntityMethod.invoke(targetHandle, damageSource, (float) amount);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void damage(Damageable target, double amount, Entity source) {
        if (target == null || target.isDead()) return;
        while (target instanceof ComplexEntityPart) {
            target = ((ComplexEntityPart) target).getParent();
        }
        if (USE_MAGIC_DAMAGE && target.getType() == EntityType.ENDER_DRAGON) {
            magicDamage(target, amount, source);
            return;
        }

        try (EnteredStateTracker.Touchable damaging = isDamaging.enter()) {
            damaging.touch();
            if (target instanceof ArmorStand) {
                double newHealth = Math.max(0, target.getHealth() - amount);
                if (newHealth <= 0) {
                    EntityDeathEvent deathEvent = new EntityDeathEvent((ArmorStand) target, new ArrayList<>());
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

    /**
     * Lazily creates potion entities that can be used when damaging players.
     *
     * @param location The location the potion should be placed at.
     * @return A potion entity placed ad the given location.
     */
    protected ThrownPotion getOrCreatePotionEntity(Location location) {
        World world = location.getWorld();

        // Maintain a separate potion entity for every world so that
        // potion.getWorld() reports the correct result.
        WeakReference<ThrownPotion> ref = worldPotions.get(world);
        ThrownPotion potion = ref == null ? null : ref.get();

        if (potion == null) {
            potion = (ThrownPotion) world.spawnEntity(
                    location,
                    EntityType.SPLASH_POTION);
            potion.remove();

            ref = new WeakReference<>(potion);
            worldPotions.put(world, ref);
        } else {
            // TODO: Make sure this actually works?
            potion.teleport(location);
        }

        return potion;
    }

    @Override
    public Location getEyeLocation(Entity entity) {
        if (entity instanceof LivingEntity) {
            return ((LivingEntity) entity).getEyeLocation();
        }

        return entity.getLocation();
    }

    @Override
    public ConfigurationSection loadConfiguration(String fileName) throws IOException, InvalidConfigurationException {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(fileName);
        } catch (FileNotFoundException ignore) {

        }
        return configuration;
    }

    @Override
    public ConfigurationSection loadConfiguration(File file) throws IOException, InvalidConfigurationException {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(file);
        } catch (FileNotFoundException ignore) {

        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error reading configuration file '" + file.getAbsolutePath() + "'");
            throw ex;
        }
        return configuration;
    }

    @Override
    public YamlConfiguration loadConfiguration(InputStream stream, String fileName) throws IOException, InvalidConfigurationException {
        YamlConfiguration configuration = new YamlConfiguration();
        if (stream == null) {
            platform.getLogger().log(Level.SEVERE, "Could not find builtin configuration file '" + fileName + "'");
            return configuration;
        }
        try {
            configuration.load(new InputStreamReader(stream, "UTF-8"));
        } catch (FileNotFoundException ignore) {

        }
        return configuration;
    }

    @Override
    public YamlConfiguration loadBuiltinConfiguration(String fileName) throws IOException, InvalidConfigurationException {
        Plugin plugin = platform.getPlugin();
        return loadConfiguration(plugin.getResource(fileName), fileName);
    }

    @Override
    public int getFacing(BlockFace direction) {
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

    @Override
    public Map<String, Object> getMap(ConfigurationSection section) {
        return getTypedMap(section);
    }

    @Override
    public Vector getNormal(Block block, Location intersection) {
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

    @Override
    public void configureMaxHeights(ConfigurationSection config) {
        maxHeights.clear();
        if (config == null) return;
        Collection<String> keys = config.getKeys(false);
        for (String key : keys) {
            try {
                World.Environment worldType = World.Environment.valueOf(key.toUpperCase());
                maxHeights.put(worldType, config.getInt(key));
            } catch (Exception ex) {
                platform.getLogger().log(Level.WARNING, "Invalid environment type: " + key, ex);
            }
        }
    }

    @Override
    public int getMinHeight(World world) {
        if (!platform.isCurrentVersion()) {
            return 0;
        }
        return -64;
    }

    @Override
    public int getMaxHeight(World world) {
        Integer maxHeight = maxHeights.get(world.getEnvironment());
        if (maxHeight == null) {
            maxHeight = world.getMaxHeight();
        }
        return maxHeight;
    }

    @Override
    public int getMaxEntityRange() {
        return MAX_ENTITY_RANGE;
    }

    @Override
    public void load(ConfigurationSection properties) {
        USE_MAGIC_DAMAGE = properties.getBoolean("use_magic_damage", USE_MAGIC_DAMAGE);
    }

    // Taken from CraftBukkit code.
    protected String toMinecraftAttribute(Attribute attribute) {
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

    @Override
    public void applyItemData(ItemStack item, Block block) {
        try {
            Object entityDataTag = platform.getNBTUtils().getTag(item, "BlockEntityTag");
            if (entityDataTag == null) return;
            setTileEntityData(block.getLocation(), entityDataTag);
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

    @Override
    public void clearBreaking(Block block) {
        setBreaking(block, 10, BLOCK_BREAK_RANGE);
    }

    @Override
    public void setBreaking(Block block, double percentage) {
        // Block break states are 0 - 9
        int breakState = (int)Math.ceil(9 * percentage);
        setBreaking(block, breakState, BLOCK_BREAK_RANGE);
    }

    @Override
    public void setBreaking(Block block, int breakAmount) {
        setBreaking(block, breakAmount, BLOCK_BREAK_RANGE);
    }

    @Override
    public void setBreaking(Block block, int breakAmount, int range) {
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

    @Override
    @SuppressWarnings("deprecation")
    public Material getMaterial(int id, byte data) {
        Material material = getMaterial(id);
        if (material != null) {
            material = fromLegacy(new org.bukkit.material.MaterialData(material, data));
        }
        if (material == null) {
            material = Material.AIR;
        }
        return material;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Material getMaterial(int id) {
        if (materialIdMap == null) {
            materialIdMap = new HashMap<>();

            Object[] allMaterials = Material.AIR.getDeclaringClass().getEnumConstants();
            for (Object o : allMaterials) {
                Material material = (Material)o;
                if (!hasLegacyMaterials() || isLegacy(material)) {
                    materialIdMap.put(material.getId(), material);
                }
            }
        }
        return materialIdMap.get(id);
    }

    @Override
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

    @Override
    public Material getMaterial(FallingBlock falling) {
        return falling.getMaterial();
    }

    @Override
    @SuppressWarnings("deprecation")
    public Material migrateMaterial(Material material, byte data) {
        return fromLegacy(new org.bukkit.material.MaterialData(material, data));
    }

    @Override
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
            } catch (Exception ignore) {
            }
        }

        String materialName = pieces[0].toUpperCase();
        Material material = Material.getMaterial(materialName);
        if (material != null && data == 0) {
            return material.name().toLowerCase();
        }

        Material legacyMaterial = data == 0 ? getLegacyMaterial(materialName) : Material.getMaterial("LEGACY_" + materialName);
        if (legacyMaterial != null) {
            org.bukkit.material.MaterialData materialData = new org.bukkit.material.MaterialData(legacyMaterial, data);
            legacyMaterial = fromLegacy(materialData);
            if (legacyMaterial != null) {
                material = legacyMaterial;
            }
        }

        if (material != null) {
            materialKey = material.name().toLowerCase();
            // This mainly covers player skulls, but .. maybe other things? Maps?
            if (!textData.isEmpty()) {
                materialKey += ":" + textData;
            }
        }
        return materialKey;
    }

    @Override
    public boolean isChunkLoaded(Block block) {
        return isChunkLoaded(block.getLocation());
    }

    @Override
    public boolean isChunkLoaded(Location location) {
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        World world = location.getWorld();
        return world.isChunkLoaded(chunkX, chunkZ);
    }

    @Override
    public boolean checkChunk(Location location) {
        return checkChunk(location, true);
    }

    /**
     * Take care if setting generate to false, the chunk will load but not show as loaded
     */
    @Override
    public boolean checkChunk(Location location, boolean generate) {
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        World world = location.getWorld();
        return checkChunk(world, chunkX, chunkZ, generate);
    }

    @Override
    public boolean checkChunk(World world, int chunkX, int chunkZ) {
        return checkChunk(world, chunkX, chunkZ, true);
    }

    /**
     * Take care if setting generate to false, the chunk will load but not show as loaded
     */
    @Override
    public boolean checkChunk(World world, int chunkX, int chunkZ, boolean generate) {
        if (!world.isChunkLoaded(chunkX, chunkZ)) {
            loadChunk(world, chunkX, chunkZ, generate);
            return false;
        }
        return isReady(world.getChunkAt(chunkX, chunkZ));
    }

    @Override
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

    @Override
    public void magicDamage(Damageable target, double amount, Entity source) {
        try {
            if (target == null || target.isDead()) return;

            if (NMSUtils.class_EntityLiving_damageEntityMethod == null || NMSUtils.object_magicSource == null || NMSUtils.class_DamageSource_getMagicSourceMethod == null) {
                damage(target, amount, source);
                return;
            }

            // Special-case for witches .. witches are immune to magic damage :\
            // And endermen are immune to indirect damage .. or something.
            // Also armor stands suck.
            // Might need to config-drive this, or just go back to defaulting to normal damage
            if (!USE_MAGIC_DAMAGE || target instanceof Witch || target instanceof Enderman || target instanceof ArmorStand || !(target instanceof LivingEntity)) {
                damage(target, amount, source);
                return;
            }

            Object targetHandle = NMSUtils.getHandle(target);
            if (targetHandle == null) return;

            Object sourceHandle = NMSUtils.getHandle(source);

            // Bukkit won't allow magic damage from anything but a potion..
            if (sourceHandle != null && source instanceof LivingEntity) {
                Location location = target.getLocation();

                ThrownPotion potion = getOrCreatePotionEntity(location);
                potion.setShooter((LivingEntity) source);

                Object potionHandle = NMSUtils.getHandle(potion);
                Object damageSource = NMSUtils.class_DamageSource_getMagicSourceMethod.invoke(null, potionHandle, sourceHandle);

                // This is a bit of hack that lets us damage the ender dragon, who is a weird and annoying collection
                // of various non-living entity pieces.
                if (NMSUtils.class_EntityDamageSource_setThornsMethod != null) {
                    NMSUtils.class_EntityDamageSource_setThornsMethod.invoke(damageSource);
                }

                try (EnteredStateTracker.Touchable damaging = isDamaging.enter()) {
                    damaging.touch();
                    NMSUtils.class_EntityLiving_damageEntityMethod.invoke(
                            targetHandle,
                            damageSource,
                            (float) amount);
                }
            } else {
                try (EnteredStateTracker.Touchable damaging = isDamaging.enter()) {
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

    @Override
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

    @Override
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

            Object explosion = NMSUtils.class_EnumExplosionEffect != null
                    ? NMSUtils.class_World_explodeMethod.invoke(worldHandle, entityHandle, x, y, z, power, setFire, breakBlocks ? NMSUtils.enum_ExplosionEffect_BREAK : NMSUtils.enum_ExplosionEffect_NONE)
                    : NMSUtils.class_World_explodeMethod.invoke(worldHandle, entityHandle, x, y, z, power, setFire, breakBlocks);
            Field cancelledField = explosion.getClass().getDeclaredField("wasCanceled");
            result = (Boolean)cancelledField.get(explosion);
        } catch (Throwable ex) {
            ex.printStackTrace();
            result = false;
        }
        return result;
    }

    @Override
    public Object getTileEntityData(Location location) {
       if (NMSUtils.class_TileEntity_saveMethod == null) return null;
        Object tileEntity = getTileEntity(location);
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

    @Override
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

    @Override
    public void clearItems(Location location) {
        if (NMSUtils.class_TileEntity_loadMethod == null || NMSUtils.class_TileEntity_updateMethod == null || NMSUtils.class_TileEntity_saveMethod == null) return;
        if (location == null) return;

        Object tileEntity = getTileEntity(location);
        if (tileEntity == null) return;
        try {
            // Capture current tile entity snapshot
            Object entityData = NMSUtils.class_NBTTagCompound_constructor.newInstance();
            NMSUtils.class_TileEntity_saveMethod.invoke(tileEntity, entityData);

            // Remove items
            NMSUtils.class_NBTTagCompound_removeMethod.invoke(entityData, "Item");
            NMSUtils.class_NBTTagCompound_removeMethod.invoke(entityData, "Items");

            // Reload tile entity
            if (NMSUtils.class_IBlockData != null) {
                Object worldHandle = NMSUtils.getHandle(location.getWorld());
                Object blockLocation = NMSUtils.class_BlockPosition_Constructor.newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                Object blockType = NMSUtils.class_World_getTypeMethod.invoke(worldHandle, blockLocation);
                NMSUtils.class_TileEntity_loadMethod.invoke(tileEntity, blockType, entityData);
            } else {
                NMSUtils.class_TileEntity_loadMethod.invoke(tileEntity, entityData);
            }
            NMSUtils.class_TileEntity_updateMethod.invoke(tileEntity);

            // Clear records from jukebox. Equivalent to
            // if (tileEntity instanceof TileEntityRecordPlayer)
            // >> tileEntity.record = null
            if (NMSUtils.class_TileEntityRecordPlayer_record != null
                    && NMSUtils.class_TileEntityRecordPlayer.isInstance(tileEntity)) {
                // TODO: Move into ItemUtils
                Object emptyStack = NMSUtils.class_CraftItemStack_copyMethod.invoke(
                        null,
                        new Object[] { null });
                NMSUtils.class_TileEntityRecordPlayer_record.set(
                        tileEntity,
                        emptyStack);
            }

            // Clear loot table
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

    @Override
    public void setTileEntityData(Location location, Object data) {
        if (NMSUtils.class_TileEntity_loadMethod == null || NMSUtils.class_TileEntity_updateMethod == null) return;
        if (location == null || data == null) return;
        Object tileEntity = getTileEntity(location);
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

    @Override
    public void setEnvironment(World world, World.Environment environment) {
        try {
            NMSUtils.class_CraftWorld_environmentField.set(world, environment);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
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

    @Override
    @SuppressWarnings("unchecked")
    public List<Entity> selectEntities(CommandSender sender, String selector) {
        if (NMSUtils.class_Bukkit_selectEntitiesMethod == null) return null;
        if (!selector.startsWith("@")) return null;
        try {
            return (List<Entity>) NMSUtils.class_Bukkit_selectEntitiesMethod.invoke(null, sender, selector);
        } catch (Throwable ex) {
            platform.getLogger().warning("Invalid selector: " + ex.getMessage());
        }
        return null;
    }

    protected Entity getBukkitEntity(Object entity)
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public Vector getPosition(Object entityData, String tag) {
        if (NMSUtils.class_NBTTagList_getDoubleMethod == null) return null;
        try {
            Object posList = NMSUtils.class_NBTTagCompound_getListMethod.invoke(entityData, tag, CompatibilityConstants.NBT_TYPE_DOUBLE);
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

    @Override
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

    @Override
    public void setTNTSource(TNTPrimed tnt, LivingEntity source)
    {
        try {
            Object tntHandle = NMSUtils.getHandle(tnt);
            Object sourceHandle = NMSUtils.getHandle(source);
            NMSUtils.class_EntityTNTPrimed_source.set(tntHandle, sourceHandle);
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Unable to set TNT source", ex);
        }
    }

    @Override
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

    @Override
    public boolean setLock(Block block, String lockName)
    {
        if (NMSUtils.class_ChestLock_Constructor == null) return false;
        if (NMSUtils.class_TileEntityContainer_setLock == null && NMSUtils.class_TileEntityContainer_lock == null) return false;
        Object tileEntity = getTileEntity(block.getLocation());
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

    @Override
    public boolean clearLock(Block block)
    {
        if (NMSUtils.class_TileEntityContainer_setLock == null && NMSUtils.class_TileEntityContainer_lock == null) return false;
        Object tileEntity = getTileEntity(block.getLocation());
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

    @Override
    public boolean isLocked(Block block)
    {
        if (NMSUtils.class_TileEntityContainer_getLock == null && NMSUtils.class_TileEntityContainer_lock == null) return false;
        Object tileEntity = getTileEntity(block.getLocation());
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

    @Override
    public String getLock(Block block)
    {
        if (NMSUtils.class_ChestLock_getString == null && NMSUtils.class_ChestLock_key == null) return null;
        if (NMSUtils.class_TileEntityContainer_getLock == null && NMSUtils.class_TileEntityContainer_lock == null) return null;
        Object tileEntity = getTileEntity(block.getLocation());
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

    @Override
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

    @Override
    public void setInvisible(Entity entity, boolean invisible) {
        if (NMSUtils.class_Entity_setInvisible == null) return;
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_setInvisible.invoke(handle, invisible);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setInvisible(ArmorStand armorStand, boolean invisible) {
        try {
            Object handle = NMSUtils.getHandle(armorStand);
            NMSUtils.class_ArmorStand_setInvisible.invoke(handle, invisible);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
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

    @Override
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

    @Override
    public void setGravity(Entity entity, boolean gravity) {
        if (NMSUtils.class_Entity_setNoGravity == null) return;
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_setNoGravity.invoke(handle, !gravity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setDisabledSlots(ArmorStand armorStand, int disabledSlots) {
        if (NMSUtils.class_EntityArmorStand_disabledSlotsField == null) return;
        try {
            Object handle = NMSUtils.getHandle(armorStand);
            NMSUtils.class_EntityArmorStand_disabledSlotsField.set(handle, disabledSlots);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
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

    @Override
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

    @Override
    public void setPersistentInvisible(Entity entity, boolean invisible) {
        if (NMSUtils.class_Entity_persistentInvisibilityField == null) return;
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_persistentInvisibilityField.set(handle, invisible);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setYawPitch(Entity entity, float yaw, float pitch) {
        try {
            Object handle = NMSUtils.getHandle(entity);
            if (handle != null) {
                NMSUtils.class_Entity_setYawPitchMethod.invoke(handle, yaw, pitch);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setLocation(Entity entity, double x, double y, double z, float yaw, float pitch) {
        try {
            Object handle = NMSUtils.getHandle(entity);
            if (handle != null) {
                NMSUtils.class_Entity_setLocationMethod.invoke(handle, x, y, z, yaw, pitch);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
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

    @Override
    public boolean isValidProjectileClass(Class<?> projectileType) {
        return projectileType != null
                && (NMSUtils.class_EntityArrow.isAssignableFrom(projectileType)
                || NMSUtils.class_EntityProjectile.isAssignableFrom(projectileType)
                || NMSUtils.class_EntityFireball.isAssignableFrom(projectileType)
                || (NMSUtils.class_IProjectile != null && NMSUtils.class_IProjectile.isAssignableFrom(projectileType))
        );
    }

    @Override
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
                platform.getLogger().log(Level.WARNING, "Error spawning projectile of class " + projectileType.getName(), ex);
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

            Entity entity = getBukkitEntity(nmsProjectile);
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

    @Override
    public void setDamage(Projectile projectile, double damage) {
        if (NMSUtils.class_EntityArrow_damageField == null) return;
        try {
            Object handle = NMSUtils.getHandle(projectile);
            NMSUtils.class_EntityArrow_damageField.set(handle, damage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void decreaseLifespan(Projectile projectile, int ticks) {
        if (NMSUtils.class_EntityArrow_lifeField == null) return;
        try {
            Object handle = NMSUtils.getHandle(projectile);
            int currentLife = (Integer) NMSUtils.class_EntityArrow_lifeField.get(handle);
            if (currentLife < ticks) {
                NMSUtils.class_EntityArrow_lifeField.set(handle, ticks);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public boolean removeItemAttribute(ItemStack item, Attribute attribute) {
        try {
            Object handle = platform.getItemUtils().getHandle(item);
            if (handle == null) return false;
            Object tag = platform.getItemUtils().getTag(handle);
            if (tag == null) return false;

            String attributeName = toMinecraftAttribute(attribute);
            Object attributesNode = platform.getNBTUtils().getTag(tag, "AttributeModifiers");
            if (attributesNode == null) {
                return false;
            }
            int size = (Integer) NMSUtils.class_NBTTagList_sizeMethod.invoke(attributesNode);
            for (int i = 0; i < size; i++) {
                Object candidate = NMSUtils.class_NBTTagList_getMethod.invoke(attributesNode, i);
                String key = platform.getNBTUtils().getString(candidate, "AttributeName");
                if (key.equals(attributeName)) {
                    if (size == 1) {
                        platform.getNBTUtils().removeMeta(tag, "AttributeModifiers");
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

    @Override
    public boolean removeItemAttributes(ItemStack item) {
        try {
            Object handle = platform.getItemUtils().getHandle(item);
            if (handle == null) return false;
            Object tag = platform.getItemUtils().getTag(handle);
            if (tag == null) return false;

            Object attributesNode = platform.getNBTUtils().getTag(tag, "AttributeModifiers");
            if (attributesNode == null) {
                return false;
            }
            platform.getNBTUtils().removeMeta(tag, "AttributeModifiers");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean setItemAttribute(ItemStack item, Attribute attribute, double value, String slot, int attributeOperation) {
        return setItemAttribute(item, attribute, value, slot, attributeOperation, UUID.randomUUID());
    }

    @Override
    public boolean setItemAttribute(ItemStack item, Attribute attribute, double value, String slot, int attributeOperation, UUID attributeUUID) {
        if (NMSUtils.class_ItemMeta_addAttributeModifierMethod != null) {
            try {
                AttributeModifier.Operation operation;
                try {
                     operation = AttributeModifier.Operation.values()[attributeOperation];
                } catch (Throwable ex) {
                    platform.getLogger().warning("[Magic] invalid attribute operation ordinal: " + attributeOperation);
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
                        platform.getLogger().warning("[Magic] invalid attribute slot: " + slot);
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
            Object handle = platform.getItemUtils().getHandle(item);
            if (handle == null) {
                return false;
            }
            Object tag = platform.getItemUtils().getOrCreateTag(handle);
            if (tag == null) return false;

            Object attributesNode = platform.getNBTUtils().getTag(tag, "AttributeModifiers");
            Object attributeNode = null;

            String attributeName = toMinecraftAttribute(attribute);
            if (attributesNode == null) {
                attributesNode = NMSUtils.class_NBTTagList_constructor.newInstance();
                NMSUtils.class_NBTTagCompound_setMethod.invoke(tag, "AttributeModifiers", attributesNode);
            } else {
                int size = (Integer) NMSUtils.class_NBTTagList_sizeMethod.invoke(attributesNode);
                for (int i = 0; i < size; i++) {
                    Object candidate = NMSUtils.class_NBTTagList_getMethod.invoke(attributesNode, i);
                    String key = platform.getNBTUtils().getString(candidate, "AttributeName");
                    if (key.equals(attributeName)) {
                        attributeNode = candidate;
                        break;
                    }
                }
            }
            if (attributeNode == null) {
                attributeNode = NMSUtils.class_NBTTagCompound_constructor.newInstance();
                platform.getNBTUtils().setString(attributeNode, "AttributeName", attributeName);
                platform.getNBTUtils().setString(attributeNode, "Name", "Equipment Modifier");
                platform.getNBTUtils().setInt(attributeNode, "Operation", attributeOperation);
                platform.getNBTUtils().setLong(attributeNode, "UUIDMost", attributeUUID.getMostSignificantBits());
                platform.getNBTUtils().setLong(attributeNode, "UUIDLeast", attributeUUID.getLeastSignificantBits());
                if (slot != null) {
                    platform.getNBTUtils().setString(attributeNode, "Slot", slot);
                }

                platform.getNBTUtils().addToList(attributesNode, attributeNode);
            }
            platform.getNBTUtils().setDouble(attributeNode, "Amount", value);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void sendExperienceUpdate(Player player, float experience, int level) {
        try {
            Object packet = NMSUtils.class_PacketPlayOutExperience_Constructor.newInstance(experience, player.getTotalExperience(), level);
            NMSUtils.sendPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
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

    @Override
    public boolean setEntityData(Entity entity, Object tag) {
        return false;
    }

    @Override
    public EntityType getEntityTypeFromNMS(World world, Object tag) {
        return null;
    }

    @Override
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

    @Override
    public void swingOffhand(Entity entity) {
        int rangeSquared = OFFHAND_BROADCAST_RANGE * OFFHAND_BROADCAST_RANGE;
        String worldName = entity.getWorld().getName();
        Location center = entity.getLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getName().equals(worldName) || player.getLocation().distanceSquared(center) > rangeSquared) {
                continue;
            }
            swingOffhand(player, entity);
        }
    }

    private void swingOffhand(Player sendToPlayer, Entity entity) {
        try {
            Object packet = NMSUtils.class_PacketPlayOutAnimation_Constructor.newInstance(NMSUtils.getHandle(entity), 3);
            NMSUtils.sendPacket(sendToPlayer, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isTopBlock(Block block) {
        // Yes this is an ugly way to do it.
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Slab) {
            Slab slab = (Slab)blockData;
            Slab.Type slabType = slab.getType();
            return slabType != Slab.Type.BOTTOM;
        }
        return false;
    }

    @Override
    public ItemStack getKnowledgeBook() {
        ItemStack book = null;
        try {
            Material bookMaterial = Material.valueOf("KNOWLEDGE_BOOK");
            book = new ItemStack(bookMaterial);
        } catch (Exception ignore) {

        }
        return book;
    }

    @Override
    public Entity getSource(Entity entity) {
        if (entity instanceof Projectile) {
            ProjectileSource source = ((Projectile)entity).getShooter();
            if (source instanceof Entity) {
                entity = (Entity)source;
            }
        }

        return entity;
    }

    @Override
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

    @Override
    public void loadChunk(Location location, boolean generate, Consumer<Chunk> consumer) {
        loadChunk(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, generate, consumer);
    }

    @Override
    public void loadChunk(World world, int x, int z, boolean generate) {
        loadChunk(world, x, z, generate, null);
    }

    /**
     * This will load chunks asynchronously if possible.
     *
     * <p>But note that it will never be truly asynchronous, it is important not to call this in a tight retry loop,
     * the main server thread needs to free up to actually process the async chunk loads.
     */
    @Override
    public void loadChunk(World world, int x, int z, boolean generate, Consumer<Chunk> consumer) {
        PaperUtils paperUtils = platform.getPaperUtils();
        if (paperUtils == null) {
            Chunk chunk = world.getChunkAt(x, z);
            chunk.load();
            if (consumer != null) {
                consumer.accept(chunk);
            }
            return;
        }

        final LoadingChunk loading = new LoadingChunk(world, x, z);
        Integer requestCount = loadingChunks.get(loading);
        if (requestCount != null) {
            requestCount++;
            if (requestCount > MAX_CHUNK_LOAD_TRY) {
                platform.getLogger().warning("Exceeded retry count for asynchronous chunk load, loading synchronously");
                if (!hasDumpedStack) {
                    hasDumpedStack = true;
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

        loadingChunks.put(loading, 1);
        paperUtils.loadChunk(world, x, z, generate, chunk -> {
            loadingChunks.remove(loading);
            if (consumer != null) {
                consumer.accept(chunk);
            }
        });
    }

    @Override
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

    protected void teleportPassengers(Entity vehicle, Location location, Collection<Entity> passengers) {
        for (Entity passenger : passengers) {
            if (passenger instanceof Player) {
                TeleportPassengerTask task = new TeleportPassengerTask(this, vehicle, passenger, location);
                Plugin plugin = platform.getPlugin();
                plugin.getServer().getScheduler().runTaskLater(plugin, task, 2);
            } else {
                // TODO: If there is a player midway in a stack of mobs do the mobs need to wait... ?
                // Might have to rig up something weird to test.
                // Otherwise this seems like too complicated of an edge case to worry about
                teleportVehicle(passenger, location);
                addPassenger(vehicle, passenger);
            }
        }
    }

    @Override
    public void teleportVehicle(Entity vehicle, Location location) {
        List<Entity> passengers = getPassengers(vehicle);
        vehicle.eject();
        vehicle.teleport(location);
        // eject seems to just not work sometimes? (on chunk load, maybe)
        // So let's try to avoid exponentially adding passengers.
        List<Entity> newPassengers = getPassengers(vehicle);
        if (newPassengers.isEmpty()) {
            teleportPassengers(vehicle, location, passengers);
        } else {
            platform.getLogger().warning("Entity.eject failed!");
        }
    }

    @Override
    public void teleportWithVehicle(Entity entity, Location location) {
        teleporting = true;
        if (entity != null && entity.isValid()) {
            final Entity vehicle = getRootVehicle(entity);
            teleportVehicle(vehicle, location);
        }
        teleporting = false;
    }

    @Override
    public boolean isTeleporting() {
        return teleporting;
    }

    @Override
    public void playRecord(Location location, Material record) {
        if (platform.isLegacy()) {
            location.getWorld().playEffect(location, Effect.RECORD_PLAY,
                    platform.getDeprecatedUtils().getId(record));
        } else {
            location.getWorld().playEffect(location, Effect.RECORD_PLAY, record);
        }
    }

    @Override
    public void cancelDismount(EntityDismountEvent event) {
        event.setCancelled(true);
    }

    protected String getHexColor(String hexCode) {
        SpigotUtils spigot = platform.getSpigotUtils();
        if (spigot != null) {
            return spigot.getHexColor(hexCode);
        }
        // Just blanking them out for now, not going to try to match
        return "";
    }

    @Override
    public String translateColors(String message) {
        if (message == null || message.isEmpty()) return message;

        // First handle custom hex color format
        Matcher matcher = hexColorPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String match = matcher.group(1);
            matcher.appendReplacement(buffer, getHexColor(match));
        }
        message = matcher.appendTail(buffer).toString();

        // Next translate color codes, including any hex color codes we just inserted
        SpigotUtils spigot = platform.getSpigotUtils();
        if (spigot != null) {
            return spigot.translateColors(message);
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    @Nullable
    public String getEnchantmentKey(Enchantment enchantment) {
        if (enchantment == null) return null;
        String name = enchantment.getName();
        if (name == null) return null;
        return name.toLowerCase();
    }

    @Override
    @Nullable
    public String getEnchantmentBaseKey(Enchantment enchantment) {
        String key = getEnchantmentKey(enchantment);
        if (key == null) return null;
        String[] pieces = StringUtils.split(key, ":", 2);
        if (pieces.length == 0) return null;
        return pieces[pieces.length - 1];
    }

    @Override
    public Enchantment getEnchantmentByKey(String key) {
        return Enchantment.getByName(key.toUpperCase());
    }

    @Override
    public Collection<String> getEnchantmentBaseKeys() {
        List<String> enchantmentKeys = new ArrayList<>();
        for (Enchantment enchantment : Enchantment.values()) {
            String key = getEnchantmentBaseKey(enchantment);
            if (key != null) {
                enchantmentKeys.add(key);
            }
        }
        return enchantmentKeys;
    }

    @Override
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

    @Override
    public void sendBreaking(Player player, long id, Location location, int breakAmount) {
        try {
            Object blockPosition = NMSUtils.class_BlockPosition_Constructor.newInstance(location.getX(), location.getY(), location.getZ());
            Object packet = NMSUtils.class_PacketPlayOutBlockBreakAnimation_Constructor.newInstance((int)id, blockPosition, breakAmount);
            NMSUtils.sendPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Set<String> getTags(Entity entity) {
        // TODO: Use Entity.getScoreboardTags in a future version.
        return null;
    }

    @Override
    public boolean isJumping(LivingEntity entity) {
        if (NMSUtils.class_Entity_jumpingField == null) return false;
        try {
            return (boolean) NMSUtils.class_Entity_jumpingField.get(NMSUtils.getHandle(entity));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public float getForwardMovement(LivingEntity entity) {
        if (NMSUtils.class_Entity_moveForwardField == null) return 0.0f;
        try {
            return (float) NMSUtils.class_Entity_moveForwardField.get(NMSUtils.getHandle(entity));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0.0f;
    }

    @Override
    public float getStrafeMovement(LivingEntity entity) {
        if (NMSUtils.class_Entity_moveStrafingField == null) return 0.0f;
        try {
            return (float) NMSUtils.class_Entity_moveStrafingField.get(NMSUtils.getHandle(entity));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0.0f;
    }

    @Override
    public boolean setBlockFast(Chunk chunk, int x, int y, int z, Material material, int data) {
        if (NMSUtils.class_Block_fromLegacyData == null || NMSUtils.class_CraftMagicNumbers_getBlockMethod == null || NMSUtils.class_Chunk_setBlockMethod == null || NMSUtils.class_BlockPosition_Constructor == null) {
            platform.getDeprecatedUtils().setTypeAndData(chunk.getWorld().getBlockAt(x, y, z), material, (byte)data, false);
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

    @Override
    public boolean setBlockFast(Block block, Material material, int data) {
        return setBlockFast(block.getChunk(), block.getX(), block.getY(), block.getZ(), material, data);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean setPickupStatus(Projectile projectile, String pickupStatus) {
        if (!(projectile instanceof Arrow)) return false;
        if (pickupStatus == null || NMSUtils.class_Arrow_setPickupStatusMethod == null || NMSUtils.class_PickupStatus == null) return false;

        try {
            Enum enumValue = Enum.valueOf(NMSUtils.class_PickupStatus, pickupStatus.toUpperCase());
            NMSUtils.class_Arrow_setPickupStatusMethod.invoke(projectile, enumValue);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public Block getHitBlock(ProjectileHitEvent event) {
        if (NMSUtils.class_ProjectileHitEvent_getHitBlockMethod == null) return null;
        try {
            return (Block) NMSUtils.class_ProjectileHitEvent_getHitBlockMethod.invoke(event);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Entity getEntity(World world, UUID uuid) {
        try {
            Object worldHandle = NMSUtils.getHandle(world);
            final Map<UUID, Entity> entityMap = (Map<UUID, Entity>) NMSUtils.class_WorldServer_entitiesByUUIDField.get(worldHandle);
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

    @Override
    public Entity getEntity(UUID uuid) {
        if (NMSUtils.class_Server_getEntityMethod != null) {
            try {
                return (Entity) NMSUtils.class_Server_getEntityMethod.invoke(Bukkit.getServer(), uuid);
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

    @Override
    public boolean canRemoveRecipes() {
        return NMSUtils.class_Server_removeRecipeMethod != null;
    }

    @Override
    public boolean removeRecipe(Recipe recipe) {
        if (NMSUtils.class_Keyed == null || NMSUtils.class_Keyed_getKeyMethod == null || NMSUtils.class_Server_removeRecipeMethod == null) {
            return false;
        }
        if (!NMSUtils.class_Keyed.isAssignableFrom(recipe.getClass())) {
            return false;
        }
        try {
            Object namespacedKey = NMSUtils.class_Keyed_getKeyMethod.invoke(recipe);
            return (boolean) NMSUtils.class_Server_removeRecipeMethod.invoke(platform.getPlugin().getServer(), namespacedKey);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removeRecipe(String key) {
        if (NMSUtils.class_NamespacedKey == null || NMSUtils.class_Server_removeRecipeMethod == null) {
            return false;
        }

        try {
            Object namespacedKey = NMSUtils.class_NamespacedKey_constructor.newInstance(platform.getPlugin(), key.toLowerCase());
            return (boolean) NMSUtils.class_Server_removeRecipeMethod.invoke(platform.getPlugin().getServer(), namespacedKey);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public ShapedRecipe createShapedRecipe(String key, ItemStack item) {
        if (NMSUtils.class_NamespacedKey == null) {
            return new ShapedRecipe(item);
        }

        try {
            Object namespacedKey = NMSUtils.class_NamespacedKey_constructor.newInstance(platform.getPlugin(), key.toLowerCase());
            return (ShapedRecipe) NMSUtils.class_ShapedRecipe_constructor.newInstance(namespacedKey, item);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ShapedRecipe(item);
        }
    }

    @Override
    public boolean discoverRecipe(HumanEntity entity, String key) {
        if (NMSUtils.class_NamespacedKey == null || NMSUtils.class_HumanEntity_discoverRecipeMethod == null) {
            return false;
        }

        try {
            Object namespacedKey = NMSUtils.class_NamespacedKey_constructor.newInstance(platform.getPlugin(), key.toLowerCase());
            return (boolean) NMSUtils.class_HumanEntity_discoverRecipeMethod.invoke(entity, namespacedKey);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean undiscoverRecipe(HumanEntity entity, String key) {
        if (NMSUtils.class_NamespacedKey == null || NMSUtils.class_HumanEntity_undiscoverRecipeMethod == null) {
            return false;
        }

        try {
            Object namespacedKey = NMSUtils.class_NamespacedKey_constructor.newInstance(platform.getPlugin(), key.toLowerCase());
            return (boolean) NMSUtils.class_HumanEntity_undiscoverRecipeMethod.invoke(entity, namespacedKey);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public double getMaxHealth(Damageable li) {
        // return li.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        return li.getMaxHealth();
    }

    @Override
    public void setMaxHealth(Damageable li, double maxHealth) {
        // li.getAttribute(Attribute.GENERIC_MAX_HEALTH).setValue(maxHealth);
        li.setMaxHealth(maxHealth);
    }

    @Override
    public Material fromLegacy(org.bukkit.material.MaterialData materialData) {
        if (NMSUtils.class_UnsafeValues_fromLegacyDataMethod != null) {
            try {
                Material converted = (Material) NMSUtils.class_UnsafeValues_fromLegacyDataMethod.invoke(platform.getDeprecatedUtils().getUnsafe(), materialData);
                if (converted == Material.AIR) {
                    materialData.setData((byte)0);
                    converted = (Material) NMSUtils.class_UnsafeValues_fromLegacyDataMethod.invoke(platform.getDeprecatedUtils().getUnsafe(), materialData);
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

    @Override
    public boolean hasLegacyMaterials() {
        return NMSUtils.class_Material_isLegacyMethod != null;
    }

    @Override
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

    @Override
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

    @Override
    public boolean applyBonemeal(Location location) {
        if (NMSUtils.class_ItemDye_bonemealMethod == null) return false;

        if (dummyItem == null) {
             dummyItem = new ItemStack(Material.DIRT, 64);
             dummyItem = platform.getItemUtils().makeReal(dummyItem);
        }
        dummyItem.setAmount(64);

        try {
            Object world = NMSUtils.getHandle(location.getWorld());
            Object itemStack = platform.getItemUtils().getHandle(dummyItem);
            Object blockPosition = NMSUtils.class_BlockPosition_Constructor.newInstance(location.getX(), location.getY(), location.getZ());
            Object result = NMSUtils.class_ItemDye_bonemealMethod.invoke(null, itemStack, world, blockPosition);
            return (Boolean)result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
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

    @Override
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

    @Override
    public boolean hasBlockDataSupport() {
        return NMSUtils.class_Block_getBlockDataMethod != null;
    }

    @Override
    public byte getLegacyBlockData(FallingBlock falling) {
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

    @Override
    public String getBlockData(FallingBlock fallingBlock) {
        return null;
    }

    @Override
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

    @Override
    public String getBlockData(Material material, byte data) {
        if (NMSUtils.class_UnsafeValues_fromLegacyMethod == null) return null;
        try {
            Object blockData = NMSUtils.class_UnsafeValues_fromLegacyMethod.invoke(platform.getDeprecatedUtils().getUnsafe(), material, data);
            if (blockData != null) {
                return (String) NMSUtils.class_BlockData_getAsStringMethod.invoke(blockData);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean setBlockData(Block block, String data) {
        if (NMSUtils.class_Block_getBlockDataMethod == null) return false;
        try {
            Object blockData = NMSUtils.class_Server_createBlockDataMethod.invoke(platform.getPlugin().getServer(), data);
            NMSUtils.class_Block_setBlockDataMethod.invoke(block, blockData, false);
            return true;
        } catch (Exception ignore) {
            // Ignore issues setting invalid block data
        }
        return false;
    }

    @Override
    public boolean applyPhysics(Block block) {
        if (NMSUtils.class_World_setTypeAndDataMethod == null || NMSUtils.class_World_getTypeMethod == null || NMSUtils.class_BlockPosition_Constructor == null) return false;
        try {
            Object worldHandle = NMSUtils.getHandle(block.getWorld());
            Object blockLocation = NMSUtils.class_BlockPosition_Constructor.newInstance(block.getX(), block.getY(), block.getZ());
            Object blockType = NMSUtils.class_World_getTypeMethod.invoke(worldHandle, blockLocation);
            clearItems(block.getLocation());
            platform.getDeprecatedUtils().setTypeAndData(block, Material.AIR, (byte)0, false);
            return (boolean) NMSUtils.class_World_setTypeAndDataMethod.invoke(worldHandle, blockLocation, blockType, 3);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
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

    @Override
    public boolean setTorchFacingDirection(Block block, BlockFace facing) {
        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Directional)) {
            return false;
        }
        Directional directional = (Directional)blockData;
        directional.setFacing(facing);
        block.setBlockData(directional);
        return true;
    }

    @Override
    public boolean tame(Entity entity, Player tamer) {
        if (entity instanceof Fox) {
            if (tamer == null) return false;
            Fox fox = (Fox)entity;
            AnimalTamer current = fox.getFirstTrustedPlayer();
            if (current != null && current.getUniqueId().equals(tamer.getUniqueId())) {
                return false;
            }
            fox.setFirstTrustedPlayer(tamer);
        }
        return tame(entity, tamer);
    }

    @Override
    public boolean isArrow(Entity projectile) {
        return projectile instanceof AbstractArrow;
    }

    @Override
    public boolean canToggleBlockPower(Block block) {
        BlockData blockData = block.getBlockData();
        if (blockData == null) {
            return false;
        }
        if (blockData instanceof Powerable) {
            return true;
        }
        if (blockData instanceof Lightable) {
            return true;
        }
        if (blockData instanceof AnaloguePowerable) {
            return true;
        }
        return false;
    }

    @Override
    public boolean extendPiston(Block block) {
        BlockData blockData = block.getBlockData();
        if (blockData == null) {
            return false;
        }
        if (blockData instanceof Piston) {
            Piston piston = (Piston)blockData;
            piston.setExtended(true);
            block.setBlockData(piston, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean toggleBlockPower(Block block) {
        BlockData blockData = block.getBlockData();
        if (blockData == null) {
            return false;
        }
        if (blockData instanceof Powerable) {
            Powerable powerable = (Powerable)blockData;
            powerable.setPowered(!powerable.isPowered());
            block.setBlockData(powerable, true);
            return true;
        }
        if (blockData instanceof Lightable) {
            Lightable lightable = (Lightable)blockData;
            lightable.setLit(!lightable.isLit());
            block.setBlockData(lightable, true);
            return true;
        }
        if (blockData instanceof AnaloguePowerable) {
            AnaloguePowerable powerable = (AnaloguePowerable)blockData;
            powerable.setPower(powerable.getMaximumPower() - powerable.getPower());
            block.setBlockData(powerable, true);
            return true;
        }
        if (blockData instanceof Dispenser) {
            Dispenser dispenser = (Dispenser)blockData;
            dispenser.setTriggered(!dispenser.isTriggered());
        }
        return false;
    }

    @Override
    public boolean isPowerable(Block block) {
        BlockData blockData = block.getBlockData();
        return blockData != null && blockData instanceof Powerable;
    }

    @Override
    public boolean isPowered(Block block) {
        BlockData blockData = block.getBlockData();
        if (blockData == null || !(blockData instanceof Powerable)) return false;
        Powerable powerable = (Powerable)blockData;
        return powerable.isPowered();
    }

    @Override
    public boolean setPowered(Block block, boolean powered) {
        BlockData blockData = block.getBlockData();
        if (blockData == null || !(blockData instanceof Powerable)) return false;
        Powerable powerable = (Powerable)blockData;
        powerable.setPowered(powered);
        block.setBlockData(powerable, true);
        return true;
    }

    @Override
    public boolean isWaterLoggable(Block block) {
        BlockData blockData = block.getBlockData();
        return blockData != null && blockData instanceof Waterlogged;
    }

    @Override
    public boolean setWaterlogged(Block block, boolean waterlogged) {
        BlockData blockData = block.getBlockData();
        if (blockData == null || !(blockData instanceof Waterlogged)) return false;
        Waterlogged waterlogger = (Waterlogged)blockData;
        waterlogger.setWaterlogged(waterlogged);
        block.setBlockData(waterlogger, true);
        return true;
    }

    protected boolean setPoweredLegacy(Block block, boolean powered) {
        BlockState blockState = block.getState();
        org.bukkit.material.MaterialData data = blockState.getData();
        boolean powerBlock = false;
        if (data instanceof org.bukkit.material.Button) {
            org.bukkit.material.Button powerData = (org.bukkit.material.Button)data;
            powerData.setPowered(powered);
            powerBlock = true;
        } else if (data instanceof org.bukkit.material.Lever) {
            org.bukkit.material.Lever powerData = (org.bukkit.material.Lever)data;
            powerData.setPowered(powered);
            powerBlock = true;
        } else if (data instanceof org.bukkit.material.PistonBaseMaterial) {
            org.bukkit.material.PistonBaseMaterial powerData = (org.bukkit.material.PistonBaseMaterial)data;
            powerData.setPowered(powered);
            powerBlock = true;
        } else if (data instanceof org.bukkit.material.PoweredRail) {
            org.bukkit.material.PoweredRail powerData = (org.bukkit.material.PoweredRail)data;
            powerData.setPowered(powered);
            powerBlock = true;
        }
        if (powerBlock) {
            blockState.update();
        }
        return powerBlock;
    }

    @Override
    public boolean setTopHalf(Block block) {
        BlockData blockData = block.getBlockData();
        if (blockData == null || !(blockData instanceof Bisected)) return false;
        Bisected bisected = (Bisected)blockData;
        bisected.setHalf(Bisected.Half.TOP);
        block.setBlockData(bisected, false);
        return true;
    }

    protected boolean setTopHalfLegacy(Block block) {
        byte data = platform.getDeprecatedUtils().getData(block);
        platform.getDeprecatedUtils().setTypeAndData(block, block.getType(), (byte)(data | 8), false);
        return true;
    }

    @Override
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

    @Override
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

    @Override
    public boolean lockChunk(Chunk chunk) {
        if (!platform.getPlugin().isEnabled()) return false;
        if (!chunk.isLoaded()) {
            platform.getLogger().info("Locking unloaded chunk");
        }
        if (NMSUtils.class_Chunk_addPluginChunkTicketMethod == null) return false;
        try {
            NMSUtils.class_Chunk_addPluginChunkTicketMethod.invoke(chunk, platform.getPlugin());
            return true;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean unlockChunk(Chunk chunk) {
        if (!platform.getPlugin().isEnabled()) return false;
        if (NMSUtils.class_Chunk_removePluginChunkTicketMethod == null) return false;
        try {
            NMSUtils.class_Chunk_removePluginChunkTicketMethod.invoke(chunk, platform.getPlugin());
            return true;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
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

    @Override
    public void setMaterialCooldown(Player player, Material material, int duration) {
        player.setCooldown(material, duration);
    }

    @Override
    public Recipe createStonecuttingRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null || source == null) {
            return null;
        }
        try {
            RecipeChoice choice = getChoice(source, ignoreDamage);
            return new StonecuttingRecipe(namespacedKey, item, choice);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating stonecutting recipe", ex);
        }
        return null;
    }

    @Override
    public ShapelessRecipe createShapelessRecipe(String key, ItemStack item, Collection<ItemStack> ingredients, boolean ignoreDamage) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null) {
            return null;
        }
        ShapelessRecipe recipe;
        try {
            recipe = new ShapelessRecipe(namespacedKey, item);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating shapeless recipe", ex);
            return null;
        }
        for (ItemStack ingredient : ingredients) {
            recipe.addIngredient(getChoice(ingredient, ignoreDamage));
        }
        return recipe;
    }

    @SuppressWarnings("deprecation")
    protected RecipeChoice getChoice(ItemStack item, boolean ignoreDamage) {
        RecipeChoice.ExactChoice exactChoice;
        short maxDurability = item.getType().getMaxDurability();
        if (ignoreDamage && maxDurability > 0) {
            List<ItemStack> damaged = new ArrayList<>();
            for (short damage = 0; damage < maxDurability; damage++) {
                item = item.clone();
                ItemMeta meta = item.getItemMeta();
                if (meta == null || !(meta instanceof org.bukkit.inventory.meta.Damageable))  break;
                org.bukkit.inventory.meta.Damageable damageable = (org.bukkit.inventory.meta.Damageable)meta;
                damageable.setDamage(damage);
                item.setItemMeta(meta);
                damaged.add(item);
            }
            // Not really deprecated, just a draft API at this point but it works.
            exactChoice = new RecipeChoice.ExactChoice(damaged);
        } else {
            exactChoice = new RecipeChoice.ExactChoice(item);
        }
        return exactChoice;
    }

    @Override
    public FurnaceRecipe createFurnaceRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage, float experience, int cookingTime) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null || source == null) {
            return null;
        }
        try {
            RecipeChoice choice = getChoice(source, ignoreDamage);
            return new FurnaceRecipe(namespacedKey, item, choice, experience, cookingTime);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating furnace recipe", ex);
        }
        return null;
    }

    @Override
    public Recipe createBlastingRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage, float experience, int cookingTime) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null || source == null) {
            return null;
        }
        try {
            RecipeChoice choice = getChoice(source, ignoreDamage);
            return new BlastingRecipe(namespacedKey, item, choice, experience, cookingTime);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating blasting recipe", ex);
        }
        return null;
    }

    @Override
    public Recipe createCampfireRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage, float experience, int cookingTime) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null || source == null) {
            return null;
        }
        try {
            RecipeChoice choice = getChoice(source, ignoreDamage);
            return new CampfireRecipe(namespacedKey, item, choice, experience, cookingTime);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating campfire recipe", ex);
        }
        return null;
    }

    @Override
    public Recipe createSmokingRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage, float experience, int cookingTime) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null || source == null) {
            return null;
        }
        try {
            RecipeChoice choice = getChoice(source, ignoreDamage);
            return new SmokingRecipe(namespacedKey, item, choice, experience, cookingTime);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating smoking recipe", ex);
        }
        return null;
    }

    @Override
    public Particle getParticle(String particleType) {
        Particle particle;
        try {
            particle = Particle.valueOf(particleType.toUpperCase());
        } catch (Exception ignored) {
            particle = getParticleReplacement(particleType);
        }
        return particle;
    }

    public Particle getParticleReplacement(String particle) {
        switch (particle.toLowerCase()) {
            case "dust_color_transition":
                return Particle.REDSTONE;
            case "falling_water":
            case "falling_obsidian_tear":
                return Particle.DRIP_WATER;
            case "falling_dust":
            case "dripping_honey":
            case "falling_honey":
            case "landing_honey":
                return Particle.DRIP_LAVA;
            case "vibration":
                return Particle.SWEEP_ATTACK;
            case "soul_fire_flame":
            case "soul":
                return Particle.FLAME;
            case "bubble_column_up":
            case "bubble_pop":
            case "current_down":
                return Particle.WATER_BUBBLE;
            case "campfire_signal_smoke":
                return Particle.SMOKE_LARGE;
            case "campfire_cosy_smoke":
                return Particle.SMOKE_NORMAL;
            case "snowflake":
                return Particle.SNOWBALL;
            case "spit":
                return Particle.CLOUD;
            case "totem":
                return Particle.MOB_APPEARANCE;
            case "flash":
                return Particle.END_ROD;
        }
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void sendBlockChange(Player player, Block block) {
        player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
    }

    @Override
    public void sendBlockChange(Player player, Location location, Material material, String blockData) {
        if (blockData != null) {
            player.sendBlockChange(location, platform.getPlugin().getServer().createBlockData(blockData));
        } else {
            sendBlockChange(player, location, material, blockData);
        }
    }

    @Override
    public void sendMessage(CommandSender sender, String message) {
        if (ChatUtils.hasJSON(message)) {
            SpigotUtils spigot = platform.getSpigotUtils();
            if (spigot == null) {
                sender.sendMessage(ChatUtils.getSimpleMessage(message));
            } else {
                spigot.sendMessage(sender, message);
            }
        } else {
            sender.sendMessage(message);
        }
    }

    @Override
    public boolean setDisplayNameRaw(ItemStack itemStack, String displayName) {
        Object handle = platform.getItemUtils().getHandle(itemStack);
        if (handle == null) return false;
        Object tag = platform.getItemUtils().getOrCreateTag(handle);
        if (tag == null) return false;

        Object displayNode = platform.getNBTUtils().createTag(tag, "display");
        if (displayNode == null) return false;
        platform.getNBTUtils().setString(displayNode, "Name", displayName);
        return true;
    }

    @Override
    public boolean setDisplayName(ItemStack itemStack, String displayName) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(displayName);
        itemStack.setItemMeta(meta);
        return true;
    }

    public boolean setLegacyLore(ItemStack itemStack, List<String> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        // Convert chat components
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (ChatUtils.hasJSON(line)) {
                lore.set(i, ChatUtils.getSimpleMessage(line));
            }
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return true;
    }

    @Override
    public boolean setLore(ItemStack itemStack, List<String> lore) {
        SpigotUtils spigot = platform.getSpigotUtils();
        if (spigot == null) {
            return setLegacyLore(itemStack, lore);
        }
        List<String> serializedLore = spigot.serializeLore(lore);
        return setRawLore(itemStack, serializedLore);
    }

    @Override
    public boolean setRawLore(ItemStack itemStack, List<String> lore) {
        Object displayNode = platform.getNBTUtils().createTag(itemStack, "display");
        platform.getItemUtils().setStringList(displayNode, "Lore", lore);
        return true;
    }

    @Override
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

    @Override
    public boolean isPrimaryThread() {
        return primaryThread.get() == Thread.currentThread();
    }

    @Override
    public boolean performDoorAction(Block[] doorBlocks, DoorActionType actionType) {
        BlockData blockData = doorBlocks[0].getBlockData();
        if (!(blockData instanceof org.bukkit.block.data.type.Door)) {
            return false;
        }
        org.bukkit.block.data.type.Door doorData = (Door)blockData;
        switch (actionType) {
            case OPEN:
                if (doorData.isOpen()) {
                    return false;
                }
                doorData.setOpen(true);
                break;
            case CLOSE:
                if (!doorData.isOpen()) {
                    return false;
                }
                doorData.setOpen(false);
                break;
            case TOGGLE:
                doorData.setOpen(!doorData.isOpen());
            default:
                return false;
        }
        // Going to assume we only need to update one of them?
        doorBlocks[0].setBlockData(doorData);
        return true;
    }

    @Override
    public boolean checkDoorAction(Block[] doorBlocks, DoorActionType actionType) {
        BlockData blockData = doorBlocks[0].getBlockData();
        if (!(blockData instanceof Door)) {
            return false;
        }
        Door doorData = (Door)blockData;
        switch (actionType) {
            case OPEN:
                return !doorData.isOpen();
            case CLOSE:
                return doorData.isOpen();
            case TOGGLE:
                return true;
            default:
                return false;
        }
    }

    @Override
    public Block[] getDoorBlocks(Block targetBlock) {
        BlockData blockData = targetBlock.getBlockData();
        if (!(blockData instanceof Door)) {
            return null;
        }
        Door doorData = (Door)blockData;
        Block[] doorBlocks = new Block[2];
        if (doorData.getHalf() == Bisected.Half.TOP) {
            doorBlocks[1] = targetBlock;
            doorBlocks[0] = targetBlock.getRelative(BlockFace.DOWN);
        } else {
            doorBlocks[1] = targetBlock.getRelative(BlockFace.UP);
            doorBlocks[0] = targetBlock;
        }
        return doorBlocks;
    }

    @Override
    public boolean isAdult(Zombie zombie) {
        return !zombie.isBaby();
    }

    @Override
    public void setBaby(Zombie zombie) {
        zombie.setBaby(true);
    }

    @Override
    public void setAdult(Zombie zombie) {
        zombie.setBaby(false);
    }

    @Override
    public boolean isFilledMap(Material material) {
        return material == Material.FILLED_MAP;
    }

    @Override
    public BlockFace getSignFacing(Block sign) {
        BlockState blockState = sign.getState();
        MaterialData data = blockState.getData();
        if (!(data instanceof org.bukkit.material.Sign)) {
            return null;
        }

        org.bukkit.material.Sign signData = (org.bukkit.material.Sign)data;
        return signData.getFacing();
    }

    @Override
    public void openSign(Player player, Location signBlock) {
        try {
            Object tileEntity = platform.getCompatibilityUtils().getTileEntity(signBlock);
            Object playerHandle = NMSUtils.getHandle(player);
            if (tileEntity != null && playerHandle != null) {
                NMSUtils.class_EntityPlayer_openSignMethod.invoke(playerHandle, tileEntity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<String> getRawLore(ItemStack itemStack) {
        List<String> lore = new ArrayList<>();
        Object displayNode = platform.getNBTUtils().getTag(itemStack, "display");
        if (displayNode == null) {
            return lore;
        }
        return platform.getItemUtils().getStringList(displayNode, "Lore");
    }

    @Override
    public Recipe createSmithingRecipe(String key, ItemStack item, ItemStack source, ItemStack addition) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null || source == null) {
            return null;
        }
        try {
            RecipeChoice choice = new RecipeChoice.ExactChoice(source);
            RecipeChoice additionChoice = new RecipeChoice.ExactChoice(addition);
            return new SmithingRecipe(namespacedKey, item, choice, additionChoice);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating smithing recipe", ex);
        }
        return null;
    }

    @Override
    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
    }

    @Override
    public void swingMainHand(Entity entity) {
        int rangeSquared = OFFHAND_BROADCAST_RANGE * OFFHAND_BROADCAST_RANGE;
        String worldName = entity.getWorld().getName();
        Location center = entity.getLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getName().equals(worldName) || player.getLocation().distanceSquared(center) > rangeSquared) {
                continue;
            }
            swingMainHand(player, entity);
        }
    }

    private void swingMainHand(Player sendToPlayer, Entity entity) {
        try {
            Object packet = NMSUtils.class_PacketPlayOutAnimation_Constructor.newInstance(NMSUtils.getHandle(entity), 0);
            NMSUtils.sendPacket(sendToPlayer, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected boolean sendActionBarPackets(Player player, String message) {
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

    @Override
    public boolean sendActionBar(Player player, String message) {
        SpigotUtils spigot = platform.getSpigotUtils();
        if (spigot != null) {
            return spigot.sendActionBar(player, message);
        }
        return sendActionBarPackets(player, message);
    }

    @Override
    public boolean sendActionBar(Player player, String message, String font) {
        if (ChatUtils.isDefaultFont(font)) {
            return sendActionBar(player, message);
        }
        SpigotUtils spigot = platform.getSpigotUtils();
        if (spigot != null) {
            return spigot.sendActionBar(player, message, font);
        }
        // We can't support fonts here
        return false;
    }

    @Override
    public void setBossBarTitle(BossBar bossBar, String title) {
        if (ChatUtils.hasJSON(title)) {
            title = ChatUtils.getSimpleMessage(title);
        }
        bossBar.setTitle(title);
    }

    @Override
    public boolean setBossBarTitle(BossBar bossBar, String title, String font) {
        // We can't support fonts here
        if (!ChatUtils.isDefaultFont(font)) {
            return false;
        }
        setBossBarTitle(bossBar, title);
        return true;
    }

    @Override
    public boolean setRecipeGroup(Recipe recipe, String group) {
        if (recipe instanceof ShapedRecipe) {
            ((ShapedRecipe)recipe).setGroup(group);
            return true;
        }
        if (recipe instanceof CookingRecipe) {
            ((CookingRecipe<?>)recipe).setGroup(group);
            return true;
        }
        if (recipe instanceof StonecuttingRecipe) {
            ((StonecuttingRecipe)recipe).setGroup(group);
            return true;
        }
        return false;
    }

    @Override
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

    @Override
    public boolean isLegacyRecipes() {
        return NMSUtils.class_RecipeChoice_ExactChoice == null || NMSUtils.class_NamespacedKey == null;
    }

    @Override
    public boolean setRecipeIngredient(ShapedRecipe recipe, char key, ItemStack ingredient, boolean ignoreDamage) {
        if (ingredient == null) return false;
        if (NMSUtils.class_RecipeChoice_ExactChoice == null) {
            if (platform.isLegacy()) {
                org.bukkit.material.MaterialData material = ingredient.getData();
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
                for (short damage = 0; damage < maxDurability; damage++) {
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

    @Override
    @SuppressWarnings("unchecked")
    public boolean setAutoBlockState(Block block, Location target, BlockFace facing, boolean physics, Player originator) {
        if (NMSUtils.class_CraftBlock == null || block == null || facing == null || target == null) return false;
        try {
            Object nmsBlock = NMSUtils.class_CraftBlock_getNMSBlockMethod.invoke(block);
            if (nmsBlock == null) return false;
            ItemStack blockItem = new ItemStack(block.getType());
            Object originatorHandle = NMSUtils.getHandle(originator);
            Object world = NMSUtils.getHandle(block.getWorld());
            Object item = platform.getItemUtils().getHandle(platform.getItemUtils().makeReal(blockItem));
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void addPassenger(Entity vehicle, Entity passenger) {
        if (NMSUtils.class_Entity_addPassengerMethod != null) {
            try {
                NMSUtils.class_Entity_addPassengerMethod.invoke(vehicle, passenger);
                return;
            } catch (Exception ex) {
                platform.getLogger().log(Level.WARNING, "Error adding entity passenger", ex);
            }
        }
        platform.getDeprecatedUtils().setPassenger(vehicle, passenger);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Entity> getPassengers(Entity entity) {
        if (NMSUtils.class_Entity_getPassengersMethod != null) {
            try {
                return (List<Entity>) NMSUtils.class_Entity_getPassengersMethod.invoke(entity);
            } catch (Exception ex) {
                platform.getLogger().log(Level.WARNING, "Error getting entity passengers", ex);
            }
        }
        List<Entity> passengerList = new ArrayList<>();
        Entity passenger = platform.getDeprecatedUtils().getPassenger(entity);
        if (passenger != null) {
            passengerList.add(passenger);
        }
        return passengerList;
    }

    @Override
    public boolean openBook(Player player, ItemStack itemStack) {
        if (NMSUtils.class_Player_openBookMethod == null) {
            return false;
        }
        try {
            NMSUtils.class_Player_openBookMethod.invoke(player, itemStack);
            return true;
        } catch (Exception ex) {
            platform.getLogger().log(Level.SEVERE, "Unexpected error showing book", ex);
        }
        return false;
    }

    @Override
    public boolean isHandRaised(Player player) {
        if (NMSUtils.class_Player_isHandRaisedMethod == null) return false;
        try {
            return (boolean) NMSUtils.class_Player_isHandRaisedMethod.invoke(player);
        } catch (Exception ex) {
            platform.getLogger().log(Level.SEVERE, "Unexpected error checking block status", ex);
        }
        return false;
    }

    @Override
    public Class<?> getProjectileClass(String projectileTypeName) {
        Class<?> projectileType = NMSUtils.getBukkitClass("net.minecraft.server.Entity" + projectileTypeName);
        if (!isValidProjectileClass(projectileType)) {
            return null;
        }
        return projectileType;
    }

    @Override
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

            Object item = platform.getItemUtils().getHandle(platform.getItemUtils().makeReal(itemStack));
            final Object fireworkHandle = NMSUtils.class_EntityFireworkConstructor.newInstance(world, location.getX(), location.getY(), location.getZ(), item);
            setSilent(fireworkHandle, silent);

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
                Object fireworkPacket = NMSUtils.class_PacketSpawnEntityConstructor.newInstance(fireworkHandle, CompatibilityConstants.FIREWORK_TYPE);
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

    @Override
    public boolean loadAllTagsFromNBT(ConfigurationSection tags, Object tag)
    {
        try {
            Set<String> keys = platform.getInventoryUtils().getTagKeys(tag);
            if (keys == null) return false;

            for (String tagName : keys) {
                Object metaBase = NMSUtils.class_NBTTagCompound_getMethod.invoke(tag, tagName);
                if (metaBase != null) {
                    if (NMSUtils.class_NBTTagCompound.isAssignableFrom(metaBase.getClass())) {
                        ConfigurationSection newSection = tags.createSection(tagName);
                        loadAllTagsFromNBT(newSection, metaBase);
                    } else {
                        tags.set(tagName, platform.getInventoryUtils().getTagValue(metaBase));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public Collection<BoundingBox> getBoundingBoxes(Block block) {
        VoxelShape voxelShape = block.getCollisionShape();
        Collection<org.bukkit.util.BoundingBox> boxes = voxelShape.getBoundingBoxes();
        if (boxes.isEmpty()) {
            return getBoundingBoxes(block);
        }
        List<BoundingBox> converted = new ArrayList<>(boxes.size());
        Vector center = block.getLocation().toVector();

        for (org.bukkit.util.BoundingBox bukkitBB : boxes) {
            converted.add(new BoundingBox(center,
                    bukkitBB.getMinX(), bukkitBB.getMaxX(),
                    bukkitBB.getMinY(), bukkitBB.getMaxY(),
                    bukkitBB.getMinZ(), bukkitBB.getMaxZ())
            );
        }
        return converted;
    }

    @Override
    public UUID getOwnerId(Entity entity) {
        String ownerIdString = platform.getEnityMetadataUtils().getString(entity, MagicMetaKeys.OWNER);
        if (ownerIdString != null && !ownerIdString.isEmpty()) {
            try {
                return UUID.fromString(ownerIdString);
            } catch (Exception ex) {
                platform.getLogger().warning("Error parsing owner id from: " + ownerIdString);
            }
        }

        if (entity instanceof Tameable) {
            Tameable tamed = (Tameable)entity;
            AnimalTamer tamer = tamed.getOwner();
            return tamer == null ? null : tamer.getUniqueId();
        }
        return null;
    }

    protected void setOwner(Entity entity, Entity owner, UUID ownerId) {
        if (ownerId != null) {
            platform.getEnityMetadataUtils().setString(entity, MagicMetaKeys.OWNER, ownerId.toString());
        } else {
            platform.getEnityMetadataUtils().remove(entity, MagicMetaKeys.OWNER);
        }
        if (entity instanceof Tameable) {
            Tameable tamed = (Tameable)entity;
            if (owner != null && owner instanceof AnimalTamer) {
                tamed.setOwner((AnimalTamer)owner);
            } else {
                tamed.setOwner(null);
            }
        }
    }

    @Override
    public void setOwner(Entity entity, Entity owner) {
        setOwner(entity, owner, owner == null ? null : owner.getUniqueId());
    }

    @Override
    public void setOwner(Entity entity, UUID ownerId) {
        Entity owner = null;
        // Don't need to look up owner entity unless this is a tameable mob
        if (entity instanceof Tameable) {
            owner = ownerId != null ? getEntity(ownerId) : null;
        }
        setOwner(entity, owner, ownerId);
    }

    @Override
    @Nonnull
    public FallingBlock spawnFallingBlock(Location location, Material material, String blockDataString) {
        if (blockDataString != null && !blockDataString.isEmpty()) {
            BlockData blockData = PlatformInterpreter.getPlatform().getPlugin().getServer().createBlockData(blockDataString);
            return location.getWorld().spawnFallingBlock(location, blockData);
        }

        return spawnFallingBlock(location, material, blockDataString);
    }

    @Override
    public void setSnowLevel(Block block, int level) {
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Snow) {
            Snow snow = (Snow)blockData;
            snow.setLayers(level);
            block.setBlockData(blockData);
        }
    }

    @Override
    public int getSnowLevel(Block block) {
        int level = 0;
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Snow) {
            Snow snow = (Snow)blockData;
            level = snow.getLayers();
        }
        return level;
    }

    @Nullable
    public BlockPopulator createOutOfBoundsPopulator(Logger logger) {
        return null;
    }

    protected static String minecraftIdToBukkit(String id) {
        if (id == null) return null;
        id = id.toLowerCase();
        id = id.replace("minecraft:", "");
        return id;
    }

    @Override
    public Enchantment getInfinityEnchantment() {
        return Enchantment.ARROW_INFINITE;
    }

    @Override
    public Enchantment getPowerEnchantment() {
        return Enchantment.ARROW_DAMAGE;
    }

    @Override
    public PotionEffectType getJumpPotionEffectType() {
        return PotionEffectType.JUMP;
    }
}
