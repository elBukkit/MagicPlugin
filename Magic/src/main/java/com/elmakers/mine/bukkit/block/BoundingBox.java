package com.elmakers.mine.bukkit.block;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;

/**
 * Represents a BoundingBox, using two BlockVectors
 */
public class BoundingBox implements com.elmakers.mine.bukkit.api.block.BoundingBox
{
    protected static final Vector UNIT_VECTOR = new Vector(1, 1, 1);
    protected BlockVector max;
    protected BlockVector min;

    /**
     * Default constructor, used by Persistence to initialize instances.
     *
     * @deprecated Persistence use only.
     */
    @Deprecated
    public BoundingBox() {
    }

    public BoundingBox(Vector p1, Vector p2)
    {
        min = new BlockVector(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()), Math.min(p1.getZ(), p2.getZ()));
        max = new BlockVector(Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()), Math.max(p1.getZ(), p2.getZ()));
    }

    /**
     * Create a new Bounding box from two BlockVectors.
     *
     * <p>The vectors will be referenced, not copied.
     *
     * @param min
     *            The minimum corner
     * @param max
     *            The maximum corner
     */
    public BoundingBox(BlockVector min, BlockVector max)
    {
        this.min = min;
        this.max = max;
    }

    /**
     * Create a new BoundingBox giving min/max dimentions
     *
     * @param minX
     *            The minimum X value
     * @param minY
     *            The minimum Y value
     * @param minZ
     *            The minimum Z value
     * @param maxX
     *            The maximum X value
     * @param maxY
     *            The maximum Y value
     * @param maxZ
     *            The maximum Z value
     */
    public BoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        min = new BlockVector(Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ));
        max = new BlockVector(Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ));
    }

    /**
     * Expand this BoundingBox (if necessary) to contain the new point.
     *
     * @param newPoint
     *            the point to contain
     * @return This BB
     */
    public BoundingBox contain(Vector newPoint)
    {
        if (contains(newPoint))
        {
            return this;
        }

        BlockVector newMin = new BlockVector(Vector.getMinimum(min, newPoint));
        BlockVector newMax = new BlockVector(Vector.getMaximum(max, newPoint));
        min = newMin;
        max = newMax;

        return this;
    }

    /**
     * Check to see if this BB contains a point
     *
     * @param p
     *            The point to check for
     * @return true if this BB contains p
     */
    @Override
    public boolean contains(Vector p)
    {
        return p.isInAABB(min, max);
    }


    /**
     * Check to see if this BB contains a point
     *
     * @param p
     *            The point to check for
     * @param threshold a threshold range, for a fuzzy check
     * @return true if this BB contains p
     */
    @Override
    public boolean contains(Vector p, int threshold)
    {
        if (threshold == 0) return contains(p);
        Vector adjustedMin = UNIT_VECTOR.clone().multiply(-threshold).add(min);
        Vector adjustedMax = UNIT_VECTOR.clone().multiply(threshold).add(max);
        return p.isInAABB(adjustedMin, adjustedMax);
    }

    /**
     * Fill this BB with a specified material, using the specified World.
     *
     * <p>This function respects a MaterialList to determine which blocks are ok to
     * replace.
     *
     * <p>It also returns any blocks placed in the "affected" UndoList.
     *
     * @param world
     *            The world to fill
     * @param material
     *            The material to fill with
     * @param destructable
     *            A MaterialList describing which blocks are okay to replace
     * @param affected
     *            A BlockList, which will be filled with the blocks that are
     *            replaced
     */
    public void fill(
            World world, Material material,
            @Nullable MaterialSet destructable, @Nullable UndoList affected)
    {
        for (int x = min.getBlockX(); x < max.getBlockX(); x++)
        {
            for (int y = min.getBlockY(); y < max.getBlockY(); y++)
            {
                for (int z = min.getBlockZ(); z < max.getBlockZ(); z++)
                {
                    Block block = world.getBlockAt(x, y, z);

                    if (destructable == null)
                    {
                        if (affected != null)
                        {
                            affected.add(block);
                        }
                        block.setType(material);
                    }
                    else
                    {
                        if (destructable.testBlock(block))
                        {
                            if (affected != null)
                            {
                                affected.add(block);
                            }
                            block.setType(material);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get the center of this BB
     *
     * @return a new BlockVector representing the center of this BB
     */
    @Override
    public BlockVector getCenter()
    {
        Vector center = new Vector(min.getX(), min.getY(), min.getZ());
        center = center.getMidpoint(max);

        return new BlockVector(center);
    }

    /**
     * Retrieve the maximum corner of this BoundingBox
     *
     * @return The maximum corner
     */
    @Override
    public BlockVector getMax()
    {
        return max;
    }

    /**
     * Retrieve the minimum corner of this BoundingBox
     *
     * @return The minimum corner
     */
    @Override
    public BlockVector getMin()
    {
        return min;
    }

    /**
     * Set the maximum corner of this BoundingBox
     *
     * @param max
     *            The new maximum corner
     */
    public void setMax(BlockVector max)
    {
        this.max = max;
    }

    /**
     * Set the minimum corner of this BoundingBox
     *
     * @param min
     *            The new minimum corner
     */
    public void setMin(BlockVector min)
    {
        this.min = min;
    }
}
