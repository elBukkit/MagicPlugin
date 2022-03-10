package com.elmakers.mine.bukkit.npc;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.block.BlockData;
import com.elmakers.mine.bukkit.entity.EntityData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.TextUtils;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class MagicNPC implements com.elmakers.mine.bukkit.api.npc.MagicNPC {
    private static final String DEFAULT_NPC_KEY = "base_npc";

    @Nonnull
    private final MagicController controller;
    @Nonnull
    private UUID id;
    @Nullable
    private Location location;
    @Nullable
    private String worldName;
    @Nonnull
    private EntityData entityData;
    @Nullable
    private String mobKey;
    @Nonnull
    private String name = "";
    @Nullable
    private UUID entityId;
    @Nullable
    private String templateKey;

    private long createdAt;
    private String creatorId;
    private String creatorName;
    private Integer importedId;

    @Nonnull
    private ConfigurationSection parameters;

    @SuppressWarnings("null") // Handled by load
    public MagicNPC(MagicController controller, ConfigurationSection configuration) {
        this.controller = controller;
        load(configuration);

        verifyNotNull(entityData);
    }

    @SuppressWarnings("null") // Handled by createEntityData
    private MagicNPC(MagicController controller, Mage creator, Location location, @Nullable String name, @Nullable EntityData template) {
        checkArgument(
                name != null || template != null,
                "Name and template cannot both be null");

        this.controller = controller;
        this.setLocation(location);
        this.name = MoreObjects.firstNonNull(
                name != null ? name : template.getName(),
                "");
        this.createdAt = System.currentTimeMillis();
        this.creatorId = creator.getId();
        this.creatorName = creator.getName();
        id = UUID.randomUUID();
        if (template != null) {
            templateKey = template.getKey();
            mobKey = template.getKey();
        } else {
            templateKey = DEFAULT_NPC_KEY;
        }

        parameters = ConfigurationUtils.newConfigurationSection();
        createEntityData();
        update();

        verifyNotNull(entityData);
    }

    public MagicNPC(MagicController controller, Mage creator, Location location, EntityData template) {
        this(controller, creator, location, null, template);
    }

    public MagicNPC(MagicController controller, Mage creator, Location location, String name) {
        this(controller, creator, location, name, null);
    }

    protected void load(ConfigurationSection configuration) {
        templateKey = configuration.getString("template", templateKey);
        if (templateKey == null) {
            templateKey = DEFAULT_NPC_KEY;
        }
        mobKey = configuration.getString("mob", mobKey);

        String idString = configuration.getString("id");
        if (idString != null && !idString.isEmpty()) {
            id = UUID.fromString(idString);
        } else {
            id = UUID.randomUUID();
        }

        // Old configs may use "uuid" for the entity UUID, so don't re-use this property!
        String uuidString = configuration.getString("entity_uuid", configuration.getString("uuid"));
        if (uuidString != null && !uuidString.isEmpty()) {
            entityId = UUID.fromString(uuidString);
        }
        name = MoreObjects.firstNonNull(
                configuration.getString("name", name),
                "");
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
            worldName = location.getString("world");
            if (worldName == null || worldName.isEmpty()) {
                worldName = "world";
                controller.getLogger().warning("NPC missing world name, defaulting to 'world'");
            }

            // This world may not be loaded yet, if that happens we may try to load it again later
            World world = Bukkit.getWorld(worldName);
            this.location = new Location(world, x, y, z, yaw, pitch);
        } else {
            this.location = null;
        }

        parameters = ConfigurationUtils.getConfigurationSection(configuration, "parameters");
        createEntityData();
    }

    private void createEntityData() {
        boolean hasMobKey = mobKey != null && !mobKey.isEmpty();
        EntityData entity = hasMobKey ? controller.getMob(mobKey) : null;
        if (entity == null) {
            ConfigurationSection templateParameters = getTemplateParameters();
            String defaultType = templateParameters == null ? "villager" : templateParameters.getString("type", "villager");
            if (hasMobKey) {
                controller.getLogger().warning("NPC " + getLabel() + " has unknown mob type: " + mobKey + ", will change to " + defaultType);
            }
            entity = controller.getMob(defaultType);
        }
        if (entity == null) {
            controller.getLogger().warning("NPC " + getLabel() + " has unknown mob type: " + mobKey + ", and no default mob type was available. Defaulting to villager.");
            entity = new EntityData(controller, EntityType.VILLAGER);
        }
        setEntityData(entity);
    }

    @Nullable
    protected ConfigurationSection getTemplateParameters() {
        if (templateKey == null || templateKey.isEmpty()) {
            return null;
        }

        // Prevents needless combining of identical configs
        if (templateKey.equals(mobKey)) {
            return null;
        }

        EntityData entityData = controller.getMob(templateKey);
        return entityData == null ? null : entityData.getConfiguration();
    }

    protected boolean isLocationValid() {
        return location != null && location.getWorld() != null;
    }

    public boolean isValid() {
        return isLocationValid();
    }

    public long getBlockId() {
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
        remove();
        restore();
        return true;
    }

    @Override
    public boolean setTemplate(String templateKey) {
        EntityData newTemplate = controller.getMob(templateKey);
        if (newTemplate == null) {
            return false;
        }
        this.templateKey = templateKey;
        if (mobKey != null) {
            setType(mobKey);
        }
        return true;
    }

    protected void setEntityData(EntityData templateData) {
        entityData = templateData.clone();
        configureEntityData();
    }

    protected void configureEntityData() {
        ConfigurationSection effectiveParameters = ConfigurationUtils.cloneConfiguration(entityData.getConfiguration());
        ConfigurationSection templateParameters = getTemplateParameters();
        if (templateParameters != null) {
            effectiveParameters = ConfigurationUtils.addConfigurations(effectiveParameters, templateParameters);
        }
        effectiveParameters = ConfigurationUtils.addConfigurations(effectiveParameters, parameters);

        // Always keep entity type and name
        EntityType entityType = entityData.getType();
        if (entityType != null) {
            effectiveParameters.set("type", entityType.name());
        }
        effectiveParameters.set("name", name);
        entityData.load(effectiveParameters);
    }

    @Override
    @Nullable
    public Entity getEntity() {
        return entityId == null ? null : CompatibilityLib.getCompatibilityUtils().getEntity(location.getWorld(), entityId);
    }

    @Override
    public boolean isEntity(Entity entity) {
        if (entity == null || entityId == null) return false;
        Entity vehicle = entity;
        while (vehicle != null) {
            if (vehicle.getUniqueId().equals(entityId)) return true;
            vehicle = vehicle.getVehicle();
        }
        // This won't work for multi-passenger mobs
        List<Entity> passengers = CompatibilityLib.getCompatibilityUtils().getPassengers(entity);
        while (!passengers.isEmpty()) {
            Entity passenger = passengers.get(0);
            if (passenger.getUniqueId().equals(entityId)) return true;
            passengers = CompatibilityLib.getCompatibilityUtils().getPassengers(passenger);
        }

        return false;
    }

    @Override
    public void teleport(@Nonnull Location location) {
        controller.unregisterNPC(this);
        setLocation(location);
        if (CompatibilityLib.getCompatibilityUtils().isChunkLoaded(location)) {
            // restore will teleport the mob, so no need to teleport separately
            restore();
        }
        controller.registerNPC(this);
    }

    public void setLocation(Location location) {
        this.location = location.clone();
        World world = this.location.getWorld();
        if (world != null) {
            this.worldName = world.getName();
        }
    }

    @Override
    @Nullable
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean isActive() {
        Entity entity = getEntity();
        return entity != null && entity.isValid();
    }

    @Override
    public void remove() {
        Entity entity = getEntity();
        if (entity != null) {
            entity.remove();
            entityData.onDeath(entity);
        }
    }

    @Nullable
    @Override
    public Entity respawn() {
        return restore();
    }

    @Nullable
    public Entity restore() {
        return restore(null);
    }

    @Nullable
    public Entity restore(Map<UUID, Entity> entityMap) {
        if (location == null || !CompatibilityLib.getCompatibilityUtils().isChunkLoaded(location)) {
            return null;
        }
        Entity entity = getEntity();
        boolean forceValid = false;
        if ((entity == null || !entity.isValid()) && entityMap != null && entityId != null)  {
            entity = entityMap.get(entityId);
            // seems like entities can still be invalid here, guess we will trust that they won't be
            forceValid = true;
        }
        if (entity == null || (!entity.isValid() && !forceValid)) {
            controller.setDisableSpawnReplacement(true);
            try {
                entity = entityData.spawn(location);
            } catch (Exception ex) {
                controller.getLogger().log(Level.SEVERE, "Unexpected error spawning NPC mob", ex);
            }
            controller.setDisableSpawnReplacement(false);
        } else {
            entityData.modify(entity);
            CompatibilityLib.getCompatibilityUtils().teleportWithVehicle(entity, location);
        }
        if (entity == null) {
            controller.getLogger().warning("Failed to restore NPC entity");
            return null;
        }
        Entity vehicle = entity;
        while (vehicle != null) {
            CompatibilityLib.getEntityMetadataUtils().setString(vehicle, MagicMetaKeys.NPC_ID, id.toString());
            vehicle = vehicle.getVehicle();
        }
        if (entityData.useNPCName()) {
            entity.setCustomName(getName());
        }
        this.entityId = entity.getUniqueId();
        return entity;
    }

    public void save(ConfigurationSection configuration) {
        configuration.set("template", templateKey);
        configuration.set("id", id.toString());
        if (entityId != null) {
            configuration.set("entity_uuid", entityId.toString());
        }
        configuration.set("mob", mobKey);
        configuration.set("name", name);
        configuration.set("created", createdAt);
        configuration.set("creator", creatorId);
        configuration.set("creator_name", creatorName);
        configuration.set("imported_id", importedId);
        ConfigurationSection locationSection = configuration.createSection("location");
        locationSection.set("world", worldName);
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
        return CompatibilityLib.getCompatibilityUtils().translateColors(name);
    }

    protected String getLabel() {
        if (!name.isEmpty()) {
            return name;
        }
        if (templateKey != null && !templateKey.isEmpty()) {
            return ":" + templateKey;
        }
        return id.toString();
    }

    @Override
    public void setName(@Nonnull String name) {
        this.name = Preconditions.checkNotNull(name, "name");
        restore();
    }

    @Nonnull
    @Override
    public UUID getId() {
        return id;
    }

    @Nullable
    @Override
    public UUID getEntityId() {
        return entityId;
    }

    @Override
    public void configure(String key, Object value) {
        value = ConfigurationUtils.convertProperty(value);
        parameters.set(key, value);
        entityData.getConfiguration().set(key, value);
        update();
    }

    @Override
    public void update() {
        // In case entity data was reloaded
        boolean updated = false;
        if (mobKey != null) {
            EntityData newEntityData = controller.getMob(mobKey);
            if (newEntityData != null) {
                setEntityData(newEntityData);
                updated = true;
            }
        }
        if (!updated) {
            configureEntityData();
        }
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
        ConfigurationSection templateParameters = getTemplateParameters();
        if (templateParameters != null) {
            parameters = ConfigurationUtils.cloneConfiguration(templateParameters);
            parameters = ConfigurationUtils.addConfigurations(parameters, this.parameters);
        } else {
            parameters = ConfigurationUtils.cloneConfiguration(this.parameters);
        }
        parameters.set("type", null);
        String interactSpell = parameters.getString("interact_spell");
        if (interactSpell != null && !interactSpell.isEmpty()) {
            SpellTemplate template = controller.getSpellTemplate(interactSpell);
            String spellName = template == null ? (ChatColor.RED + interactSpell) : (ChatColor.YELLOW + template.getName());
            sender.sendMessage(ChatColor.AQUA + "Casts" + ChatColor.DARK_AQUA + ": " + spellName
                + ChatColor.DARK_GRAY + " (" + ChatColor.GRAY + interactSpell + ChatColor.DARK_GRAY + ")");
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
                sender.sendMessage(propertyColor.toString() + key + ChatColor.GRAY + ": " + ChatColor.WHITE + CompatibilityLib.getInventoryUtils().describeProperty(value, CompatibilityConstants.MAX_PROPERTY_DISPLAY_LENGTH));
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

    public boolean isStatic() {
        return entityData.isStatic();
    }
}
