package com.elmakers.mine.bukkit.world.spawn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.entity.EntityData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public abstract class SpawnRule implements Comparable<SpawnRule> {
    protected MagicController controller;
    protected String key;
    protected final List<EntityType> targetEntityTypes = new ArrayList<>();
    protected Class<? extends Entity> targetEntityClass;
    protected float percentChance;
    protected int minY;
    protected int maxY;
    protected int cooldown;
    protected int priority;
    protected long lastSpawn;
    protected boolean allowIndoors;
    protected boolean targetCustom;
    protected boolean targetNPC;
    protected ConfigurationSection parameters;
    protected Set<String> tags;
    protected Set<Biome> biomes;
    protected Set<Biome> notBiomes;
    protected Set<EntityType> notTypes;
    protected boolean isGlobal;

    protected static final Random rand = new Random();

    @Nonnull
    public abstract SpawnResult onProcess(Plugin plugin, LivingEntity entity);

    public SpawnRule() {
    }

    public void finalizeLoad(String worldName) {
    }

    @Nullable
    protected Set<EntityType> loadEntityTypes(List<String> typeNames) {
        if (typeNames == null || typeNames.isEmpty()) return null;
        Set<EntityType> set = new HashSet<>();
        for (String typeName : typeNames) {
            try {
                EntityType entityType = EntityType.valueOf(typeName.trim().toUpperCase());
                set.add(entityType);
            } catch (Exception ex) {
                this.controller.getLogger().warning(" Invalid entity type: " + typeName);
            }
        }
        return set;
    }

    @SuppressWarnings("unchecked")
    public boolean load(String key, ConfigurationSection parameters, MagicController controller) {
        this.parameters = parameters;
        this.key = key;
        this.controller = controller;
        isGlobal = true;
        List<String> entityTypes = parameters.getStringList("target_types");
        String entityTypeName = parameters.getString("target_type");
        if (entityTypeName != null && !entityTypeName.isEmpty() && !entityTypeName.equals("*") && !entityTypeName.equalsIgnoreCase("all")) {
            entityTypes.add(entityTypeName);
        }
        for (String entityType : entityTypes) {
            isGlobal = false;
            EntityType targetType = EntityData.parseEntityType(entityType);
            if (targetType == null) {
                this.controller.getLogger().warning(" Invalid entity type: " + entityTypeName);
            } else {
                targetEntityTypes.add(targetType);
            }
        }

        String entityClassName = parameters.getString("target_class");
        if (entityClassName != null && !entityClassName.isEmpty()) {
            try {
                targetEntityClass = (Class<? extends Entity>)Class.forName("org.bukkit.entity." + entityClassName);
            } catch (Throwable ex) {
                controller.getLogger().warning("Unknown entity class in target_class of " + getKey() + ": " + entityClassName);
                targetEntityClass = null;
            }
        }

        this.targetCustom = parameters.getBoolean("target_custom", false);
        this.targetNPC = parameters.getBoolean("target_npc", false);
        this.allowIndoors = parameters.getBoolean("allow_indoors", true);
        this.minY = parameters.getInt("min_y", 0);
        this.maxY = parameters.getInt("max_y", 255);
        this.percentChance = (float)parameters.getDouble("probability", 1.0);
        this.cooldown = parameters.getInt("cooldown", 0);
        this.priority = parameters.getInt("priority", 0);
        Collection<String> tagList = ConfigurationUtils.getStringList(parameters, "tags");
        if (tagList != null && !tagList.isEmpty()) {
            tags = new HashSet<>(tagList);
        }
        biomes = ConfigurationUtils.loadBiomes(ConfigurationUtils.getStringList(parameters, "biomes"), controller.getLogger(), "spawn rule " + key);
        notBiomes = ConfigurationUtils.loadBiomes(ConfigurationUtils.getStringList(parameters, "not_biomes"), controller.getLogger(), "spawn rule " + key);
        notTypes = loadEntityTypes(ConfigurationUtils.getStringList(parameters, "not_types"));
        priority = parameters.getInt("priority");
        return true;
    }

    public List<EntityType> getTargetTypes() {
        return targetEntityTypes;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public String getKey() {
        return key;
    }

    @Nonnull
    public SpawnResult process(Plugin plugin, LivingEntity entity) {
        if (targetEntityClass != null && !targetEntityClass.isAssignableFrom(entity.getClass())) return SpawnResult.SKIP;
        if (!targetCustom && entity.getCustomName() != null) return SpawnResult.SKIP;
        if (!targetNPC && controller.isNPC(entity)) return SpawnResult.SKIP;
        if (percentChance < rand.nextFloat()) return SpawnResult.SKIP;
        if (notTypes != null && notTypes.contains(entity.getType())) return SpawnResult.SKIP;
        long now = System.currentTimeMillis();
        if (cooldown > 0 && lastSpawn != 0 && now < lastSpawn + cooldown) return SpawnResult.SKIP;
        Location entityLocation = entity.getLocation();
        int y = entityLocation.getBlockY();
        if (y < minY || y > maxY) return SpawnResult.SKIP;

        if (tags != null && !controller.inTaggedRegion(entity.getLocation(), tags)) {
            return SpawnResult.SKIP;
        }
        if (biomes != null && !biomes.contains(entity.getLocation().getBlock().getBiome())) return SpawnResult.SKIP;
        if (notBiomes != null && notBiomes.contains(entity.getLocation().getBlock().getBiome())) return SpawnResult.SKIP;

        if (!this.allowIndoors) {
            // Bump it up two to miss things like tall grass
            Block highest = entityLocation.getWorld().getHighestBlockAt(entityLocation);
            if (highest.getY() - entityLocation.getY() > 3) {
                return SpawnResult.SKIP;
            }
        }
        lastSpawn = now;
        return onProcess(plugin, entity);
    }

    @Override
    public int compareTo(SpawnRule other)
    {
        if (this.priority > other.priority) {
            return -1;
        } else if (this.priority < other.priority) {
            return 1;
        }
        return this.key.compareTo(other.key);
    }

    protected String getTargetEntityTypeName() {
        String typeNames;
        if (targetEntityClass != null) {
            typeNames = "entities of class " + targetEntityClass.getSimpleName();
        } else {
            typeNames = targetEntityTypes.isEmpty() ? "all entities" : StringUtils.join(targetEntityTypes, ",");
        }
        if (notTypes != null && !notTypes.isEmpty()) {
            typeNames = typeNames + " and not any of " + notTypes;
        }
        return typeNames;
    }

    protected void logSpawnRule(String message) {
        if (minY > 0) {
            message += " at y > " + minY;
        }
        if (percentChance < 1) {
            message += " at a " + (percentChance * 100) + "% chance";
        }
        if (tags != null) {
            message = message + " in regions tagged with any of " + tags.toString();
        }
        if (biomes != null) {
            message = message + " in biomes " + biomes.toString();
        }
        if (notBiomes != null) {
            message = message + " not in biomes " + notBiomes.toString();
        }
        controller.info(message);
    }
}
