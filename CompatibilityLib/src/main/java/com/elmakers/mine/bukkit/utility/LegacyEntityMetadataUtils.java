package com.elmakers.mine.bukkit.utility;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class LegacyEntityMetadataUtils extends EntityMetadataUtils {
    // This is leaky.
    // There does not seem to be a way to really track entities to clean up this map, other than periodic checks
    // which don't feel performant enough to be worth the small amount of memory leaked here.
    // Modern MC versions will use the persistent metadata system instead.
    private final Map<String, Map<String, Object>> metadata = new HashMap<>();

    protected LegacyEntityMetadataUtils(Plugin plugin) {
        super(plugin);
    }

    protected Object getRawValue(Entity entity, String key) {
        Map<String, Object> data = metadata.get(entity.getUniqueId().toString());
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
        Map<String, Object> values = metadata.get(entity.getUniqueId().toString());
        if (values == null) {
            values = new HashMap<>();
            metadata.put(entity.getUniqueId().toString(), values);
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
