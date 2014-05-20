package com.elmakers.mine.bukkit.api.block;

/**
 * Represents a batched Block update, usually started by a construction Spell.
 * 
 * Magic will process pending BlockBatch requests once every tick, up to a
 * maximum number of Block updates per tick (the default is 1,000).
 * 
 * Every BlockBatch is required to perform only as many updates as requested,
 * and to report how many updates were performed.
 * 
 * A BlockBatch must also report when it is finished, and perform any required
 * actions on finish, such as registering an UndoBatch for undo.
 */
public interface BlockBatch {
    /**
     * Process one iteration of this batch.
     *
     * Return the number of blocks processed.
     *
     * @param maxBlocks The maximum number of blocks the batch should process
     * @return The number of blocks processed.
     */
    public int process(int maxBlocks);

    /**
     * Whether or not this batch is finished
     *
     * @return true if finished
     */
    public boolean isFinished();

    /**
     * Immediatelly finish this batch.
     *
     * This may cancel any remaining operations, but should
     * clean up, add to undo queues, etc, whatever has been
     * done so far.
     */
    public void finish();

    /**
     * The size of this batch. May be in blocks, or some
     * other abstract unit.
     *
     * Can be used in conjunction with remaining() for a progress indicator.
     *
     * @return The size of this batch.
     */
    public int size();

    /**
     * The remaining size of this batch. May be in blocks, or some
     * other abstract unit.
     *
     * Can be used in conjunction with size() for a progress indicator.
     *
     * @return The remaining size of this batch.
     */
    public int remaining();
}
