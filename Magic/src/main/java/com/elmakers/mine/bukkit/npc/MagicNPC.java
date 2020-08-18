package com.elmakers.mine.bukkit.npc;

import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.block.BlockData;
import com.elmakers.mine.bukkit.entity.EntityData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.TextUtils;

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
    private Integer importedId;

    @Nonnull
    private ConfigurationSection parameters;
    @Nullable
    private ConfigurationSection parentParameters;

    public MagicNPC(MagicController controller, ConfigurationSection configuration) {
        this.controller = controller;

        String template = configuration.getString("inherit", "base_npc");
        if (!template.isEmpty()) {
            EntityData entityData = controller.getMob(template);
            parentParameters = entityData == null ? null : entityData.getConfiguration();
        }
        mobKey = configuration.getString("mob", mobKey);
        String uuidString = configuration.getString("uuid");
        if (uuidString != null && !uuidString.isEmpty()) {
            uuid = UUID.fromString(uuidString);
        }
        name = configuration.getString("name", name);
        createdAt = configuration.getLong("created", createdAt);
        creatorId = configuration.getString("creator", creatorId);
        creatorName = configuration.getString("creator_name", creatorName);
        if (configuration.contains("imported_id")) {
            importedId = configuration.getInt("imported_id");
        }
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

        boolean hasMobKey = mobKey != null && !mobKey.isEmpty();
        EntityData entity = hasMobKey ? controller.getMob(mobKey) : null;
        if (entity == null) {
            String defaultType = parentParameters == null ? "villager" : parentParameters.getString("type", "villager");
            if (hasMobKey) {
                controller.getLogger().warning("NPC has unknown mob type: " + mobKey + ", will change to " + defaultType);
            }
            entity = controller.getMob(defaultType);
        }
        if (entity == null) {
            controller.getLogger().warning("NPC has unknown mob type: " + mobKey + ", and no deafult mob type was available. Defaulting to villager.");
            entity = new EntityData(EntityType.VILLAGER);
        }
        setEntityData(entity);
    }

    public MagicNPC(MagicController controller, Mage creator, Location location, String name) {
        this(controller, new MemoryConfiguration());
        this.location = location.clone();
        this.name = name;
        this.createdAt = System.currentTimeMillis();
        this.creatorId = creator.getId();
        this.creatorName = creator.getName();
        update();
    }

    protected void defaultMob() {
        entityData = new EntityData(EntityType.VILLAGER);
        configureEntityData();
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
        Entity entity = getEntity();
        if (entity != null && entity.isValid()) {
            entity.remove();
            restore();
        }
        return true;
    }

    protected void setEntityData(EntityData templateData) {
        entityData = templateData.clone();
        configureEntityData();
    }

    protected void configureEntityData() {
        ConfigurationSection effectiveParameters;
        if (parentParameters != null) {
            effectiveParameters = ConfigurationUtils.cloneConfiguration(parentParameters);
            effectiveParameters = ConfigurationUtils.addConfigurations(effectiveParameters, parameters);
        } else {
            effectiveParameters = ConfigurationUtils.cloneConfiguration(parameters);
        }

        // Always keep entity type and name
        effectiveParameters.set("type", null);
        effectiveParameters.set("name", name);
        entityData.load(controller, effectiveParameters);
    }

    @Nullable
    protected Entity getEntity() {
        return uuid == null ? null : CompatibilityUtils.getEntity(location.getWorld(), uuid);
    }

    @Override
    public void teleport(@Nonnull Location location) {
        this.location = location.clone();
        Entity entity = getEntity();
        if (entity != null && entity.isValid()) {
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
        Entity entity = getEntity();
        if (entity != null) {
            entity.remove();
        }
    }

    @Nullable
    public Entity restore() {
        Entity entity = getEntity();
        if (entity == null || !entity.isValid()) {
            entity = entityData.spawn(controller, location);
        } else {
            entityData.modify(controller, entity);
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
        configuration.set("creator_name", creatorName);
        configuration.set("imported_id", importedId);
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
        restore();
    }

    @Nonnull
    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public void configure(String key, Object value) {
        value = ConfigurationUtils.convertProperty(value);
        parameters.set(key, value);
        update();
    }

    @Override
    public void update() {
        configureEntityData();
        restore();
    }

    @Override
    @Nonnull
    public ConfigurationSection getParameters() {
        return parameters;
    }

    @Override
    public void describe(CommandSender sender) {
        String mobTypeName = mobKey == null ? "Default Mob Type" : mobKey;
        sender.sendMessage(ChatColor.GOLD + name + ChatColor.DARK_GRAY + " (" + ChatColor.GRAY + mobTypeName + ChatColor.DARK_GRAY + ")");
        sender.sendMessage(ChatColor.AQUA + "Location: " + ChatColor.WHITE + TextUtils.printLocation(location));
        sender.sendMessage(ChatColor.AQUA + "Created By: " + ChatColor.WHITE + creatorName);
        ConfigurationSection parameters;
        if (this.parentParameters != null) {
            parameters = ConfigurationUtils.cloneConfiguration(this.parentParameters);
            parameters = ConfigurationUtils.addConfigurations(parameters, this.parameters);
        } else {
            parameters = ConfigurationUtils.cloneConfiguration(this.parameters);
        }
        parameters.set("type", null);
        String interactSpell = parameters.getString("interact_spell");
        if (interactSpell != null && !interactSpell.isEmpty()) {
            SpellTemplate template = controller.getSpellTemplate(interactSpell);
            String spellName = template == null ? (ChatColor.RED + interactSpell) : (ChatColor.YELLOW + template.getName());
            sender.sendMessage(ChatColor.AQUA + "Casts" + ChatColor.GRAY + ": " + spellName);
            parameters.set("interact_spell", null);
        }
        Set<String> keys = parameters.getKeys(false);
        for (String key : keys) {
            Object value = parameters.get(key);
            if (value != null) {
                ChatColor propertyColor = ChatColor.GRAY;
                if (this.parameters.contains(key)) {
                    propertyColor = ChatColor.DARK_AQUA;
                }
                sender.sendMessage(propertyColor.toString() + key + ChatColor.GRAY + ": " + ChatColor.WHITE + InventoryUtils.describeProperty(value, InventoryUtils.MAX_PROPERTY_DISPLAY_LENGTH));
            }
        }
    }

    @Override
    @Nullable
    public Integer getImportedId() {
        return importedId;
    }

    public void setImportedId(int importedId) {
        this.importedId = importedId;
    }
}
