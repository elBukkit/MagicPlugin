package com.elmakers.mine.bukkit.block;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.api.block.BlockData;

public class UndoRegistry {
    protected Map<Long, BlockData> modified = new HashMap<>();
    protected Map<Long, BlockData> watching = new HashMap<>();
    protected Map<Long, Double> reflective = new HashMap<>();
    protected Map<Long, Double> breakable = new HashMap<>();
    protected Map<Long, Double> breaking = new HashMap<>();

    public void registerModified(BlockData blockData)
    {
        BlockData priorState = modified.get(blockData.getId());
        if (priorState != null)
        {
            priorState.setNextState(blockData);
            blockData.setPriorState(priorState);
        }

        modified.put(blockData.getId(), blockData);
    }

    public void registerWatched(BlockData blockData)
    {
        BlockData priorState = watching.get(blockData.getId());
        if (priorState != null)
        {
            priorState.setNextState(blockData);
            blockData.setPriorState(priorState);
        }

        watching.put(blockData.getId(), blockData);
    }

    public void commitAll()
    {
        Collection<BlockData> blocks = modified.values();
        modified.clear();
        watching.clear();
        for (BlockData block : blocks) {
            block.commit();
        }
    }

    public void commit(BlockData block)
    {
        BlockData currentState = modified.get(block.getId());
        if (currentState == block)
        {
            modified.remove(block.getId());
        }
        block.commit();
        reflective.remove(block.getId());
        breakable.remove(block.getId());
    }


    protected void removeFromModified(BlockData block)
    {
        removeFromModified(block, block.getPriorState());
        block.unlink();
    }

    protected void removeFromModified(BlockData block, BlockData priorState)
    {
        BlockData currentState = modified.get(block.getId());
        if (currentState == block)
        {
            if (priorState == null)
            {
                modified.remove(block.getId());
            }
            else
            {
                modified.put(block.getId(), priorState);
            }
        }
    }

    protected void removeFromWatched(BlockData block)
    {
        removeFromWatched(block, block.getPriorState());
        block.unlink();
    }

    protected void removeFromWatched(BlockData block, BlockData priorState)
    {
        BlockData currentState = watching.get(block.getId());
        if (currentState == block)
        {
            if (priorState == null)
            {
                watching.remove(block.getId());
            }
            else
            {
                watching.put(block.getId(), priorState);
            }
        }
    }

    public void removeBreakable(BlockData block) {
        breakable.remove(block.getId());
    }

    public void removeReflective(BlockData block) {
        reflective.remove(block.getId());
    }

    /**
     * Subtract some amount of damage
     * @param block The block to remove damage from.
     * @return The amount of damage remaining, or null if no damage was removed.
     */
    @Nullable
    public Double removeDamage(BlockData block) {
        double amount = block.getDamage();
        if (amount <= 0) return null;
        Double currentAmount = breaking.get(block.getId());
        if (currentAmount == null) return null;
        currentAmount -= amount;
        if (currentAmount <= 0) {
            removeBreaking(block);
            return 0.0;
        } else {
            breaking.put(block.getId(), currentAmount);
        }
        return currentAmount;

    }

    public Double removeBreaking(BlockData block) {
        return breaking.remove(block.getId());
    }

    public double registerBreaking(Block block, double addAmount) {
        if (block == null) return 0;
        long blockId = com.elmakers.mine.bukkit.block.BlockData.getBlockId(block);
        Double currentAmount = breaking.get(blockId);
        currentAmount = currentAmount == null ? addAmount : currentAmount + addAmount;
        currentAmount = Math.min(currentAmount, 1);
        breaking.put(blockId, currentAmount);
        return currentAmount;
    }

    @Nullable
    public BlockData getBlockData(Location location) {
        long blockId = com.elmakers.mine.bukkit.block.BlockData.getBlockId(location.getBlock());

        // Prefer to return blocks that are watched by lists which are going to auto-undo.
        BlockData watchedBlock = watching.get(blockId);
        if (watchedBlock != null && watchedBlock.getUndoList() != null && watchedBlock.getUndoList().isScheduled()) {
            return watchedBlock;
        }
        BlockData modifiedBlock = modified.get(blockId);
        if (modifiedBlock != null) {
            return modifiedBlock;
        }
        if (watchedBlock != null) {
            return watchedBlock;
        }

        return null;
    }

    public boolean isReflective(Block block) {
        return block != null && reflective.containsKey(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block));
    }

    public boolean isBreakable(Block block) {
        return block != null && breakable.containsKey(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block));
    }

    @Nullable
    public Double getReflective(Block block) {
        return block == null ? null : reflective.get(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block));
    }

    public Map<Long, Double> getReflective() {
        return reflective;
    }

    @Nullable
    public Double getBreakable(Block block) {
        return block == null ? null : breakable.get(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block));
    }

    public Map<Long, Double> getBreakable() {
        return breakable;
    }

    public void registerReflective(Block block, double amount) {
        if (block == null) return;
        reflective.put(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block), amount);
    }

    public void registerBreakable(Block block, double amount) {
        if (block == null) return;
        breakable.put(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block), amount);
    }

    public void unregisterBreaking(Block block) {
        if (block == null) return;
        breaking.remove(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block));
    }

    public void unregisterBreakable(Block block) {
        if (block == null) return;
        breakable.remove(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block));
    }

    public void unregisterReflective(Block block) {
        if (block == null) return;
        reflective.remove(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block));
    }

    public Map<Long, BlockData> getModified() {
        return modified;
    }

    public Map<Long, BlockData> getWatching() {
        return watching;
    }

    public Map<Long, Double> getBreaking() {
        return breaking;
    }

    public Double getBreaking(Block block) {
        return breaking.get(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block));
    }
}
