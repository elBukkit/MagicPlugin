package com.elmakers.mine.bukkit.utility.platform.base_v26_1;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
import org.bukkit.ExplosionResult;
import org.bukkit.FireworkEffect;
import org.bukkit.Keyed;
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
import org.bukkit.block.Container;
import org.bukkit.block.Jukebox;
import org.bukkit.block.Lectern;
import org.bukkit.block.Lockable;
import org.bukkit.block.Sign;
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
import org.bukkit.block.data.type.WallSign;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.CraftArt;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftArmorStand;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.entity.CraftHanging;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.scheduler.CraftTask;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ComplexEntityPart;
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
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sittable;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
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
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.loot.Lootable;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;
import org.yaml.snakeyaml.Yaml;

import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.utility.BoundingBox;
import com.elmakers.mine.bukkit.utility.ChatUtils;
import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.DoorActionType;
import com.elmakers.mine.bukkit.utility.EnteredStateTracker;
import com.elmakers.mine.bukkit.utility.LoadingChunk;
import com.elmakers.mine.bukkit.utility.ReflectionUtils;
import com.elmakers.mine.bukkit.utility.StringUtils;
import com.elmakers.mine.bukkit.utility.TeleportPassengerTask;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;
import com.elmakers.mine.bukkit.utility.platform.PaperUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.PlatformInterpreter;
import com.elmakers.mine.bukkit.utility.platform.SpigotUtils;
import com.elmakers.mine.bukkit.utility.platform.base_v26_1.populator.OutOfBoundsEntityCleanup;
import com.google.common.collect.Multimap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CompatibilityUtilsBase implements CompatibilityUtils {
    // This is really here to prevent infinite loops, but sometimes these requests legitimately come in many times
    // (for instance when undoing a spell in an unloaded chunk that threw a ton of different falling blocks)
    // So putting some lower number on this will trigger a lot of false-positives.
    protected static final int MAX_CHUNK_LOAD_TRY = 10000;
    protected static final int MAX_ENTITY_RANGE = 72;
    protected static boolean USE_MAGIC_DAMAGE = true;
    protected static int BLOCK_BREAK_RANGE = 64;

    private static final PotionEffectType[] _negativeEffects =
            {PotionEffectType.BLINDNESS, PotionEffectType.NAUSEA, PotionEffectType.INSTANT_DAMAGE,
                    PotionEffectType.HUNGER, PotionEffectType.POISON, PotionEffectType.WEAKNESS,
                    PotionEffectType.SLOWNESS, PotionEffectType.WEAKNESS, PotionEffectType.WITHER};
    protected static final Set<PotionEffectType> negativeEffects = new HashSet<>(Arrays.asList(_negativeEffects));

    protected static final BoundingBox BLOCK_BOUNDING_BOX = new BoundingBox(0, 1, 0, 1, 0, 1);
    protected final List<BoundingBox> blockBoundingBoxes = new ArrayList<>();

    protected final Pattern hexColorPattern = Pattern.compile("&(#[A-Fa-f0-9]{6})");
    private final Map<String, net.minecraft.world.entity.EntityType<?>> projectileEntityTypes = new HashMap<>();
    private final Map<String, Class<? extends net.minecraft.world.entity.projectile.Projectile>> projectileClasses = new HashMap<>();
    protected ItemStack dummyItem;
    protected boolean hasDumpedStack = false;
    protected boolean teleporting = false;
    protected final Map<World.Environment, Integer> maxHeights = new HashMap<>();
    protected final Map<LoadingChunk, Integer> loadingChunks = new HashMap<>();
    protected final EnteredStateTracker isDamaging = new EnteredStateTracker();
    protected final Map<World, WeakReference<ThrownPotion>> worldPotions = new WeakHashMap<>();
    protected final Platform platform;

    protected CompatibilityUtilsBase(final Platform platform) {
        this.platform = platform;
        // This will be replaced, but adding it here lets us initialize the list to be the right size
        // There's probably a cleaner way to get a mutable pre-initialized list, but I couldn't figure it ou.
        blockBoundingBoxes.add(BLOCK_BOUNDING_BOX);
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
    public boolean isInvulnerable(Entity entity) {
        return entity.isInvulnerable();
    }

    @Override
    public void setInvulnerable(Entity entity, boolean flag) {
        entity.setInvulnerable(flag);
    }

    @Override
    public void setInvulnerable(Entity entity) {
        setInvulnerable(entity, true);
    }

    @Override
    public ArmorStand createArmorStand(Location location) {
        return (ArmorStand)createEntity(location, EntityType.ARMOR_STAND);
    }

    private Component toNMSComponent(String serialized) {
        if (serialized == null || serialized.isEmpty()) {
            return null;
        }
        Component component;
        try {
            if (serialized.isEmpty()) {
                component = CommonComponents.EMPTY;
            } else if (serialized.startsWith("{")) {
                component = CraftChatMessage.fromJSON(serialized);
            } else {
                component = Component.literal(serialized);
            }
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Invalid JSON message: " + serialized, ex);
            component = CommonComponents.EMPTY;
        }
        return component;
    }

    private String fromNMSComponent(Component component) {
        return CraftChatMessage.toJSON(component);
    }

    @Override
    public Runnable getTaskRunnable(BukkitTask task) {
        return (Runnable)ReflectionUtils.getPrivate(platform.getLogger(), task, CraftTask.class, "rTask");
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
                    EntityDeathEvent deathEvent = new EntityDeathEvent((ArmorStand) target, DamageSource.builder(DamageType.MAGIC).withCausingEntity(source).build(), new ArrayList<>());
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
        net.minecraft.world.entity.Entity targetHandle = ((CraftEntity)target).getHandle();
        if (targetHandle == null) return;
        net.minecraft.world.entity.Entity sourceHandle = ((CraftEntity)source).getHandle();
        net.minecraft.world.damagesource.DamageSource damageSource = getDamageSource(damageType, sourceHandle, targetHandle.damageSources());
        if (damageSource == null) {
            magicDamage(target, amount, source);
            return;
        }

        try (EnteredStateTracker.Touchable damaging = isDamaging.enter()) {
            damaging.touch();
            targetHandle.hurt(damageSource, (float)amount);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected net.minecraft.world.damagesource.DamageSource getDamageSource(String damageType, net.minecraft.world.entity.Entity source, DamageSources damageSources) {
        switch (damageType.toUpperCase()) {
            case "IN_FIRE" : return damageSources.inFire();
            case "LIGHTNING_BOLT" : return damageSources.lightningBolt();
            case "ON_FIRE" : return damageSources.onFire();
            case "LAVA" : return damageSources.lava();
            case "HOT_FLOOR" : return damageSources.hotFloor();
            case "IN_WALL" : return damageSources.inWall();
            case "CRAMMING" : return damageSources.cramming();
            case "DROWN" : return damageSources.drown();
            case "STARVE" : return damageSources.starve();
            case "CACTUS" : return damageSources.cactus();
            case "FALL" : return damageSources.fall();
            case "FLY_INTO_WALL" : return damageSources.flyIntoWall();
            case "OUT_OF_WORLD" : return damageSources.fellOutOfWorld();
            case "GENERIC" : return damageSources.generic();
            case "MAGIC" : return damageSources.magic();
            case "WITHER" : return damageSources.wither();
            case "ANVIL" : return damageSources.anvil(source);
            case "FALLING_BLOCK" : return damageSources.fallingBlock(source);
            case "DRAGON_BREATH" : return damageSources.dragonBreath();
            case "DRY_OUT" : return damageSources.dryOut();
            case "SWEET_BERRY_BUSH" : return damageSources.sweetBerryBush();
            case "FREEZE" : return damageSources.freeze();
            case "FALLING_STALACTITE" : return damageSources.fallingStalactite(source);
            case "STALAGMITE" : return damageSources.stalagmite();
            default: return null;
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

    public YamlConfiguration debugLoadConfiguration(String fileName, String fileContents) {
        Yaml yaml = new Yaml();
        Map<String, Object> loadedMap = yaml.load(fileContents);
        YamlConfiguration combined = new YamlConfiguration();
        if (loadedMap == null) {
            platform.getLogger().info("File is missing or empty: " + fileName);
            return combined;
        }
        Map<String, Object> singleMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : loadedMap.entrySet()) {
            String key = entry.getKey();
            platform.getLogger().info("Loading " + entry.getKey() + " from " + fileName + ": ");
            singleMap.clear();
            singleMap.put(key, entry.getValue());
            StringWriter writer = new StringWriter();
            yaml.dump(singleMap, writer);
            String valueString = writer.toString();
            try {
                YamlConfiguration nodeYaml = new YamlConfiguration();
                nodeYaml.loadFromString(valueString);
                Object nodeValue = nodeYaml.get(key);
                if (nodeValue != null) {
                    combined.set(key, nodeYaml.get(key));
                } else {
                    platform.getLogger().log(Level.SEVERE, " The node " + entry.getKey() + " from " + fileName + " was empty");
                }
            } catch (Throwable ex) {
                platform.getLogger().log(Level.SEVERE, " Error loading " + entry.getKey() + " from " + fileName + ": " + ex.getMessage());
            }
        }
        return combined;
    }

    @Override
    public ConfigurationSection loadConfiguration(String fileName) throws IOException, InvalidConfigurationException {
        if (platform.getController().isDebugConfigurationFiles()) {
            return debugLoadConfiguration(fileName, Files.readString(Path.of(fileName)));
        }
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(fileName);
        } catch (FileNotFoundException ignore) {

        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, " Error reading configuration file '" + fileName + "': " + ex.getMessage());
        }
        return configuration;
    }

    @Override
    public ConfigurationSection loadConfiguration(File file) throws IOException, InvalidConfigurationException {
        if (platform.getController().isDebugConfigurationFiles()) {
            return debugLoadConfiguration(file.getPath(), Files.readString(file.toPath()));
        }
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(file);
        } catch (FileNotFoundException ignore) {

        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, " Error reading configuration file '" + file.getAbsolutePath() + "': " + ex.getMessage());
        }
        return configuration;
    }

    @Override
    public YamlConfiguration loadConfiguration(InputStream stream, String fileName) throws IOException, InvalidConfigurationException {
        if (platform.getController().isDebugConfigurationFiles()) {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = stream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            String contents = result.toString(StandardCharsets.UTF_8.name());
            return debugLoadConfiguration(fileName, contents);
        }
        YamlConfiguration configuration = new YamlConfiguration();
        if (stream == null) {
            platform.getLogger().log(Level.SEVERE, "Could not find builtin configuration file '" + fileName + "'");
            return configuration;
        }
        try {
            configuration.load(new InputStreamReader(stream, StandardCharsets.UTF_8.name()));
        } catch (FileNotFoundException ignore) {

        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, " Error reading configuration file '" + fileName + "': " + ex.getMessage());
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
        return world.getMinHeight();
    }

    @Override
    public int getMaxHeight(World world) {
        Integer maxHeight = maxHeights.get(world.getEnvironment());
        if (maxHeight == null) {
            maxHeight = world.getMaxHeight();
        }
        // The max build height is one less than this value, so we subtract one since this is
        // often used to prevent building above max height.
        return maxHeight - 1;
    }

    @Override
    public int getMaxEntityRange() {
        return MAX_ENTITY_RANGE;
    }

    @Override
    public void load(ConfigurationSection properties) {
        USE_MAGIC_DAMAGE = properties.getBoolean("use_magic_damage", USE_MAGIC_DAMAGE);
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
    public boolean setBlockFast(Block block, Material material, int data) {
        return setBlockFast(block.getChunk(), block.getX(), block.getY(), block.getZ(), material, data);
    }

    @Override
    public boolean setBlockFast(Chunk chunk, int x, int y, int z, Material material, int data) {
        // Bailed on this in 1.20
        chunk.getBlock(x, y, z).setType(material);
        return true;
    }

    @Override
    public Material getMaterial(FallingBlock falling) {
        return falling.getBlockData().getMaterial();
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
    public String getEnchantmentKey(Enchantment enchantment) {
        // We don't use toString here since we'll be parsing this ourselves
        return enchantment.getKey().getNamespace() + ":" + enchantment.getKey().getKey();
    }

    @Override
    @SuppressWarnings("deprecation")
    public Enchantment getEnchantmentByKey(String key) {
        // Really wish there was a fromString that took a string default namespace
        String namespace = NamespacedKey.MINECRAFT;
        Enchantment enchantment = null;
        if (key.contains(":")) {
            String[] pieces = StringUtils.split(key, ":", 2);
            namespace = pieces[0];
            key = pieces[1];
        } else {
            // Convert legacy enum names
            enchantment = Enchantment.getByName(key.toUpperCase(Locale.ROOT));
            if (enchantment != null) {
                return enchantment;
            }
        }

        // API says plugins aren't supposed to use this, but i have no idea how to deal
        // with custom enchants otherwise
        try {
            NamespacedKey namespacedKey = new NamespacedKey(namespace, key.toLowerCase(Locale.ROOT));
            enchantment = Enchantment.getByKey(namespacedKey);
            if (enchantment == null) {
                // Convert legacy enchantments
                enchantment = Enchantment.getByName(key.toUpperCase(Locale.ROOT));
            }
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Unexpected error parsing enchantment key", ex);
        }
        return enchantment;
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
        if (!(entity instanceof Tameable)) {
            return false;
        }

        Tameable tameable = (Tameable)entity;
        if (tameable.isTamed()) {
            return false;
        }
        tameable.setTamed(true);
        if (tamer != null) {
            tameable.setOwner(tamer);
        }
        return true;
    }

    @Override
    public boolean isArrow(Entity projectile) {
        return projectile instanceof AbstractArrow;
    }

    @Override
    public void setMaterialCooldown(Player player, Material material, int duration) {
        player.setCooldown(material, duration);
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

    @Override
    public Particle getParticle(String particleType) {
        Particle particle = null;
        try {
            particle = Particle.valueOf(particleType.toUpperCase());
        } catch (Exception ignored) {
        }
        return particle;
    }

    @Override
    public void sendBlockChange(Player player, Block block) {
        player.sendBlockChange(block.getLocation(), block.getBlockData());
    }

    @Override
    public void sendBlockChange(Player player, Location location, Material material, String blockData) {
        if (blockData != null) {
            player.sendBlockChange(location, platform.getPlugin().getServer().createBlockData(blockData));
        } else {
            player.sendBlockChange(location, platform.getPlugin().getServer().createBlockData(material));
        }
    }

    @Override
    @Nonnull
    public FallingBlock spawnFallingBlock(Location location, Material material, String blockDataString) {
        if (blockDataString != null && !blockDataString.isEmpty()) {
            BlockData blockData = PlatformInterpreter.getPlatform().getPlugin().getServer().createBlockData(blockDataString);
            return location.getWorld().spawnFallingBlock(location, blockData);
        }

        return location.getWorld().spawnFallingBlock(location, material, (byte)0);
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

    @Override
    public List<String> getRawLore(ItemStack itemStack) {
        List<String> lines = new ArrayList<>();
        ItemUtils itemUtils = platform.getItemUtils();
        net.minecraft.world.item.ItemStack mcItemStack = (net.minecraft.world.item.ItemStack)itemUtils.getHandle(itemStack);
        if (mcItemStack == null) return lines;
        ItemLore itemLore = mcItemStack.get(DataComponents.LORE);
        for (Component component : itemLore.lines()) {
            lines.add(fromNMSComponent(component));
        }
        return lines;
    }

    @Override
    public boolean setRawLore(ItemStack itemStack, List<String> lore) {
        ItemUtils itemUtils = platform.getItemUtils();
        net.minecraft.world.item.ItemStack mcItemStack = (net.minecraft.world.item.ItemStack)itemUtils.getHandle(itemStack);
        if (mcItemStack == null) {
            return false;
        }
        List<Component> components = new ArrayList<>();
        for (String line : lore) {
            components.add(toNMSComponent(line));
        }
        ItemLore loreComponent = new ItemLore(components);
        mcItemStack.set(DataComponents.LORE, loreComponent);
        return true;
    }

    @Override
    public boolean setLore(ItemStack itemStack, List<String> lore) {
        SpigotUtils spigot = platform.getSpigotUtils();
        if (spigot == null) {
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
        List<String> serializedLore = spigot.serializeLore(lore);
        return setRawLore(itemStack, serializedLore);
    }

    protected boolean sendActionBarPackets(Player player, String message) {
        Component component = Component.literal(message);
        ClientboundSystemChatPacket packet = new ClientboundSystemChatPacket(component, true);
        try {
            sendPacket(player, packet);
        } catch (Exception ex) {
            platform.getLogger().log(Level.SEVERE, "Error updating action bar", ex);
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
    public boolean setBossBarTitle(BossBar bossBar, String title, String font) {
        if (ChatUtils.isDefaultFont(font)) {
            setBossBarTitle(bossBar, title);
            return true;
        }
        SpigotUtils spigot = platform.getSpigotUtils();
        if (spigot == null) {
            // Can't do fonts without chat components
            return false;
        }
        setBossBarTitleComponents(bossBar, spigot.serializeBossBar(title, font), title);
        return true;
    }

    @Override
    public void setBossBarTitle(BossBar bossBar, String title) {
        if (ChatUtils.hasJSON(title)) {
            SpigotUtils spigot = platform.getSpigotUtils();
            if (spigot != null) {
                setBossBarTitleComponents(bossBar, spigot.serializeBossBar(title), title);
            } else {
                bossBar.setTitle(ChatUtils.getSimpleMessage(title));
            }
        } else {
            bossBar.setTitle(title);
        }
    }

    protected void setBossBarTitleComponents(BossBar bossBar, String serialized, String fallback) {
        Object handle = ReflectionUtils.getHandle(platform.getLogger(), bossBar);
        if (handle == null || !(handle instanceof ServerBossEvent)) {
            bossBar.setTitle(fallback);
            return;
        }
        ServerBossEvent bossEvent = (ServerBossEvent)handle;
        Component component = toNMSComponent(serialized);
        if (component == null) {
            bossBar.setTitle(fallback);
        } else {
            bossEvent.setName(component);
        }
    }

    @Override
    public Collection<BoundingBox> getBoundingBoxes(Block block) {
        VoxelShape voxelShape = block.getCollisionShape();
        Collection<org.bukkit.util.BoundingBox> boxes = voxelShape.getBoundingBoxes();
        if (boxes.isEmpty()) {
            BoundingBox translated = new BoundingBox(block.getLocation().toVector(), BLOCK_BOUNDING_BOX);
            blockBoundingBoxes.set(0, translated);
            return blockBoundingBoxes;
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
    public void setSnowLevel(Block block, int level) {
        org.bukkit.block.data.BlockData blockData = block.getBlockData();
        if (blockData instanceof Snow) {
            Snow snow = (Snow)blockData;
            snow.setLayers(level);
            block.setBlockData(blockData);
        }
    }

    @Override
    public int getSnowLevel(Block block) {
        int level = 0;
        org.bukkit.block.data.BlockData blockData = block.getBlockData();
        if (blockData instanceof Snow) {
            Snow snow = (Snow)blockData;
            level = snow.getLayers();
        }
        return level;
    }

    @Nullable
    public BlockPopulator createOutOfBoundsPopulator(Logger logger) {
        return new OutOfBoundsEntityCleanup(logger);
    }

    @Override
    public Enchantment getInfinityEnchantment() {
        return Enchantment.INFINITY;
    }

    @Override
    public Enchantment getPowerEnchantment() {
        return Enchantment.POWER;
    }

    @Override
    public PotionEffectType getJumpPotionEffectType() {
        return PotionEffectType.JUMP_BOOST;
    }

    @Override
    public Set<PotionEffectType> getNegativeEffects() {
        return negativeEffects;
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
    public boolean performDoorAction(Block[] doorBlocks, DoorActionType actionType) {
        BlockData blockData = doorBlocks[0].getBlockData();
        if (!(blockData instanceof Door)) {
            return false;
        }
        Door doorData = (Door)blockData;
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
                break;
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

    @Override
    public boolean setTopHalf(Block block) {
        BlockData blockData = block.getBlockData();
        if (blockData == null || !(blockData instanceof Bisected)) return false;
        Bisected bisected = (Bisected)blockData;
        bisected.setHalf(Bisected.Half.TOP);
        block.setBlockData(bisected, false);
        return true;
    }

    @Override
    public boolean isFilledMap(Material material) {
        return material == Material.FILLED_MAP;
    }

    @Override
    public Inventory createInventory(InventoryHolder holder, int size, final String name) {
        size = (int) (Math.ceil((double) size / 9) * 9);
        size = Math.min(size, 54);
        String translatedName = translateColors(name);
        return Bukkit.createInventory(holder, size, translatedName);
    }

    @Override
    public boolean isSilent(Entity entity) {
        return entity.isSilent();
    }

    @Override
    public void setSilent(Entity entity, boolean flag) {
        entity.setSilent(flag);
    }

    @Override
    public boolean isPersist(Entity entity) {
        return entity.isPersistent();
    }

    @Override
    public void setPersist(Entity entity, boolean flag) {
        entity.setPersistent(flag);
    }

    @Override
    public void setRemoveWhenFarAway(Entity entity, boolean flag) {
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity li = (LivingEntity)entity;
        li.setRemoveWhenFarAway(flag);
    }

    @Override
    public boolean isSitting(Entity entity) {
        if (!(entity instanceof Sittable)) return false;
        Sittable sittable = (Sittable)entity;
        return sittable.isSitting();
    }

    @Override
    public void setSitting(Entity entity, boolean flag) {
        if (!(entity instanceof Sittable)) return;
        Sittable sittable = (Sittable)entity;
        sittable.setSitting(flag);
    }

    private void addProjectileClass(String key, Class<? extends net.minecraft.world.entity.projectile.Projectile> projectileClass, net.minecraft.world.entity.EntityType<?> entityType) {
        projectileClasses.put(key, projectileClass);
        projectileEntityTypes.put(projectileClass.getSimpleName(), entityType);
    }

    protected void populateProjectileClasses() {
        // Can't use reflection, so gonna do this the hard (coded) way.
        addProjectileClass("arrow", net.minecraft.world.entity.projectile.arrow.Arrow.class, net.minecraft.world.entity.EntityType.ARROW);
        addProjectileClass("tippedarrow", net.minecraft.world.entity.projectile.arrow.Arrow.class, net.minecraft.world.entity.EntityType.ARROW);
        addProjectileClass("tipped_arrow", net.minecraft.world.entity.projectile.arrow.Arrow.class, net.minecraft.world.entity.EntityType.ARROW);
        addProjectileClass("dragonfireball", net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball.class, net.minecraft.world.entity.EntityType.DRAGON_FIREBALL);
        addProjectileClass("dragon_fireball", net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball.class, net.minecraft.world.entity.EntityType.DRAGON_FIREBALL);
        addProjectileClass("fireball", net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball.class, net.minecraft.world.entity.EntityType.FIREBALL);
        addProjectileClass("largefireball", net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball.class, net.minecraft.world.entity.EntityType.FIREBALL);
        addProjectileClass("large_fireball", net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball.class, net.minecraft.world.entity.EntityType.FIREBALL);
        addProjectileClass("smallfireball", net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball.class, net.minecraft.world.entity.EntityType.SMALL_FIREBALL);
        addProjectileClass("small_fireball", net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball.class, net.minecraft.world.entity.EntityType.SMALL_FIREBALL);
        addProjectileClass("fireworks", net.minecraft.world.entity.projectile.FireworkRocketEntity.class, net.minecraft.world.entity.EntityType.FIREWORK_ROCKET);
        addProjectileClass("firework", net.minecraft.world.entity.projectile.FireworkRocketEntity.class, net.minecraft.world.entity.EntityType.FIREWORK_ROCKET);
        addProjectileClass("fireworkrocket", net.minecraft.world.entity.projectile.FireworkRocketEntity.class, net.minecraft.world.entity.EntityType.FIREWORK_ROCKET);
        addProjectileClass("firework_rocket", net.minecraft.world.entity.projectile.FireworkRocketEntity.class, net.minecraft.world.entity.EntityType.FIREWORK_ROCKET);
        addProjectileClass("fireworkrocketentity", net.minecraft.world.entity.projectile.FireworkRocketEntity.class, net.minecraft.world.entity.EntityType.FIREWORK_ROCKET);
        addProjectileClass("fishinghook", net.minecraft.world.entity.projectile.FishingHook.class, net.minecraft.world.entity.EntityType.FISHING_BOBBER);
        addProjectileClass("fishing_hook", net.minecraft.world.entity.projectile.FishingHook.class, net.minecraft.world.entity.EntityType.FISHING_BOBBER);
        addProjectileClass("fishing_bobber", net.minecraft.world.entity.projectile.FishingHook.class, net.minecraft.world.entity.EntityType.FISHING_BOBBER);
        addProjectileClass("llamaspit", net.minecraft.world.entity.projectile.LlamaSpit.class, net.minecraft.world.entity.EntityType.LLAMA_SPIT);
        addProjectileClass("llama_spit", net.minecraft.world.entity.projectile.LlamaSpit.class, net.minecraft.world.entity.EntityType.LLAMA_SPIT);
        addProjectileClass("shulkerbullet", net.minecraft.world.entity.projectile.ShulkerBullet.class, net.minecraft.world.entity.EntityType.SHULKER_BULLET);
        addProjectileClass("shulker_bullet", net.minecraft.world.entity.projectile.ShulkerBullet.class, net.minecraft.world.entity.EntityType.SHULKER_BULLET);
        addProjectileClass("snowball", net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball.class, net.minecraft.world.entity.EntityType.SNOWBALL);
        addProjectileClass("spectralarrow", net.minecraft.world.entity.projectile.arrow.SpectralArrow.class, net.minecraft.world.entity.EntityType.SPECTRAL_ARROW);
        addProjectileClass("spectral_arrow", net.minecraft.world.entity.projectile.arrow.SpectralArrow.class, net.minecraft.world.entity.EntityType.SPECTRAL_ARROW);
        addProjectileClass("egg", net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg.class, net.minecraft.world.entity.EntityType.EGG);
        addProjectileClass("thrownegg", net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg.class, net.minecraft.world.entity.EntityType.EGG);
        addProjectileClass("enderpearl", net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl.class, net.minecraft.world.entity.EntityType.ENDER_PEARL);
        addProjectileClass("ender_pearl", net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl.class, net.minecraft.world.entity.EntityType.ENDER_PEARL);
        addProjectileClass("thrownenderpearl", net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl.class, net.minecraft.world.entity.EntityType.ENDER_PEARL);
        addProjectileClass("thrownexperiencebottle", net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle.class, net.minecraft.world.entity.EntityType.EXPERIENCE_BOTTLE);
        addProjectileClass("experiencebottle", net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle.class, net.minecraft.world.entity.EntityType.EXPERIENCE_BOTTLE);
        addProjectileClass("thrownpotion", net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion.class, net.minecraft.world.entity.EntityType.SPLASH_POTION);
        addProjectileClass("potion", net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion.class, net.minecraft.world.entity.EntityType.SPLASH_POTION);
        addProjectileClass("throwntrident", net.minecraft.world.entity.projectile.arrow.ThrownTrident.class, net.minecraft.world.entity.EntityType.TRIDENT);
        addProjectileClass("trident", net.minecraft.world.entity.projectile.arrow.ThrownTrident.class, net.minecraft.world.entity.EntityType.TRIDENT);
        addProjectileClass("witherskull", net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull.class, net.minecraft.world.entity.EntityType.WITHER_SKULL);
        addProjectileClass("wither_skull", net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull.class, net.minecraft.world.entity.EntityType.WITHER_SKULL);
    }

    @Override
    public Painting createPainting(Location location, BlockFace facing, Art art) {
        Painting newPainting = null;
        ServerLevel level = ((CraftWorld)location.getWorld()).getHandle();
        Direction directionEnum = null;
        try {
            directionEnum = Direction.valueOf(facing.name());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        BlockPos blockLocation = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        net.minecraft.world.entity.decoration.painting.Painting newEntity = new net.minecraft.world.entity.decoration.painting.Painting(level, blockLocation, directionEnum, Holder.direct(CraftArt.bukkitToMinecraft(art)));
        Entity bukkitEntity = newEntity.getBukkitEntity();
        if (bukkitEntity != null && bukkitEntity instanceof Painting) {
            newPainting = (Painting)bukkitEntity;
        }
        return newPainting;
    }

    @Override
    public ItemFrame createItemFrame(Location location, BlockFace facing, Rotation rotation, ItemStack item) {
        ItemFrame newItemFrame = null;
        ServerLevel level = ((CraftWorld)location.getWorld()).getHandle();
        Direction directionEnum = null;
        try {
            directionEnum = Direction.valueOf(facing.name());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        BlockPos blockLocation = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        net.minecraft.world.entity.decoration.ItemFrame newEntity = new net.minecraft.world.entity.decoration.ItemFrame(level, blockLocation, directionEnum);
        Entity bukkitEntity = newEntity.getBukkitEntity();
        if (bukkitEntity != null && bukkitEntity instanceof ItemFrame) {
            newItemFrame = (ItemFrame)bukkitEntity;
            newItemFrame.setItem(platform.getItemUtils().getCopy(item));
            newItemFrame.setRotation(rotation);
        }
        return newItemFrame;
    }

    @Override
    public Entity createEntity(Location location, EntityType entityType) {
        World world = location.getWorld();
        Entity bukkitEntity = null;
        try {
            bukkitEntity = world.createEntity(location, entityType.getEntityClass());
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return bukkitEntity;
    }

    @Override
    public boolean addToWorld(World world, Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        ServerLevel level = ((CraftWorld)world).getHandle();
        net.minecraft.world.entity.Entity entityHandle = ((CraftEntity)entity).getHandle();
        level.addFreshEntity(entityHandle, reason);
        return true;
    }

    @Override
    public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z) {
        if (location == null) return null;
        x = Math.min(x, MAX_ENTITY_RANGE);
        z = Math.min(z, MAX_ENTITY_RANGE);
        // Note this no longer special-cases ComplexParts
        return location.getWorld().getNearbyEntities(location, x, y, z);
    }

    @Override
    public void ageItem(Item item, int ticksToAge) {
        ItemEntity nmsItem = ((CraftItem)item).getHandle();
        nmsItem.age = ticksToAge;
    }

    @Override
    public void magicDamage(Damageable target, double amount, Entity source) {
        try {
            if (target == null || target.isDead()) return;

            // Special-case for witches .. witches are immune to magic damage :\
            // And endermen are immune to indirect damage .. or something.
            // Also armor stands suck.
            // Might need to config-drive this, or just go back to defaulting to normal damage
            if (!USE_MAGIC_DAMAGE || target instanceof Witch || target instanceof Enderman || target instanceof ArmorStand || !(target instanceof LivingEntity)) {
                damage(target, amount, source);
                return;
            }

            net.minecraft.world.entity.Entity targetHandle = ((CraftEntity)target).getHandle();
            if (targetHandle == null) return;

            net.minecraft.world.entity.Entity sourceHandle = source == null ? null : ((CraftEntity)source).getHandle();

            // Bukkit won't allow magic damage from anything but a potion..
            if (sourceHandle != null && source instanceof LivingEntity) {
                Location location = target.getLocation();

                ThrownPotion potion = getOrCreatePotionEntity(location);
                net.minecraft.world.entity.Entity potionHandle = ((CraftEntity)potion).getHandle();
                potion.setShooter((LivingEntity) source);

                net.minecraft.world.damagesource.DamageSource magicSource = sourceHandle.damageSources().indirectMagic(potionHandle, sourceHandle);

                /*
                // We can't  modify the damage source anymore, it is taken from a common registry, rather than
                // created on demand. We'll need to check and make sure that the ender dragon is damageable from spells.
                // This is a bit of hack that lets us damage the ender dragon, who is a weird and annoying collection
                // of various non-living entity pieces.
                ((EntityDamageSource)magicSource).setThorns();
                */

                try (EnteredStateTracker.Touchable damaging = isDamaging.enter()) {
                    damaging.touch();
                    targetHandle.hurt(magicSource, (float)amount);
                }
            } else {
                try (EnteredStateTracker.Touchable damaging = isDamaging.enter()) {
                    damaging.touch();
                    targetHandle.hurt(targetHandle.damageSources().magic(), (float)amount);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isReady(Chunk chunk) {
        return true;
    }

    @Override
    public boolean createExplosion(Entity entity, World world, double x, double y, double z, float power, boolean setFire, boolean breakBlocks) {
        return world.createExplosion(x, y, z, power, setFire, breakBlocks, entity);
    }

    @Override
    public BlockState getTileEntityData(Location location) {
        if (location == null) return null;
        return location.getBlock().getState();
    }

    protected AttributeModifier createAttributeModifier(UUID attributeUUID, double value, AttributeModifier.Operation operation, EquipmentSlotGroup equipmentSlotGroup) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), "modifier");
        return new AttributeModifier(namespacedKey, value, operation, equipmentSlotGroup);
    }

    @Override
    public Object getTileEntity(Location location) {
        throw new UnsupportedOperationException("The getTileEntity method is no longer supported");
    }

    @Override
    public void clearItems(Location location) {
        if (location == null) return;

        // Block-specific behaviors
        Block block = location.getBlock();
        BlockState blockState = block.getState();
        if (blockState instanceof Lootable) {
            Lootable lootable = (Lootable)blockState;
            lootable.setLootTable(null);
            blockState.update();
        }
        if (blockState instanceof Lectern) {
            Lectern lectern = (Lectern)blockState;
            lectern.getInventory().setItem(0, new ItemStack(Material.AIR));
            blockState.update();
        }
        if (blockState instanceof Jukebox) {
            ((Jukebox) blockState).setRecord(null);
            blockState.update();
        }
        if (blockState instanceof Container) {
            ((Container) blockState).getInventory().clear();
            blockState.update();
        }
    }

    @Override
    public void setTileEntityData(Location location, Object data) {
        if (location == null || data == null || !(data instanceof BlockState)) return;
        BlockState blockState = (BlockState)data;
        blockState.copy(location);
        blockState.update();
    }

    @Override
    public void setEnvironment(World world, World.Environment environment) {
        // Nah, broken and too ugly anyway
    }

    @Override
    public void playCustomSound(Player player, Location location, String sound, float volume, float pitch) {
        player.playSound(location, sound, volume, pitch);
    }

    @Override
    public List<Entity> selectEntities(CommandSender sender, String selector) {
        if (!selector.startsWith("@")) return null;
        try {
            return Bukkit.selectEntities(sender, selector);
        } catch (IllegalArgumentException ex) {
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
    @SuppressWarnings("deprecation")
    public MapView getMapById(int id) {
        return Bukkit.getMap(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getTypedMap(ConfigurationSection section) {
        if (section == null) return null;
        if (section instanceof MemorySection) {
            return (Map<String, T>) ReflectionUtils.getPrivate(platform.getLogger(), section, MemorySection.class, "map");
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
    public boolean setMap(ConfigurationSection section, Map<String, Object> map) {
        if (section == null) return false;
        if (section instanceof MemorySection) {
            return ReflectionUtils.setPrivate(platform.getLogger(), section, MemorySection.class, "map", map);
        }

        return true;
    }

    @Override
    public Vector getPosition(Object entityData, String tag) {
        if (entityData == null || !(entityData instanceof CompoundTag)) return null;
        CompoundTag data = (CompoundTag)entityData;
        Optional<ListTag> optionalList = data.getList(tag);
        if (!optionalList.isPresent()) return null;
        ListTag list = optionalList.get();
        Optional<Double> optionalX = list.getDouble(0);
        Optional<Double> optionalY = list.getDouble(1);
        Optional<Double> optionalZ = list.getDouble(2);
        if (!optionalX.isPresent() || !optionalY.isPresent() || !optionalZ.isPresent()) return null;
        double x = optionalX.get();
        double y = optionalY.get();
        double z = optionalZ.get();
        return new Vector(x, y, z);
    }

    @Override
    public BlockVector getBlockVector(Object entityData, String tag) {
        if (entityData == null || !(entityData instanceof CompoundTag)) return null;
        CompoundTag data = (CompoundTag)entityData;
        Optional<int[]> optionalArray = data.getIntArray(tag);
        if (!optionalArray.isPresent()) return null;
        int[] coords = optionalArray.get();
        if (coords.length < 3) return null;
        return new BlockVector(coords[0], coords[1], coords[2]);
    }

    @Override
    public void setTNTSource(TNTPrimed tnt, LivingEntity source) {
        tnt.setSource(source);
    }

    @Override
    public void setEntityMotion(Entity entity, Vector motion) {
        net.minecraft.world.entity.Entity nms = ((CraftEntity)entity).getHandle();
        nms.setDeltaMovement(new Vec3(motion.getX(), motion.getY(), motion.getZ()));
    }

    @Override
    public boolean setLock(Block block, String lockName) {
        BlockState blockData = block.getState();
        if (!(blockData instanceof Lockable)) return false;
        Lockable lockable = (Lockable)blockData;
        lockable.setLock(lockName);
        blockData.update();
        return true;
    }

    @Override
    public boolean clearLock(Block block) {
        BlockState blockData = block.getState();
        if (!(blockData instanceof Lockable)) return false;
        Lockable lockable = (Lockable)blockData;
        lockable.setLock(null);
        blockData.update();
        return true;
    }

    @Override
    public boolean isLocked(Block block) {
        BlockState blockData = block.getState();
        if (!(blockData instanceof Lockable)) return false;
        Lockable lockable = (Lockable)blockData;
        return lockable.isLocked();
    }

    @Override
    public String getLock(Block block) {
        BlockState blockData = block.getState();
        if (!(blockData instanceof Lockable)) return null;
        Lockable lockable = (Lockable)blockData;
        return lockable.getLock();
    }

    @Override
    public void setFallingBlockDamage(FallingBlock entity, float fallHurtAmount, int fallHurtMax) {
        entity.setHurtEntities(true);
        FallingBlockEntity nms = (FallingBlockEntity)((CraftEntity)entity).getHandle();
        nms.fallDamageMax = fallHurtMax;
        nms.fallDamagePerDistance = fallHurtAmount;
    }

    @Override
    public void setInvisible(Entity entity, boolean invisible) {
        net.minecraft.world.entity.Entity nms = ((CraftEntity)entity).getHandle();
        nms.setInvisible(invisible);
    }

    @Override
    public void setInvisible(ArmorStand armorStand, boolean invisible) {
        armorStand.setInvisible(invisible);
    }

    @Override
    public Boolean isInvisible(Entity entity) {
        net.minecraft.world.entity.Entity nms = ((CraftEntity)entity).getHandle();
        return nms.isInvisible();
    }

    @Override
    public void setGravity(ArmorStand armorStand, boolean gravity) {
        // I think the NMS method may be slightly different, so if things go wrong we'll have to dig deeper
        armorStand.setGravity(gravity);
    }

    @Override
    public void setGravity(Entity entity, boolean gravity) {
        entity.setGravity(gravity);
    }

    @Override
    public void setDisabledSlots(ArmorStand armorStand, int disabledSlots) {
        net.minecraft.world.entity.decoration.ArmorStand nms = ((CraftArmorStand)armorStand).getHandle();
        nms.disabledSlots = disabledSlots;
    }

    @Override
    public int getDisabledSlots(ArmorStand armorStand) {
        net.minecraft.world.entity.decoration.ArmorStand nms = ((CraftArmorStand)armorStand).getHandle();
        return nms.disabledSlots;
    }

    @Override
    public boolean isPersistentInvisible(Entity entity) {
        net.minecraft.world.entity.Entity nms = ((CraftEntity)entity).getHandle();
        return nms.persistentInvisibility;
    }

    @Override
    public void setPersistentInvisible(Entity entity, boolean invisible) {
        net.minecraft.world.entity.Entity nms = ((CraftEntity)entity).getHandle();
        nms.persistentInvisibility = invisible;
    }

    @Override
    public void setYawPitch(Entity entity, float yaw, float pitch) {
        net.minecraft.world.entity.Entity nms = ((CraftEntity)entity).getHandle();
        // Entity setYawPitch is protected, but I think we can do without the extra
        // CraftBukkit checks ... er, hopefully.
        nms.setYRot(yaw % 360.0F);
        nms.setXRot(pitch % 360.0F);
    }

    @Override
    public void setLocation(Entity entity, double x, double y, double z, float yaw, float pitch) {
        net.minecraft.world.entity.Entity nms = ((CraftEntity)entity).getHandle();
        nms.absSnapTo(x, y, z, yaw, pitch);
    }

    @Override
    public void addFlightExemption(Player player, int ticks) {
        ServerPlayer nms = ((CraftPlayer)player).getHandle();
        ReflectionUtils.setPrivate(platform.getLogger(), nms.connection, ServerGamePacketListenerImpl.class, "aboveGroundTickCount", -ticks);
    }

    @Override
    public boolean isValidProjectileClass(Class<?> projectileType) {
        return projectileType != null && net.minecraft.world.entity.projectile.Projectile.class.isAssignableFrom(projectileType);
    }

    @Override
    public Projectile spawnProjectile(Class<?> projectileType, Location location, Vector direction, ProjectileSource source, float speed, float spread, float spreadLocations, Random random) {
        Constructor<? extends Object> constructor = null;
        ServerLevel nmsWorld = ((CraftWorld)location.getWorld()).getHandle();
        Projectile projectile = null;
        try {
            Object entityType = null;
            constructor = projectileType.getConstructor(net.minecraft.world.entity.EntityType.class, net.minecraft.world.level.Level.class);
            entityType = projectileEntityTypes.get(projectileType.getSimpleName());
            if (entityType == null) {
                throw new Exception("Failed to find entity type for projectile class " + projectileType.getName());
            }

            Object nmsProjectile = null;
            try {
                nmsProjectile = constructor.newInstance(entityType, nmsWorld);
            } catch (Exception ex) {
                nmsProjectile = null;
                platform.getLogger().log(Level.WARNING, "Error spawning projectile of class " + projectileType.getName(), ex);
            }

            if (nmsProjectile == null || !(nmsProjectile instanceof net.minecraft.world.entity.Entity)) {
                throw new Exception("Failed to spawn projectile of class " + projectileType.getName());
            }

            // Set position and rotation, and potentially velocity (direction)
            // Velocity must be set manually- EntityFireball.setDirection applies a crazy-wide gaussian distribution!
            if (nmsProjectile instanceof AbstractHurtingProjectile) {
                AbstractHurtingProjectile fireballIsh = (AbstractHurtingProjectile)nmsProjectile;
                // Taken from EntityArrow
                double spreadWeight = Math.min(0.4f,  spread * 0.007499999832361937D);

                double dx = direction.getX() + (random.nextGaussian() * spreadWeight);
                double dy = direction.getY() + (random.nextGaussian() * spreadWeight);
                double dz = direction.getZ() + (random.nextGaussian() * spreadWeight);

                fireballIsh.assignDirectionalMovement(new Vec3(dx, dy, dz), speed * 0.1D);
            }

            net.minecraft.world.entity.Entity nmsEntity = ((net.minecraft.world.entity.Entity)nmsProjectile);
            Vector modifiedLocation = location.toVector().clone();
            if (Fireball.class.isAssignableFrom(projectileType) && spreadLocations > 0) {
                modifiedLocation.setX(modifiedLocation.getX() + direction.getX() + (random.nextGaussian() * spread / 5));
                modifiedLocation.setY(modifiedLocation.getY() + direction.getY() + (random.nextGaussian() * spread / 5));
                modifiedLocation.setZ(modifiedLocation.getZ() + direction.getZ() + (random.nextGaussian() * spread / 5));
            }
            nmsEntity.snapTo(modifiedLocation.getX(), modifiedLocation.getY(), modifiedLocation.getZ(), location.getYaw(), location.getPitch());

            if (nmsEntity instanceof net.minecraft.world.entity.projectile.Projectile) {
                net.minecraft.world.entity.projectile.Projectile nms = (net.minecraft.world.entity.projectile.Projectile)nmsEntity;
                nms.shoot(direction.getX(), direction.getY(), direction.getZ(), speed, spread);
            }

            Entity entity = nmsEntity.getBukkitEntity();
            if (entity == null || !(entity instanceof Projectile)) {
                throw new Exception("Got invalid bukkit entity from projectile of class " + projectileType.getName());
            }

            projectile = (Projectile)entity;
            if (source != null) {
                projectile.setShooter(source);
                nmsEntity.projectileSource = source;
            }

            nmsWorld.addFreshEntity(nmsEntity, CreatureSpawnEvent.SpawnReason.DEFAULT);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }

        return projectile;
    }

    @Override
    public void setDamage(Projectile projectile, double damage) {
        net.minecraft.world.entity.Entity nms = ((CraftEntity)projectile).getHandle();
        if (!(nms instanceof net.minecraft.world.entity.projectile.arrow.Arrow)) {
            return;
        }
        net.minecraft.world.entity.projectile.arrow.Arrow arrow = (net.minecraft.world.entity.projectile.arrow.Arrow)nms;
        arrow.setBaseDamage(damage);
    }

    @Override
    public void decreaseLifespan(Projectile projectile, int ticks) {
        net.minecraft.world.entity.Entity nms = ((CraftEntity)projectile).getHandle();
        if (!(nms instanceof net.minecraft.world.entity.projectile.arrow.Arrow)) {
            return;
        }
        net.minecraft.world.entity.projectile.arrow.Arrow arrow = (net.minecraft.world.entity.projectile.arrow.Arrow)nms;
        arrow.life = ticks;
    }

    @Override
    public Entity spawnEntity(Location target, EntityType entityType, CreatureSpawnEvent.SpawnReason spawnReason) {
        CraftWorld craftWorld = (CraftWorld)target.getWorld();
        return craftWorld.spawn(target, entityType.getEntityClass(), null, spawnReason);
    }

    @Override
    public String getResourcePack(Server server) {
        return server.getResourcePack();
    }

    @Override
    public boolean setResourcePack(Player player, String rp, byte[] hash) {
        player.setResourcePack(rp, hash);
        return true;
    }

    @Override
    public boolean removeItemAttribute(ItemStack item, Attribute attribute) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        if (!meta.removeAttributeModifier(attribute)) {
            return false;
        }
        item.setItemMeta(meta);
        return true;
    }

    @Override
    public boolean removeItemAttributes(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        Multimap<Attribute, AttributeModifier> modifiers = meta.getAttributeModifiers();
        if (modifiers == null || modifiers.isEmpty()) {
            return false;
        }
        for (Attribute attribute : modifiers.keySet()) {
            meta.removeAttributeModifier(attribute);
        }
        item.setItemMeta(meta);
        return true;
    }

    @Override
    public boolean setItemAttribute(ItemStack item, Attribute attribute, double value, String slot, int attributeOperation) {
        return setItemAttribute(item, attribute, value, slot, attributeOperation, UUID.randomUUID());
    }

    @Override
    public boolean setItemAttribute(ItemStack item, Attribute attribute, double value, String slot, int attributeOperation, UUID attributeUUID) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        try {
            AttributeModifier.Operation operation;
            try {
                operation = AttributeModifier.Operation.values()[attributeOperation];
            } catch (Throwable ex) {
                platform.getLogger().warning("[Magic] invalid attribute operation ordinal: " + attributeOperation);
                return false;
            }
            EquipmentSlotGroup equipmentSlotGroup = EquipmentSlotGroup.ANY;
            if (slot != null && !slot.isEmpty()) {
                try {
                    if (slot.equalsIgnoreCase("mainhand")) {
                        equipmentSlotGroup = EquipmentSlotGroup.MAINHAND;
                    } else if (slot.equalsIgnoreCase("offhand")) {
                        equipmentSlotGroup = EquipmentSlotGroup.OFFHAND;
                    } else {
                        equipmentSlotGroup = EquipmentSlotGroup.getByName(slot.toUpperCase());
                    }
                } catch (Throwable ex) {
                    platform.getLogger().warning("[Magic] invalid attribute slot: " + slot);
                    return false;
                }
            }
            AttributeModifier modifier = createAttributeModifier(attributeUUID, value, operation, equipmentSlotGroup);
            meta.addAttributeModifier(attribute, modifier);
            item.setItemMeta(meta);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void sendExperienceUpdate(Player player, float experience, int level) {
        player.sendExperienceChange(experience, level);
    }

    @Override
    public void swingOffhand(Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        ((LivingEntity)entity).swingOffHand();
    }

    @Override
    public void swingMainHand(Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        ((LivingEntity)entity).swingMainHand();
    }

    @Override
    public float getDurability(Material material) {
        return material.getBlastResistance();
    }

    @Override
    public CompoundTag getEntityData(Entity entity) {
        if (entity == null) return null;
        TagValueOutput valueOutput = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        ((CraftEntity)entity).getHandle().save(valueOutput);
        return valueOutput.buildResult();
    }

    @Override
    public boolean setEntityData(Entity entity, Object tag) {
        if (entity == null) return false;
        try {
            net.minecraft.world.entity.Entity nms = ((CraftEntity) entity).getHandle();
            ValueInput valueInput = TagValueInput.create(ProblemReporter.DISCARDING, nms.level().registryAccess(), (CompoundTag)tag);
            nms.load(valueInput);
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Could not load entity data for: " + entity.getType());
            return false;
        }
        return true;
    }

    @Override
    public EntityType getEntityTypeFromNMS(World world, Object tag) {
        net.minecraft.world.level.Level level = ((CraftWorld)world).getHandle();
        ValueInput valueInput = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), (CompoundTag)tag);
        Optional<net.minecraft.world.entity.EntityType<?>> optionalType = net.minecraft.world.entity.EntityType.by(valueInput);
        if (!optionalType.isPresent()) {
            return null;
        }

        return CraftEntityType.minecraftToBukkit(optionalType.get());
    }

    @Override
    public String getEntityType(Entity entity) {
        if (entity == null) return null;
        return ((CraftEntity)entity).getHandle().getEncodeId();
    }

    protected void sendPacket(Server server, Location source, Collection<? extends Player> players, Packet<?> packet) throws Exception  {
        players = ((players != null && players.size() > 0) ? players : server.getOnlinePlayers());

        int viewDistance = Bukkit.getServer().getViewDistance() * 16;
        int viewDistanceSquared =  viewDistance * viewDistance;
        World sourceWorld = source.getWorld();
        for (Player player : players) {
            Location location = player.getLocation();
            if (!location.getWorld().equals(sourceWorld)) continue;
            if (location.distanceSquared(source) <= viewDistanceSquared) {
                sendPacket(player, packet);
            }
        }
    }

    protected void sendPacket(Player player, Packet<?> packet) throws Exception {
        ServerPlayer nmsPlayer = ((CraftPlayer)player).getHandle();
        nmsPlayer.connection.send(packet);
    }

    @Override
    public void sendBreaking(Player player, long id, Location location, int breakAmount) {
        try {
            BlockPos blockPosition = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            Packet<?> packet = new ClientboundBlockDestructionPacket((int)id, blockPosition, breakAmount);
            sendPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Set<String> getTags(Entity entity) {
        return entity.getScoreboardTags();
    }

    @Override
    public boolean isJumping(LivingEntity entity) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            return player.getCurrentInput().isJump();
        }
        return false;
    }

    @Override
    public float getForwardMovement(LivingEntity entity) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (player.getCurrentInput().isForward()) {
                // Forward + backwards cancels out
                if (player.getCurrentInput().isBackward()) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (player.getCurrentInput().isBackward()) {
                return -1;
            }
        }
        return 0.0f;
    }

    @Override
    public float getStrafeMovement(LivingEntity entity) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (player.getCurrentInput().isRight()) {
                // Left + right cancels out
                if (player.getCurrentInput().isLeft()) {
                    return 0;
                } else {
                    return -1;
                }
            } else if (player.getCurrentInput().isLeft()) {
                return 1;
            }
        }
        return 0.0f;
    }

    @Override
    public boolean setPickupStatus(Projectile projectile, String pickupStatus) {
        if (!(projectile instanceof AbstractArrow)) return false;
        AbstractArrow.PickupStatus status;
        try {
            status = AbstractArrow.PickupStatus.valueOf(pickupStatus.toUpperCase(Locale.ROOT));
        } catch (Throwable ex) {
            platform.getLogger().warning("Invalid pickup status: " + pickupStatus);
            return false;
        }
        ((AbstractArrow)projectile).setPickupStatus(status);
        return true;
    }

    @Override
    public Block getHitBlock(ProjectileHitEvent event) {
        return event.getHitBlock();
    }

    @Override
    public boolean canRemoveRecipes() {
        return true;
    }

    @Override
    public boolean removeRecipe(Recipe recipe) {
        if (!(recipe instanceof Keyed)) {
            return false;
        }
        Keyed keyed = (Keyed)recipe;
        return platform.getPlugin().getServer().removeRecipe(keyed.getKey());
    }

    @Override
    public boolean removeRecipe(String key) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key.toLowerCase(Locale.ROOT));
        return platform.getPlugin().getServer().removeRecipe(namespacedKey);
    }

    @Override
    public ShapedRecipe createShapedRecipe(String key, ItemStack item) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key.toLowerCase(Locale.ROOT));
        return new ShapedRecipe(namespacedKey, item);
    }

    @Override
    public boolean discoverRecipe(HumanEntity entity, String key) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key.toLowerCase(Locale.ROOT));
        return entity.discoverRecipe(namespacedKey);
    }

    @Override
    public boolean undiscoverRecipe(HumanEntity entity, String key) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key.toLowerCase(Locale.ROOT));
        return entity.undiscoverRecipe(namespacedKey);
    }

    @Override
    public double getMaxHealth(Damageable li) {
        if (li instanceof LivingEntity) {
            return ((LivingEntity)li).getAttribute(getMinecraftAttribute("max_health")).getValue();
        }
        return 0;
    }

    @Override
    public void setMaxHealth(Damageable li, double maxHealth) {
        if (li instanceof LivingEntity) {
            ((LivingEntity)li).getAttribute(getMinecraftAttribute("max_health")).setBaseValue(maxHealth);
        }
    }

    @Override
    public boolean isDestructive(EntityExplodeEvent explosion) {
        ExplosionResult result = explosion.getExplosionResult();
        return result == ExplosionResult.DESTROY || result == ExplosionResult.DESTROY_WITH_DECAY;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Attribute getMinecraftAttribute(String attributeKey) {
        return Attribute.valueOf(attributeKey.toUpperCase(Locale.ROOT));
    }

    @Override
    public Entity getEntity(World world, UUID uuid) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftWorld)world).getHandle().getEntity(uuid);
        return nmsEntity == null ? null : nmsEntity.getBukkitEntity();
    }

    @Override
    public Entity getEntity(UUID uuid) {
        return Bukkit.getEntity(uuid);
    }

    @Override
    public boolean applyBonemeal(Location location) {
        if (dummyItem == null) {
            dummyItem = new ItemStack(Material.DIRT, 64);
            dummyItem = platform.getItemUtils().makeReal(dummyItem);
        }
        dummyItem.setAmount(64);
        ServerLevel nmsWorld = ((CraftWorld)location.getWorld()).getHandle();
        net.minecraft.world.item.ItemStack itemStack = (net.minecraft.world.item.ItemStack)platform.getItemUtils().getHandle(dummyItem);
        BlockPos blockPosition = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return BoneMealItem.growCrop(itemStack, nmsWorld, blockPosition);
    }

    @Override
    public Color getColor(PotionMeta meta) {
        return meta.getColor();
    }

    @Override
    public boolean setColor(PotionMeta meta, Color color) {
        meta.setColor(color);
        return true;
    }

    @Override
    public byte getLegacyBlockData(FallingBlock falling) {
        return 0;
    }

    @Override
    public String getBlockData(FallingBlock fallingBlock) {
        BlockData blockData = fallingBlock.getBlockData();
        return blockData.getAsString();
    }

    @Override
    public String getBlockData(Material material, byte data) {
        @SuppressWarnings("deprecation")
        BlockData blockData = platform.getDeprecatedUtils().getUnsafe().fromLegacy(material, data);
        return blockData == null ? null : blockData.getAsString();
    }

    @Override
    public String getBlockData(Block block) {
        BlockData blockData = block.getBlockData();
        return blockData == null ? null : blockData.getAsString();
    }

    @Override
    public boolean setBlockData(Block block, String data) {
        BlockData blockData = platform.getPlugin().getServer().createBlockData(data);
        block.setBlockData(blockData, false);
        return true;
    }

    @Override
    public boolean applyPhysics(Block block) {
        ServerLevel nmsWorld = ((CraftWorld)block.getWorld()).getHandle();
        BlockPos blockLocation = new BlockPos(block.getX(), block.getY(), block.getZ());
        net.minecraft.world.level.block.state.BlockState blockState = nmsWorld.getBlockState(blockLocation);
        clearItems(block.getLocation());
        platform.getDeprecatedUtils().setTypeAndData(block, Material.AIR, (byte)0, false);
        return nmsWorld.setBlock(blockLocation, blockState, 3);
    }

    @Override
    public boolean addRecipeToBook(ItemStack book, Plugin plugin, String recipeKey) {
        if (book == null) return false;
        ItemMeta meta = book.getItemMeta();
        if (!(meta instanceof KnowledgeBookMeta)) return false;
        KnowledgeBookMeta bookMeta = (KnowledgeBookMeta)meta;
        NamespacedKey key = new NamespacedKey(plugin, recipeKey.toLowerCase(Locale.ROOT));
        bookMeta.addRecipe(key);
        book.setItemMeta(bookMeta);
        return true;
    }

    @Override
    public boolean stopSound(Player player, Sound sound) {
        player.stopSound(sound);
        return true;
    }

    @Override
    public boolean stopSound(Player player, String sound) {
        player.stopSound(sound);
        return true;
    }

    @Override
    public boolean lockChunk(Chunk chunk) {
        if (!platform.getPlugin().isEnabled()) return false;
        if (!chunk.isLoaded()) {
            platform.getLogger().info("Locking unloaded chunk");
        }
        chunk.addPluginChunkTicket(platform.getPlugin());
        return true;
    }

    @Override
    public boolean unlockChunk(Chunk chunk) {
        if (!platform.getPlugin().isEnabled()) return false;
        chunk.removePluginChunkTicket(platform.getPlugin());
        return true;
    }

    @Override
    public Location getHangingLocation(Entity entity) {
        Location location = entity.getLocation();
        if (!(entity instanceof Hanging)) return location;
        HangingEntity nms = ((CraftHanging)entity).getHandle();
        BlockPos position = nms.blockPosition();
        location.setX(position.getX());
        location.setY(position.getY());
        location.setZ(position.getZ());
        return location;
    }

    @Override
    public boolean isSameKey(Plugin plugin, String key, Object keyedObject) {
        if (!(keyedObject instanceof Keyed)) return false;
        String namespace = plugin.getName().toLowerCase(Locale.ROOT);
        key = key.toLowerCase(Locale.ROOT);
        Keyed keyed = (Keyed)keyedObject;
        NamespacedKey namespacedKey = keyed.getKey();
        String keyNamespace = namespacedKey.getNamespace();
        String keyKey = namespacedKey.getKey();
        return keyNamespace.equals(namespace) && keyKey.equals(key);
    }

    @Override
    public boolean setRecipeIngredient(ShapedRecipe recipe, char key, ItemStack ingredient, boolean ignoreDamage) {
        if (ingredient == null) return false;
        try {
            short maxDurability = ingredient.getType().getMaxDurability();
            if (ignoreDamage && maxDurability > 0) {
                List<ItemStack> damaged = new ArrayList<>();
                for (short damage = 0; damage < maxDurability; damage++) {
                    ingredient = ingredient.clone();
                    ItemMeta meta = ingredient.getItemMeta();
                    if (meta == null || !(meta instanceof org.bukkit.inventory.meta.Damageable))  break;
                    org.bukkit.inventory.meta.Damageable damageable = (org.bukkit.inventory.meta.Damageable)meta;
                    damageable.setDamage(damage);
                    ingredient.setItemMeta(meta);
                    damaged.add(ingredient);
                }
                RecipeChoice.ExactChoice exactChoice = new RecipeChoice.ExactChoice(damaged);
                recipe.setIngredient(key, exactChoice);
                return true;
            }

            RecipeChoice.ExactChoice exactChoice = new RecipeChoice.ExactChoice(ingredient);
            recipe.setIngredient(key, exactChoice);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean setAutoBlockState(Block block, Location target, BlockFace facing, boolean physics, Player originator) {
        if (block == null || facing == null || target == null) return false;
        net.minecraft.world.level.block.state.BlockState blockState = ((CraftBlock)block).getNMS();
        if (blockState == null) return false;
        net.minecraft.world.level.block.Block nmsBlock = blockState.getBlock();
        ItemStack blockItem = new ItemStack(block.getType());
        ServerPlayer originatorHandle = ((CraftPlayer)originator).getHandle();
        ServerLevel world = ((CraftWorld)block.getWorld()).getHandle();
        Object item = platform.getItemUtils().getHandle(platform.getItemUtils().makeReal(blockItem));
        if (originatorHandle == null || world == null || item == null) {
            return false;
        }
        BlockPos blockPosition = new BlockPos(block.getX(), block.getY(), block.getZ());
        Vec3 vec3D = new Vec3(target.getX(), target.getY(), target.getZ());
        Direction direction;
        try {
            direction = Direction.valueOf(facing.name());
        } catch (Exception ex) {
            platform.getLogger().log(Level.SEVERE, "Could not translate to NMS direction: " + facing);
            return false;
        }
        BlockHitResult hitResult = new BlockHitResult(vec3D, direction, blockPosition, false);
        BlockPlaceContext actionContext = new BlockPlaceContext(originatorHandle, InteractionHand.MAIN_HAND, (net.minecraft.world.item.ItemStack)item, hitResult);
        net.minecraft.world.level.block.state.BlockState state = nmsBlock.getStateForPlacement(actionContext);
        if (state == null) return false;
        CraftBlock cBlock = (CraftBlock)block;
        CraftBlock.setTypeAndData(cBlock.getHandle(), cBlock.getPosition(), cBlock.getNMS(), state, physics);
        return true;
    }

    @Override
    public boolean forceUpdate(Block block, boolean physics) {
        if (block == null) return false;
        net.minecraft.world.level.block.state.BlockState blockState = ((CraftBlock)block).getNMS();
        if (blockState == null) return false;
        net.minecraft.world.level.block.Block nmsBlock = blockState.getBlock();
        net.minecraft.world.level.block.state.BlockState blockData = nmsBlock.defaultBlockState();
        ServerLevel world = ((CraftWorld)block.getWorld()).getHandle();
        BlockPos blockPosition = new BlockPos(block.getX(), block.getY(), block.getZ());
        world.setBlock(blockPosition, blockData, 11);
        return false;
    }

    @Override
    public int getPhantomSize(Entity entity) {
        if (entity == null || !(entity instanceof Phantom)) return 0;
        return ((Phantom)entity).getSize();
    }

    @Override
    public boolean setPhantomSize(Entity entity, int size) {
        if (entity == null || !(entity instanceof Phantom)) return false;
        ((Phantom)entity).setSize(size);
        return true;
    }

    @Override
    public Location getBedSpawnLocation(Player player) {
        // This used to do a bunch of NMS, let's just try the API now.
        return player.getRespawnLocation();
    }

    @Override
    public void addPassenger(Entity vehicle, Entity passenger) {
        vehicle.addPassenger(passenger);
    }

    @Override
    public List<Entity> getPassengers(Entity entity) {
        return entity.getPassengers();
    }

    @Override
    public boolean openBook(Player player, ItemStack itemStack) {
        player.openBook(itemStack);
        return true;
    }

    @Override
    public boolean isHandRaised(Player player) {
        return player.isHandRaised();
    }

    @Override
    public Class<?> getProjectileClass(String projectileTypeName) {
        return projectileClasses.get(projectileTypeName.toLowerCase());
    }

    @Override
    public Entity spawnFireworkEffect(Material fireworkMaterial, Server server, Location location, FireworkEffect effect, int power, Vector direction, Integer expectedLifespan, Integer ticksFlown, boolean silent) {
        Entity entity = null;
        try {
            if (fireworkMaterial == null) {
                return null;
            }
            ServerLevel level = ((CraftWorld)location.getWorld()).getHandle();
            ItemStack itemStack = new ItemStack(fireworkMaterial);
            FireworkMeta meta = (FireworkMeta) itemStack.getItemMeta();
            meta.addEffect(effect);
            meta.setPower(power);
            itemStack.setItemMeta(meta);

            Object item = platform.getItemUtils().getHandle(platform.getItemUtils().makeReal(itemStack));
            final FireworkRocketEntity fireworkHandle = new FireworkRocketEntity(level, location.getX(), location.getY(), location.getZ(), (net.minecraft.world.item.ItemStack)item);
            fireworkHandle.setSilent(silent);

            if (direction != null) {
                fireworkHandle.setDeltaMovement(new Vec3(direction.getX(), direction.getY(), direction.getZ()));
            }

            if (ticksFlown != null) {
                fireworkHandle.life = ticksFlown;
            }
            if (expectedLifespan != null) {
                fireworkHandle.lifetime = expectedLifespan;
            }

            if (direction == null) {
                BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                ClientboundAddEntityPacket fireworkPacket = new ClientboundAddEntityPacket(fireworkHandle, CompatibilityConstants.FIREWORK_TYPE, blockPos);
                int fireworkId = fireworkHandle.getId();
                SynchedEntityData watcher = fireworkHandle.getEntityData();
                ClientboundSetEntityDataPacket metadataPacket = new ClientboundSetEntityDataPacket(fireworkId, watcher.packDirty());
                ClientboundEntityEventPacket statusPacket = new ClientboundEntityEventPacket(fireworkHandle, (byte)17);
                ClientboundRemoveEntitiesPacket destroyPacket = new ClientboundRemoveEntitiesPacket(fireworkId);

                Collection<? extends Player> players = server.getOnlinePlayers();
                sendPacket(server, location, players, fireworkPacket);
                sendPacket(server, location, players, metadataPacket);
                sendPacket(server, location, players, statusPacket);
                sendPacket(server, location, players, destroyPacket);
                return null;
            }

            level.addFreshEntity(fireworkHandle, CreatureSpawnEvent.SpawnReason.CUSTOM);
            entity = fireworkHandle.getBukkitEntity();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return entity;
    }

    @Override
    public boolean loadAllTagsFromNBT(ConfigurationSection tags, Object tag)
    {
        if (!(tag instanceof CompoundTag)) return false;
        CompoundTag compoundTag = (CompoundTag) tag;
        Set<String> keys = platform.getNBTUtils().getTagKeys(tag);
        if (keys == null) return false;
        for (String tagName : keys) {
            Tag metaBase = compoundTag.get(tagName);
            if (metaBase != null) {
                if (metaBase instanceof CompoundTag) {
                    ConfigurationSection newSection = tags.createSection(tagName);
                    loadAllTagsFromNBT(newSection, metaBase);
                } else {
                    try {
                        tags.set(tagName, platform.getNBTUtils().getTagValue(metaBase));
                    } catch (Exception ex) {
                        platform.getLogger().log(Level.SEVERE, "Failed to load NBT tags", ex);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public BoundingBox getHitbox(Entity entity) {
        org.bukkit.util.BoundingBox boundingBox = entity.getBoundingBox();
        return new BoundingBox(boundingBox);
    }

    @Override
    public boolean isPrimaryThread() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public boolean isAdult(Zombie zombie) {
        return zombie.isAdult();
    }

    @Override
    public void setBaby(Zombie zombie) {
        zombie.setBaby();
    }

    @Override
    public void setAdult(Zombie zombie) {
        zombie.setAdult();
    }

    @Override
    public BlockFace getSignFacing(Block signBlock) {
        BlockData blockData = signBlock.getBlockData();
        if (!(blockData instanceof WallSign)) {
            return null;
        }
        WallSign sign = (WallSign)blockData;
        return sign.getFacing();
    }

    @Override
    public void openSign(Player player, Location signBlock) {
        Block block = signBlock.getBlock();
        BlockState blockState = block.getState();
        if (blockState instanceof Sign) {
            player.openSign((Sign)blockState);
        }
    }

    @Override
    public boolean setCompassTarget(ItemMeta meta, Location targetLocation, boolean trackLocation) {
        if (meta == null || !(meta instanceof CompassMeta)) {
            return false;
        }
        CompassMeta compassMeta = (CompassMeta)meta;
        compassMeta.setLodestoneTracked(trackLocation);
        compassMeta.setLodestone(targetLocation);
        return true;
    }

    @Override
    public boolean isAware(Entity entity) {
        if (!(entity instanceof org.bukkit.entity.Mob)) {
            return true;
        }
        return ((org.bukkit.entity.Mob)entity).isAware();
    }

    @Override
    public void setAware(Entity entity, boolean aware) {
        if (!(entity instanceof org.bukkit.entity.Mob)) {
            return;
        }
        ((org.bukkit.entity.Mob)entity).setAware(aware);
    }

    @Override
    public boolean isSwingingArm(Entity entity) {
        if (entity == null) return false;
        net.minecraft.world.entity.Entity nms = ((CraftEntity)entity).getHandle();
        if (nms == null || !(nms instanceof net.minecraft.world.entity.LivingEntity)) {
            return false;
        }
        return ((net.minecraft.world.entity.LivingEntity)nms).swinging;
    }

    @Override
    public boolean setLastDamaged(Entity damaged, Entity damager) {
        if (damager == null) {
            return false;
        }
        net.minecraft.world.entity.Entity nmsDamager = ((CraftEntity)damager).getHandle();
        if (nmsDamager == null || !(nmsDamager instanceof net.minecraft.world.entity.LivingEntity)) {
            return false;
        }
        net.minecraft.world.entity.LivingEntity livingDamager = (net.minecraft.world.entity.LivingEntity)nmsDamager;
        net.minecraft.world.entity.Entity nmsDamaged = damaged == null ? null : ((CraftEntity)damaged).getHandle();
        livingDamager.setLastHurtMob(nmsDamaged);
        return true;
    }

    @Override
    public boolean setLastDamagedBy(Entity damaged, Entity damager) {
        if (damager == null) {
            return false;
        }
        net.minecraft.world.entity.Entity nmsDamaged = ((CraftEntity)damager).getHandle();
        if (nmsDamaged == null || !(nmsDamaged instanceof net.minecraft.world.entity.LivingEntity)) {
            return false;
        }
        net.minecraft.world.entity.LivingEntity livingDamaged = (net.minecraft.world.entity.LivingEntity)nmsDamaged;
        net.minecraft.world.entity.Entity nmsDamager = ((CraftEntity)damager).getHandle();
        net.minecraft.world.entity.LivingEntity livingDamager = nmsDamager instanceof net.minecraft.world.entity.LivingEntity
                ? (net.minecraft.world.entity.LivingEntity)nmsDamaged : null;
        livingDamaged.setLastHurtByMob(livingDamager);
        return true;
    }
}
