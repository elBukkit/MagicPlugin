package com.elmakers.mine.bukkit.utility;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

public class CompatibilityLib {

    public static boolean initialize(Plugin plugin, Logger logger) {
        return NMSUtils.initialize(plugin, logger);
    }

    public static boolean isLegacy() {
        return NMSUtils.legacy;
    }

    public static boolean isCurrentVersion() {
        return NMSUtils.isModernVersion;
    }

    public static boolean hasStatistics() {
        return NMSUtils.hasStatistics;
    }

    public static boolean hasEntityTransformEvent() {
        return NMSUtils.hasEntityTransformEvent;
    }

    public static boolean hasTimeSkipEvent() {
        return NMSUtils.hasTimeSkipEvent;
    }
}
