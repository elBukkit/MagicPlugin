package com.elmakers.mine.bukkit.utility.platform;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

public interface Platform {
    Plugin getPlugin();

    Logger getLogger();

    boolean isLegacy();

    boolean isCurrentVersion();

    boolean hasStatistics();

    boolean hasEntityTransformEvent();

    boolean hasTimeSkipEvent();

    boolean isValid();

    CompatibilityUtils getCompatibilityUtils();

    DeprecatedUtils getDeprecatedUtils();

    InventoryUtils getInventoryUtils();

    ItemUtils getItemUtils();

    NBTUtils getNBTUtils();

    SchematicUtils getSchematicUtils();

    SkinUtils getSkinUtils();
}
