package com.elmakers.mine.bukkit.npc;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.block.BlockData;
import com.elmakers.mine.bukkit.entity.EntityData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class MagicNPC implements com.elmakers.mine.bukkit.api.npc.MagicNPC {
    @Nonnull
    private final MagicController controller;
    @Nullable
    private Location location;
    @Nonnull
    private EntityData entityData;
    @Nonnull
    private String mobKey;
    @Nonnull
    private String name;
    @Nonnull
    private UUID uuid;

    private long createdAt;
    private String creatorId;
    private String creatorName;

    @Nonnull
    private ConfigurationSection parameters;

    public MagicNPC(MagicController controller, ConfigurationSection configuration) {
        this.controller = controller;

        mobKey = configuration.getString("mob");
        uuid = UUID.fromString(configuration.getString("uuid"));
        name = configuration.getString("name");
        createdAt = configuration.getLong("created", 0);
        creatorId = configuration.getString("creator");
        creatorName = configuration.getString("creator_name");
        ConfigurationSection location = ConfigurationUtils.getConfigurationSection(configuration, "location");
        if (location != null) {
            double x = location.getDouble("x");
            double y = location.getDouble("y");
            double z = location.getDouble("z");
            float yaw = (float)location.getDouble("yaw");
            float pitch = (float)location.getDouble("pitch");
            String worldName = location.getString("world");
            if (worldName == null || worldName.isEmpty()) {
                worldName = "world";
                controller.getLogger().warning("NPC missing world name, defaulting to 'world'");
            }
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                controller.getLogger().warning("NPC has unknown world: " + worldName + ", will be removed!");
            }
            this.location = new Location(world, x, y, z, yaw, pitch);
        } else {
            this.location = null;
        }

        parameters = ConfigurationUtils.getConfigurationSection(configuration, "parameters");
        if (parameters == null) {
            parameters = new MemoryConfiguration();
        }

        EntityData entity = controller.getMob(mobKey);
        if (entity == null) {
            controller.getLogger().warning("NPC has unknown mob type: " + mobKey + ", will change to villager");
            defaultMob();
        } else {
            setEntityData(entity);
        }
    }

    public MagicNPC(MagicController controller, Mage creator, Location location, String name) {
        this.controller = controller;
        this.location = location.clone();
        this.name = name;
        this.parameters = new MemoryConfiguration();
        this.createdAt = System.currentTimeMillis();
        this.creatorId = creator.getId();
        this.creatorName = creator.getName();
        defaultMob();
        restore();
    }

    protected void defaultMob() {
        entityData = new EntityData(EntityType.VILLAGER);
        configureEntityData();
        mobKey = "villager";
    }

    protected boolean isLocationValid() {
        return location != null && location.getWorld() != null;
    }

    public boolean isValid() {
        return entityData != null && isLocationValid();
    }

    public long getId() {
        return BlockData.getBlockId(getLocation());
    }

    @Override
    public boolean setType(@Nonnull String mobKey) {
        EntityData newEntityData = controller.getMob(mobKey);
        if (newEntityData == null) {
            return false;
        }
        setEntityData(newEntityData);
        this.mobKey = mobKey;
        return true;
    }

    protected void setEntityData(EntityData templateData) {
        entityData = templateData.clone();
        configureEntityData();
    }

    protected void configureEntityData() {
        entityData.setAI(false);
        entityData.setInvulnerable(false);
        entityData.setGravity(false);
        entityData.setPersist(false);
        ConfigurationSection mobOverrides = parameters.getConfigurationSection("mob");
        if (mobOverrides != null) {
            entityData.load(controller, mobOverrides);
        }
    }

    @Override
    public void teleport(@Nonnull Location location) {
        this.location = location.clone();
        Entity entity = CompatibilityUtils.getEntity(location.getWorld(), uuid);
        if (entity != null) {
            entity.teleport(location);
        }
    }

    @Override
    @Nullable
    public Location getLocation() {
        return location;
    }

    @Override
    public void remove() {
        Entity entity = CompatibilityUtils.getEntity(location.getWorld(), uuid);
        if (entity != null) {
            entity.remove();
        }
    }

    @Nullable
    public Entity restore() {
        Entity entity = CompatibilityUtils.getEntity(location.getWorld(), uuid);
        if (entity == null) {
            entity = entityData.spawn(controller, location);
        } else {
            entityData.modify(entity);
            entity.teleport(location);
        }
        if (entity == null) {
            controller.getLogger().warning("Failed to restore NPC entity");
            return null;
        }
        entity.setCustomName(name);
        this.uuid = entity.getUniqueId();
        return entity;
    }

    public void save(ConfigurationSection configuration) {
        configuration.set("uuid", uuid.toString());
        configuration.set("mob", mobKey);
        configuration.set("name", name);
        configuration.set("created", createdAt);
        configuration.set("creator", creatorId);
        configuration.set("creatorName", creatorName);
        ConfigurationSection locationSection = configuration.createSection("location");
        locationSection.set("world", location.getWorld().getName());
        locationSection.set("x", location.getX());
        locationSection.set("y", location.getY());
        locationSection.set("z", location.getZ());
        locationSection.set("yaw", location.getYaw());
        locationSection.set("pitch", location.getPitch());
        configuration.set("parameters", parameters);
    }

    @Override
    @Nonnull
    public EntityData getEntityData() {
        return entityData;
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    public void setName(@Nonnull String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public void configure(String key, Object value) {
        parameters.set(key, value);
    }
}
