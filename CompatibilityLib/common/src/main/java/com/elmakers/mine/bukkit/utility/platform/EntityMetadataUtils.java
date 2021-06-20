package com.elmakers.mine.bukkit.utility.platform;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.MetaKey;

public abstract class EntityMetadataUtils {
    protected final Plugin plugin;

    protected EntityMetadataUtils(Plugin plugin) {
        this.plugin = plugin;
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
