package com.elmakers.mine.bukkit.utility.platform.base_v1_17_0;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
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
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fox;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;
import org.spigotmc.event.entity.EntityDismountEvent;

import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.utility.BoundingBox;
import com.elmakers.mine.bukkit.utility.ChatUtils;
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
import com.elmakers.mine.bukkit.utility.platform.VersionedPotionEffectType;
import com.elmakers.mine.bukkit.utility.platform.base_v1_17_0.map.BufferedMapCanvas;

public abstract class CompatibilityUtilsBase implements CompatibilityUtils {
    // This is really here to prevent infinite loops, but sometimes these requests legitimately come in many time
    // (for instance when undoing a spell in an unloaded chunk that threw a ton of different falling blocks)
    // So putting some lower number on this will trigger a lot of false-positives.
    protected static final int MAX_CHUNK_LOAD_TRY = 10000;
    protected static final int MAX_ENTITY_RANGE = 72;
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

    private static final PotionEffectType[] _negativeEffects =
            {PotionEffectType.BLINDNESS, PotionEffectType.CONFUSION, PotionEffectType.HARM,
                    PotionEffectType.HUNGER, PotionEffectType.POISON, PotionEffectType.SLOW,
                    PotionEffectType.SLOW_DIGGING, PotionEffectType.WEAKNESS, PotionEffectType.WITHER};
    protected static final Set<PotionEffectType> negativeEffects = new HashSet<>(Arrays.asList(_negativeEffects));

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
    public Runnable getTaskRunnable(BukkitTask task) {
        return null;
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

    @SuppressWarnings("deprecation")
    public Material migrateMaterial(Material material, byte data) {
        return fromLegacy(new org.bukkit.material.MaterialData(material, data));
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
    public boolean setItemAttribute(ItemStack item, Attribute attribute, double value, String slot, String attributeOperation) {
        return setItemAttribute(item, attribute, value, slot, attributeOperation, UUID.randomUUID());
    }

    @Override
    public boolean setItemAttribute(ItemStack item, Attribute attribute, double value, String slot, String attributeOperation, UUID attributeUUID) {
        return setItemAttribute(item, attribute, value, slot, attributeOperation, attributeUUID, "");
    }

    @Override
    public boolean setItemAttribute(ItemStack item, Attribute attribute, double value, String slot, String attributeOperation, UUID attributeUUID, String attributeKey) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        try {
            AttributeModifier.Operation operation = AttributeModifier.Operation.ADD_NUMBER;
            if (attributeOperation != null && !attributeOperation.isEmpty()) {
                try {
                    int operationIndex = Integer.parseInt(attributeOperation);
                    operation = AttributeModifier.Operation.values()[operationIndex];
                } catch (Throwable ignore) {
                    try {
                        operation = AttributeModifier.Operation.valueOf(attributeOperation.toUpperCase());
                    } catch (Throwable ex) {
                        platform.getLogger().warning("Invalid operation " + attributeOperation);
                    }
                }
            }

            EquipmentSlot equipmentSlot = parseEquipmentSlotGroup(slot);
            AttributeModifier modifier = createAttributeModifier(attributeUUID, value, operation, equipmentSlot);
            meta.addAttributeModifier(attribute, modifier);
            item.setItemMeta(meta);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public EquipmentSlot parseEquipmentSlotGroup(String slot) {
        EquipmentSlot equipmentSlotGroup = null;
        if (slot != null && !slot.isEmpty()) {
            try {
                if (slot.equalsIgnoreCase("mainhand")) {
                    equipmentSlotGroup = EquipmentSlot.HAND;
                } else if (slot.equalsIgnoreCase("offhand")) {
                    equipmentSlotGroup = EquipmentSlot.OFF_HAND;
                } else {
                    equipmentSlotGroup = EquipmentSlot.valueOf(slot.toUpperCase());
                }
            } catch (Throwable ex) {
                platform.getLogger().warning("[Magic] invalid attribute slot: " + slot);
            }
        }
        return equipmentSlotGroup;
    }

    protected AttributeModifier createAttributeModifier(UUID attributeUUID, double value, AttributeModifier.Operation operation, EquipmentSlot equipmentSlot) {
        return new AttributeModifier(attributeUUID, "Equipment Modifier", value, operation, equipmentSlot);
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

    public void playRecord(Location location, Material record) {
        if (platform.isLegacy()) {
            DeprecatedUtilsBase deprecatedUtils = ((DeprecatedUtilsBase)platform.getDeprecatedUtils());
            location.getWorld().playEffect(location, Effect.RECORD_PLAY,
                    deprecatedUtils.getId(record));
        } else {
            location.getWorld().playEffect(location, Effect.RECORD_PLAY, record);
        }
    }

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
    public Set<String> getTags(Entity entity) {
        // TODO: Use Entity.getScoreboardTags in a future version.
        return null;
    }

    @Override
    public boolean setBlockFast(Block block, Material material, int data) {
        return setBlockFast(block.getChunk(), block.getX(), block.getY(), block.getZ(), material, data);
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

    public Material fromLegacy(org.bukkit.material.MaterialData materialData) {
        return materialData.getItemType();
    }

    public boolean hasLegacyMaterials() {
        return false;
    }

    public boolean isLegacy(Material material) {
        return false;
    }

    public Material getLegacyMaterial(String materialName) {
        return Material.getMaterial(materialName);
    }

    public boolean hasBlockDataSupport() {
        return true;
    }

    @Override
    public byte getLegacyBlockData(FallingBlock falling) {
        return 0;
    }

    @Override
    public String getBlockData(FallingBlock fallingBlock) {
        return null;
    }

    @Override
    public String getBlockData(Block block) {
        return null;
    }

    @Override
    public String getBlockData(Material material, byte data) {
        return null;
    }

    @Override
    public boolean setBlockData(Block block, String data) {
        return false;
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

    protected abstract boolean sendActionBarPackets(Player player, String message);

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

    public boolean isLegacyRecipes() {
        return false;
    }

    public Collection<BoundingBox> getLegacyBoundingBoxes(Block block) {
        BoundingBox translated = new BoundingBox(block.getLocation().toVector(), BLOCK_BOUNDING_BOX);
        blockBoundingBoxes.set(0, translated);
        return blockBoundingBoxes;
    }

    @Override
    public Collection<BoundingBox> getBoundingBoxes(Block block) {
        VoxelShape voxelShape = block.getCollisionShape();
        Collection<org.bukkit.util.BoundingBox> boxes = voxelShape.getBoundingBoxes();
        if (boxes.isEmpty()) {
            return getLegacyBoundingBoxes(block);
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
    public Set<PotionEffectType> getNegativeEffects() {
        return negativeEffects;
    }

    @Override
    public boolean isDestructive(EntityExplodeEvent explosion) {
        return true;
    }

    @Override
    public Attribute getMinecraftAttribute(String attributeKey) {
        return Attribute.valueOf(attributeKey.toUpperCase());
    }

    @Override
    public PotionEffectType getPotionEffectType(VersionedPotionEffectType type) {
        switch (type) {
            case NAUSEA: return PotionEffectType.CONFUSION;
            case JUMP_BOOST: return PotionEffectType.JUMP;
            case RESISTANCE: return PotionEffectType.DAMAGE_RESISTANCE;
            default: throw new RuntimeException("Unhandled PotionEffectType: " + type);
        }
    }

    @Override
    public void renderMap(MapRenderer renderer, MapView map, com.elmakers.mine.bukkit.map.BufferedMapCanvas canvas, Player player) {
        renderer.render(map, (BufferedMapCanvas)canvas, player);
    }

    @Override
    public BufferedMapCanvas createMapCanvas() {
        return new BufferedMapCanvas();
    }
}
