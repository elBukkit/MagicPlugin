package com.elmakers.mine.bukkit.utility.metadata;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.MetaKey;

final class LegacyEntityMetadataUtils extends EntityMetadataUtils {
    // This is leaky.
    // There does not seem to be a way to really track entities to clean up this map, other than periodic checks
    // which don't feel performant enough to be worth the small amount of memory leaked here.
    // Modern MC versions will use the persistent metadata system instead.
    private final Map<String, Map<String, Object>> metadata = new HashMap<>();

    protected LegacyEntityMetadataUtils(Plugin plugin) {
        super(plugin);
    }

    protected <T> T getRawValue(Entity entity, MetaKey<T> key) {
        Map<String, Object> data = metadata.get(entity.getUniqueId().toString());
        Object v = data == null ? null : data.get(key.getName());
        return key.getType().isInstance(v) ? key.getType().cast(v) : null;
    }

    @Override
    public boolean getBoolean(Entity entity, MetaKey<Boolean> key) {
        Boolean value = getRawValue(entity, key);
        return value != null && value.booleanValue();
    }

    @Override
    public Double getDouble(Entity entity, MetaKey<Double> key) {
        return getRawValue(entity, key);
    }

    @Override
    public Long getLong(Entity entity, MetaKey<Long> key) {
        return getRawValue(entity, key);
    }

    @Override
    public String getString(Entity entity, MetaKey<String> key) {
        return getRawValue(entity, key);
    }

    protected <T> void setRawValue(Entity entity, MetaKey<T> key, T value) {
        Map<String, Object> values = metadata.get(entity.getUniqueId().toString());
        if (values == null) {
            values = new HashMap<>();
            metadata.put(entity.getUniqueId().toString(), values);
        }
        values.put(key.getName(), value);
    }

    @Override
    public void setBoolean(Entity entity, MetaKey<Boolean> key, boolean value) {
        setRawValue(entity, key, value);
    }

    @Override
    public void setDouble(Entity entity, MetaKey<Double> key, double value) {
        setRawValue(entity, key, value);
    }

    @Override
    public void setLong(Entity entity, MetaKey<Long> key, long value) {
        setRawValue(entity, key, value);
    }

    @Override
    public void setString(Entity entity, MetaKey<String> key, String value) {
        setRawValue(entity, key, value);
    }

    @Override
    public void remove(Entity entity, MetaKey<?> key) {
        Map<String, Object> values = metadata.get(entity.getUniqueId().toString());
        if (values != null) {
            values.remove(key);
        }
    }
}
