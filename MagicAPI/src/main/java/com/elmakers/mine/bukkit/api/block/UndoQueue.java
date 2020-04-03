package com.elmakers.mine.bukkit.api.block;

import java.util.Collection;
import javax.annotation.Nullable;

import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.api.data.UndoData;

public interface UndoQueue {
    void add(UndoList blocks);
    @Nullable
    UndoList getLast();
    @Nullable
    UndoList getLast(Block target);

    /**
     * Undo a recent construction performed by this Mage.
     *
     * <p>This will restore anything changed by the last-cast
     * construction spell, and remove that construction from
     * the Mage's UndoQueue.
     *
     * <p>It will skip undoing if the UndoList is older than
     * the specified timeout.
     *
     * @param timeout The maximum age of the change
     * @return The UndoList that was undone, or null if none.
     */
    @Nullable
    UndoList undoRecent(int timeout);
    @Nullable
    UndoList undoRecent(int timeout, String spellKey);

    /**
     * Undo a recent construction performed by this Mage against the
     * given Block
     *
     * <p>This will restore anything changed by the last-cast
     * construction spell by this Mage that targeted the specific Block,
     * even if it was not the most recent Spell cast by that Mage.
     *
     * <p>It will skip undoing if the UndoList is older than
     * the specified timeout.
     *
     * @param block The block to check for modifications.
     * @param timeout The maximum age of the change
     * @return The UndoList that was undone, or null if the Mage has no constructions for the given Block.
     */
    @Nullable
    UndoList undoRecent(Block block, int timeout);

    /**
     * Immediately undo any scheduled undo tasks.
     *
     * @return The number of tasks (spells) undone.
     */
    int undoScheduled();

    /**
     * Get the current size of the queue.
     *
     * @return The queue depth.
     */
    int getSize();

    boolean isEmpty();

    boolean commit();

    /**
     * Save the data in this undo queue.
     */
    void save(UndoData data);

    /**
     * Load the data in this undo queue.
     */
    void load(UndoData data);

    /**
     * Check to see if there are any scheduled undo changes pending in this queue.
     */
    boolean hasScheduled();

    Collection<UndoList> getAll();
}
