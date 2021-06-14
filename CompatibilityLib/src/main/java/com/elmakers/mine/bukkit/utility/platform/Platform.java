package com.elmakers.mine.bukkit.utility.platform;

public class Platform {
    private final boolean valid;

    public Platform() {
        valid = NMSUtils.initialize();
    }

    public boolean isLegacy() {
        return NMSUtils.legacy;
    }

    public boolean isCurrentVersion() {
        return NMSUtils.isModernVersion;
    }

    public boolean hasStatistics() {
        return NMSUtils.hasStatistics;
    }

    public boolean hasEntityTransformEvent() {
        return NMSUtils.hasEntityTransformEvent;
    }

    public boolean hasTimeSkipEvent() {
        return NMSUtils.hasTimeSkipEvent;
    }

    public String getVersionPrefix() {
        return NMSUtils.versionPrefix;
    }

    public boolean isValid() {
        return valid;
    }

    public CompatibilityUtils getCompatibilityUtils() {
        return new CompatibilityUtilsBase();
    }

    public DeprecatedUtils getDeprecatedUtils() {
        return new DeprecatedUtilsBase();
    }

    public InventoryUtils getInventoryUtils() {
        return new InventoryUtilsBase();
    }

    public ItemUtils getItemUtils() {
        return new ItemUtilsBase();
    }

    public NBTUtils getNBTUtils() {
        return new NBTUtils();
    }

    public SchematicUtils getSchematicUtils() {
        return new SchematicUtils();
    }

    public SkinUtils getSkinUtils() {
        return new SkinUtils();
    }
}
