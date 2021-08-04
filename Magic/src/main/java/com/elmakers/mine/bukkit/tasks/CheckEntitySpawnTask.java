package com.elmakers.mine.bukkit.tasks;

import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.magic.listener.MobController;

public class CheckEntitySpawnTask implements Runnable {
    private final MobController mobController;
    private final Entity entity;

    public CheckEntitySpawnTask(MobController mobController, Entity entity) {
        this.mobController = mobController;
        this.entity = entity;
    }

    @Override
    public void run() {
        if (!mobController.checkEntitySpawn(entity, false)) {
            mobController.checkDefaultSpawn(entity, false);
        }
    }
}
