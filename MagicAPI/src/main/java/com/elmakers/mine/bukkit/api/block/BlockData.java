package com.elmakers.mine.bukkit.api.block;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.api.magic.MaterialSet;

/**
 * Stores a cached Block. Stores the coordinates and world, but will look up a block reference on demand.
 * 
 * This also stores the block state using the MaterialAndData structure as a base, and can be
 * used to restore a previously stored state.
 * 
 * In addition, BlockData instances can be linked to each other for layered undo queues that work
 * even when undone out of order.
 * 
 */
public interface BlockData extends MaterialAndData {
    long getId();
    World getWorld();
    String getWorldName();
    Block getBlock();
    BlockVector getPosition();
    void restore();
    void restore(boolean applyPhysics);
    void commit();
    boolean undo();
    boolean undo(boolean applyPhysics);
    boolean undo(ModifyType modifyType);
    boolean isDifferent();

    void unlink();
    BlockData getNextState();
    void setNextState(BlockData next);
    BlockData getPriorState();
    void setPriorState(BlockData prior);
    UndoList getUndoList();
    void setUndoList(UndoList undoList);
    BlockVector getLocation();
    @Deprecated
    boolean containsAny(Set<Material> materials);
    boolean containsAny(MaterialSet materials);
}
