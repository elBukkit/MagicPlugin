package com.elmakers.mine.bukkit.world.spawn;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;

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
    protected EntityType targetEntityType;
    protected Class<? extends Entity> targetEntityClass;
    protected float percentChance;
    protected int minY;
    protected int maxY;
    protected int cooldown;
    protected long lastSpawn;
    protected boolean allowIndoors;
    protected boolean targetCustom;
    protected boolean targetNPC;
    protected ConfigurationSection parameters;
    protected Set<String> tags;
    protected Set<Biome> biomes;
    protected Set<Biome> notBiomes;

    protected static final Random rand = new Random();

    public abstract LivingEntity onProcess(Plugin plugin, LivingEntity entity);

    public SpawnRule() {
    }

    public void finalizeLoad(String worldName) {
    }

    @Nullable
    protected Set<Biome> loadBiomes(List<String> biomeNames) {
        if (biomeNames == null || biomeNames.isEmpty()) return null;
        Set<Biome> set = new HashSet<Biome>();
        for (String biomeName : biomeNames) {
            try {
                Biome biome = Biome.valueOf(biomeName.trim().toUpperCase());
                set.add(biome);
            } catch (Exception ex) {
                this.controller.getLogger().warning(" Invalid biome: " + biomeName);
            }
        }
        return set;
    }

    public boolean load(String key, ConfigurationSection parameters, MagicController controller) {
        this.parameters = parameters;
        this.key = key;
        this.controller = controller;
        String entityTypeName = parameters.getString("target_type");
        if (entityTypeName != null && !entityTypeName.isEmpty() && !entityTypeName.equals("*")) {
            this.targetEntityType = EntityData.parseEntityType(entityTypeName);
            if (targetEntityType == null) {
                this.controller.getLogger().warning(" Invalid entity type: " + entityTypeName);
                return false;
            }
        } else {
            this.targetEntityType = null;
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
        Collection<String> tagList = ConfigurationUtils.getStringList(parameters, "tags");
        if (tagList != null && !tagList.isEmpty()) {
            tags = new HashSet<String>(tagList);
        }
        biomes = loadBiomes(ConfigurationUtils.getStringList(parameters, "biomes"));
        notBiomes = loadBiomes(ConfigurationUtils.getStringList(parameters, "not_biomes"));
        return true;
    }

    public void setPercentChance(float percentChance) {
        this.percentChance = percentChance;
    }

    public float getPercentChance() {
        return percentChance;
    }

    public EntityType getTargetType() {
        return targetEntityType;
    }

    public String getKey() {
        return key;
    }

    @Nullable
    public LivingEntity process(Plugin plugin, LivingEntity entity) {
        if (targetEntityType != null && targetEntityType != entity.getType()) return null;
        if (targetEntityClass != null && !targetEntityClass.isAssignableFrom(entity.getClass())) return null;
        if (!targetCustom && entity.getCustomName() != null) return null;
        if (!targetNPC && controller.isNPC(entity)) return null;
        if (percentChance < rand.nextFloat()) return null;
        long now = System.currentTimeMillis();
        if (cooldown > 0 && lastSpawn != 0 && now < lastSpawn + cooldown) return null;
        Location entityLocation = entity.getLocation();
        int y = entityLocation.getBlockY();
        if (y < minY || y > maxY) return null;

        if (tags != null && !controller.inTaggedRegion(entity.getLocation(), tags)) {
            return null;
        }
        if (biomes != null && !biomes.contains(entity.getLocation().getBlock().getBiome())) return null;
        if (notBiomes != null && notBiomes.contains(entity.getLocation().getBlock().getBiome())) return null;

        if (!this.allowIndoors) {
            // Bump it up two to miss things like tall grass
            Block highest = entityLocation.getWorld().getHighestBlockAt(entityLocation);
            if (highest.getY() - entityLocation.getY() > 3) {
                return null;
            }
        }
        lastSpawn = now;
        return onProcess(plugin, entity);
    }

    @Override
    public int compareTo(SpawnRule other)
    {
        return this.key.compareTo(other.key);
    }

    protected String getTargetEntityTypeName() {
        if (targetEntityClass != null) {
            return "entities of class " + targetEntityClass.getSimpleName();
        }
        return targetEntityType == null ? "all entities" : targetEntityType.name();
    }
}
