package com.elmakers.mine.bukkit.api.batch;

import com.elmakers.mine.bukkit.api.block.UndoList;

public interface UndoBatch extends Batch {
    UndoList getUndoList();
}
