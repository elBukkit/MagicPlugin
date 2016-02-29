package com.elmakers.mine.bukkit.api.event;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDeathEvent;

public class MagicMobDeathEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private EntityDeathEvent deathEvent;
    private EntityData entityData;
    
    public MagicMobDeathEvent(EntityData entityData, EntityDeathEvent deathEvent) {
        this.entityData =entityData;
        this.deathEvent = deathEvent;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public EntityDeathEvent getDeathEvent() {
        return deathEvent;
    }

    public EntityData getEntityData() {
        return entityData;
    }
}
