package com.elmakers.mine.bukkit.utility;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.EntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.platform.EntityUtils;
import com.elmakers.mine.bukkit.utility.platform.InventoryUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;
import com.elmakers.mine.bukkit.utility.platform.NBTUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.PlatformInterpreter;
import com.elmakers.mine.bukkit.utility.platform.SchematicUtils;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;

public class CompatibilityLib extends PlatformInterpreter {
    private static com.elmakers.mine.bukkit.utility.platform.Platform platform;

    public static boolean initialize(Plugin plugin, Logger logger) {
        int[] version = getServerVersion();
        String versionDescription = StringUtils.join(ArrayUtils.toObject(version), ".");
        if (version.length < 2 || version[0] != 1) {
            logger.severe("Could not parse server version: " + versionDescription);
            return false;
        }
        int minorVersion = version[1];
        if (minorVersion < 9) {
            logger.severe("Not compatible with version: " + versionDescription);
            return false;
        }
        if (minorVersion >= 17) {
            logger.info("Loading modern compatibility layer for server version " + versionDescription);
            try {
                String versionPackage = StringUtils.join(ArrayUtils.toObject(version), "_");
                Class<?> platformClass = Class.forName("com.elmakers.mine.bukkit.utility.platform.v" + versionPackage + ".Platform");
                Constructor<?> platformConstructor = platformClass.getConstructor(Plugin.class, Logger.class);
                platform = (Platform)platformConstructor.newInstance(plugin, logger);
            } catch (Exception ex) {
                logger.severe("Failed to load compatibility layer, the plugin may need to be updated to work with your server version");
                return false;
            }
        } else {
            versionDescription = version[0] + "." + version[1];
            logger.info("Loading legacy  compatibility layer for server version " + versionDescription);
            try {
                // No minor minor version numbers here
                String versionPackage = version[0] + "_" + version[1];
                Class<?> platformClass = Class.forName("com.elmakers.mine.bukkit.utility.platform.v" + versionPackage + ".Platform");
                Constructor<?> platformConstructor = platformClass.getConstructor(Plugin.class, Logger.class);
                platform = (Platform)platformConstructor.newInstance(plugin, logger);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Failed to load compatibility layer, this is unexpected for legacy versions, please report this error", ex);
                return false;
            }
        }
        setPlatform(platform);
        return platform.isValid();
    }

    public static boolean isInitialized() {
        return platform != null;
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

    public static boolean isLegacy() {
        return platform == null ? false : platform.isLegacy();
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

    public static EntityMetadataUtils getEntityMetadataUtils() {
        if (platform == null) {
            throw new IllegalStateException("EntityMetadataUtils used before being initialized");
        }
        return platform.getEnityMetadataUtils();
    }

    public static EntityUtils getEntityUtils() {
        if (platform == null) {
            throw new IllegalStateException("EntityUtils used before being initialized");
        }
        return platform.getEntityUtils();
    }

    public static int[] getServerVersion() {
        int[] version = new int[3];
        String versionString = CompatibilityConstants.getVersionPrefix();
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
            if (pieces.length > 2) {
                version[2] = Integer.parseInt(pieces[2]);
            }
        } catch (Exception ignore) {

        }
        return version;
    }
}
