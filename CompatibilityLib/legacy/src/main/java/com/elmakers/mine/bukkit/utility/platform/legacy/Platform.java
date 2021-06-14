package com.elmakers.mine.bukkit.utility.platform.legacy;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.platform.base.PlatformBase;

public class Platform extends PlatformBase {

    public Platform(Plugin plugin, Logger logger) {
        super(plugin, logger);
        valid = NMSUtils.initialize(this);
        compatibilityUtils = new CompatibilityUtils(this);
        deprecatedUtils = new DeprecatedUtils(this);
        inventoryUtils = new InventoryUtils(this);
        itemUtils = new ItemUtils(this);
        nbtUtils = new NBTUtils(this);
        schematicUtils = new SchematicUtils(this);
        skinUtils = new SkinUtils(this);
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
}
