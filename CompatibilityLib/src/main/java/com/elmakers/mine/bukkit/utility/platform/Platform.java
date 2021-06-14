package com.elmakers.mine.bukkit.utility.platform;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

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
}
