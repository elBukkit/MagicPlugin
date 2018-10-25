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
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
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
    private boolean respawn = false;
    protected String name = null;
    protected Art art;
    protected BlockFace facing;
    protected Rotation rotation;
    protected ItemStack item;

    protected Double maxHealth;
    protected Double health;
    protected Integer airLevel;
    protected boolean isBaby;
    protected boolean isSilent;
    protected boolean isTamed;
    protected boolean hasAI = true;
    protected boolean hasGravity = true;
    protected int fireTicks;

    protected DyeColor dyeColor;
    protected Ocelot.Type ocelotType;
    protected Rabbit.Type rabbitType = null;

    protected Collection<PotionEffect> potionEffects = null;
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
    protected ConfigurationSection disguise;

    protected EntityMageData mageData;

    public EntityData(Entity entity) {
        this(entity.getLocation(), entity);
    }

    public EntityData(Location location, Entity entity) {
        setEntity(entity);
        this.isTemporary = entity.hasMetadata("temporary");
        this.isLiving = entity instanceof LivingEntity;
        this.isHanging = entity instanceof Hanging;
        this.isProjectile = entity instanceof Projectile;
        this.type = entity.getType();
        this.location = location;
        this.fireTicks = entity.getFireTicks();
        this.isSilent = CompatibilityUtils.isSilent(entity);
        this.canPickupItems = (entity instanceof Creature) ? ((Creature)entity).getCanPickupItems() : false;
        name = entity.getCustomName();
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

    protected void load(@Nonnull MageController controller, ConfigurationSection parameters) {
        // This is required to allow changes to health
        hasChangedHealth = true;
        name = parameters.getString("name");
        if (name != null) {
            name = ChatColor.translateAlternateColorCodes('&', name);
        }
        if (parameters.contains("health")) {
            health = parameters.getDouble("health", 1);
            maxHealth = health;
        }
        if (parameters.contains("max_health")) {
            maxHealth = parameters.getDouble("max_health", 1);
        }
        isSilent = parameters.getBoolean("silent", false);

        String entityName = parameters.contains("type") ? parameters.getString("type") : key;
        type = parseEntityType(entityName);
        if (type == null) {
            controller.getLogger().log(Level.WARNING, " Invalid entity type: " + entityName + " in mob config for " + entityName + ", did you forget the 'type' parameter?");
            return;
        }

        String colorString = parameters.getString("color");
        if (colorString != null) {
            try {
                dyeColor = DyeColor.valueOf(colorString.toUpperCase());
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid color: " + colorString);
            }
        }

        disguise = ConfigurationUtils.getConfigurationSection(parameters, "disguise");

        isTamed = parameters.getBoolean("tamed", false);
        isBaby = parameters.getBoolean("baby", false);
        hasAI = parameters.getBoolean("ai", true);
        hasGravity = parameters.getBoolean("gravity", true);

        potionEffects = ConfigurationUtils.getPotionEffectObjects(parameters, "potion_effects", controller.getLogger());
        hasPotionEffects = potionEffects != null && !potionEffects.isEmpty();

        defaultDrops = parameters.getBoolean("default_drops", true);
        dropsRequirePlayerKiller = parameters.getBoolean("drops_require_player_killer", false);
        if (parameters.contains("xp")) {
            xp = parameters.getInt("xp");
        }
        if (parameters.contains("drop_xp")) {
            dropXp = parameters.getInt("drop_xp");
        }
        interactSpell = parameters.getString("interact_spell");
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
            else if (type == EntityType.ZOMBIE || type == EntityType.PIG_ZOMBIE) {
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
            } else if (type.name().equals("PARROT")) {
                extraData = new EntityParrotData(parameters, controller);
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

        itemInHand = controller.getOrCreateItemOrWand(parameters.getString("item"));
        itemInOffhand = controller.getOrCreateItemOrWand(parameters.getString("offhand"));
        helmet = controller.getOrCreateItemOrWand(parameters.getString("helmet"));
        chestplate = controller.getOrCreateItemOrWand(parameters.getString("chestplate"));
        leggings = controller.getOrCreateItemOrWand(parameters.getString("leggings"));
        boots = controller.getOrCreateItemOrWand(parameters.getString("boots"));

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
        if (entity == null || entity.getType() != type) return false;

        boolean isPlayer = (entity instanceof Player);
        if (extraData != null) {
            extraData.apply(entity);
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

        if (entity instanceof Painting) {
            Painting painting = (Painting) entity;
            if (art != null) {
                painting.setArt(art, true);
            }
            if (facing != null) {
                painting.setFacingDirection(facing, true);
            }
        }
        else if (entity instanceof ItemFrame) {
            ItemFrame itemFrame = (ItemFrame)entity;
            itemFrame.setItem(item);
            if (facing != null) {
                itemFrame.setFacingDirection(facing, true);
            }
        } else if (entity instanceof Item) {
            Item droppedItem = (Item)entity;
            droppedItem.setItemStack(item);
        } else if (entity instanceof Wolf && dyeColor != null) {
            Wolf wolf = (Wolf)entity;
            wolf.setCollarColor(dyeColor);
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
            if (hasPotionEffects) {
                Collection<PotionEffect> currentEffects = li.getActivePotionEffects();
                for (PotionEffect effect : currentEffects) {
                    li.removePotionEffect(effect.getType());
                }
                if (potionEffects != null) {
                    for (PotionEffect effect : potionEffects) {
                        li.addPotionEffect(effect);
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
        if (controller != null) {
            attach(controller, entity);
        }

        if (controller != null && disguise != null) {
            if (!controller.disguise(entity, disguise)) {
                controller.getLogger().warning("Invalid disguise type: " + disguise.getString("type"));
            }
        }

        return true;
    }

    @Override
    public void attach(@Nonnull MageController controller, @Nonnull Entity entity) {
        if (mageData != null) {
            Mage apiMage = controller.getMage(entity);
            if (apiMage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                ((com.elmakers.mine.bukkit.magic.Mage)apiMage).setEntityData(this);
            }

            mageData.onSpawn(apiMage);
        }
    }

    @Nullable
    public ConfigurationSection getMageProperties() {
        return mageData != null ? mageData.mageProperties : null;
    }

    private boolean modifyPostSpawn(MageController controller, Entity entity) {
        if (entity == null || entity.getType() != type) return false;

        if (hasMoved && location != null && !location.equals(entity.getLocation())) {
            entity.teleport(location);
        }

        if (hasVelocity && velocity != null) {
            SafetyUtils.setVelocity(entity, velocity);
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
            if (damager instanceof Projectile) {
                ProjectileSource source = ((Projectile)damager).getShooter();
                if (source instanceof Entity) {
                    damager = (Entity)source;
                }
            }
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

    public void onDeath(Mage mage) {
        if (mageData != null) {
            mageData.onDeath(mage);
        }
    }

    public void onDamage(Mage mage, double damage) {
        if (mageData != null) {
            mageData.onDamage(mage, damage);
        }
    }

    public boolean onLaunch(Mage mage, double pull) {
        if (mageData != null) {
            return mageData.onLaunch(mage, pull);
        }
        return false;
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

        return mageData.trigger(mage, trigger);
    }

    @Override
    public String getInteractSpell() {
        return interactSpell;
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
}
