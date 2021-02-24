package com.elmakers.mine.bukkit.api.block;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Chunk;
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

    /**
     * Behavior is undefined if list covers more than one world
     */
    @Deprecated
    @Nullable
    String getWorldName();

    /**
     * Behavior is undefined if list covers more than one world
     */
    @Deprecated
    BoundingBox getArea();
    Map<String, ? extends BoundingBox> getAreas();
    boolean add(Block block);

    /**
     * This method will assume the list only covers one world
     */
    @Deprecated
    void contain(Vector vector);
    boolean contain(BlockData block);
    boolean contains(Block block);
    @Nonnull
    Set<Chunk> getChunks();
}
