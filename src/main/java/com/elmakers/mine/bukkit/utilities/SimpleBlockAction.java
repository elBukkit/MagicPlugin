package com.elmakers.mine.bukkit.utilities;

import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.persistence.dao.BlockList;

public class SimpleBlockAction implements BlockAction
{
    protected BlockList blocks = null;
    
    public SimpleBlockAction()
    {
        blocks = new BlockList();
    }
    
    public boolean perform(Block block)
    {
        blocks.add(block);
        return true;
    }

    public BlockList getBlocks()
    {
        return blocks;
    }

}
