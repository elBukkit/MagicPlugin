package com.elmakers.mine.bukkit.blocks;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;


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

	protected static Server server;
	
	// Transient
	protected Block     block;
	protected BlockData	nextState;
	protected BlockData	priorState;

	// Persistent
	protected BlockVector  location;
	protected String       world;
	
	public static void setServer(Server server) {
		BlockData.server = server;
	}

	public static long getBlockId(Block block)
	{
		return getBlockId(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
	}
	
	public static long getBlockId(String world, int x, int y, int z)
	{
		// Long is 63 bits
		// 15 sets of F's (4-bits)
		// world gets 4 bits
	    // y gets 8 bits
		// and x and z get 24 bits each
		return ((world.hashCode() & 0xF) << 56)
			| (((long)x & 0xFFFFFF) << 32) 
			| (((long)z & 0xFFFFFF) << 8) 
			| ((long)y & 0xFF);
	}
	
	public long getId()
	{
		return getBlockId(world, location.getBlockX(), location.getBlockY(), location.getBlockZ());
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
		super(block);
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
	
	public BlockData(ConfigurationNode node) {
		this(node.getLocation("location"), node.getMaterial("material"), (byte)node.getInt("data", 0));
	}
	
	public void save(ConfigurationNode node) {
		node.setProperty("material", material);
		node.setProperty("data", data);
		Location location = new Location(Bukkit.getWorld(world), this.location.getX(), this.location.getY(), this.location.getZ());
		node.setProperty("location", location);
	}

	protected boolean checkBlock()
	{
		if (block == null)
		{
			block = getBlock();
		}

		return block != null;
	}

	public World getWorld()
	{
		return server.getWorld(world);
	}
	
	public Block getBlock()
	{
		if (block == null && location != null && server != null)
		{
			if (world != null) {
				World world = getWorld();
				block = world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
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

	protected boolean undo()
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
		
		if (priorState != null) {
			priorState.setNextState(nextState);
		}
		if (nextState != null) {
			nextState.setPriorState(priorState);
			nextState.updateFrom(this);
		}

		return true;
	}
	
	protected void commit()
	{
		if (nextState != null) {
			nextState.setPriorState(null);
			nextState.updateFrom(block);
		}
		
		if (priorState != null) {
			// Very important for recursion!
			priorState.setNextState(null);
			priorState.commit();
		}
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
	
	public BlockVector getLocation() {
		return location;
	}
	
	public BlockData getNextState() {
		return nextState;
	}
	
	public void setNextState(BlockData next) {
		nextState = next;
	}
	
	public BlockData getPriorState() {
		return priorState;
	}
	
	public void setPriorState(BlockData prior) {
		priorState = prior;
	}
	
	public void restore() {
		modify(getBlock());
	}
}
