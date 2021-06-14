package com.elmakers.mine.bukkit.utility.platform;

public interface Platform {
    boolean isLegacy();

    boolean isCurrentVersion();

    boolean hasStatistics();

    boolean hasEntityTransformEvent();

    boolean hasTimeSkipEvent();

    String getVersionPrefix();

    boolean isValid();

    CompatibilityUtils getCompatibilityUtils();

    DeprecatedUtils getDeprecatedUtils();

    InventoryUtils getInventoryUtils();

    ItemUtils getItemUtils();

    NBTUtils getNBTUtils();

    SchematicUtils getSchematicUtils();

    SkinUtils getSkinUtils();
}
