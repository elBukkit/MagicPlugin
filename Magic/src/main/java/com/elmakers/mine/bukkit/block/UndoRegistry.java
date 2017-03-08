package com.elmakers.mine.bukkit.block;

import com.elmakers.mine.bukkit.api.block.BlockData;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UndoRegistry {
    protected Map<Long, BlockData> modified = new HashMap<>();
    protected Map<Long, BlockData> watching = new HashMap<>();
    protected Map<Long, Double> reflective = new HashMap<>();
    protected Map<Long, Double> breakable = new HashMap<>();

    public BlockData registerModified(Block block)
    {
        BlockData blockData = new com.elmakers.mine.bukkit.block.BlockData(block);
        registerModified(blockData);
        return blockData;
    }

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

    public BlockData getBlockData(Location location) {
        long blockId = com.elmakers.mine.bukkit.block.BlockData.getBlockId(location.getBlock());
        BlockData modifiedBlock = modified.get(blockId);
        if (modifiedBlock != null) {
            return modifiedBlock;
        }
        BlockData watchedBlock = watching.get(blockId);
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

    public Double getReflective(Block block) {
        return block == null ? null : reflective.get(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block));
    }

    public Double getBreakable(Block block) {
        return block == null ? null : breakable.get(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block));
    }

    public void registerReflective(Block block, double amount) {
        if (block == null) return;
        reflective.put(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block), amount);
    }

    public void registerBreakable(Block block, double amount) {
        if (block == null) return;
        breakable.put(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block), amount);
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

    public Map<Long, Double> getReflective() {
        return reflective;
    }

    public Map<Long, Double> getBreakable() {
        return breakable;
    }
}
