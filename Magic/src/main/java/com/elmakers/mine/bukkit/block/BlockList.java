package com.elmakers.mine.bukkit.block;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.BlockData;

public class BlockList implements com.elmakers.mine.bukkit.api.block.BlockList {

    protected BoundingBox               area;
    protected @Nullable String          worldName;

    protected final Deque<BlockData>    blockList = new ArrayDeque<>();
    protected final HashSet<Long>       blockIdMap = new HashSet<>();

    public BlockList()
    {

    }

    public BlockList(BlockList other)
    {
        this.worldName = other.worldName;
        for (BlockData block : other)
        {
            BlockData newBlock = new com.elmakers.mine.bukkit.block.BlockData(block);
            add(newBlock);
        }
    }

    @Override
    public boolean add(Block block)
    {
        if (contains(block))
        {
            return false;
        }
        BlockData newBlock = new com.elmakers.mine.bukkit.block.BlockData(block);
        return add(newBlock);
    }

    @Override
    public boolean add(BlockData blockData)
    {
        if (!contain(blockData))
        {
            return false;
        }

        synchronized (blockList) {
            blockIdMap.add(blockData.getId());
            blockList.addLast(blockData);
        }
        return true;
    }

    public boolean contain(BlockData blockData)
    {
        // First do a sanity check with the map
        // Currently, we don't replace blocks!
        if (contains(blockData)) return false;

        // Check the world name
        if (worldName != null && !worldName.equals(blockData.getWorldName())) return false;

        // Set a world name if this block list doesn't have one yet
        if (worldName == null || worldName.length() == 0) worldName = blockData.getWorldName();

        BlockVector blockLocation = blockData.getPosition();
        contain(blockLocation);

        return true;
    }

    @Override
    public void contain(Vector vector)
    {
        if (area == null)
        {
            area = new BoundingBox(vector, vector);
        }
        else
        {
            area.contain(vector);
        }
    }

    @Override
    public boolean addAll(Collection<? extends BlockData> blocks)
    {
        // Iterate to maintain BB area
        boolean added = true;
        for (BlockData block : blocks)
        {
            added = added && add(block);
        }
        return added;
    }

    @Override
    public void clear()
    {
        synchronized (blockList) {
            blockIdMap.clear();
            blockList.clear();
        }
    }

    @Override
    public boolean contains(Block block)
    {
        return blockIdMap.contains(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block));
    }

    public boolean contains(BlockData blockData)
    {
        return blockIdMap.contains(blockData.getId());
    }

    @Override
    public boolean contains(Object arg0)
    {
        if (arg0 instanceof Block) {
            return contains((Block)arg0);
        }
        if (arg0 instanceof BlockData) {
            return contains((BlockData)arg0);
        }
        // Fall back to map
        return blockIdMap.contains(arg0);
    }

    @Override
    public boolean containsAll(Collection<?> arg0)
    {
        return blockIdMap.containsAll(arg0);
    }

    // Collection interface- would be great if I could just extend HashSet and
    // have this "just work"

    // For now, this is here to keep the map up to date, and to pass through to
    // the blockList.

    @Override
    public BoundingBox getArea()
    {
        return area;
    }

    public Collection<BlockData> getBlockList()
    {
        return blockList;
    }

    @Override
    public int size()
    {
        return blockList.size();
    }

    @Override
    public boolean isEmpty()
    {
        return blockList.isEmpty();
    }

    @Override
    public Iterator<BlockData> iterator()
    {
        return blockList.iterator();
    }

    @Override
    public boolean remove(Object removeObject)
    {
        // Note that we never shrink the BB!
        if (removeObject instanceof BlockData)
        {
            blockIdMap.remove(((BlockData)removeObject).getId());
        }
        return blockList.remove(removeObject);
    }

    @Override
    public boolean removeAll(Collection<?> removeCollection)
    {
        for (Object removeObject : removeCollection)
        {
            if (removeObject instanceof BlockData)
            {
                blockIdMap.remove(((BlockData)removeObject).getId());
            }
        }
        return blockList.removeAll(removeCollection);
    }

    @Override
    public boolean retainAll(Collection<?> arg0)
    {
        return blockList.retainAll(arg0);
    }

    public void setArea(BoundingBox area)
    {
        this.area = area;
    }

    @Override
    @Nullable
    public Object[] toArray() {
        return blockList.toArray();
    }

    @Override
    @Nullable
    public <T> T[] toArray(T[] arg0) {
        return blockList.toArray(arg0);
    }

    @Override
    public void load(ConfigurationSection node) {
        worldName = node.getString("world");
        List<String> blockData = node.getStringList("blocks");
        if (blockData != null) {
            for (String blockString : blockData) {
                BlockData deserialized = com.elmakers.mine.bukkit.block.BlockData.fromString(blockString);
                if (worldName == null) worldName = deserialized.getWorldName();
                add(deserialized);
            }
        }
    }

    @Override
    public void save(ConfigurationSection node) {
        node.set("world", worldName);
        synchronized (blockList) {
            if (!blockList.isEmpty()) {
                List<String> blockData = new ArrayList<>();
                    for (BlockData block : blockList) {
                        blockData.add(block.toString());
                    }
                node.set("blocks", blockData);
            }
        }
    }

    @Override
    @Nullable
    public String getWorldName() {
        return worldName;
    }
}
