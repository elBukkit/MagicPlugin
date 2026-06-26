package com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.entity.SpawnedEntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.NBTUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;

public class EntityNMSData extends EntityExtraData {
    private final Platform platform;
    private Object tag;

    private EntityNMSData(final Platform platform) {
       this.platform = platform;
    }

    public EntityNMSData(final Platform platform, Entity entity) {
        this(platform);
        this.tag = platform.getCompatibilityUtils().getEntityData(entity);
    }

    public EntityNMSData(final Platform platform, Object tag) {
        this(platform);
        this.tag = tag;
    }

    @Override
    public void apply(Entity entity) {
        // Make sure some values are not changed
        Object currentTag = platform.getCompatibilityUtils().getEntityData(entity);
        if (currentTag != null && tag != null) {
            NBTUtils nbt = platform.getNBTUtils();
            tag = nbt.copyTag(tag);
            nbt.setTag(tag, "WorldUUIDMost", nbt.getTag(currentTag, "WorldUUIDMost"));
            nbt.setTag(tag, "WorldUUIDLeast", nbt.getTag(currentTag, "WorldUUIDLeast"));
            nbt.setTag(tag, "UUID", nbt.getTag(currentTag, "UUID"));
            nbt.setTag(tag, "Pos", nbt.getTag(currentTag, "Pos"));
        }
        platform.getCompatibilityUtils().setEntityData(entity, tag);
    }

    @Override
    public SpawnedEntityExtraData spawn(EntityType entityType, Location location) {
        Entity entity = platform.getMobUtils().spawnWithData(entityType, location, tag);
        if (entity == null) {
            return null;
        }
        return new SpawnedEntityExtraData(entity, false);
    }
}
