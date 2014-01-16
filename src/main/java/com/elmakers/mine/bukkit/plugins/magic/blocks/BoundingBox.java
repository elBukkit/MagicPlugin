package com.elmakers.mine.bukkit.plugins.magic.blocks;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

/**
 * Represents a BoundingBox, using two BlockVectors
 * 
 * @author NathanWolf
 * 
 */
public class BoundingBox
{
	protected BlockVector max;

	protected BlockVector min;

	/**
	 * Default constructor, used by Persistence to initialize instances.
	 */
	public BoundingBox()
	{

	}

	/**
	 * Create a new Bounding box from two BlockVectors.
	 * 
	 * The vectors will be referenced, not copied.
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
	 * TODO : Returns a copy of this BoundingBox, centerd around the target
	 * point
	 * 
	 * @param newCenter
	 *            The new center for this BB
	 * @return A new BoundingBox representing this BB translated to newCenter
	 */
	public BoundingBox centered(BlockVector newCenter)
	{
		// TODO
		// BlockVector currentCenter = getCenter();
		return this;
	}

	/**
	 * Expand this BoundingBox (if necessary) to contain the new point.
	 * 
	 * @param newPoint
	 *            the point to contain
	 * @return This BB, if it already contains newPoint, else a new BB
	 */
	public BoundingBox contain(BlockVector newPoint)
	{
		if (contains(newPoint))
		{
			return this;
		}

		BlockVector newMin = new BlockVector(Vector.getMinimum(min, newPoint));
		BlockVector newMax = new BlockVector(Vector.getMaximum(max, newPoint));
		BoundingBox newBB = new BoundingBox(newMin, newMax);
		return newBB;
	}

	/**
	 * Check to see if this BB contains a point
	 * 
	 * @param p
	 *            The point to check for
	 * @return true if this BB contains p
	 */
	public boolean contains(BlockVector p)
	{
		return p.isInAABB(min, max);
	}

	/**
	 * Fill this BB with a specified material, using the specified World
	 * 
	 * @param world
	 *            The world to fill
	 * @param material
	 *            The material to fill with
	 */
	public void fill(World world, Material material)
	{
		fill(world, material, null, null);
	}

	/**
	 * Fill this BB with a specified material, using the specified World.
	 * 
	 * This function respects a MaterialList to determine which blocks are ok to
	 * replace.
	 * 
	 * Note that the HashMap will become a MaterialList soon!
	 * 
	 * @param world
	 *            The world to fill
	 * @param material
	 *            The material to fill with
	 * @param destructable
	 *            A MaterialList describing which blocks are okay to replace
	 */
	public void fill(World world, Material material, MaterialList destructable)
	{
		fill(world, material, destructable, null);
	}

	/**
	 * Fill this BB with a specified material, using the specified World.
	 * 
	 * This function respects a MaterialList to determine which blocks are ok to
	 * replace.
	 * 
	 * It also returns any blocks placed in "blocks", which will become a
	 * BlockList eventually.
	 * 
	 * Note that the HashMap will become a MaterialList soon!
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
	public void fill(World world, Material material, MaterialList destructable, BlockList affected)
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
						Material blockType = block.getType();
						if (destructable.contains(blockType))
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
	 * Fill a BlockList with blocks from the BoundingBox, given the specified
	 * World.
	 * 
	 * Chunks must be loaded first!
	 * 
	 * Note: the List will become a BlockList eventually.
	 * 
	 * @param world
	 *            The world to fetch blocks from
	 * @param blocks
	 *            A BlockList to fill with blocks from world
	 */
	public void getBlocks(World world, List<Block> blocks)
	{
		for (int x = min.getBlockX(); x < max.getBlockX(); x++)
		{
			for (int y = min.getBlockY(); y < max.getBlockY(); y++)
			{
				for (int z = min.getBlockZ(); z < max.getBlockZ(); z++)
				{
					Block block = world.getBlockAt(x, y, z);
					blocks.add(block);
				}
			}
		}
	}

	/**
	 * Get the center of this BB
	 * 
	 * @return a new BlockVector representing the center of this BB
	 */
	public BlockVector getCenter()
	{
		Vector center = new Vector(min.getX(), min.getY(), min.getZ());
		center = center.getMidpoint(max);

		return new BlockVector(center);
	}

