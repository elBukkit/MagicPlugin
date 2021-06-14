package com.elmakers.mine.bukkit.utility.platform;

import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class PlatformBase implements Platform {
    private final boolean valid;

    public PlatformBase() {
        valid = NMSUtils.initialize();
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
        return new CompatibilityUtilsBase();
    }

    @Override
    public DeprecatedUtils getDeprecatedUtils() {
        return new DeprecatedUtilsBase();
    }

    @Override
    public InventoryUtils getInventoryUtils() {
        return new InventoryUtilsBase();
    }

    @Override
    public ItemUtils getItemUtils() {
        return new ItemUtilsBase();
    }

    @Override
    public NBTUtils getNBTUtils() {
        return new NBTUtilsBase();
    }

    @Override
    public SchematicUtils getSchematicUtils() {
        return new SchematicUtilsBase();
    }

    @Override
    public SkinUtils getSkinUtils() {
        return new SkinUtilsBase(CompatibilityLib.getPlugin());
    }
}
