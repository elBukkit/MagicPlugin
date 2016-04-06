package com.elmakers.mine.bukkit.entity;

import org.bukkit.entity.Entity;

// TODO
public class EntityAreaEffectCloudData extends EntityExtraData {
    
    @Override
    public void apply(Entity entity) {

    }

    @Override
    public EntityExtraData clone() {
        EntityAreaEffectCloudData copy = new EntityAreaEffectCloudData();
        
        return copy;
    }
}
