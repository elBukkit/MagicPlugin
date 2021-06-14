package com.elmakers.mine.bukkit.utility.platform.legacy;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

public class Platform implements com.elmakers.mine.bukkit.utility.platform.Platform {
    private final boolean valid;
    private final Logger logger;
    private final Plugin plugin;
    private final com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils compatibilityUtils;
    private final com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils deprecatedUtils;
    private final com.elmakers.mine.bukkit.utility.platform.InventoryUtils inventoryUtils;
    private final com.elmakers.mine.bukkit.utility.platform.ItemUtils itemUtils;
    private final com.elmakers.mine.bukkit.utility.platform.NBTUtils nbtUtils;
    private final com.elmakers.mine.bukkit.utility.platform.SchematicUtils schematicUtils;
    private final com.elmakers.mine.bukkit.utility.platform.SkinUtils skinUtils;

    public Platform(Plugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
        valid = NMSUtils.initialize(logger);
        compatibilityUtils = new CompatibilityUtils(this);
        deprecatedUtils = new DeprecatedUtils(this);
        inventoryUtils = new InventoryUtils(this);
        itemUtils = new ItemUtils(this);
        nbtUtils = new NBTUtils(this);
        schematicUtils = new SchematicUtils(this);
        skinUtils = new SkinUtils(this);
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public boolean isLegacy() {
        return NMSUtils.legacy;
    }

    @Override
    public boolean isCurrentVersion() {
        return NMSUtils.isModernVersion;
    }

    @Override
    public boolean hasStatistics() {
        return NMSUtils.hasStatistics;
    }

    @Override
    public boolean hasEntityTransformEvent() {
        return NMSUtils.hasEntityTransformEvent;
    }

    @Override
    public boolean hasTimeSkipEvent() {
        return NMSUtils.hasTimeSkipEvent;
    }

    @Override
    public String getVersionPrefix() {
        return NMSUtils.versionPrefix;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils getCompatibilityUtils() {
        return compatibilityUtils;
    }

    @Override
    public com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils getDeprecatedUtils() {
        return deprecatedUtils;
    }

    @Override
    public com.elmakers.mine.bukkit.utility.platform.InventoryUtils getInventoryUtils() {
        return inventoryUtils;
    }

    @Override
    public com.elmakers.mine.bukkit.utility.platform.ItemUtils getItemUtils() {
        return itemUtils;
    }

    @Override
    public com.elmakers.mine.bukkit.utility.platform.NBTUtils getNBTUtils() {
        return nbtUtils;
    }

    @Override
    public com.elmakers.mine.bukkit.utility.platform.SchematicUtils getSchematicUtils() {
        return schematicUtils;
    }

    @Override
    public com.elmakers.mine.bukkit.utility.platform.SkinUtils getSkinUtils() {
        return skinUtils;
    }
}
