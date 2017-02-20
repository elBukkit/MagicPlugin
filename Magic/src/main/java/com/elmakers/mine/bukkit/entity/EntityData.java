package com.elmakers.mine.bukkit.entity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Art;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
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
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.material.Colorable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * This class stores information about an Entity.
 *
 */
public class EntityData implements com.elmakers.mine.bukkit.api.entity.EntityData, Cloneable {
    protected static Map<UUID, WeakReference<Entity>> respawned = new HashMap<>();

    protected String key;
    protected WeakReference<Entity> entity = null;
    protected UUID uuid = null;
    
    protected EntityType type;
    protected EntityExtraData extraData;
    protected Location location;
    protected Vector relativeLocation;
    protected boolean hasMoved = false;
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
    protected int fireTicks;
    
    protected DyeColor dyeColor;
    protected SkeletonType skeletonType;
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

    protected ItemData itemInHand;
    protected ItemData helmet;
    protected ItemData chestplate;
    protected ItemData leggings;
    protected ItemData boots;
    
    protected Integer xp;
    protected Integer dropXp;

    protected boolean defaultDrops;
    protected List<String> drops;

    // Spellcasting
    protected long tickInterval;
    protected LinkedList<WeightedPair<String>> spells;
    protected boolean requiresTarget;
    
    protected ConfigurationSection disguise;
    
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
        name = entity.getCustomName();

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
            this.maxHealth = li.getMaxHealth();
            