	/**
	 * Return a (width 1) "face" of this BoundingBox.
	 * 
	 * A "face" represents the side of this BB as given by "face".
	 * 
	 * This can be used for defining a wall, floor, or ceiling for a volume.
	 * 
	 * @param face
	 *            The BlockFace used to represent the face of this BB we want
	 * @return A new BB representing the specified face of the current BB
	 * @see #getFace(BlockFace, int, int)
	 */
	public BoundingBox getFace(BlockFace face)
	{
		return getFace(face, 1, 0);
	}

	/**
	 * Return a "face" of a BoundingBox
	 * 
	 * A "face" represents the side of this BB as given by "face".
	 * 
	 * This can be used for defining a wall, floor, or ceiling for a volume.
	 * 
	 * @param face
	 *            face The BlockFace used to represent the face of this BB we
	 *            want
	 * @param thickness
	 *            How thick to make the new BB
	 * @param offset
	 *            The offset (from the outside of this BB) to move the resultant
	 *            BB
	 * @return A new BB representing the specified face of the current BB, at
	 *         the specified offset and with the specified thickness
	 * @see #getFace(BlockFace)
	 */
	public BoundingBox getFace(BlockFace face, int thickness, int offset)
	{
		// Brute-force this for now. There's probably a Matrix-y way to do this!
		switch (face)
		{
		case UP:
			return new BoundingBox(min.getBlockX(), max.getBlockY() + offset, min.getBlockZ(), max.getBlockX(), max.getBlockY() + offset + thickness, max.getBlockZ());
		case DOWN:
			return new BoundingBox(min.getBlockX(), min.getBlockY() - offset - thickness, min.getBlockZ(), max.getBlockX(), min.getBlockY() - offset, max.getBlockZ());
		case WEST:
			return new BoundingBox(min.getBlockX(), min.getBlockY(), max.getBlockZ() + offset, max.getBlockX(), max.getBlockY(), max.getBlockZ() + offset + thickness);
		case EAST:
			return new BoundingBox(min.getBlockX(), min.getBlockY(), min.getBlockZ() - offset - thickness, max.getBlockX(), max.getBlockY(), min.getBlockZ() - offset);
		case SOUTH:
			return new BoundingBox(max.getBlockX() + offset, min.getBlockY(), min.getBlockZ(), max.getBlockX() + offset + thickness, max.getBlockY(), max.getBlockZ());
		case NORTH:
			return new BoundingBox(min.getBlockX() - offset - thickness, min.getBlockY(), min.getBlockZ(), min.getBlockX() - offset, max.getBlockY(), max.getBlockZ());
		default:
			return null;
		}
	}

	/**
	 * Retrieve the maximum corner of this BoundingBox
	 * 
	 * @return The maximum corner
	 */
	public BlockVector getMax()
	{
		return max;
	}

	/**
	 * Retrieve the minimum corner of this BoundingBox
	 * 
	 * @return The minimum corner
	 */
	public BlockVector getMin()
	{
		return min;
	}

	/**
	 * Get the X-size of this BoundingBox
	 * 
	 * @return The size of this BB
	 */
	public int getSizeX()
	{
		return max.getBlockX() - min.getBlockX();
	}

	/**
	 * Get the Y-size of this BoundingBox
	 * 
	 * @return The size of this BB
	 */
	public int getSizeY()
	{
		return max.getBlockY() - min.getBlockY();
	}

	/**
	 * Get the Z-size of this BoundingBox
	 * 
	 * @return The size of this BB
	 */
	public int getSizeZ()
	{
		return max.getBlockZ() - min.getBlockZ();
	}

	/**
	 * TODO: Scale this BB by the specified amount
	 * 
	 * @param scale
	 *            The percent amount to scale this BB
	 * @return A new BB, representing this BB scaled by scale
	 */
	public BoundingBox scale(double scale)
	{
		/*
		 * minY = 0; maxY = 128; minX = location.getBlockX() -
		 * PortalArea.defaultSize * ratio / 2; maxX = location.getBlockX() +
		 * PortalArea.defaultSize * ratio / 2; minZ = location.getBlockZ() -
		 * PortalArea.defaultSize * ratio / 2; maxZ = location.getBlockZ() +
		 * PortalArea.defaultSize * ratio / 2;.
		 */

		return new BoundingBox(min, max);
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

	/**
	 * Translate this bounding box in a specific direction
	 * 
	 * @param direction
	 *            The direction to move this BB
	 * @return A new BB representing this BB translated by direction
	 */
	public BoundingBox translate(BlockVector direction)
	{
		// TODO
		return this;
	}

}
