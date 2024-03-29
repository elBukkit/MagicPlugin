package com.elmakers.mine.bukkit.api.data;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.rp.ResourcePackPreference;
import com.elmakers.mine.bukkit.api.wand.Wand;

public class MageData {
    private String id;
    private String name;
    private ConfigurationSection data;
    private ConfigurationSection properties;
    private ConfigurationSection variables;
    private ConfigurationSection kits;
    private Map<String, ConfigurationSection> classProperties;
    private Map<String, ConfigurationSection> modifierProperties;
    private String activeClass;
    private long lastCast;
    private SerializedLocation lastDeathLocation;
    private SerializedLocation location;
    private String destinationWarp;
    private long cooldownExpiration;
    private long fallProtectionCount;
    private long fallProtectionDuration;
    private Map<String, ItemStack> boundWands;
    private Map<Integer, ItemStack> respawnArmor;
    private Map<Integer, ItemStack> respawnInventory;
    private List<ItemStack> storedInventory;
    private Collection<SpellData> spellData;
    private BrushData brushData;
    private UndoData undoData;
    private Map<String, UndoData> externalUndoData;
    private Float storedExperience;
    private Integer storedLevel;
    private boolean openWand;
    private boolean gaveWelcomeWand;
    private ResourcePackPreference resourcePackPreference;
    private String preferredResourcePack;
    private long created;
    private double health;
    private boolean shownHelp;

    // Transient
    private long cachedTimestamp;

