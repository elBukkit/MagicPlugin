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
        return new CompatibilityUtils();
    }

    public DeprecatedUtils getDeprecatedUtils() {
        return new DeprecatedUtils();
    }

    public InventoryUtils getInventoryUtils() {
        return new InventoryUtils();
    }

    public ItemUtils getItemUtils() {
        return new ItemUtils();
    }

    public NBTUtils getNBTUtils() {
        return new NBTUtils();
    }
}
