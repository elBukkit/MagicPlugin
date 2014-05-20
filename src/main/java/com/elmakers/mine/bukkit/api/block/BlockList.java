package com.elmakers.mine.bukkit.api.block;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Implements a Collection of Blocks, for quick getting/putting while iterating
 * over a set or area of blocks.
 * 
 * A BlockList also tracks the BoundingBox that contains all of its blocks.
 * 
 * A BlockList should not contain blocks from more than one World in it.
 */
public interface BlockList extends Collection<BlockData> {
    public void save(ConfigurationSection node);
    public void load(ConfigurationSection node);
    public String getWorldName();
    public BoundingBox getArea();
}
