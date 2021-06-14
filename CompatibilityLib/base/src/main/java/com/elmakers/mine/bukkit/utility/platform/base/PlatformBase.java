package com.elmakers.mine.bukkit.utility.platform.base;

import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.InventoryUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;
import com.elmakers.mine.bukkit.utility.platform.NBTUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.SchematicUtils;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;

public abstract class PlatformBase implements Platform {
    private final Logger logger;
    private final Plugin plugin;
    protected final String versionPrefix;
    protected boolean valid = false;
    protected CompatibilityUtils compatibilityUtils;
    protected DeprecatedUtils deprecatedUtils;
    protected InventoryUtils inventoryUtils;
    protected ItemUtils itemUtils;
    protected NBTUtils nbtUtils;
    protected SchematicUtils schematicUtils;
    protected SkinUtils skinUtils;

    public PlatformBase(Plugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
        String className = Bukkit.getServer().getClass().getName();
        String[] packages = StringUtils.split(className, '.');
        if (packages.length == 5) {
            versionPrefix = packages[3] + ".";
        } else {
            versionPrefix = "";
        }
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
        return false;
    }

    @Override
    public boolean isCurrentVersion() {
        return true;
    }

    @Override
    public boolean hasStatistics() {
        return true;
    }

    @Override
    public boolean hasEntityTransformEvent() {
        return true;
    }

    @Override
    public boolean hasTimeSkipEvent() {
        return true;
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

    @Override
    public String getVersionPrefix() {
        return versionPrefix;
    }

    @Override
    public int[] getServerVersion() {
        int[] version = new int[2];
        String versionString = getVersionPrefix();
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
}
