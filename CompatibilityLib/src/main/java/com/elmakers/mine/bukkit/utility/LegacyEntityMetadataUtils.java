package com.elmakers.mine.bukkit.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class LegacyEntityMetadataUtils extends EntityMetadataUtils {
    private final Map<Entity, Map<String, Object>> metadata = new WeakHashMap<>();

    protected LegacyEntityMetadataUtils(Plugin plugin) {
        super(plugin);
    }

    protected Object getRawValue(Entity entity, String key) {
        Map<String, Object> data = metadata.get(entity);
        return data == null ? null : data.get(key);
    }

    @Override
    public boolean getBoolean(Entity entity, String key) {
        Object value = getRawValue(entity, key);
        return value == null || !(value instanceof Boolean) ? false : (Boolean)value;
    }

    @Override
    public Double getDouble(Entity entity, String key) {
        Object value = getRawValue(entity, key);
        return value == null || !(value instanceof Double) ? null : (Double)value;
    }

    @Override
    public Long getLong(Entity entity, String key) {
        Object value = getRawValue(entity, key);
        return value == null || !(value instanceof Long) ? null : (Long)value;
    }

    @Override
    public String getString(Entity entity, String key) {
        Object value = getRawValue(entity, key);
        return value == null || !(value instanceof String) ? null : (String)value;
    }

    protected void setRawValue(Entity entity, String key, Object value) {
        Map<String, Object> values = metadata.get(entity);
        if (values == null) {
            values = new HashMap<>();
            metadata.put(entity, values);
        }
        values.put(key, value);
    }

    @Override
    public void setBoolean(Entity entity, String key, boolean value) {
        setRawValue(entity, key, value);
    }

    @Override
    public void setDouble(Entity entity, String key, double value) {
        setRawValue(entity, key, value);
    }

    @Override
    public void setLong(Entity entity, String key, long value) {
        setRawValue(entity, key, value);
    }

    @Override
    public void setString(Entity entity, String key, String value) {
        setRawValue(entity, key, value);
    }
}
