package com.elmakers.mine.bukkit.utility.platform.legacy;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.InventoryUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;
import com.elmakers.mine.bukkit.utility.platform.NBTUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.SchematicUtils;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;

public class PlatformBase implements Platform {
    private final boolean valid;
    private final Logger logger;
    private final Plugin plugin;
    private final CompatibilityUtils compatibilityUtils;
    private final DeprecatedUtils deprecatedUtils;
    private final InventoryUtils inventoryUtils;
    private final ItemUtils itemUtils;
    private final NBTUtils nbtUtils;
    private final SchematicUtils schematicUtils;
    private final SkinUtils skinUtils;

    public PlatformBase(Plugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
        valid = NMSUtils.initialize(logger);
        compatibilityUtils = new CompatibilityUtilsBase(this);
        deprecatedUtils = new DeprecatedUtilsBase(this);
        inventoryUtils = new InventoryUtilsBase(this);
        itemUtils = new ItemUtilsBase(this);
        nbtUtils = new NBTUtilsBase(this);
        schematicUtils = new SchematicUtilsBase(this);
        skinUtils = new SkinUtilsBase(this);
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
    public CompatibilityUtils getCompatibilityUtils() {
        return compatibilityUtils;
    }

    @Override
    public DeprecatedUtils getDeprecatedUtils() {
        return deprecatedUtils;
    }

    @Override
    public InventoryUtils getInventoryUtils() {
        return inventoryUtils;
    }

    @Override
    public ItemUtils getItemUtils() {
        return itemUtils;
    }

    @Override
    public NBTUtils getNBTUtils() {
        return nbtUtils;
    }

    @Override
    public SchematicUtils getSchematicUtils() {
        return schematicUtils;
    }

    @Override
    public SkinUtils getSkinUtils() {
        return skinUtils;
    }
}
