package com.elmakers.mine.bukkit.dao;

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
public class BlockData
{
	public static final BlockFace[] FACES = new BlockFace[] { BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN };
	public static final BlockFace[] SIDES = new BlockFace[] { BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST };

	// Transient
	protected Block     block;

	protected BlockVector  location;
	protected String       world;
	protected Material     material;
	protected byte         materialData;

	protected static Server server;
	
	public static void setServer(Server server) {
		BlockData.server = server;
	}

	public static long getBlockId(Block block)
	{
		return block.getWorld().getName().hashCode() << 28 ^ Integer.valueOf(block.getX()).hashCode() << 13 ^ Integer.valueOf(block.getY()).hashCode() << 7 ^ Integer.valueOf(block.getZ()).hashCode();
	}

	public static long getBlockId(BlockData blockData)
	{
		return getBlockId(blockData.getBlock());
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

	@SuppressWarnings("deprecation")
	public BlockData(Block block)
	{
		this.block = block;

		location = new BlockVector(block.getX(), block.getY(), block.getZ());
		world = block.getWorld().getName();
		material = block.getType();
		materialData = block.getData();
	}

	public BlockData(BlockData copy)
	{
		location = copy.location;
		world = copy.world;
		material = copy.material;
		materialData = copy.materialData;

		block = copy.block;
	}
	
	public BlockData(Location location, Material material, byte data)
	{
		this.location = new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		this.world = location.getWorld().getName();
		this.material = material;
		this.materialData = data;
	}
	
	public BlockData(int x, int y, int z, String world, Material material, byte data)
	{
		this.location = new BlockVector(x, y, z);
		this.world = world;
		this.material = material;
		this.materialData = data;
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

	public Material getMaterial()
	{
		return material;
	}

	public byte getMaterialData()
	{
		return materialData;
	}

	public void setPosition(BlockVector location)
	{
		this.location = location;
	}

	public void setMaterial(Material material)
	{
		this.material = material;
	}

	public void setMaterialData(byte materialData)
	{
		this.materialData = materialData;
	}

	@SuppressWarnings("deprecation")
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

		if (block.getType() != material || block.getData() != materialData)
		{
			block.setType(material);
			block.setData(materialData);
		}

		return true;
	}
	
	@SuppressWarnings("deprecation")
	public String toString() {
		return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + world + "|" + getMaterial().getId() + ":" + getMaterialData();
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
