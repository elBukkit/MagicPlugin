package com.elmakers.mine.bukkit.world.tasks;

import org.bukkit.World;

import com.elmakers.mine.bukkit.world.MagicWorld;

public class CopyWorldTask implements Runnable {
    private final MagicWorld world;
    private final World sourceWorld;

    public CopyWorldTask(MagicWorld world, World sourceWorld) {
        this.world = world;
        this.sourceWorld = sourceWorld;
    }

    @Override
    public void run() {
        world.copyWorld(sourceWorld);
    }
}
