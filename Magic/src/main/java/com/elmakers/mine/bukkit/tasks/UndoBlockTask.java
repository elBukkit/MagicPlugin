package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.api.block.BlockData;

public class UndoBlockTask implements Runnable {
    private final BlockData block;

    public UndoBlockTask(BlockData block) {
        this.block = block;
    }

    @Override
    public void run() {
        block.undo();
    }
}
