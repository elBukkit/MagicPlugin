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
import org.bukkit.Location;
import org.bukkit.Rotation;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.integration.ModelEngine;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.item.ItemUpdatedCallback;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MageModifier;
import com.elmakers.mine.bukkit.boss.BossBarConfiguration;
import com.elmakers.mine.bukkit.boss.BossBarTracker;
import com.elmakers.mine.bukkit.configuration.MageParameters;
import com.elmakers.mine.bukkit.item.Cost;
import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.mob.GoalConfiguration;
import com.elmakers.mine.bukkit.mob.GoalType;
import com.elmakers.mine.bukkit.tasks.DisguiseTask;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.SafetyUtils;
import com.elmakers.mine.bukkit.utility.platform.MobUtils;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.utility.random.WeightedPair;

/**
 * This class stores information about an Entity.
 *
 */
public class EntityData
        implements com.elmakers.mine.bukkit.api.entity.EntityData, Cloneable {
    protected static Map<UUID, WeakReference<Entity>> respawned = new HashMap<>();

    public static boolean isSpawning = false;
    private static int mobStackSize = 0;
    private static final int maxMobStackSize = 255;

    @Nonnull
    private final MageController controller;
    protected String key;
    protected String mythicMobKey;
    protected Double mythicMobLevel;
    protected WeakReference<Entity> entity = null;
    protected UUID uuid = null;
    protected UUID ownerId = null;

    protected EntityType type;
    protected EntityExtraData extraData;
    protected Location location;
    protected Vector relativeLocation;
    protected boolean hasMoved = false;
    protected boolean hasChangedHealth = false;
    protected boolean isTemporary = false;
    protected boolean cancelExplosion = false;
    protected boolean magicSpawned = false;
    private boolean respawn = true;
    protected String name = null;

    protected Double maxHealth;
    protected Double health;
    protected Integer airLevel;
    protected Boolean isBaby;
    protected Boolean isSilent;
    protected Boolean isTamed;
    protected Boolean isSitting;
    protected Boolean isInvulnerable;
    protected Boolean isAware;
    protected Boolean hasAI;
    protected Boolean hasGravity;
    protected Boolean isCollidable;
    protected boolean isDocile;
    protected boolean isRelentless;
    protected boolean transformable = true;
    protected boolean combustible = true;
    protected boolean ownable = false;
    protected boolean isStatic = false;
    protected boolean preventProjectiles;
    protected boolean preventMelee;
    protected Boolean nameVisible;
    protected boolean isNPC;
    protected boolean isHidden;
    protected boolean useNPCName;
    protected boolean preventDismount;
    protected boolean preventTeleport;
    protected boolean equipOnRespawn = true;
    protected Boolean stay;
    protected Boolean invisible;
    protected Boolean persistentInvisible;
    protected Boolean persist;
    protected Boolean removeWhenFarAway;
    protected Boolean canPickupItems;
    protected Integer fireTicks;
    protected Set<String> permissions;

    protected Collection<PotionEffect> potionEffects = null;
    protected Collection<PotionEffectType> removeEffects = null;
    protected Map<String, ConfigurationSection> modifiers = null;
    protected Collection<String> removeModifiers = null;
    protected Map<Attribute, Double> attributes = null;

    protected Vector velocity = null;
    protected boolean hasPotionEffects = false;
    protected boolean hasVelocity = false;
    protected boolean isLiving = false;
    protected boolean isProjectile = false;
    protected boolean isSuperProtected = false;
    protected boolean registerByName = false;

    protected Deque<WeightedPair<ItemData>> itemInHand;
    protected Deque<WeightedPair<ItemData>> itemInOffhand;
    protected Deque<WeightedPair<ItemData>> helmet;
    protected Deque<WeightedPair<ItemData>> chestplate;
    protected Deque<WeightedPair<ItemData>> leggings;
    protected Deque<WeightedPair<ItemData>> boots;

    protected Integer xp;
    protected Integer dropXp;

    protected boolean defaultDrops;
    protected boolean dropsRequirePlayerKiller;
    protected List<Deque<WeightedPair<String>>> drops;
    protected ConfigurationSection loot;
    protected ConfigurationSection brain;
    protected int interval;
    protected Set<String> tags;
    protected Set<String> removeMounts;
    protected String interactSpell;
    protected String interactPermission;
    protected boolean interactRequiresOwner;
    protected List<com.elmakers.mine.bukkit.api.item.Cost> interactCosts;
    protected ConfigurationSection interactSpellParameters;
    protected EntityData.SourceType interactSpellSource;
    protected EntityData.TargetType interactSpellTarget;
    protected EntityData.SourceType interactCommandSource;
    protected List<String> interactCommands;
    protected boolean cancelInteract;
    protected ConfigurationSection disguise;
    protected ConfigurationSection model;
    protected BossBarConfiguration bossBar;

    protected EntityMageData mageData;
    protected EntityData mount;
    protected String mountType;

    protected ConfigurationSection configuration;

    public EntityData(MageController controller, Entity entity) {
        this.controller = controller;
        setEntity(entity);
        this.location = CompatibilityLib.getCompatibilityUtils().getHangingLocation(entity);
        this.magicSpawned = CompatibilityLib.getEntityMetadataUtils().getBoolean(entity, MagicMetaKeys.MAGIC_SPAWNED);
        this.cancelExplosion = CompatibilityLib.getEntityMetadataUtils().getBoolean(entity, MagicMetaKeys.CANCEL_EXPLOSION);
        this.isTemporary = CompatibilityLib.getEntityMetadataUtils().getBoolean(entity, MagicMetaKeys.TEMPORARY);
        this.isLiving = entity instanceof LivingEntity;
        this.isProjectile = entity instanceof Projectile;
        this.type = entity.getType();
        this.fireTicks = entity.getFireTicks();
        this.isSilent = CompatibilityLib.getCompatibilityUtils().isSilent(entity);
        this.invisible = CompatibilityLib.getCompatibilityUtils().isInvisible(entity);
        this.persistentInvisible = CompatibilityLib.getCompatibilityUtils().isPersistentInvisible(entity);

        // This will actually always be true so we need a better way to track this.
        // this.persist = CompatibilityUtils.isPersist(entity);
        this.canPickupItems = (entity instanceof Creature) ? ((Creature)entity).getCanPickupItems() : false;
        name = entity.getCustomName();
        nameVisible = entity.isCustomNameVisible();
        tags = CompatibilityLib.getCompatibilityUtils().getTags(entity);

        // This can sometimes throw an exception on an invalid
        // entity velocity!
        try {
            this.velocity = entity.getVelocity();
        } catch (Exception ex) {
            this.velocity = null;
        }

        this.isAware = CompatibilityLib.getCompatibilityUtils().isAware(entity);
        if (entity instanceof LivingEntity) {
            LivingEntity li = (LivingEntity)entity;
            this.health = li.getHealth();
            this.potionEffects = li.getActivePotionEffects();
            this.airLevel = li.getRemainingAir();
            this.maxHealth = CompatibilityLib.getCompatibilityUtils().getMaxHealth(li);
            this.hasAI = li.hasAI();

            EntityEquipment equipment = li.getEquipment();
            itemInHand = equipment == null ? null : RandomUtils.createProbabilityConstant(getItem(equipment.getItemInMainHand()));
            itemInOffhand = equipment == null ? null : RandomUtils.createProbabilityConstant(getItem(equipment.getItemInOffHand()));
            helmet = equipment == null ? null : RandomUtils.createProbabilityConstant(getItem(equipment.getHelmet()));
            chestplate = equipment == null ? null : RandomUtils.createProbabilityConstant(getItem(equipment.getChestplate()));
            leggings = equipment == null ? null : RandomUtils.createProbabilityConstant(getItem(equipment.getLeggings()));
            boots = equipment == null ? null : RandomUtils.createProbabilityConstant(getItem(equipment.getBoots()));
        }

        if (entity instanceof Tameable) {
            isTamed = ((Tameable)entity).isTamed();
        }
        isSitting = CompatibilityLib.getCompatibilityUtils().isSitting(entity);
        stay = CompatibilityLib.getEntityMetadataUtils().getBoolean(entity, MagicMetaKeys.STAY);
        isInvulnerable = CompatibilityLib.getCompatibilityUtils().isInvulnerable(entity);
        mythicMobKey = controller.getMythicMobKey(entity);
        mythicMobLevel = controller.getMythicMobLevel(entity);

        if (entity instanceof Ageable) {
            Ageable ageable = (Ageable)entity;
            this.isBaby = !ageable.isAdult();
        }

        ownerId = CompatibilityLib.getCompatibilityUtils().getOwnerId(entity);

        // TODO: Extra data class for this?
        if (entity instanceof ExperienceOrb) {
            xp = ((ExperienceOrb)entity).getExperience();
        }
        extraData = CompatibilityLib.getEntityUtils().getExtraData(controller, entity);
    }

    public EntityData(MageController controller, EntityType type) {
        this.controller = controller;
        this.type = type;
    }

    public EntityData(@Nonnull MageController controller, @Nonnull String key, ConfigurationSection parameters) {
        this.controller = controller;
        this.key = key;
        load(parameters);
    }

    public EntityData(@Nonnull MageController controller, ConfigurationSection parameters) {
        this.controller = controller;
        load(parameters);
    }

    private EntityData(@Nonnull MageController controller, @Nonnull String key) {
        this.controller = controller;
        this.key = key;
    }

    public static EntityData wrapMythicMob(@Nonnull MageController controller, String mythicMobKey) {
        EntityData wrapped = new EntityData(controller, mythicMobKey);
        wrapped.mythicMobKey = mythicMobKey;
        return wrapped;
    }

    public void setMythicMobKey(String mythicMobKey) {
        this.mythicMobKey = mythicMobKey;
    }

    @Nullable
    private ItemData getItem(ItemStack item) {
        return item == null ? null : controller.createItemData(item);
    }

    @Override
    @Deprecated
    public ItemStack getItem() {
        return null;
    }

    @Deprecated
    @Override
    public void load(@Nonnull MageController controller, ConfigurationSection parameters) {
        load(parameters);
    }

    @Override
    public void load(ConfigurationSection parameters) {
        this.configuration = parameters;
        // This is required to allow changes to health
        hasChangedHealth = true;
        name = parameters.getString("name");
        if (name == null && key != null) {
            name = controller.getMessages().getIfSet("mobs." + key + ".name");
        }
        if (name != null) {
            name = CompatibilityLib.getCompatibilityUtils().translateColors(name);
        }
        registerByName = true;
        if (name != null && (name.equals("Grumm") || name.equals("Dinnerbone") || name.equals("jeb_"))) {
            registerByName = false;
        }
        registerByName = parameters.getBoolean("register_by_name", registerByName);
        isNPC = parameters.getBoolean("npc");
        isStatic = parameters.getBoolean("static", isNPC);
        useNPCName = parameters.getBoolean("use_npc_name", false);
        isHidden = parameters.getBoolean("hidden");
        nameVisible = ConfigUtils.getOptionalBoolean(parameters, "show_name");
        mythicMobKey = parameters.getString("mythic_mob");
        mythicMobLevel = ConfigUtils.getOptionalDouble(parameters, "mythic_mob_level");
        health = ConfigUtils.getOptionalDouble(parameters, "health");
        maxHealth = ConfigUtils.getOptionalDouble(parameters, "max_health");
        // Shortcut for max_health
        if (health != null && maxHealth == null) maxHealth = health;
        isSilent = ConfigUtils.getOptionalBoolean(parameters, "silent");
        if (parameters.contains("persist")) {
            persist = parameters.getBoolean("persist");
        }
        if (parameters.contains("remove_when_far_away")) {
            removeWhenFarAway = parameters.getBoolean("remove_when_far_away");
        } else if (persist != null && persist) {
            removeWhenFarAway = false;
        } else if (persist != null) {
            removeWhenFarAway = true;
        }
        if (parameters.contains("invisible")) {
            invisible = parameters.getBoolean("invisible");
            persistentInvisible = invisible;
        }
        isDocile = parameters.getBoolean("docile");
        isRelentless = parameters.getBoolean("relentless");
        transformable = parameters.getBoolean("transformable", true);
        combustible = parameters.getBoolean("combustible", true);
        ownable = parameters.getBoolean("ownable", false);
        preventProjectiles = parameters.getBoolean("prevent_projectiles", false);
        preventMelee = parameters.getBoolean("prevent_melee", false);
        bossBar = BossBarConfiguration.parse(controller, parameters, "$pn");
        preventDismount = parameters.getBoolean("prevent_dismount", false);
        preventTeleport = parameters.getBoolean("prevent_teleport", false);
        cancelExplosion = parameters.getBoolean("cancel_explosion", false);
        isTemporary = parameters.getBoolean("temporary", false);
        equipOnRespawn = parameters.getBoolean("equip_on_respawn", true);
        List<String> permissionsList = ConfigurationUtils.getStringList(parameters, "permissions");
        if (permissionsList != null && !permissionsList.isEmpty()) {
            permissions = new HashSet<>(permissionsList);
        }

        String entityName = parameters.contains("type") ? parameters.getString("type") : key;
        if (entityName != null && !entityName.isEmpty()) {
            type = parseEntityType(entityName);
        }

        ConfigurationSection mountConfig = ConfigurationUtils.getConfigurationSection(parameters, "mount");
        if (mountConfig != null) {
            mount = new EntityData(controller, mountConfig);
        } else {
            mountType = parameters.getString("mount");
        }
        List<String> removeMountKeys = ConfigurationUtils.getStringList(parameters, "remove_mounts");
        if (removeMountKeys != null && !removeMountKeys.isEmpty()) {
            removeMounts = new HashSet<>(removeMountKeys);
        }
        setOwnerId(parameters.getString("owner"));

        disguise = ConfigurationUtils.getConfigurationSection(parameters, "disguise");
        model = ConfigurationUtils.getConfigurationSection(parameters, "model");
        if (model == null) {
            String modelId = parameters.getString("model");
            if (modelId != null && !modelId.isEmpty()) {
                model = ConfigurationUtils.newConfigurationSection();
                model.set("id", modelId);
            }
        }

        isTamed = ConfigUtils.getOptionalBoolean(parameters, "tamed");
        isSitting = ConfigUtils.getOptionalBoolean(parameters, "sitting");
        isInvulnerable = ConfigUtils.getOptionalBoolean(parameters, "invulnerable");
        isBaby = ConfigUtils.getOptionalBoolean(parameters, "baby");
        isAware = ConfigUtils.getOptionalBoolean(parameters, "aware");
        hasGravity = ConfigUtils.getOptionalBoolean(parameters, "gravity");
        isCollidable = ConfigUtils.getOptionalBoolean(parameters, "collidable");
        canPickupItems = ConfigUtils.getOptionalBoolean(parameters, "can_pickup_items");

        isSuperProtected = parameters.getBoolean("protected", false);
        stay = ConfigUtils.getOptionalBoolean(parameters, "stay");

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
        interactRequiresOwner = parameters.getBoolean("interact_requires_owner", false);
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
                        table = ConfigurationUtils.toConfigurationSection(parameters, (Map<?, ?>)item);
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
                        RandomUtils.populateStringProbabilityList(dropProbability, dropList);
                        drops.add(dropProbability);
                    }
                }
            }
        }

        hasAI = ConfigUtils.getOptionalBoolean(parameters, "ai");
        brain = ConfigurationUtils.getConfigurationSection(parameters, "brain");
        interval = parameters.getInt("interval", 1000);

        loot = parameters.getConfigurationSection("loot");
        cancelInteract = parameters.getBoolean("cancel_interact");
        List<String> tagList = ConfigurationUtils.getStringList(parameters, "tags");
        if (tagList != null) {
            tags = new HashSet<>(tagList);
        }

        try {
            extraData = type == null ? null : CompatibilityLib.getEntityUtils().getExtraData(controller, type, parameters);
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
                    controller.getLogger().log(Level.WARNING, "Invalid attribute type: " + attributeKey);
                }
            }
        }

        EquipmentParser parser = EquipmentParser.getInstance(controller);

        itemInHand = RandomUtils.createProbabilityMap(parser, parameters, "item");
        itemInOffhand = RandomUtils.createProbabilityMap(parser, parameters, "offhand");
        helmet = RandomUtils.createProbabilityMap(parser, parameters, "helmet");
        chestplate = RandomUtils.createProbabilityMap(parser, parameters, "chestplate");
        leggings = RandomUtils.createProbabilityMap(parser, parameters, "leggings");
        boots = RandomUtils.createProbabilityMap(parser, parameters, "boots");

        EntityMageData mageData = new EntityMageData(controller, parameters);
        if (!mageData.isEmpty()) {
            this.mageData = mageData;
        }
    }

    private void setOwnerId(String ownerIdString) {
        if (ownerIdString != null && !ownerIdString.isEmpty()) {
            try {
                ownerId = UUID.fromString(ownerIdString);
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid owner id: " + ownerIdString);
            }
        } else {
            ownerId = null;
        }
    }

    public void validate() {
        if (mythicMobKey != null) {
            if (!controller.isMythicMobKey(mythicMobKey)) {
                controller.getLogger().warning("Invalid mythic mob key: " + mythicMobKey);
            }
            return;
        }
        if (type == null) {
            controller.getLogger().warning("Mob config missing 'type' and not a vanilla mob type: " + key);
        }
    }

    public static EntityData loadPainting(MageController controller, Vector location, Art art, BlockFace direction) {
        EntityData data = new EntityData(controller, EntityType.PAINTING);
        data.extraData = CompatibilityLib.getEntityUtils().getPaintingData(art, direction);
        data.relativeLocation = location.clone();
        return data;
    }

    public static EntityData loadItemFrame(MageController controller, Vector location, ItemStack item, BlockFace direction, Rotation rotation) {
        EntityData data = new EntityData(controller, EntityType.ITEM_FRAME);
        data.extraData = CompatibilityLib.getEntityUtils().getItemFrameData(item, direction, rotation);
        data.relativeLocation = location.clone();
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
    @Deprecated
    public Art getArt() {
        return null;
    }

    @Override
    @Deprecated
    public BlockFace getFacing() {
        return BlockFace.SELF;
    }

    @Override
    public double getHealth() {
        return health == null ? 0 : health;
    }

    @Nullable
    protected Entity trySpawn(CreatureSpawnEvent.SpawnReason reason) {
        Entity spawned = null;
        boolean addedToWorld = false;
        if (mythicMobKey != null) {
            spawned = controller.spawnMythicMob(mythicMobKey, location);
            if (spawned != null) {
                if (mythicMobLevel != null) {
                    controller.setMythicMobLevel(spawned, mythicMobLevel == null ? 1 : mythicMobLevel);
                }
                addedToWorld = true;
            } else {
                controller.getLogger().warning("Could not spawn mythic mob: " + mythicMobKey + " from mob config " + getKey());
            }
        }
        if (spawned == null && type != null && type != EntityType.PLAYER) {
            controller.setDisableSpawnReplacement(true);
            try {
                SpawnedEntityExtraData spawnedEntity = null;
                if (extraData != null) {
                    spawnedEntity = extraData.spawn(location);
                }
                if (spawnedEntity != null) {
                    spawned = spawnedEntity.getEntity();
                    addedToWorld = spawnedEntity.isAddedToWorld();
                } else {
                    spawned = CompatibilityLib.getCompatibilityUtils().createEntity(location, type);
                }
            } catch (Exception ex) {
                org.bukkit.Bukkit.getLogger().log(Level.WARNING, "Error restoring entity type " + getType() + " at " + getLocation(), ex);
            }
            controller.setDisableSpawnReplacement(false);
        }
        if (spawned != null) {
            try {
                modifyPreSpawn(spawned, true, true);
                if (!addedToWorld) {
                    isSpawning = true;
                    reason = reason == null ? CreatureSpawnEvent.SpawnReason.CUSTOM : reason;
                    CompatibilityLib.getCompatibilityUtils().addToWorld(location.getWorld(), spawned, reason);
                    isSpawning = false;
                }
                modifyPostSpawn(spawned);
            } catch (Exception ex) {
                 org.bukkit.Bukkit.getLogger().log(Level.WARNING, "Error restoring entity properties for] " + getType() + " at " + getLocation(), ex);
            }
        }
        return spawned;
    }

    @Nullable
    @Override
    public EntityData getRelativeTo(Location center) {
        EntityData copy = this.clone();
        if (relativeLocation != null) {
            copy.location = center.clone().add(relativeLocation);
        } else if (location != null) {
            copy.location = location.clone();
        }

        return copy;
    }

    @Nullable
    @Override
    public Entity spawn() {
        return spawn((Location)null, null);
    }

    @Nullable
    @Override
    public Entity spawn(Location location) {
        return spawn(location, null);
    }

    @Deprecated
    @Nullable
    @Override
    public Entity spawn(MageController controller) {
        return spawn((Location)null, null);
    }

    @Deprecated
    @Nullable
    @Override
    public Entity spawn(MageController controller, Location location) {
        return spawn(location, null);
    }

    @Deprecated
    @Nullable
    @Override
    public Entity spawn(MageController controller, Location location, CreatureSpawnEvent.SpawnReason reason) {
        return spawn(location, reason);
    }

    @Nullable
    @Override
    public Entity spawn(Location location, CreatureSpawnEvent.SpawnReason reason) {
        if (location != null) this.location = location;
        else if (this.location == null) return null;
        Entity entity = trySpawn(reason);
        if (entity != null && mageData != null) {
            Mage mage = controller.getMage(entity);
            mageData.trigger(mage, "spawn");
        }
        return entity;
    }

    @Nullable
    @Override
    public Entity undo() {
        Entity entity = this.getEntity();

        // Re-spawn if dead or missing
        if (respawn && !isTemporary && !magicSpawned && uuid != null && (entity == null || !entity.isValid() || entity.isDead()) && !(entity instanceof Player)) {
            // Avoid re-re-spawning an entity
            WeakReference<Entity> respawnedEntity = respawned.get(uuid);
            if (respawnedEntity != null) {
                entity = respawnedEntity.get();
            } else {
                entity = trySpawn(null);
                if (entity != null) {
                    respawned.put(uuid, new WeakReference<>(entity));

                    // Undo'ing an entity won't drop loot
                    CompatibilityLib.getEntityMetadataUtils().setBoolean(entity, MagicMetaKeys.NO_DROPS, true);
                }
            }
            if (entity != null && mageData != null) {
                Mage mage = controller.getMage(entity);
                mageData.trigger(mage, "respawn");
            }
            setEntity(entity);
        } else if (entity != null) {
            modify(entity, false);
        }
        return entity;
    }

    @Override
    public boolean respawned(Entity entity) {
        boolean success = modify(entity, true);
        if (success && mageData != null) {
            Mage mage = controller.getMage(entity);
            mageData.trigger(mage, "respawn");
        }
        return success;
    }

    public Entity respawn() {
        Entity entity = spawn();
        if (entity != null && mageData != null) {
            Mage mage = controller.getMage(entity);
            mageData.trigger(mage, "respawn");
        }
        return entity;
    }

    @Deprecated
    @Override
    public boolean modify(MageController controller, Entity entity) {
        return modify(entity);
    }

    @Override
    public boolean modify(Entity entity) {
        return modify(entity, true);
    }

    private boolean modify(Entity entity, boolean register) {
        // Don't check isValid here since it will be false on the spawn event!
        if (entity.isDead()) return false;
        if (register && !(entity instanceof Player)) {
            controller.registerMob(entity, this);
        }
        boolean modifiedPre = modifyPreSpawn(entity, false, false);
        boolean modifiedPost = modifyPostSpawn(entity);
        return modifiedPre || modifiedPost;
    }

    private boolean modifyPreSpawn(Entity entity, boolean isFirstSpawn, boolean register) {
        if (entity == null || (type != null && entity.getType() != type)) return false;

        if (!(entity instanceof Player) && register) {
            controller.registerMob(entity, this);
        }
        boolean isPlayer = (entity instanceof Player);
        if (extraData != null) {
            extraData.apply(entity);
        }

        if (persist != null) {
            CompatibilityLib.getCompatibilityUtils().setPersist(entity, persist);
        }
        if (invisible != null) {
            CompatibilityLib.getCompatibilityUtils().setInvisible(entity, invisible);
        }
        if (persistentInvisible != null) {
            CompatibilityLib.getCompatibilityUtils().setPersistentInvisible(entity, persistentInvisible);
        }
        if (removeWhenFarAway != null) {
            CompatibilityLib.getCompatibilityUtils().setRemoveWhenFarAway(entity, removeWhenFarAway);
        }
        if (isSilent != null) {
            CompatibilityLib.getCompatibilityUtils().setSilent(entity, isSilent);
        }
        if (fireTicks != null) {
            entity.setFireTicks(fireTicks);
        }
        if (entity instanceof Ageable && isBaby != null) {
            Ageable ageable = (Ageable)entity;
            if (isBaby) {
                ageable.setBaby();
            } else {
                ageable.setAdult();
            }
        }
        if (entity instanceof Tameable && isTamed != null) {
            ((Tameable)entity).setTamed(isTamed);
        }
        if (isSitting != null) {
            CompatibilityLib.getCompatibilityUtils().setSitting(entity, isSitting);
        }
        if (stay != null) {
            CompatibilityLib.getEntityMetadataUtils().setBoolean(entity, MagicMetaKeys.STAY, stay);
        }
        if (isInvulnerable != null) CompatibilityLib.getCompatibilityUtils().setInvulnerable(entity, isInvulnerable);

        if (tags != null && !tags.isEmpty()) {
            Set<String> entityTags = CompatibilityLib.getCompatibilityUtils().getTags(entity);
            if (entityTags != null) {
                entityTags.addAll(tags);
            }
        }

        if (entity instanceof Creature && canPickupItems != null) {
            Creature creature = (Creature)entity;
            creature.setCanPickupItems(canPickupItems);
        }
        if (entity instanceof ExperienceOrb && xp != null) {
            ((ExperienceOrb)entity).setExperience(xp);
        }

        // Armor stands handle gravity themselves, for now
        if (hasGravity != null && !(entity instanceof ArmorStand)) {
            CompatibilityLib.getCompatibilityUtils().setGravity(entity, hasGravity);
        }

        if (isAware != null) {
            CompatibilityLib.getCompatibilityUtils().setAware(entity, isAware);
        }

        if (entity instanceof LivingEntity) {
            LivingEntity li = (LivingEntity)entity;
            if (isCollidable != null) {
                li.setCollidable(isCollidable);
            }
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
                    if (equipOnRespawn || isFirstSpawn) {
                        copyEquipmentTo(li);
                    }
                    if (maxHealth != null) {
                        CompatibilityLib.getCompatibilityUtils().setMaxHealth(li, maxHealth);
                    }
                }
                if (health != null && hasChangedHealth) {
                    li.setHealth(Math.min(health, CompatibilityLib.getCompatibilityUtils().getMaxHealth(li)));
                }
                if (airLevel != null) {
                    li.setRemainingAir(Math.min(airLevel, li.getRemainingAir()));
                }
                if (hasAI != null) {
                    li.setAI(hasAI);
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        if (!isPlayer && name != null && name.length() > 0) {
            entity.setCustomName(name);
        }
        if (!isPlayer && nameVisible != null) {
            entity.setCustomNameVisible(nameVisible);
        }
        attachToMage(entity);

        if (disguise != null) {
            tryDisguise(entity, disguise);
            int redisguise = disguise.getString("type", "").equalsIgnoreCase("player") ? 2 : 0;
            redisguise = disguise.getInt("redisguise", redisguise);
            if (redisguise > 0) {
                Bukkit.getScheduler().runTaskLater(controller.getPlugin(), new DisguiseTask(controller, entity, disguise), redisguise);
            }
        }

        if (model != null) {
            ModelEngine modelEngine = controller.getModelEngine();
            if (modelEngine != null && !modelEngine.applyModel(entity, model)) {
                controller.getLogger().warning("Invalid model config in mob " + getName() + " (" + getKey() + ")");
            }
        }

        return true;
    }

    private void tryDisguise(final Entity entity, final ConfigurationSection disguise) {
        if (!controller.hasDisguises()) return;
        if (!controller.disguise(entity, disguise)) {
            controller.getLogger().warning("Invalid disguise type in mob " + getName() + " (" + getKey() + "): " + disguise.getString("type"));
        }
    }

    @Deprecated
    @Override
    public void attach(@Nonnull MageController controller, @Nonnull Entity entity) {
        attach(entity);
    }

    @Override
    public void attach(@Nonnull Entity entity) {
        attachToMage(entity);
    }

    public Mage attachToMage(@Nonnull Entity entity) {
        Mage apiMage = null;
        if (mageData != null) {
            apiMage = controller.getMage(entity);
            if (apiMage.getEntityData() == this) {
                return apiMage;
            }

            if (apiMage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                ((com.elmakers.mine.bukkit.magic.Mage)apiMage).setEntityData(this);
            }

            mageData.resetTriggers();
        }
        return apiMage;
    }

    @Nullable
    public ConfigurationSection getMageProperties() {
        return mageData != null ? mageData.mageProperties : null;
    }

    private boolean modifyPostSpawn(Entity entity) {
        if (entity == null || (type != null && entity.getType() != type)) return false;

        if (hasMoved && location != null && !location.equals(entity.getLocation())) {
            entity.teleport(location);
        }
        if (hasVelocity && velocity != null) {
            SafetyUtils.setVelocity(entity, velocity);
        }
        if (mount == null && mountType != null) {
            mount = (EntityData)controller.getMob(mountType);
            if (mount == null) {
                controller.getLogger().warning("Mob " + key + " has invalid mount: " + mountType);
            }
        }
        if (mount != null) {
            Entity mountEntity = entity.getVehicle();
            mobStackSize++;
            boolean allowMount = true;
            if (mobStackSize > maxMobStackSize) {
                controller.getLogger().warning("Mob " + key + " has more than " + maxMobStackSize + " mounts");
                allowMount = false;
            }
            // This prevents respawning mounts on chunk load for persistent mobs
            if (mountEntity == null) {
                mountEntity = mount.spawn(entity.getLocation());
            } else {
                if (mountEntity.getType() == mount.getType()) {
                    // Don't re-mount
                    allowMount = false;
                    // Update the mount in case the config was changed
                    mount.modify(mountEntity);
                } else {
                    // Mount type has changed, now we need to respawn it
                    mountEntity.remove();
                    entity.eject();
                    mountEntity = mount.spawn(entity.getLocation());
                }
            }
            if (allowMount && mountEntity != null) {
                CompatibilityLib.getCompatibilityUtils().addPassenger(mountEntity, entity);
            }
            mobStackSize--;
        }
        if (cancelExplosion) {
            CompatibilityLib.getEntityMetadataUtils().setBoolean(entity, MagicMetaKeys.CANCEL_EXPLOSION, true);
        }
        if (isTemporary) {
            CompatibilityLib.getEntityMetadataUtils().setBoolean(entity, MagicMetaKeys.TEMPORARY, true);
        }

        if (this.key != null) {
            CompatibilityLib.getEntityMetadataUtils().setString(entity, MagicMetaKeys.MAGIC_MOB, this.key);
        }
        // Do this one last time again at the very end, it seems some other changes (like facing direction on an item frame
        // can remove invisibility, somehow.
        if (invisible != null) {
            CompatibilityLib.getCompatibilityUtils().setInvisible(entity, invisible);
        }
        if (this.ownerId != null) {
            CompatibilityLib.getCompatibilityUtils().setOwner(entity, ownerId);
        }
        if (extraData != null) {
            extraData.applyPostSpawn(entity);
        }
        applyBrain(entity);
        return true;
    }

    private void applyBrain(Entity entity) {
        if (brain == null) return;

        // See if there are any custom goals
        MobUtils mobUtils = CompatibilityLib.getMobUtils();
        Collection<GoalConfiguration> goals = GoalConfiguration.fromList(brain, "goals",  controller.getLogger(), "mob " + getKey());

        // Remove any existing goals first
        boolean removeDefaultGoals = brain.getBoolean("remove_default_goals", goals != null && !goals.isEmpty());
        if (removeDefaultGoals) {
            if (!mobUtils.removeGoals(entity)) {
                // This indicates we don't have support for goals, so just stop here.
                return;
            }
        }
        List<String> removeGoals = ConfigurationUtils.getStringList(brain, "remove_goals");
        if (removeGoals != null) {
            for (String goalKey : removeGoals) {
                GoalType goalType;
                try {
                    goalType = GoalType.valueOf(goalKey.toUpperCase());
                } catch (Exception ex) {
                    controller.getLogger().info("Invalid goal type in remove_goals of mob " + getKey() + ": " + goalKey);
                    continue;
                }
                mobUtils.removeGoal(entity, goalType);
            }
        }

        // Apply custom goals
        applyGoals(entity, goals);

        // Now look for target goals
        Collection<GoalConfiguration> targets = GoalConfiguration.fromList(brain, "targets",  controller.getLogger(), "mob " + getKey());

        // Remove any existing goals first
        boolean removeDefaultTargets = brain.getBoolean("remove_default_targets", targets != null && !targets.isEmpty());
        if (removeDefaultTargets) {
            if (!mobUtils.removeTargetGoals(entity)) {
                // This indicates we don't have support for goals, so just stop here.
                return;
            }
        }
        List<String> removeTargets = ConfigurationUtils.getStringList(brain, "remove_targets");
        if (removeTargets != null) {
            for (String goalKey : removeTargets) {
                GoalType goalType;
                try {
                    goalType = GoalType.valueOf(goalKey.toUpperCase());
                } catch (Exception ex) {
                    controller.getLogger().info("Invalid goal type in remove_targets of mob " + getKey() + ": " + goalKey);
                    continue;
                }
                mobUtils.removeTargetGoal(entity, goalType);
            }
        }

        // Apply custom goals
        applyTargetGoals(entity, targets);
    }

    private void applyGoals(Entity entity, Collection<GoalConfiguration> goals) {
        MobUtils mobUtils = CompatibilityLib.getMobUtils();
        if (goals == null || goals.isEmpty()) {
            return;
        }

        // Goals require AI, turn it on no matter what
        if (entity instanceof LivingEntity) {
            ((LivingEntity)entity).setAI(true);
        }
        for (GoalConfiguration goal : goals) {
            goal.setDefault("interval", interval);
            if (!mobUtils.addGoal(entity, goal)) {
                controller.getLogger().warning("Invalid goal " + goal.getGoalType() + " for mob type " + entity.getType().name().toLowerCase());
            }
        }
    }

    private void applyTargetGoals(Entity entity, Collection<GoalConfiguration> goals) {
        MobUtils mobUtils = CompatibilityLib.getMobUtils();
        if (goals == null || goals.isEmpty()) {
            return;
        }

        // Goals require AI, turn it on no matter what
        if (entity instanceof LivingEntity) {
            ((LivingEntity)entity).setAI(true);
        }
        for (GoalConfiguration goal : goals) {
            goal.setDefault("interval", interval);
            if (!mobUtils.addTargetGoal(entity, goal)) {
                controller.getLogger().warning("Invalid target goal " + goal.getGoalType() + " for mob type " + entity.getType().name().toLowerCase());
            }
        }
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

    protected void copyEquipmentPieceTo(Deque<WeightedPair<ItemData>> item, ItemUpdatedCallback callback) {
        ItemData itemData = RandomUtils.weightedRandom(item);
        if (itemData != null) {
            itemData.getItemStack(1, callback);
        }
    }

    public void copyEquipmentTo(LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return;
        copyEquipmentPieceTo(itemInHand, equipment::setItemInMainHand);
        copyEquipmentPieceTo(itemInOffhand, equipment::setItemInOffHand);
        copyEquipmentPieceTo(helmet, equipment::setHelmet);
        copyEquipmentPieceTo(chestplate, equipment::setChestplate);
        copyEquipmentPieceTo(leggings, equipment::setLeggings);
        copyEquipmentPieceTo(boots, equipment::setBoots);
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

    public boolean isRegisterByName() {
        return registerByName;
    }

    @Override
    public EntityData clone() {
        try {
            return (EntityData) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone not supported", e);
        }
    }

    public boolean isRespawn() {
        return respawn;
    }

    public void setRespawn(boolean respawn) {
        this.respawn = respawn;
    }

    private void removeVehicles(Entity entity) {
        entity = entity.getVehicle();
        if (entity != null) {
            if (!removeMounts.contains("*")) {
                com.elmakers.mine.bukkit.api.entity.EntityData entityType = controller.getMob(entity);
                if (entityType == null || !removeMounts.contains(entityType.getKey())) {
                    return;
                }
            }
            removeVehicles(entity);
            entity.remove();
        }
    }

    private void removePassengers(Entity entity) {
        List<Entity> passengers = CompatibilityLib.getCompatibilityUtils().getPassengers(entity);
        for (Entity passenger : passengers) {
            if (!removeMounts.contains("*")) {
                com.elmakers.mine.bukkit.api.entity.EntityData entityType = controller.getMob(passenger);
                if (entityType == null || !removeMounts.contains(entityType.getKey())) {
                    continue;
                }
            }
            removePassengers(passenger);
            passenger.remove();
        }
    }

    public void onDeath(Entity died) {
        if (removeMounts != null) {
            removeVehicles(died);
            removePassengers(died);
        }
    }

    public void modifyDrops(EntityDeathEvent event) {
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
            damager = controller.getDamageSource(damager);
            if (!(damager instanceof Player) || damager == event.getEntity()) return;
        }

        if (drops != null) {
            for (Deque<WeightedPair<String>> dropTable : drops) {
                String key = RandomUtils.weightedRandom(dropTable);
                if (key != null && !key.equalsIgnoreCase("none")) {
                    ItemStack item = controller.createItem(key);
                    if (item != null) {
                        dropList.add(item);
                    }
                }
            }
        }
        if (loot != null) {
            ConfigurationSection processLoot = loot;
            EntityDamageEvent lastDamage = event.getEntity().getLastDamageCause();
            if (lastDamage instanceof EntityDamageByEntityEvent) {
                Entity damager = ((EntityDamageByEntityEvent)lastDamage).getDamager();
                damager = controller.getDamageSource(damager);
                Mage mage = controller.getMage(damager);
                processLoot = new MageParameters(mage, key + ".loot");
                ConfigurationUtils.addConfigurations(processLoot, loot);
            }
            Set<String> keys = processLoot.getKeys(false);
            for (String key : keys) {
                int count = 1;
                double probability;
                ConfigurationSection config = processLoot.getConfigurationSection(key);
                if (config != null) {
                    probability = config.getDouble("probability", 1);
                    count = config.getInt("count", 1);
                } else {
                    probability = processLoot.getDouble(key);
                }
                if (!RandomUtils.checkProbability(probability)) continue;
                ItemStack item = controller.createItem(key);
                item.setAmount(count);
                dropList.add(item);
            }
        }
    }

    @Override
    public String describe() {
        if (name != null && !name.isEmpty()) {
            return name;
        }
        if (mythicMobKey != null) return "Mythic: " + mythicMobKey;
        if (type == null) return "Unknown";
        return type.name().toLowerCase();
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        if (key == null) {
            if (type == null) {
                return null;
            }
            return type.name().toLowerCase();
        }
        return key;
    }

    @Override
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
        return cancelInteract;
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
        if (extraData != null) {
            extraData.setMaterialAndData(material);
        }
    }

    @Override
    @Nullable
    public com.elmakers.mine.bukkit.api.block.MaterialAndData getMaterial() {
        if (extraData != null) {
            return extraData.getMaterialAndData();
        }
        // Note: this was changed to not return item material, so this *only* works for falling block entities now
        return null;
    }

    public boolean isCancelLaunch() {
        return mageData != null ? mageData.isCancelLaunch : true;
    }

    public boolean hasAI() {
        return hasAI == null || hasAI;
    }

    public void setAI(boolean hasAI) {
        this.hasAI = hasAI;
    }

    public boolean isInvulnerable() {
        return isInvulnerable != null && isInvulnerable;
    }

    public void setInvulnerable(boolean invulnerable) {
        isInvulnerable = invulnerable;
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
    public Entity getOwner() {
        if (ownerId == null) {
            return null;
        }
        return CompatibilityLib.getCompatibilityUtils().getEntity(ownerId);
    }

    @Override
    public boolean isRelentless() {
        return isRelentless;
    }

    @Override
    public boolean hasPermission(String key) {
        return permissions != null && permissions.contains(key);
    }

    @Override
    public boolean canTarget(Entity target) {
        if (target == null || mageData == null) return true;
        return mageData.canTarget(target);
    }

    @Override
    public boolean isFriendly(Entity target) {
        if (target == null || mageData == null) return false;
        return mageData.isFriendly(target);
    }

    @Override
    public boolean isTransformable() {
        return transformable;
    }

    @Override
    public boolean isCombustible() {
        return combustible;
    }

    @Override
    public boolean isOwnable() {
        return ownable;
    }

    @Override
    public boolean isPersistent() {
        return persist == null ? false : persist;
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

    @Override
    public boolean getInteractRequiresOwner() {
        return interactRequiresOwner;
    }

    public boolean isSplittable() {
        return extraData == null || extraData.isSplittable();
    }

    @Override
    public boolean isPreventProjectiles() {
        return preventProjectiles;
    }

    @Override
    public boolean isPreventMelee() {
        return preventMelee;
    }

    @Override
    public boolean isNPC() {
        return isNPC;
    }

    public boolean useNPCName() {
        return useNPCName;
    }

    @Override
    public boolean isHidden() {
        return isHidden;
    }

    @Override
    public boolean isSuperProtected() {
        return isSuperProtected;
    }

    @Override
    public boolean isPreventDismount() {
        return preventDismount;
    }

    @Override
    public boolean isPreventTeleport() {
        return preventTeleport;
    }

    @Nullable
    public BossBarTracker getBossBar(Mage mage) {
        return bossBar == null ? null : bossBar.createTracker(mage);
    }

    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public EntityData createVariant(ConfigurationSection parameters) {
        if (parameters == null || parameters.getKeys(false).isEmpty()) {
            return this;
        }
        EntityData variant = this.clone();

        ConfigurationSection effectiveParameters = ConfigurationUtils.cloneConfiguration(getConfiguration());
        effectiveParameters = ConfigurationUtils.addConfigurations(effectiveParameters, parameters);
        variant.load(effectiveParameters);

        return variant;
    }
}
