package com.elmakers.mine.bukkit.blocks;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;


/**
 * Stores a cached Block. Stores the coordinates and world, but will look up a block reference on demand.
 * 
 * @author NathanWolf
 *
 */
public class BlockData extends MaterialAndData
{
	public static final BlockFace[] FACES = new BlockFace[] { BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN };
	public static final BlockFace[] SIDES = new BlockFace[] { BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST };

	// Transient
	protected Block     block;

	protected BlockVector  location;
	protected String       world;

	protected static Server server;
	
	public static void setServer(Server server) {
		BlockData.server = server;
	}

	public static long getBlockId(Block block)
	{
		return block.getWorld().getName().hashCode() << 28 ^ Integer.valueOf(block.getX()).hashCode() << 13 ^ Integer.valueOf(block.getY()).hashCode() << 7 ^ Integer.valueOf(block.getZ()).hashCode();
	}
	
	public long getId()
	{
		return world.hashCode() << 28 ^ Integer.valueOf(location.getBlockX()).hashCode() << 13 ^ Integer.valueOf(location.getBlockY()).hashCode() << 7 ^ Integer.valueOf(location.getBlockZ()).hashCode();
	}

	public static BlockFace getReverseFace(BlockFace blockFace)
	{
		switch (blockFace)
		{
		case NORTH:
			return BlockFace.SOUTH;
		case WEST:
			return BlockFace.EAST;
		case SOUTH:
			return BlockFace.NORTH;
		case EAST:
			return BlockFace.WEST;
		case UP:
			return BlockFace.DOWN;
		case DOWN:
			return BlockFace.UP;
		default:
			return BlockFace.SELF;
		}
	}

	public BlockData()
	{
	}

	public BlockData(Block block)
	{
		updateFrom(block);
		this.block = block;

		location = new BlockVector(block.getX(), block.getY(), block.getZ());
		world = block.getWorld().getName();
	}

	public BlockData(BlockData copy)
	{
		super(copy);
		location = copy.location;
		world = copy.world;
		block = copy.block;
	}
	
	public BlockData(Location location, Material material, byte data)
	{
		super(material, data);
		this.location = new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		this.world = location.getWorld().getName();
	}
	
	public BlockData(int x, int y, int z, String world, Material material, byte data)
	{
		super(material, data);
		this.location = new BlockVector(x, y, z);
		this.world = world;
	}

	protected boolean checkBlock()
	{
		if (block == null)
		{
			block = getBlock();
		}

		return block != null;
	}

	public Block getBlock()
	{
		if (block == null && location != null && server != null)
		{
			Location blockLocation = new Location(server.getWorld(world), location.getBlockX(), location.getBlockY(), location.getBlockZ());
			if (blockLocation != null)
			{
				block = blockLocation.getBlock();
			}
		}
		return block;
	}

	public BlockVector getPosition()
	{
		return location;
	}

	public void setPosition(BlockVector location)
	{
		this.location = location;
	}

	public boolean undo()
	{
		if (!checkBlock())
		{
			return true;
		}

		Chunk chunk = block.getChunk();
		if (!chunk.isLoaded())
		{
			chunk.load();
			return false;
		}

		if (isDifferent(block))
		{
			modify(block);
		}

		return true;
	}
	
	@SuppressWarnings("deprecation")
	public String toString() {
		return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + world + "|" + getMaterial().getId() + ":" + getData();
	}
	
	@SuppressWarnings("deprecation")
	public static BlockData fromString(String s) {
		BlockData result = null;
		if (s == null) return null;
		try {
			String[] pieces = StringUtils.split(s, '|');
			String[] locationPieces = StringUtils.split(pieces[0], ',');
			int x = Integer.parseInt(locationPieces[0]);
			int y = Integer.parseInt(locationPieces[1]);
			int z = Integer.parseInt(locationPieces[2]);
			String world = locationPieces[3];
			String[] materialPieces = StringUtils.split(pieces[1], ':');
			int materialId = Integer.parseInt(materialPieces[0]);
			byte dataId = Byte.parseByte(materialPieces[1]);
			return new BlockData(x, y, z, world, Material.getMaterial(materialId), dataId);
		} catch(Exception ex) {
		}
		
		return result;
	}
	
	public String getWorldName() {
		return world;
	}
}