    public MageData(String id) {
        this.id = id;
        this.cachedTimestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExtraData(ConfigurationSection data) {
        this.data = data;
    }

    public ConfigurationSection getExtraData() {
        if (data == null) {
            data = new MemoryConfiguration();
        }
        return data;
    }

    public long getLastCast() {
        return lastCast;
    }

    public void setLastCast(long lastCast) {
        this.lastCast = lastCast;
    }

    /**
     * Data can be saved asynchronously, and Locations' Worlds can be invalidated if the server unloads a world.
     * So do not call this method during saving.
     */
    @Nullable
    public Location getLastDeathLocation() {
        return lastDeathLocation == null ? null : lastDeathLocation.asLocation();
    }

    public void setLastDeathLocation(Location lastDeathLocation) {
        this.lastDeathLocation = lastDeathLocation == null ? null : new SerializedLocation(lastDeathLocation);
    }

    /**
     * Data can be saved asynchronously, and Locations' Worlds can be invalidated if the server unloads a world.
     * So do not call this method during saving.
     */
    @Nullable
    public Location getLocation() {
        return location == null ? null : location.asLocation();
    }

    public void setLocation(Location location) {
        this.location = location == null ? null : new SerializedLocation(location);
    }

    public void setSerializedLocation(SerializedLocation location) {
        this.location = location;
    }

    public SerializedLocation getSerializedLocation() {
        return location;
    }

    public void setSerializedLastDeathLocation(SerializedLocation location) {
        this.lastDeathLocation = location;
    }

    public SerializedLocation getSerializedLastDeathLocation() {
        return lastDeathLocation;
    }

    public String getDestinationWarp() {
        return destinationWarp;
    }

    public void setDestinationWarp(String destinationWarp) {
        this.destinationWarp = destinationWarp;
    }

    public long getFallProtectionCount() {
        return fallProtectionCount;
    }

    public void setFallProtectionCount(long fallProtectionCount) {
        this.fallProtectionCount = fallProtectionCount;
    }

    public long getFallProtectionDuration() {
        return fallProtectionDuration;
    }

    public void setFallProtectionDuration(long fallProtectionDuration) {
        this.fallProtectionDuration = fallProtectionDuration;
    }

    public BrushData getBrushData() {
        return brushData;
    }

    public void setBrushData(BrushData brush) {
        this.brushData = brush;
    }

    public Collection<SpellData> getSpellData() {
        return spellData;
    }

    public void setSpellData(Collection<SpellData> spellData) {
        this.spellData = spellData;
    }

    public Map<String, ItemStack> getBoundWands() {
        return boundWands;
    }

    public void setBoundWands(Map<String, ItemStack> boundWands) {
        this.boundWands = boundWands;
    }

    public Map<Integer, ItemStack> getRespawnArmor() {
        return respawnArmor;
    }

    public void setRespawnArmor(Map<Integer, ItemStack> respawnArmor) {
        this.respawnArmor = respawnArmor;
    }

    public Map<Integer, ItemStack> getRespawnInventory() {
        return respawnInventory;
    }

    public void setRespawnInventory(Map<Integer, ItemStack> respawnInventory) {
        this.respawnInventory = respawnInventory;
    }

    public List<ItemStack> getStoredInventory() {
        return storedInventory;
    }

    public void setStoredInventory(List<ItemStack> storedInventory) {
        this.storedInventory = storedInventory;
    }

    public UndoData getUndoData() {
        return undoData;
    }

    public void setUndoData(UndoData undoData) {
        this.undoData = undoData;
    }

    public Map<String, UndoData> getExternalUndoData() {
        return externalUndoData;
    }

    public void setExternalUndoData(Map<String, UndoData> undoData) {
        externalUndoData = undoData;
    }

    public long getCooldownExpiration() {
        return cooldownExpiration;
    }

    public void setCooldownExpiration(long cooldownExpiration) {
        this.cooldownExpiration = cooldownExpiration;
    }


    public Float getStoredExperience() {
        return storedExperience;
    }

    public void setStoredExperience(Float storedExperience) {
        this.storedExperience = storedExperience;
    }

    public Integer getStoredLevel() {
        return storedLevel;
    }

    public void setStoredLevel(Integer storedLevel) {
        this.storedLevel = storedLevel;
    }

    public boolean isOpenWand() {
        return openWand;
    }

    public void setOpenWand(boolean openWand) {
        this.openWand = openWand;
    }

    public boolean getGaveWelcomeWand() {
        return gaveWelcomeWand;
    }

    public void setGaveWelcomeWand(boolean gaveWelcomeWand) {
        this.gaveWelcomeWand = gaveWelcomeWand;
    }

    public ResourcePackPreference getResourcePackPreference() {
        return resourcePackPreference;
    }

    public void setResourcePackPreference(ResourcePackPreference resourcePackPreference) {
        this.resourcePackPreference = resourcePackPreference;
    }

    public String getPreferredResourcePack() {
        return preferredResourcePack;
    }

    public void setPreferredResourcePack(String preferredResourcePack) {
        this.preferredResourcePack = preferredResourcePack;
    }

    public ConfigurationSection getProperties() {
        if (properties == null) {
            properties = new MemoryConfiguration();
        }
        return properties;
    }

    public void setProperties(ConfigurationSection properties) {
        this.properties = properties;
    }

    public Map<String, ConfigurationSection> getClassProperties() {
        return classProperties;
    }

    public void setClassProperties(Map<String, ConfigurationSection> classProperties) {
        this.classProperties = classProperties;
    }

    public Map<String, ConfigurationSection> getModifierProperties() {
        return modifierProperties;
    }

    public void setModifierProperties(Map<String, ConfigurationSection> modifierProperties) {
        this.modifierProperties = modifierProperties;
    }

    public String getActiveClass() {
        return activeClass;
    }

    public void setActiveClass(String activeClass) {
        this.activeClass = activeClass;
    }

    public long getCreatedTime() {
        return created;
    }

    public void setCreatedTime(long created) {
        this.created = created;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    @Deprecated
    @Nullable
    public Wand getSoulWand() {
        return null;
    }

    @Deprecated
    public void setSoulWand(Wand soulWand) {

    }

    public ConfigurationSection getVariables() {
        return variables;
    }

    public void setVariables(ConfigurationSection variables) {
        this.variables = variables;
    }

    public ConfigurationSection getKits() {
        return kits;
    }

    public void setKits(ConfigurationSection kits) {
        this.kits = kits;
    }

    public long getCachedTimestamp() {
        return cachedTimestamp;
    }

    public boolean getShownHelp() {
        return shownHelp;
    }

    public void setShownHelp(boolean shownHelp) {
        this.shownHelp = shownHelp;
    }
}
