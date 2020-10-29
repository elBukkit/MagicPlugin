package com.elmakers.mine.bukkit.entity;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MageModifier;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.item.Cost;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.SafetyUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;

/**
 * This class stores information about an Entity.
 *
 */
public class EntityData implements com.elmakers.mine.bukkit.api.entity.EntityData, Cloneable {
    protected static Map<UUID, WeakReference<Entity>> respawned = new HashMap<>();

    public static boolean isSpawning = false;
    private static int mobStackSize = 0;
    private static final int maxMobStackSize = 255;

    protected String key;
    protected WeakReference<Entity> entity = null;
    protected UUID uuid = null;

    protected EntityType type;
    protected EntityExtraData extraData;
    protected Location location;
    protected Vector relativeLocation;
    protected boolean hasMoved = false;
    protected boolean hasChangedHealth = false;
    protected boolean isTemporary = false;
    protected boolean isSplittable = false;
    private boolean respawn = false;
    protected String name = null;
    protected Art art;
    protected BlockFace facing;
    protected Rotation rotation;
    protected ItemStack item;

    protected Double maxHealth;
    protected Double health;
    protected Integer airLevel;
    protected boolean isAngry;
    protected boolean isBaby;
    protected boolean isSilent;
    protected boolean isTamed;
    protected boolean isSitting;
    protected boolean isInvulnerable;
    protected boolean hasAI = true;
    protected boolean hasGravity = true;
    protected boolean isDocile;
    protected boolean transformable;
    protected boolean preventProjectiles;
    protected boolean preventMelee;
    protected boolean nameVisible;
    protected Boolean persist = null;
    protected int fireTicks;

    protected DyeColor dyeColor;
    protected Ocelot.Type ocelotType;
    protected Rabbit.Type rabbitType = null;

    protected Collection<PotionEffect> potionEffects = null;
    protected Collection<PotionEffectType> removeEffects = null;
    protected Map<String, ConfigurationSection> modifiers = null;
    protected Collection<String> removeModifiers = null;
    protected Map<Attribute, Double> attributes = null;

    protected Vector velocity = null;
    protected boolean hasPotionEffects = false;
    protected boolean hasVelocity = false;
    protected boolean isHanging = false;
    protected boolean isLiving = false;
    protected boolean isProjectile = false;
    protected boolean canPickupItems = false;

    protected ItemData itemInHand;
    protected ItemData itemInOffhand;
    protected ItemData helmet;
    protected ItemData chestplate;
    protected ItemData leggings;
    protected ItemData boots;

    protected Integer xp;
    protected Integer dropXp;

    protected boolean defaultDrops;
    protected boolean dropsRequirePlayerKiller;
    protected List<Deque<WeightedPair<String>>> drops;
    protected Set<String> tags;
    protected String interactSpell;
    protected String interactPermission;
    protected List<com.elmakers.mine.bukkit.api.item.Cost> interactCosts;
    protected ConfigurationSection interactSpellParameters;
    protected EntityData.SourceType interactSpellSource;
    protected EntityData.TargetType interactSpellTarget;
    protected EntityData.SourceType interactCommandSource;
    protected List<String> interactCommands;
    protected ConfigurationSection disguise;

    protected EntityMageData mageData;
    protected EntityData mount;
    protected String mountType;

    protected ConfigurationSection configuration;

