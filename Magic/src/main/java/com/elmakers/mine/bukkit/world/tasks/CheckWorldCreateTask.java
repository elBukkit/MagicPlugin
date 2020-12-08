package com.elmakers.mine.bukkit.world.tasks;

import com.elmakers.mine.bukkit.world.MagicWorld;

public class CheckWorldCreateTask implements Runnable {
    private final MagicWorld world;

    public CheckWorldCreateTask(MagicWorld world) {
        this.world = world;
    }

    @Override
    public void run() {
        world.checkWorldCreate();
    }
}
