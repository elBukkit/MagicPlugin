package com.elmakers.mine.bukkit.utility;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class PersistentEntityMetadataUtils extends EntityMetadataUtils {
    protected PersistentEntityMetadataUtils(Plugin plugin) {
        super(plugin);
    }

    protected NamespacedKey getKey(String key) {
        return new NamespacedKey(plugin, key);
    }

    @Override
    public void remove(Entity entity, String key) {
        entity.getPersistentDataContainer().remove(getKey(key));
    }

    @Override
    public boolean getBoolean(Entity entity, String key) {
        Byte b = entity.getPersistentDataContainer().get(getKey(key), PersistentDataType.BYTE);
        return b != null && b != 0;
    }

    @Override
    public Double getDouble(Entity entity, String key) {
        return entity.getPersistentDataContainer().get(getKey(key), PersistentDataType.DOUBLE);
    }

    @Override
    public Long getLong(Entity entity, String key) {
        return entity.getPersistentDataContainer().get(getKey(key), PersistentDataType.LONG);
    }

    @Override
    public String getString(Entity entity, String key) {
        return entity.getPersistentDataContainer().get(getKey(key), PersistentDataType.STRING);
    }

    @Override
    public void setBoolean(Entity entity, String key, boolean value) {
        entity.getPersistentDataContainer().set(getKey(key), PersistentDataType.BYTE, (byte)(value ? 1 : 0));
    }

    @Override
    public void setDouble(Entity entity, String key, double value) {
        entity.getPersistentDataContainer().set(getKey(key), PersistentDataType.DOUBLE, value);
    }

    @Override
    public void setLong(Entity entity, String key, long value) {
        entity.getPersistentDataContainer().set(getKey(key), PersistentDataType.LONG, value);
    }

    @Override
    public void setString(Entity entity, String key, String value) {
        entity.getPersistentDataContainer().set(getKey(key), PersistentDataType.STRING, value);
    }
}
