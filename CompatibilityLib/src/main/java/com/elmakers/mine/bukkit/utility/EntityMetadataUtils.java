package com.elmakers.mine.bukkit.utility;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public abstract class EntityMetadataUtils {
    public static EntityMetadataUtils metadataUtils;

    protected final Plugin plugin;

    protected EntityMetadataUtils(Plugin plugin) {
        this.plugin = plugin;
    }

    public static void initialize(Plugin plugin) {
        metadataUtils = new LegacyEntityMetadataUtils(plugin);
    }

    public static EntityMetadataUtils instance() {
        return metadataUtils;
    }

    public abstract boolean hasMetadata(Entity entity, String key);
    public abstract boolean getBoolean(Entity entity, String key);
    public abstract Double getDouble(Entity entity, String key);
    public abstract Long getLong(Entity entity, String key);
    public abstract String getString(Entity entity, String key);

    public abstract void setBoolean(Entity entity, String key, boolean value);
    public abstract void setDouble(Entity entity, String key, double value);
    public abstract void setLong(Entity entity, String key, long value);
    public abstract void setString(Entity entity, String key, String value);
}
