package com.elmakers.mine.bukkit.api.block;

import java.util.Collection;
import javax.annotation.Nullable;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

/**
 * Implements a Collection of Blocks, for quick getting/putting while iterating
 * over a set or area of blocks.
 *
 * <p>A BlockList also tracks the BoundingBox that contains all of its blocks.
 *
 * <p>A BlockList should not contain blocks from more than one World in it.
 */
public interface BlockList extends Collection<BlockData> {
    void save(ConfigurationSection node);
    void load(ConfigurationSection node);
    @Nullable
    String getWorldName();
    BoundingBox getArea();
    boolean add(Block block);
    void contain(Vector vector);
    boolean contains(Block block);
}