    public EntityData(Entity entity) {
        setEntity(entity);
        this.location = CompatibilityUtils.getHangingLocation(entity);
        this.isTemporary = entity.hasMetadata("temporary");
        this.isLiving = entity instanceof LivingEntity;
        this.isHanging = entity instanceof Hanging;
        this.isProjectile = entity instanceof Projectile;
        this.type = entity.getType();
        this.fireTicks = entity.getFireTicks();
        this.isSilent = CompatibilityUtils.isSilent(entity);
        this.persist = CompatibilityUtils.isPersist(entity);
        this.canPickupItems = (entity instanceof Creature) ? ((Creature)entity).getCanPickupItems() : false;
        name = entity.getCustomName();
        nameVisible = entity.isCustomNameVisible();
        tags = CompatibilityUtils.getTags(entity);

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
                org.bukkit.Bukkit.getLogger().log(Level.WARNING, "Error reading HangingEntity " + entity + " of type " + entity.getType(), ex);
            }
        }

        if (entity instanceof LivingEntity) {
            LivingEntity li = (LivingEntity)entity;
            this.health = li.getHealth();
            this.potionEffects = li.getActivePotionEffects();
            this.airLevel = li.getRemainingAir();
            this.maxHealth = CompatibilityUtils.getMaxHealth(li);
            this.hasAI = li.hasAI();

            itemInHand = getItem(li.getEquipment().getItemInMainHand());
            itemInOffhand = getItem(li.getEquipment().getItemInOffHand());
            helmet = getItem(li.getEquipment().getHelmet());
            chestplate = getItem(li.getEquipment().getChestplate());
            leggings = getItem(li.getEquipment().getLeggings());
            boots = getItem(li.getEquipment().getBoots());
        }

        if (entity instanceof Tameable) {
            isTamed = ((Tameable)entity).isTamed();
        }
        isSitting = CompatibilityUtils.isSitting(entity);
        isInvulnerable = CompatibilityUtils.isInvulnerable(entity);

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
        } else if (entity instanceof Villager) {
            extraData = new EntityVillagerData((Villager)entity);
        } else if (entity instanceof Wolf) {
            Wolf wolf = (Wolf)entity;
            dyeColor = wolf.getCollarColor();
            isAngry = wolf.isAngry();
        } else if (entity instanceof Ocelot) {
            Ocelot ocelot = (Ocelot)entity;
            ocelotType = ocelot.getCatType();
        } else if (entity instanceof Rabbit) {
            Rabbit rabbit = (Rabbit)entity;
            rabbitType = rabbit.getRabbitType();
        } else if (entity instanceof ArmorStand) {
            extraData = new EntityArmorStandData((ArmorStand)entity);
        } else if (entity instanceof ExperienceOrb) {
            xp = ((ExperienceOrb)entity).getExperience();
        } else if (entity instanceof Zombie) {
            extraData = new EntityZombieData((Zombie)entity);
        } else if (entity instanceof AreaEffectCloud) {
            extraData = new EntityAreaEffectCloudData((AreaEffectCloud)entity);
        } else if (entity instanceof Slime) {
            extraData = new EntitySlimeData((Slime)entity);
        } else if (entity instanceof FallingBlock) {
            extraData = new EntityFallingBlockData((FallingBlock)entity);
        } else if (entity.getType().name().equals("PARROT")) {
            extraData = new EntityParrotData(entity);
        } else if (entity instanceof EnderDragon) {
            extraData = new EntityEnderDragonData(entity);
        } else if (CompatibilityUtils.isFox(entity)) {
            extraData = new EntityFoxData(entity);
        }
    }

    @Nullable
    private ItemData getItem(ItemStack item) {
        return item == null ? null : new com.elmakers.mine.bukkit.item.ItemData(item);
    }

    @Override
    public ItemStack getItem() {
        return item;
    }

    public EntityData(EntityType type) {
        this.type = type;
    }

    public EntityData(@Nonnull MageController controller, @Nonnull String key, ConfigurationSection parameters) {
        this.key = key;
        load(controller, parameters);
    }

    public EntityData(@Nonnull MageController controller, ConfigurationSection parameters) {
        load(controller, parameters);
    }

    @Override
    public void load(@Nonnull MageController controller, ConfigurationSection parameters) {
        this.configuration = parameters;
        // This is required to allow changes to health
        hasChangedHealth = true;
        name = parameters.getString("name");
        if (name != null) {
            name = ChatColor.translateAlternateColorCodes('&', name);
        }
        nameVisible = parameters.getBoolean("show_name");
        if (parameters.contains("health")) {
            health = parameters.getDouble("health", 1);
            maxHealth = health;
        }
        if (parameters.contains("max_health")) {
            maxHealth = parameters.getDouble("max_health", 1);
        }
        isSilent = parameters.getBoolean("silent", false);
        if (parameters.contains("persist")) {
            persist = parameters.getBoolean("persist");
        }
        isDocile = parameters.getBoolean("docile");
        transformable = parameters.getBoolean("transformable", true);
        preventProjectiles = parameters.getBoolean("prevent_projectiles", false);
        preventMelee = parameters.getBoolean("prevent_melee", false);

        String entityName = parameters.contains("type") ? parameters.getString("type") : key;
        if (entityName != null && !entityName.isEmpty()) {
            type = parseEntityType(entityName);
            if (type == null) {
                controller.getLogger().log(Level.WARNING, " Invalid entity type: " + entityName + " in mob config for " + key + ", did you forget the 'type' parameter?");
                return;
            }
        }

        String colorString = parameters.getString("color");
        if (colorString != null) {
            try {
                dyeColor = DyeColor.valueOf(colorString.toUpperCase());
            } catch (Exception ex) {
                dyeColor = null;
            }
        }

        ConfigurationSection mountConfig = ConfigurationUtils.getConfigurationSection(parameters, "mount");
        if (mountConfig != null) {
            mount = new EntityData(controller, mountConfig);
        } else {
            mountType = parameters.getString("mount");
        }

        disguise = ConfigurationUtils.getConfigurationSection(parameters, "disguise");

        isTamed = parameters.getBoolean("tamed", false);
        isSitting = parameters.getBoolean("sitting", false);
        isInvulnerable = parameters.getBoolean("invulnerable", false);
        isBaby = parameters.getBoolean("baby", false);
        isAngry = parameters.getBoolean("angry", false);
        hasAI = parameters.getBoolean("ai", true);
        hasGravity = parameters.getBoolean("gravity", true);
        isSplittable = parameters.getBoolean("split", true);

        potionEffects = ConfigurationUtils.getPotionEffectObjects(parameters, "potion_effects", controller.getLogger());
        hasPotionEffects = potionEffects != null && !potionEffects.isEmpty();

        ConfigurationSection modifierSection = parameters.getConfigurationSection("modifiers");
        if (modifierSection == null) {
            List<String> modifierList = ConfigurationUtils.getStringList(parameters, "modifiers");
            if (modifierList != null) {
                modifiers = new HashMap<>();
                for (String addKey : modifierList) {
                    modifiers.put(addKey, null);
                }
            }
        } else {
            modifiers = new HashMap<>();
            for (String addKey : modifierSection.getKeys(false)) {
                modifiers.put(addKey, modifierSection.getConfigurationSection(addKey));
            }
        }

        defaultDrops = parameters.getBoolean("default_drops", true);
        dropsRequirePlayerKiller = parameters.getBoolean("drops_require_player_killer", false);
        if (parameters.contains("xp")) {
            xp = parameters.getInt("xp");
        }
        if (parameters.contains("drop_xp")) {
            dropXp = parameters.getInt("drop_xp");
        }
        interactSpell = parameters.getString("interact_spell");
        interactSpellParameters = ConfigurationUtils.getConfigurationSection(parameters, "interact_spell_parameters");
        List<Cost> interactCosts = Cost.parseCosts(ConfigurationUtils.getConfigurationSection(parameters, "interact_costs"), controller);
        this.interactCosts = (interactCosts == null) ? null : new ArrayList<>(interactCosts);
        interactPermission = parameters.getString("interact_permission");
        String sourceType = parameters.getString("interact_spell_source", "PLAYER");
        if (sourceType.equalsIgnoreCase("NPC")) {
            sourceType = "MOB";
        } else if (sourceType.equalsIgnoreCase("OPPED_PLAYER")) {
            controller.getLogger().warning("Invalid spell source type: " + sourceType);
            sourceType = "PLAYER";
        }
        try {
            interactSpellSource = EntityData.SourceType.valueOf(sourceType.toUpperCase());
        } catch (Exception ex) {
            controller.getLogger().warning("Invalid mob source type: " + sourceType);
            interactSpellSource = EntityData.SourceType.PLAYER;
        }
        String targetType = parameters.getString("interact_spell_target", "MOB");
        if (targetType.equalsIgnoreCase("NPC")) {
            targetType = "MOB";
        }
        try {
            interactSpellTarget = EntityData.TargetType.valueOf(targetType.toUpperCase());
        } catch (Exception ex) {
            controller.getLogger().warning("Invalid mob target type: " + targetType);
            interactSpellTarget = EntityData.TargetType.MOB;
        }

        sourceType = parameters.getString("interact_command_source", "CONSOLE");
        if (sourceType.equalsIgnoreCase("MOB") || sourceType.equalsIgnoreCase("NPC")) {
            controller.getLogger().warning("Invalid command source type: " + sourceType);
            sourceType = "CONSOLE";
        }
        try {
            interactCommandSource = EntityData.SourceType.valueOf(sourceType.toUpperCase());
        } catch (Exception ex) {
            controller.getLogger().warning("Invalid command source type: " + sourceType);
            interactCommandSource = SourceType.CONSOLE;
        }

        interactCommands = ConfigurationUtils.getStringList(parameters, "interact_commands", ";");
        if (parameters.isList("drops")) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>)parameters.getList("drops");
            drops = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map || item instanceof ConfigurationSection) {
                    ConfigurationSection table = null;
                    if (item instanceof Map) {
                        table = ConfigurationUtils.toConfigurationSection((Map<?, ?>)item);
                    } else {
                        table = (ConfigurationSection)item;
                    }
                    Deque<WeightedPair<String>> dropProbability = new ArrayDeque<>();
                    RandomUtils.populateStringProbabilityMap(dropProbability, table, 0, 0, 0);
                    drops.add(dropProbability);
                } else {
                    List<String> dropList = ConfigurationUtils.getStringList(item);
                    if (dropList != null) {
                        Deque<WeightedPair<String>> dropProbability = new ArrayDeque<>();
                        RandomUtils.populateProbabilityList(String.class, dropProbability, dropList);
                        drops.add(dropProbability);
                    }
                }
            }
        }
        List<String> tagList = ConfigurationUtils.getStringList(parameters, "tags");
        if (tagList != null) {
            tags = new HashSet<>(tagList);
        }

        try {
            if (type == EntityType.HORSE) {
                extraData = new EntityHorseData(parameters, controller);
            }
            else if (type == EntityType.VILLAGER) {
                extraData = new EntityVillagerData(parameters, controller);
            }
            else if (type == EntityType.AREA_EFFECT_CLOUD) {
                extraData = new EntityAreaEffectCloudData(parameters, controller);
            }
            else if (type == EntityType.OCELOT && parameters.contains("ocelot_type")) {
                ocelotType = Ocelot.Type.valueOf(parameters.getString("ocelot_type").toUpperCase());
            }
            else if (type == EntityType.RABBIT && parameters.contains("rabbit_type")) {
                rabbitType = Rabbit.Type.valueOf(parameters.getString("rabbit_type").toUpperCase());
            }
            else if (type == EntityType.ZOMBIE || (type != null && type.name().equals("PIG_ZOMBIE"))) {
                EntityZombieData zombieData = new EntityZombieData();
                zombieData.isBaby = isBaby;
                extraData = zombieData;
            }
            else if (type == EntityType.ARMOR_STAND) {
                extraData = new EntityArmorStandData(parameters);
            } else if (type == EntityType.SLIME || type == EntityType.MAGMA_CUBE) {
                EntitySlimeData slimeData = new EntitySlimeData();
                slimeData.size = parameters.getInt("size", 16);
                extraData = slimeData;
            } else if (type == EntityType.FALLING_BLOCK) {
                extraData = new EntityFallingBlockData(parameters);
            } else if (type != null && type.name().equals("PARROT")) {
                extraData = new EntityParrotData(parameters, controller);
            } else if (type == EntityType.ENDER_DRAGON) {
                extraData = new EntityEnderDragonData(parameters, controller);
            } else if (type != null && type.name().equals("FOX")) {
                extraData = new EntityFoxData(parameters, controller);
            }

        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Invalid entity type or sub-type", ex);
        }

        ConfigurationSection attributeConfiguration = ConfigurationUtils.getConfigurationSection(parameters, "entity_attributes");
        // Migrate old attributes
        ConfigurationSection migrateAttributes = ConfigurationUtils.getConfigurationSection(parameters, "attributes");
        if (migrateAttributes != null) {
            boolean nagged = false;
            Set<String> keys = migrateAttributes.getKeys(false);
            for (String attributeKey : keys) {
                try {
                    Attribute.valueOf(attributeKey.toUpperCase());
                } catch (IllegalArgumentException ignored) {
                    continue;
                }

                if (attributeConfiguration == null) {
                    attributeConfiguration = parameters.createSection("entity_attributes");
                }
                attributeConfiguration.set(attributeKey, migrateAttributes.get(attributeKey));
                parameters.set("attributes", null);
                if (key != null && !nagged) {
                    controller.getLogger().warning("You have vanilla entity attributes in the 'attributes' property of mob template '" + key + "', please rename that to entity_attributes.");
                    nagged = true;
                }
            }
        }

        if (attributeConfiguration != null) {
            Set<String> keys = attributeConfiguration.getKeys(false);
            if (keys.size() > 0) {
                attributes = new HashMap<>();
            }
            for (String attributeKey : keys) {
                try {
                    Attribute attribute = Attribute.valueOf(attributeKey.toUpperCase());
                    attributes.put(attribute, attributeConfiguration.getDouble(attributeKey));
                } catch (Exception ex) {
                    controller.getLogger().log(Level.WARNING, "Invalid attribute type: " + attributeKey, ex);
                }
            }
        }

        MaterialAndData itemData = ConfigurationUtils.getMaterialAndData(parameters, "item");
        item = itemData == null || !itemData.isValid() ? null : itemData.getItemStack(parameters.getInt("amount", 1));

        itemInHand = controller.getOrCreateMagicItem(parameters.getString("item"));
        itemInOffhand = controller.getOrCreateMagicItem(parameters.getString("offhand"));
        helmet = controller.getOrCreateMagicItem(parameters.getString("helmet"));
        chestplate = controller.getOrCreateMagicItem(parameters.getString("chestplate"));
        leggings = controller.getOrCreateMagicItem(parameters.getString("leggings"));
        boots = controller.getOrCreateMagicItem(parameters.getString("boots"));

        canPickupItems = parameters.getBoolean("can_pickup_items", false);

        EntityMageData mageData = new EntityMageData(controller, parameters);
        if (!mageData.isEmpty()) {
            this.mageData = mageData;
        }
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
        this.entity = entity == null ? null : new WeakReference<>(entity);
        this.uuid = entity == null ? null : entity.getUniqueId();
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public static EntityType parseEntityType(String typeString) {
        if (typeString == null) return null;

        EntityType returnType = null;
        try {
            returnType = EntityType.valueOf(typeString.toUpperCase());
        } catch (Exception ex) {
            returnType = null;
        }
        if (returnType == null) {
            returnType = EntityType.fromName(typeString);
        }
        return returnType;
    }

    /**
     * API Implementation
     */

    @Override
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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
    public double getHealth() {
        return health == null ? 0 : health;
    }

    @Nullable
    @SuppressWarnings("deprecation")
    protected Entity trySpawn(MageController controller, CreatureSpawnEvent.SpawnReason reason) {
        Entity spawned = null;
        boolean addedToWorld = false;
        if (type != null && type != EntityType.PLAYER) {
            try {
                switch (type) {
                    case PAINTING:
                        spawned = CompatibilityUtils.createPainting(location, facing, art);
                        break;
                    case ITEM_FRAME:
                        spawned = CompatibilityUtils.createItemFrame(location, facing, rotation, item);
                        break;
                    case DROPPED_ITEM:
                        spawned = location.getWorld().dropItem(location, item);
                        addedToWorld = true;
                        break;
                    case FALLING_BLOCK:
                        Material material = null;
                        byte data = 0;
                        if (extraData != null && extraData instanceof EntityFallingBlockData) {
                            EntityFallingBlockData falling = (EntityFallingBlockData)extraData;
                            material = falling.getMaterial();
                            data = falling.getData();
                        }
                        if (material == null) {
                            material = Material.DIRT;
                        }
                        spawned = location.getWorld().spawnFallingBlock(location, material, data);
                        addedToWorld = true;
                        break;
                    default:
                        spawned = CompatibilityUtils.createEntity(location, type);
                    }
            } catch (Exception ex) {
                org.bukkit.Bukkit.getLogger().log(Level.WARNING, "Error restoring entity type " + getType() + " at " + getLocation(), ex);
            }
        }
        if (spawned != null) {
            modifyPreSpawn(controller, spawned);
            if (!addedToWorld) {
                isSpawning = true;
                reason = reason == null ? CreatureSpawnEvent.SpawnReason.CUSTOM : reason;
                CompatibilityUtils.addToWorld(location.getWorld(), spawned, reason);
                isSpawning = false;
            }
            modifyPostSpawn(controller, spawned);
        }
        return spawned;
    }

    @Nullable
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

    @Nullable
    @Override
    public Entity spawn() {
        return spawn(null, null);
    }

    @Nullable
    @Override
    public Entity spawn(Location location) {
        return spawn(null, location, null);
    }

    @Nullable
    @Override
    public Entity spawn(MageController controller) {
        return spawn(controller, null);
    }

    @Nullable
    @Override
    public Entity spawn(MageController controller, Location location) {
        return spawn(controller, location, null);
    }

    @Nullable
    @Override
    public Entity spawn(MageController controller, Location location, CreatureSpawnEvent.SpawnReason reason) {
        if (location != null) this.location = location;
        else if (this.location == null) return null;
        return trySpawn(controller, reason);
    }

    @Nullable
    @Override
    public Entity undo() {
        Entity entity = this.getEntity();

        // Re-spawn if dead or missing
        if (respawn && !isTemporary && uuid != null && (entity == null || !entity.isValid() || entity.isDead()) && !(entity instanceof Player)) {
            // Avoid re-re-spawning an entity
            WeakReference<Entity> respawnedEntity = respawned.get(uuid);
            if (respawnedEntity != null) {
                entity = respawnedEntity.get();
            } else {
                entity = trySpawn(null, null);
                if (entity != null) {
                    respawned.put(uuid, new WeakReference<>(entity));

                    // Undo'ing an entity won't drop loot
                    entity.setMetadata("nodrops", new FixedMetadataValue(MagicPlugin.getAPI().getPlugin(), true));
                }
            }
            setEntity(entity);
        } else if (entity != null) {
            modify(entity);
        }
        return entity;
    }

    @Override
    public boolean modify(Entity entity) {
        return modify(null, entity);
    }

    @Override
    public boolean modify(MageController controller, Entity entity) {
        // Don't check isValid here since it will be false on the spawn event!
        if (entity.isDead()) return false;
        boolean modifiedPre = modifyPreSpawn(controller, entity);
        boolean modifiedPost = modifyPostSpawn(controller, entity);
        return modifiedPre || modifiedPost;
    }

    private boolean modifyPreSpawn(MageController controller, Entity entity) {
        if (entity == null || (type != null && entity.getType() != type)) return false;

        if (controller != null) {
            controller.registerMob(entity, this);
        }
        boolean isPlayer = (entity instanceof Player);
        if (extraData != null) {
            extraData.apply(entity);
        }

        if (persist != null) {
            CompatibilityUtils.setPersist(entity, persist);
        }
        CompatibilityUtils.setSilent(entity, isSilent);
        entity.setFireTicks(fireTicks);
        if (entity instanceof Ageable) {
            Ageable ageable = (Ageable)entity;
            if (isBaby) {
                ageable.setBaby();
            } else {
                ageable.setAdult();
            }
        }
        if (entity instanceof Tameable) {
            ((Tameable)entity).setTamed(isTamed);
        }
        CompatibilityUtils.setSitting(entity, isSitting);
        CompatibilityUtils.setInvulnerable(entity, isInvulnerable);

        if (entity instanceof Colorable && dyeColor != null) {
            Colorable colorable = (Colorable)entity;
            colorable.setColor(dyeColor);
        }

        if (tags != null && !tags.isEmpty()) {
            Set<String> entityTags = CompatibilityUtils.getTags(entity);
            if (entityTags != null) {
                entityTags.addAll(tags);
            }
        }

        if (entity instanceof Creature) {
            Creature creature = (Creature)entity;
            creature.setCanPickupItems(canPickupItems);
        }

        if (entity instanceof Item) {
            Item droppedItem = (Item)entity;
            droppedItem.setItemStack(item);
        } else if (entity instanceof Wolf) {
            Wolf wolf = (Wolf)entity;
            if (dyeColor != null) {
                wolf.setCollarColor(dyeColor);
            }
            wolf.setAngry(isAngry);
        } else if (entity instanceof Ocelot && ocelotType != null) {
            Ocelot ocelot = (Ocelot)entity;
            ocelot.setCatType(ocelotType);
        } else if (entity instanceof Rabbit && rabbitType != null) {
            Rabbit rabbit = (Rabbit)entity;
            rabbit.setRabbitType(rabbitType);
        } else if (entity instanceof ExperienceOrb && xp != null) {
            ((ExperienceOrb)entity).setExperience(xp);
        }

        // Armor stands handle gravity themselves, for now
        if (!hasGravity && !(entity instanceof ArmorStand)) {
            CompatibilityUtils.setGravity(entity, hasGravity);
        }

        if (entity instanceof LivingEntity) {
            LivingEntity li = (LivingEntity)entity;
            if (hasPotionEffects && potionEffects != null) {
                for (PotionEffect effect : potionEffects) {
                        li.addPotionEffect(effect);
                }
            }
            if (removeEffects != null) {
                for (PotionEffectType effectType : removeEffects) {
                    li.removePotionEffect(effectType);
                }
            }
            if (modifiers != null || removeModifiers != null) {
                Mage mage = controller.getRegisteredMage(li);
                if (mage != null) {
                    if (modifiers != null) {
                        for (Map.Entry<String, ConfigurationSection> modifier : modifiers.entrySet()) {
                            mage.addModifier(modifier.getKey(), modifier.getValue());
                        }
                    }
                    if (removeModifiers != null) {
                        for (String modifierKey : removeModifiers) {
                            mage.removeModifier(modifierKey);
                        }
                    }
                }
            }

            try {
                if (!isPlayer) {
                    applyAttributes(li);
                    copyEquipmentTo(li);
                    if (maxHealth != null) {
                        CompatibilityUtils.setMaxHealth(li, maxHealth);
                    }
                }
                if (health != null && hasChangedHealth) {
                    li.setHealth(Math.min(health, CompatibilityUtils.getMaxHealth(li)));
                }
                if (airLevel != null) {
                    li.setRemainingAir(Math.min(airLevel, li.getRemainingAir()));
                }
                if (!hasAI) {
                    li.setAI(hasAI);
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        if (!isPlayer && name != null && name.length() > 0) {
            entity.setCustomName(name);
        }
        if (!isPlayer) {
            entity.setCustomNameVisible(nameVisible);
        }
        if (controller != null) {
            attach(controller, entity);
        }

        if (controller != null && disguise != null) {
            tryDisguise(controller, entity, disguise);
            int redisguise = disguise.getString("type", "").equalsIgnoreCase("player") ? 2 : 0;
            redisguise = disguise.getInt("redisguise", redisguise);
            if (redisguise > 0) {
                Bukkit.getScheduler().runTaskLater(controller.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        tryDisguise(controller, entity, disguise);
                    }
                }, redisguise);
            }
        }

        return true;
    }

    private void tryDisguise(final MageController controller, final Entity entity, final ConfigurationSection disguise) {
        if (!controller.disguise(entity, disguise)) {
            controller.getLogger().warning("Invalid disguise type: " + disguise.getString("type"));
        }
    }

    @Override
    public void attach(@Nonnull MageController controller, @Nonnull Entity entity) {
        if (mageData != null) {
            Mage apiMage = controller.getMage(entity);
            if (apiMage.getEntityData() == this) return;

            if (apiMage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                ((com.elmakers.mine.bukkit.magic.Mage)apiMage).setEntityData(this);
            }

            mageData.resetTriggers();
            mageData.trigger(apiMage, "spawn");
        }
    }

    @Nullable
    public ConfigurationSection getMageProperties() {
        return mageData != null ? mageData.mageProperties : null;
    }

    private boolean modifyPostSpawn(MageController controller, Entity entity) {
        if (entity == null || (type != null && entity.getType() != type)) return false;

        if (hasMoved && location != null && !location.equals(entity.getLocation())) {
            entity.teleport(location);
        }

        if (hasVelocity && velocity != null) {
            SafetyUtils.setVelocity(entity, velocity);
        }
        if (mount != null || mountType != null) {
            mobStackSize++;
            boolean allowMount = true;
            if (mobStackSize > maxMobStackSize) {
                if (controller != null) {
                    controller.getLogger().warning("Mob " + key + " has more than " + maxMobStackSize + " mounts");
                }
                allowMount = false;
            }
            if (mount == null) {
                mount = (EntityData)controller.getMob(mountType);
                if (mount == null) {
                    if (controller != null) {
                        controller.getLogger().warning("Mob " + key + " has invalid mount: " + mountType);
                    }
                    allowMount = false;
                }
            }
            if (allowMount) {
                Entity mountEntity = mount.spawn(controller, entity.getLocation());
                DeprecatedUtils.setPassenger(mountEntity, entity);
            }
            mobStackSize--;
        }
        if (entity instanceof Painting) {
            Painting painting = (Painting) entity;
            if (art != null) {
                painting.setArt(art, true);
            }
            if (facing != null) {
                painting.setFacingDirection(facing, true);
            }
        } else if (entity instanceof ItemFrame) {
            ItemFrame itemFrame = (ItemFrame)entity;
            itemFrame.setItem(item);
            if (facing != null) {
                itemFrame.setFacingDirection(facing, true);
            }
        }
        return true;
    }

    public void applyAttributes(LivingEntity entity) {
        if (attributes != null) {
            for (Map.Entry<Attribute, Double> entry : attributes.entrySet()) {
                AttributeInstance attribute = entity.getAttribute(entry.getKey());
                if (attribute != null) {
                    attribute.setBaseValue(entry.getValue());
                }
            }
        }
    }

    public void copyEquipmentTo(LivingEntity entity) {
        if (itemInHand != null) {
            entity.getEquipment().setItemInMainHand(itemInHand.getItemStack(1));
        }
        if (itemInOffhand != null) {
            entity.getEquipment().setItemInOffHand(itemInOffhand.getItemStack(1));
        }
        if (helmet != null) {
            entity.getEquipment().setHelmet(helmet.getItemStack(1));
        }
        if (chestplate != null) {
            entity.getEquipment().setChestplate(chestplate.getItemStack(1));
        }
        if (leggings != null) {
            entity.getEquipment().setLeggings(leggings.getItemStack(1));
        }
        if (boots != null) {
            entity.getEquipment().setBoots(boots.getItemStack(1));
        }
    }

    @Override
    public void setHasMoved(boolean moved) {
        this.hasMoved = moved;
    }

    @Override
    public void setDamaged(boolean damaged) {
        this.hasChangedHealth = damaged;
    }

    public void setHasPotionEffects(boolean changed) {
        this.hasPotionEffects = changed;
    }

    public void addPotionEffectForRemoval(PotionEffectType potionEffectType) {
        if (removeEffects == null) {
            removeEffects = new ArrayList<>();
        }
        removeEffects.add(potionEffectType);
    }

    public void addModifier(MageModifier modifier) {
        if (this.modifiers == null) {
            this.modifiers = new HashMap<>();
        }
        this.modifiers.put(modifier.getKey(), modifier.getConfiguration());
    }

    public void addModifierForRemoval(String modifierKey) {
        if (removeModifiers == null) {
            removeModifiers = new ArrayList<>();
        }
        removeModifiers.add(modifierKey);
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

    @Nullable
    public Entity getEntity() {
        return entity == null ? null : entity.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public EntityData clone() {
        try {
            return (EntityData)super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean isRespawn() {
        return respawn;
    }

    public void setRespawn(boolean respawn) {
        this.respawn = respawn;
    }

    public void modifyDrops(MageController controller, EntityDeathEvent event) {
        if (dropXp != null) {
            event.setDroppedExp(dropXp);
        }

        List<ItemStack> dropList = event.getDrops();
        if (!defaultDrops) {
            dropList.clear();
        }

        if (dropsRequirePlayerKiller) {
            EntityDamageEvent lastDamage = event.getEntity().getLastDamageCause();
            if (!(lastDamage instanceof EntityDamageByEntityEvent)) return;
            Entity damager = ((EntityDamageByEntityEvent)lastDamage).getDamager();
            damager = CompatibilityUtils.getSource(damager);
            if (!(damager instanceof Player) || damager == event.getEntity()) return;
        }

        if (drops != null) {
            for (Deque<WeightedPair<String>> dropTable : drops) {
                String key = RandomUtils.weightedRandom(dropTable);
                if (key != null) {
                    ItemStack item = controller.createItem(key);
                    if (item != null) {
                        dropList.add(item);
                    }
                }
            }
        }
    }

    @Override
    public String describe() {
        if (name != null && !name.isEmpty()) {
            return name;
        }
        if (type == null) return "Unknown";

        String name = type.name();
        if (ocelotType != null) {
            name += ":" + ocelotType;
        } else if (rabbitType != null) {
            name += ":" + rabbitType;
        }
        return name;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    public long getTickInterval() {
        return mageData == null ? 0 : mageData.tickInterval;
    }

    public void tick(Mage mage) {
        if (mageData != null) {
            mageData.tick(mage);
        }
    }

    public boolean trigger(Mage mage, String trigger) {
        if (mageData == null) {
            return false;
        }

        boolean result = mageData.trigger(mage, trigger);
        mageData.resetTriggers();
        return result;
    }

    @Override
    @Nullable
    public String getInteractSpell() {
        return interactSpell;
    }

    @Override
    @Nullable
    public ConfigurationSection getInteractSpellParameters() {
        return interactSpellParameters;
    }

    @Override
    public EntityData.SourceType getInteractSpellSource() {
        return interactSpellSource;
    }

    @Override
    public EntityData.TargetType getInteractSpellTarget() {
        return interactSpellTarget;
    }

    @Override
    @Nullable
    public List<String> getInteractCommands() {
        return interactCommands;
    }

    @Override
    public boolean hasInteract() {
        if (interactSpell != null && !interactSpell.isEmpty()) return true;
        if (interactCommands != null && !interactCommands.isEmpty()) return true;
        return false;
    }

    @Override
    public EntityData.SourceType getInteractCommandSource() {
        return interactCommandSource;
    }

    @Override
    @Nullable
    public Collection<com.elmakers.mine.bukkit.api.item.Cost> getInteractCosts() {
        return interactCosts;
    }

    public boolean shouldFocusOnDamager() {
        return mageData == null ? false : mageData.aggro;
    }

    public double getTrackRadiusSquared() {
        return mageData == null ? 0 : mageData.getTrackRadiusSquared();
    }

    @Override
    public void setMaterial(@Nonnull com.elmakers.mine.bukkit.api.block.MaterialAndData material) {
        if (extraData != null && extraData instanceof EntityFallingBlockData) {
            ((EntityFallingBlockData)extraData).setMaterialAndData(material);
        }

        // Not sure if I should mess with "item" here
    }

    @Override
    @Nullable
    public com.elmakers.mine.bukkit.api.block.MaterialAndData getMaterial() {
        if (extraData != null && extraData instanceof EntityFallingBlockData) {
            return ((EntityFallingBlockData)extraData).getMaterialAndData();
        }

        if (item != null) {
            return new MaterialAndData(item);
        }

        return null;
    }

    public boolean isCancelLaunch() {
        return mageData != null ? mageData.isCancelLaunch : true;
    }

    public boolean hasAI() {
        return hasAI;
    }

    public void setAI(boolean hasAI) {
        this.hasAI = hasAI;
    }

    public boolean isInvulnerable() {
        return isInvulnerable;
    }

    public void setInvulnerable(boolean invulnerable) {
        isInvulnerable = invulnerable;
    }

    public boolean hasGravity() {
        return hasGravity;
    }

    public void setGravity(boolean hasGravity) {
        this.hasGravity = hasGravity;
    }

    public boolean isPersist() {
        return persist != null && persist;
    }

    public void setPersist(boolean persist) {
        this.persist = persist;
    }

    @Override
    public boolean isDocile() {
        return isDocile;
    }

    @Override
    public boolean isTransformable() {
        return transformable;
    }

    @Nullable
    @Override
    public ConfigurationSection getConfiguration() {
        return configuration;
    }

    @Nullable
    @Override
    public String getInteractPermission() {
        return interactPermission;
    }

    public boolean isSplittable() {
        return isSplittable;
    }

    @Override
    public boolean isPreventProjectiles() {
        return preventProjectiles;
    }

    @Override
    public boolean isPreventMelee() {
        return preventMelee;
    }
}
