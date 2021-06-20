package com.elmakers.mine.bukkit.utility.platform.modern;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.MetaKey;
import com.elmakers.mine.bukkit.utility.platform.EntityMetadataUtils;

public class PersistentEntityMetadataUtils extends EntityMetadataUtils {
    public PersistentEntityMetadataUtils(Plugin plugin) {
        super(plugin);
    }

    protected NamespacedKey getKey(MetaKey<?> key) {
        return new NamespacedKey(plugin, key.getName());
    }

    @Override
    public void remove(Entity entity, MetaKey<?> key) {
        entity.getPersistentDataContainer().remove(getKey(key));
    }

    @Override
    public boolean getBoolean(Entity entity, MetaKey<Boolean> key) {
        Byte b = entity.getPersistentDataContainer().get(getKey(key), PersistentDataType.BYTE);
        return b != null && b != 0;
    }

    @Override
    public Double getDouble(Entity entity, MetaKey<Double> key) {
        return entity.getPersistentDataContainer().get(getKey(key), PersistentDataType.DOUBLE);
    }

    @Override
    public Long getLong(Entity entity, MetaKey<Long> key) {
        return entity.getPersistentDataContainer().get(getKey(key), PersistentDataType.LONG);
    }

    @Override
    public String getString(Entity entity, MetaKey<String> key) {
        return entity.getPersistentDataContainer().get(getKey(key), PersistentDataType.STRING);
    }

    @Override
    public void setBoolean(Entity entity, MetaKey<Boolean> key, boolean value) {
        entity.getPersistentDataContainer().set(getKey(key), PersistentDataType.BYTE, (byte)(value ? 1 : 0));
    }

    @Override
    public void setDouble(Entity entity, MetaKey<Double> key, double value) {
        entity.getPersistentDataContainer().set(getKey(key), PersistentDataType.DOUBLE, value);
    }

    @Override
    public void setLong(Entity entity, MetaKey<Long> key, long value) {
        entity.getPersistentDataContainer().set(getKey(key), PersistentDataType.LONG, value);
    }

    @Override
    public void setString(Entity entity, MetaKey<String> key, String value) {
        entity.getPersistentDataContainer().set(getKey(key), PersistentDataType.STRING, value);
    }
}
