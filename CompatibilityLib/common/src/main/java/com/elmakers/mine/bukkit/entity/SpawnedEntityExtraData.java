package com.elmakers.mine.bukkit.entity;

import org.bukkit.entity.Entity;

public class SpawnedEntityExtraData {
    private final Entity entity;
    private final boolean addedToWorld;

    public SpawnedEntityExtraData(Entity entity, boolean addedToWorld) {
        this.entity = entity;
        this.addedToWorld = addedToWorld;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean isAddedToWorld() {
        return addedToWorld;
    }
}
