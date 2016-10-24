package com.elmakers.mine.bukkit.api.data;

import com.elmakers.mine.bukkit.api.block.UndoList;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a placeholder for the eventuality of having a DAO
 * for UndoQueue data.
 *
 * The complexity in implementing this requires a stopgap solution.
 */
public class UndoData {
    private final List<UndoList> blockList;

    public UndoData() {
        this.blockList = new ArrayList<>();
    }

    public List<UndoList> getBlockList() {
        return blockList;
    }
}
