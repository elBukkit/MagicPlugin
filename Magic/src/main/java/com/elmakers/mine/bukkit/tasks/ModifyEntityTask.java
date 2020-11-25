package com.elmakers.mine.bukkit.tasks;

import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class ModifyEntityTask implements Runnable {
    private final MageController controller;
    private final EntityData entityData;
    private final Entity entity;

    public ModifyEntityTask(MageController controller, EntityData entityData, Entity entity) {
        this.controller = controller;
        this.entity = entity;
        this.entityData = entityData;
    }

    @Override
    public void run() {
        entityData.modify(entity);
    }
}
