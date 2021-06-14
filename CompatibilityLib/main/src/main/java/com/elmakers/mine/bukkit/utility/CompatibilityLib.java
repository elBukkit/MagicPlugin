package com.elmakers.mine.bukkit.utility;

import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.metadata.EntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.InventoryUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;
import com.elmakers.mine.bukkit.utility.platform.NBTUtils;
import com.elmakers.mine.bukkit.utility.platform.SchematicUtils;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;
import com.elmakers.mine.bukkit.utility.platform.legacy.Platform;

public class CompatibilityLib {
    private static com.elmakers.mine.bukkit.utility.platform.Platform platform;

    public static boolean initialize(Plugin plugin, Logger logger) {
        platform = new Platform(plugin, logger);
        EntityMetadataUtils.initialize(plugin);
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
        CompatibilityUtils compatibilityUtils = platform == null ? null : platform.getCompatibilityUtils();
        return compatibilityUtils == null ? false : compatibilityUtils.isLegacy(material);
    }

    public static boolean hasLegacyMaterials() {
        CompatibilityUtils compatibilityUtils = platform == null ? null : platform.getCompatibilityUtils();
        return compatibilityUtils == null ? false : compatibilityUtils.hasLegacyMaterials();
    }

    @Nonnull
    public static Logger getLogger() {
        Logger logger = platform == null ? null : platform.getLogger();
        return logger == null ? Bukkit.getLogger() : logger;
    }

    public static Plugin getPlugin() {
        return platform == null ? null : platform.getPlugin();
    }

    public static CompatibilityUtils getCompatibilityUtils() {
        if (platform == null) {
            throw new IllegalStateException("CompatibilityUtils used before being initialized");
        }
        return platform.getCompatibilityUtils();
    }

    public static DeprecatedUtils getDeprecatedUtils() {
        if (platform == null) {
            throw new IllegalStateException("DeprecatedUtils used before being initialized");
        }
        return platform.getDeprecatedUtils();
    }

    public static InventoryUtils getInventoryUtils() {
        if (platform == null) {
            throw new IllegalStateException("InventoryUtils used before being initialized");
        }
        return platform.getInventoryUtils();
    }

    public static ItemUtils getItemUtils() {
        if (platform == null) {
            throw new IllegalStateException("ItemUtils used before being initialized");
        }
        return platform.getItemUtils();
    }

    public static NBTUtils getNBTUtils() {
        if (platform == null) {
            throw new IllegalStateException("NBTUtils used before being initialized");
        }
        return platform.getNBTUtils();
    }

    public static SchematicUtils getSchematicUtils() {
        if (platform == null) {
            throw new IllegalStateException("SchematicUtils used before being initialized");
        }
        return platform.getSchematicUtils();
    }

    public static SkinUtils getSkinUtils() {
        if (platform == null) {
            throw new IllegalStateException("SkinUtils used before being initialized");
        }
        return platform.getSkinUtils();
    }
}
