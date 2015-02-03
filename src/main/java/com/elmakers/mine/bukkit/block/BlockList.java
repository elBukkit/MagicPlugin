package com.elmakers.mine.bukkit.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.BlockData;

public class BlockList implements com.elmakers.mine.bukkit.api.block.BlockList {

    protected BoundingBox          		area;
    protected String					worldName;

    protected LinkedList<BlockData> 	blockList;
    protected HashSet<Long>        		blockIdMap;

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
            return true;
        }
        BlockData newBlock = new com.elmakers.mine.bukkit.block.BlockData(block);
        return add(newBlock);
    }

    public boolean add(BlockData blockData)
    {
        // First do a sanity check with the map
        // Currently, we don't replace blocks!
        if (contains(blockData)) return true;

        // Check the world name
        if (worldName != null && !worldName.equals(blockData.getWorldName())) return false;

        // Set a world name if this block list doesn't have one yet
        if (worldName == null || worldName.length() == 0) worldName = blockData.getWorldName();

        if (blockIdMap == null)
        {
            blockIdMap = new HashSet<Long>();
        }

        if (blockList == null)
        {
            blockList = new LinkedList<BlockData>();
        }
        BlockVector blockLocation = blockData.getPosition();
        contain(blockLocation);

        blockIdMap.add(blockData.getId());
        blockList.addLast(blockData);
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
        if (blockList == null)
        {
            return;
        }
        blockList.clear();
    }

    @Override
    public boolean contains(Block block)
    {
        if (blockIdMap == null) return false;
        return blockIdMap.contains(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block));
    }

    public boolean contains(BlockData blockData)
    {
        if (blockIdMap == null || blockData == null)
        {
            return false;
        }

        return blockIdMap.contains(blockData.getId());
    }

    public boolean contains(Object arg0)
    {
        if (arg0 instanceof Block) {
            return contains((Block)arg0);
        }
        if (arg0 instanceof BlockData) {
            return contains((BlockData)arg0);
        }
        // Fall back to map
        return blockIdMap == null ? false : blockIdMap.contains(arg0);
    }

    public boolean containsAll(Collection<?> arg0)
    {
        if (blockIdMap == null)
        {
            return false;
        }
        return blockIdMap.containsAll(arg0);
    }

    // Collection interface- would be great if I could just extend HashSet and
    // have this "just work"

    // For now, this is here to keep the map up to date, and to pass through to
    // the blockList.

    public BoundingBox getArea()
    {
        return area;
    }

    public Collection<BlockData> getBlockList()
    {
        return blockList;
    }

    public int size()
    {
        return blockList == null ? 0 :blockList.size();
    }

    public boolean isEmpty()
    {
        return blockList == null || blockList.isEmpty();
    }

    public Iterator<BlockData> iterator()
    {
        if (blockList == null)
        {
            return Collections.<BlockData>emptyList().iterator();
        }
        return blockList.iterator();
    }

    public boolean remove(Object arg0)
    {
        // Note that we never shrink the BB!
        if (blockList == null)
        {
            return false;
        }
        return blockList.remove(arg0);
    }

    public boolean removeAll(Collection<?> arg0)
    {
        if (blockList == null)
        {
            return false;
        }
        return blockList.removeAll(arg0);
    }

    public boolean retainAll(Collection<?> arg0)
    {
        if (blockList == null)
        {
            return false;
        }
        return blockList.retainAll(arg0);
    }

    public void setArea(BoundingBox area)
    {
        this.area = area;
    }

    public void setBlockList(Collection<BlockData> blockList)
    {
        this.blockList = null;
        if (blockList != null)
        {
            this.blockList = new LinkedList<BlockData>(blockList);
            blockIdMap = new HashSet<Long>();
            for (BlockData block : blockList)
            {
                blockIdMap.add(block.getId());
            }
        }
    }

    public BlockData get(int index)
    {
        if (blockList == null || index >= blockList.size())
        {
            return null;
        }
        return blockList.get(index);
    }

    public Object[] toArray()
    {
        if (blockList == null)
        {
            return null;
        }
        return blockList.toArray();
    }

    public <T> T[] toArray(T[] arg0)
    {
        if (blockList == null)
        {
            return null;
        }
        return blockList.toArray(arg0);
    }

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

    public void save(ConfigurationSection node) {
        node.set("world", worldName);
        List<String> blockData = new ArrayList<String>();
        if (blockList != null) {
            for (BlockData block : blockList) {
                blockData.add(block.toString());
            }
            node.set("blocks", blockData);
        }
    }

    public String getWorldName() {
        return worldName;
    }
}
