package com.elmakers.mine.bukkit.utility.metadata;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.MetaKey;

public abstract class EntityMetadataUtils {
    public static EntityMetadataUtils metadataUtils;

    protected final Plugin plugin;

    protected EntityMetadataUtils(Plugin plugin) {
        this.plugin = plugin;
    }

    protected static boolean hasPersistentMetadata() {
        // Unfortunately this API is bugged prior to 1.16, it does not work for dropped items so we can not use it.
        int[] version = CompatibilityLib.getServerVersion();
        if (version[0] <= 1 && version[1] < 16) return false;

        try {
            Class.forName("org.bukkit.persistence.PersistentDataContainer");
            return true;
        } catch (Exception noPersistence) {

        }
        return false;
    }

    public static void initialize(Plugin plugin) {
        if (hasPersistentMetadata()) {
            metadataUtils = new PersistentEntityMetadataUtils(plugin);
        } else {
            plugin.getLogger().info("Persistent metadata is not available, will rely on custom names to restore persistent magic mobs");
            metadataUtils = new LegacyEntityMetadataUtils(plugin);
        }
    }

    public static EntityMetadataUtils instance() {
        return metadataUtils;
    }

    public abstract void remove(Entity entity, MetaKey<?> key);

    public abstract boolean getBoolean(Entity entity, MetaKey<Boolean> key);
    public abstract Double getDouble(Entity entity, MetaKey<Double> key);
    public abstract Long getLong(Entity entity, MetaKey<Long> key);
    public abstract String getString(Entity entity, MetaKey<String> key);

    public abstract void setBoolean(Entity entity, MetaKey<Boolean> key, boolean value);
    public abstract void setDouble(Entity entity, MetaKey<Double> key, double value);
    public abstract void setLong(Entity entity, MetaKey<Long> key, long value);
    public abstract void setString(Entity entity, MetaKey<String> key, String value);
}
