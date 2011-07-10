package com.elmakers.mine.bukkit.dao;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.utilities.BukkitServer;

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
    
    public BlockVector  location;
    public String       world;
    public Material     material;
    public byte         materialData;
    
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
        }

        return BlockFace.SELF;
    }

    public BlockData()
    {
    }

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
        if (block == null && location != null)
        {
            Location blockLocation = new Location(BukkitServer.getWorld(world), location.getBlockX(), location.getBlockY(), location.getBlockZ());
            if (blockLocation != null)
            {
                block = blockLocation.getWorld().getBlockAt(blockLocation);
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

    public boolean undo()
    {
        if (!checkBlock())
        {
            return false;
        }

        World world = block.getWorld();
        Chunk chunk = world.getChunkAt(block);
        if (!world.isChunkLoaded(chunk))
        {
            return false;
        }

        if (block.getType() != material || block.getData() != materialData)
        {
            block.setType(material);
            block.setData(materialData);
        }

        return true;
    }
}
