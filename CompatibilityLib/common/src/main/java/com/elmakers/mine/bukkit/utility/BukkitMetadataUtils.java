package com.elmakers.mine.bukkit.utility;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class BukkitMetadataUtils {

    public static Location getLocation(Entity entity, String key) {
        return getLocation(entity, key, null);
    }

    public static Location getLocation(Entity entity, String key, Plugin plugin) {
        if (entity == null || key == null) return null;
        Collection<MetadataValue> metadata = entity.getMetadata(key);
        for (MetadataValue value : metadata) {
            Object rawValue = value.value();
            if (rawValue != null && rawValue instanceof Location) {
                if (plugin != null && !plugin.equals(value.getOwningPlugin())) {
                    continue;
                }
                return (Location)rawValue;
            }
        }
        return null;
    }
}
