package com.elmakers.mine.bukkit.world.tasks;

import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.block.MaterialAndData;

public class ModifyBlockTask implements Runnable {
    private final Block block;
    private final MaterialAndData material;

    public ModifyBlockTask(Block block, MaterialAndData material) {
        this.block = block;
        this.material = material;
    }

    @Override
    public void run() {
        material.modify(block);
    }
}
