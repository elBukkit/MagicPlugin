package com.elmakers.mine.bukkit.utility;

import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;

public class CompatibilityLib {
    private static Platform platform;
    private static CompatibilityUtils compatibilityUtils;

    public static boolean initialize(Plugin plugin, Logger logger) {
        platform = new Platform(plugin, logger);
        compatibilityUtils = platform.getCompatibilityUtils();
        return platform.isValid();
    }

    public static boolean isLegacy() {
        return platform == null ? false : platform.isLegacy();
    }

    public static boolean isCurrentVersion() {
        return platform == null ? true : platform.isCurrentVersion();
    }

    public static boolean hasStatistics() {
        return platform == null ? true : platform.hasStatistics();
    }

    public static boolean hasEntityTransformEvent() {
        return platform == null ? true : platform.hasEntityTransformEvent();
    }

    public static boolean hasTimeSkipEvent() {
        return platform == null ? true : platform.hasTimeSkipEvent();
    }

    public static int[] getServerVersion() {
        int[] version = new int[2];
        String versionString = platform == null ? null : platform.getVersionPrefix();
        if (versionString == null || versionString.isEmpty()) {
            return version;
        }
        // Format:  v1_12_R1
        versionString = versionString.substring(1);
        try {
            String[] pieces = StringUtils.split(versionString, '_');
            if (pieces.length > 0) {
                version[0] = Integer.parseInt(pieces[0]);
            }
            if (pieces.length > 1) {
                version[1] = Integer.parseInt(pieces[1]);
            }
        } catch (Exception ex) {

        }
        return version;
    }

    // This is here as a bit of a hack, MaterialAndData needs to know how to parse materials, but this is used
    // by the MaterialSetTest test framework, where we don't actually have a server and can't really
    // initialize CompatibilityLib.
    // Kind of ugly, but this sidesteps the problem.
    public static boolean isLegacy(Material material) {
        return compatibilityUtils == null ? false : compatibilityUtils.isLegacy(material);
    }

    public static boolean hasLegacyMaterials() {
        return compatibilityUtils == null ? false : compatibilityUtils.hasLegacyMaterials();
    }

    public static CompatibilityUtils getCompatibilityUtils() {
        if (compatibilityUtils == null) {
            Bukkit.getLogger().info("Warning: CompatibilityUtils used before being initialized");
            compatibilityUtils = new CompatibilityUtils();
        }
        return compatibilityUtils;
    }
}