            itemInHand = getItem(li.getEquipment().getItemInMainHand());
            helmet = getItem(li.getEquipment().getHelmet());
            chestplate = getItem(li.getEquipment().getChestplate());
            leggings = getItem(li.getEquipment().getLeggings());
            boots = getItem(li.getEquipment().getBoots());
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
        } else if (entity instanceof Skeleton) {
            Skeleton skeleton = (Skeleton)entity;
            skeletonType = skeleton.getSkeletonType();
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
        }
    }
    
    private ItemData getItem(ItemStack item) {
        return item == null ? null : new com.elmakers.mine.bukkit.item.ItemData(item);
    }

    public EntityData(EntityType type) {
        this.type = type;
    }

    public EntityData(MageController controller, ConfigurationSection parameters) {
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

        String entityName = parameters.getString("type");
        if (entityName != null) {
            type = parseEntityType(entityName);
            if (type == null) {
                controller.getLogger().log(Level.WARNING, " Invalid entity type: " + entityName);
            }
        }

        disguise = ConfigurationUtils.getConfigurationSection(parameters, "disguise");

        isBaby = parameters.getBoolean("baby", false);
        tickInterval = parameters.getLong("cast_interval", 0);
        if (parameters.contains("cast")) {
            spells = new LinkedList<>();
            RandomUtils.populateStringProbabilityMap(spells, parameters.getConfigurationSection("cast"));
        }
        requiresTarget = parameters.getBoolean("cast_requires_target", true);

        Collection<ConfigurationSection> potionEffectList = ConfigurationUtils.getNodeList(parameters, "potion_effects");
        if (potionEffectList != null) {
            potionEffects = new ArrayList<>();
            for (ConfigurationSection potionEffectSection : potionEffectList) {
                try {
                    PotionEffectType effectType = PotionEffectType.getByName(potionEffectSection.getString("type").toUpperCase());
                    if (effectType == null) {
                        controller.getLogger().log(Level.WARNING, "Invalid potion effect type in mob config (" + name + "): " + potionEffectSection.getString("type", "(null)"));
                        continue;
                    }
                    int ticks = (int)(potionEffectSection.getLong("duration", 3600000) / 50);
                    ticks = potionEffectSection.getInt("ticks", ticks);
                    int amplifier = potionEffectSection.getInt("amplifier", 0);
                    boolean ambient = potionEffectSection.getBoolean("ambient", true);
                    boolean particles = potionEffectSection.getBoolean("particles", true);

                    potionEffects.add(new PotionEffect(effectType, ticks, amplifier, ambient, particles));
                } catch (Exception ex) {
                    controller.getLogger().log(Level.WARNING, "Invalid potion effect type in mob config (" + name + "): " + potionEffectSection.getString("type", "(null)"), ex);
                }
            }
            hasPotionEffects = !potionEffects.isEmpty();
        }

        defaultDrops = parameters.getBoolean("default_drops", true);
        if (parameters.contains("xp")) {
            xp = parameters.getInt("xp");
        }
        if (parameters.contains("drop_xp")) {
            dropXp = parameters.getInt("drop_xp");
        }
        drops = ConfigurationUtils.getStringList(parameters, "drops");
        
        try {
            if (type == EntityType.HORSE) {
                EntityHorseData horseData = new EntityHorseData();
    
                if (parameters.contains("horse_color")) {
                    try {
                        String colorString = parameters.getString("horse_color");
                        horseData.color = Horse.Color.valueOf(colorString.toUpperCase());
                    } catch (Exception ex) {
                        controller.getLogger().log(Level.WARNING, "Invalid horse_color: " + parameters.getString("horse_color"), ex);
                    }
                }
    
                if (parameters.contains("horse_style")) {
                    try {
                        String styleString = parameters.getString("horse_style");
                        horseData.style = Horse.Style.valueOf(styleString.toUpperCase());
                    } catch (Exception ex) {
                        controller.getLogger().log(Level.WARNING, "Invalid horse_style: " + parameters.getString("horse_style"), ex);
                    }
                }
    
                if (parameters.contains("horse_jump_strength")) {
                    horseData.jumpStrength = parameters.getDouble("horse_jump_strength");
                }

                if (parameters.contains("tamed")) {
                    horseData.tamed = parameters.getBoolean("tamed");
                }
                
                extraData = horseData;
            }
            else if (type == EntityType.VILLAGER) {
                EntityVillagerData villagerData = new EntityVillagerData();
                if (parameters.contains("villager_profession")) {
                    villagerData.profession = Villager.Profession.valueOf(parameters.getString("villager_profession").toUpperCase());
                }
                if (parameters.contains("villager_riches")) {
                    villagerData.riches = parameters.getInt("villager_riches");
                }
                if (parameters.contains("villager_trades")) {
                    villagerData.recipes = new ArrayList<>();
                    Collection<ConfigurationSection> tradeList = ConfigurationUtils.getNodeList(parameters, "villager_trades");
                    for (ConfigurationSection tradeConfiguration : tradeList) {
                        String outputKey = tradeConfiguration.getString("output");
                        ItemStack output = controller.createItem(outputKey);
                        if (output == null || output.getType() == Material.AIR)
                        {
                            controller.getLogger().warning("Invalid output specified in villager trade: " + outputKey);
                            continue;
                        }
                        MerchantRecipe recipe = new MerchantRecipe(output, tradeConfiguration.getInt("max_uses", 1));
                        recipe.setExperienceReward(tradeConfiguration.getBoolean("experience_reward", true));
                        List<String> ingredientConfiguration = tradeConfiguration.getStringList("ingredients");
                        for (String ingredientKey : ingredientConfiguration) {
                            ItemStack ingredient = controller.createItem(ingredientKey);
                            if (ingredient == null || ingredient.getType() == Material.AIR)
                            {
                                controller.getLogger().warning("Invalid ingredient specified in villager trade: " + ingredientKey);
                                continue;
                            }
                            recipe.addIngredient(ingredient);
                        }
                        villagerData.recipes.add(recipe);
                    }
                }

                extraData = villagerData;
            }
            else if (type == EntityType.SKELETON && parameters.contains("skeleton_type")) {
                skeletonType = Skeleton.SkeletonType.valueOf(parameters.getString("skeleton_type").toUpperCase());
            }
            else if (type == EntityType.OCELOT && parameters.contains("ocelot_type")) {
                ocelotType = Ocelot.Type.valueOf(parameters.getString("ocelot_type").toUpperCase());
            }
            else if (type == EntityType.RABBIT && parameters.contains("rabbit_type")) {
                rabbitType = Rabbit.Type.valueOf(parameters.getString("rabbit_type").toUpperCase());
            }
            else if (type == EntityType.ZOMBIE) {
                EntityZombieData zombieData = new EntityZombieData();
                if (parameters.contains("villager_profession")) {
                    zombieData.profession = Villager.Profession.valueOf(parameters.getString("villager_profession").toUpperCase());
                }
                zombieData.isBaby = isBaby;
                extraData = zombieData;
            }
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Invalid entity type or sub-type", ex);
        }
        ConfigurationSection attributeConfiguration = ConfigurationUtils.getConfigurationSection(parameters, "attributes");
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
        item = itemData == null ? null : itemData.getItemStack(parameters.getInt("amount", 1));
        
        if (controller != null) {
            itemInHand = controller.getOrCreateItem(parameters.getString("item"));
            helmet = controller.getOrCreateItem(parameters.getString("helmet"));
            chestplate = controller.getOrCreateItem(parameters.getString("chestplate"));
            leggings = controller.getOrCreateItem(parameters.getString("leggings"));
            boots = controller.getOrCreateItem(parameters.getString("boots"));
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

    @SuppressWarnings("deprecation")
    public static EntityType parseEntityType(String typeString)
    {
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
    public ItemStack getItem() {
        return item;
    }

    @Override
    public double getHealth() {
        return health;
    }

    protected Entity trySpawn(CreatureSpawnEvent.SpawnReason reason) {
        Entity spawned = null;
        if (type != null && type != EntityType.PLAYER) {
            try {
                if (reason != null) {
                    spawned = CompatibilityUtils.spawnEntity(location, type, reason);
                } else
                    switch (type) {
                        case PAINTING:
                            spawned = CompatibilityUtils.spawnPainting(location, facing, art);
                            break;
                        case ITEM_FRAME:
                            spawned = CompatibilityUtils.spawnItemFrame(location, facing, rotation, item);
                            break;
                        case DROPPED_ITEM:
                            spawned = location.getWorld().dropItem(location, item);
                            break;
                        default:
                            spawned = location.getWorld().spawnEntity(location, type);
                    }
            } catch (Exception ex) {
                org.bukkit.Bukkit.getLogger().log(Level.WARNING, "Error restoring entity type " + getType() + " at " + getLocation(), ex);
            }
        }
        return spawned;
    }

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

    @Override
    public Entity spawn() {
        return spawn(null, null);
    }

    @Override
    public Entity spawn(Location location) {
        return spawn(null, location, null);
    }

    @Override
    public Entity spawn(MageController controller, Location location) {
        return spawn(controller, location, null);
    }
    
    @Override
    public Entity spawn(MageController controller, Location location, CreatureSpawnEvent.SpawnReason reason) {
        if (location != null) this.location = location;
        else if (this.location == null) return null;
        Entity spawned = trySpawn(reason);
        if (spawned != null) {
            modify(controller, spawned);
        }

        return spawned;
    }

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
                entity = trySpawn(null);
                if (entity != null) {
                    respawned.put(uuid, new WeakReference<>(entity));

                    // Undo'ing an entity won't drop loot
                    entity.setMetadata("nodrops", new FixedMetadataValue(MagicPlugin.getAPI().getPlugin(), true));
                }
            }
            setEntity(entity);
        }

        modify(entity);
        return entity;
    }

    @Override
    public boolean modify(Entity entity) {
        return modify(null, entity);
    }

    @Override
    public boolean modify(MageController controller, Entity entity) {
        if (entity == null || entity.getType() != type) return false;

        boolean isPlayer = (entity instanceof Player);
        if (extraData != null) {
            extraData.apply(entity);
        }

        entity.setFireTicks(fireTicks);
        if (entity instanceof Ageable) {
            Ageable ageable = (Ageable)entity;
            if (isBaby) {
                ageable.setBaby();
            } else {
                ageable.setAdult();
            }
        }

        if (entity instanceof Colorable && dyeColor != null) {
            Colorable colorable = (Colorable)entity;
            colorable.setColor(dyeColor);
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
        } else if (entity instanceof Skeleton && skeletonType != null) {
            Skeleton skeleton = (Skeleton)entity;
            skeleton.setSkeletonType(skeletonType);
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
                        li.setMaxHealth(maxHealth);
                    }
                }
                if (health != null) {
                    li.setHealth(Math.min(health, li.getMaxHealth()));
                }
                if (airLevel != null) {
                    li.setRemainingAir(Math.min(airLevel, li.getRemainingAir()));
                }
            } catch (Throwable ex) {
            }
        }

        if (!isPlayer && name != null && name.length() > 0) {
            entity.setCustomName(name);
        }

        if (hasMoved && location != null) {
            entity.teleport(location);
        }

        if (hasVelocity && velocity != null) {
            entity.setVelocity(velocity);
        }

        if (controller != null && spells != null && tickInterval >= 0) {
            Mage apiMage = controller.getMage(entity);
            if (apiMage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                ((com.elmakers.mine.bukkit.magic.Mage)apiMage).setEntityData(this);
            }
        }
        
        if (controller != null && disguise != null) {
            if (!controller.disguise(entity, disguise)) {
                controller.getLogger().warning("Invalid disguise type: " + disguise.getString("type"));
            }
        }

        return true;
    }
    
    public void applyAttributes(LivingEntity entity) {
        if (attributes != null) {
            for (Map.Entry<Attribute, Double> entry : attributes.entrySet()) {
                entity.getAttribute(entry.getKey()).setBaseValue(entry.getValue());
            }
        }
    }

    public void copyEquipmentTo(LivingEntity entity) {
        if (itemInHand != null) {
            entity.getEquipment().setItemInMainHand(itemInHand.getItemStack(1));
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

    public Entity getEntity() {
        return entity == null ? null : entity.get();
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public EntityData clone() {
        try {
            return (EntityData)super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void removed(Entity entity) {
        if (extraData != null) {
            extraData.removed(entity);
        }
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
        if (drops != null) {
            for (String key : drops) {
                ItemStack item = controller.createItem(key);
                if (item != null) {
                    dropList.add(item);
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
        if (skeletonType != null) {
            name += ":" + skeletonType;
        } else if (ocelotType != null) {
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
        return tickInterval;
    }
    
    public void tick(Mage mage) {
        if (spells == null) return;
        Entity entity = mage.getLivingEntity();
        Creature creature = (entity instanceof Creature) ? (Creature)entity : null;
        if (requiresTarget && (creature == null || creature.getTarget() == null)) return;
        
        String castSpell = RandomUtils.weightedRandom(spells);
        if (castSpell.length() > 0) {

            String[] parameters = null;
            Spell spell = null;
            if (!castSpell.equalsIgnoreCase("none"))
            {
                if (castSpell.contains(" ")) {
                    parameters = StringUtils.split(castSpell, ' ');
                    castSpell = parameters[0];
                    parameters = Arrays.copyOfRange(parameters, 1, parameters.length);
                }
                spell = mage.getSpell(castSpell);
            }
            if (spell != null) {
                spell.cast(parameters);
            }
        }
    }
}
