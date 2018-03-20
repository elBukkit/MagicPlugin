package com.elmakers.mine.bukkit.block;

import com.elmakers.mine.bukkit.api.block.ModifyType;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.BlockVector;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

import java.lang.ref.WeakReference;
import java.util.Set;

/**
 * Stores a cached Block. Stores the coordinates and world, but will look up a block reference on demand.
 * 
 * This also stores the block state using the MaterialAndData structure as a base, and can be
 * used to restore a previously stored state.
 * 
 * In addition, BlockData instances can be linked to each other for layered undo queues that work
 * even when undone out of order.
 * 
 */
public class BlockData extends MaterialAndData implements com.elmakers.mine.bukkit.api.block.BlockData {
    public static final BlockFace[] FACES = new BlockFace[]{BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN};
    public static final BlockFace[] SIDES = new BlockFace[]{BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST};

    // Transient
    protected com.elmakers.mine.bukkit.api.block.BlockData nextState;
    protected com.elmakers.mine.bukkit.api.block.BlockData priorState;

    // Persistent
    protected BlockVector location;
    protected String worldName;

    // Used for UndoList lookups
    protected WeakReference<UndoList> undoList = null;

    public static long getBlockId(Block block) {
        return getBlockId(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    public static long getBlockId(String world, int x, int y, int z) {
        // Long is 63 bits
        // 15 sets of F's (4-bits)
        // world gets 4 bits
        // y gets 8 bits
        // and x and z get 24 bits each
        long worldHashCode = world == null ? 0 : world.hashCode();
        return ((worldHashCode & 0xF) << 56)
                | (((long) x & 0xFFFFFF) << 32)
                | (((long) z & 0xFFFFFF) << 8)
                | ((long) y & 0xFF);
    }

    @Override
    public int hashCode() {
        return (int) getId();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof BlockData) {
            return getId() == ((BlockData) other).getId();
        }
        return super.equals(other);
    }

    @Override
    public long getId() {
        if (location == null) return 0;
        return getBlockId(worldName, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static BlockFace getReverseFace(BlockFace blockFace) {
        switch (blockFace) {
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

    public BlockData() {
    }

    public BlockData(Block block) {
        super(block);
        location = new BlockVector(block.getX(), block.getY(), block.getZ());
        worldName = block.getWorld().getName();
    }

    public BlockData(com.elmakers.mine.bukkit.api.block.BlockData copy) {
        super(copy);
        location = copy.getPosition();
        worldName = copy.getWorldName();
    }

    public BlockData(Location location, Material material, byte data) {
        super(material, data);
        if (location != null && location.getWorld() != null) {
            this.location = new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            this.worldName = location.getWorld().getName();
        }
    }

    public BlockData(int x, int y, int z, String world, Material material, byte data) {
        super(material, data);
        this.location = new BlockVector(x, y, z);
        this.worldName = world;
    }

    public BlockData(ConfigurationSection node) {
        this(
                ConfigurationUtils.getLocation(node, "location"),
                ConfigurationUtils.getMaterial(node, "material"),
                (byte) node.getInt("data", 0)
        );
    }

    public void save(ConfigurationSection node) {
        if (worldName == null) return;
        node.set("material", ConfigurationUtils.fromMaterial(material));
        node.set("data", data);
        Location location = new Location(Bukkit.getWorld(worldName), this.location.getX(), this.location.getY(), this.location.getZ());
        node.set("location", ConfigurationUtils.fromLocation(location));
    }

    protected boolean checkBlock() {
        return getBlock() != null;
    }

    public void setPosition(BlockVector location) {
        this.location = location;
    }

    @Override
    public void unlink() {
        if (priorState != null) {
            priorState.setNextState(nextState);
        }
        if (nextState != null) {
            // Pass state up the chain
            nextState.updateFrom(this);
            nextState.setPriorState(priorState);
        }

        priorState = null;
        nextState = null;
    }

    @Override
    public boolean undo() {
        return undo(false);
    }

    @Override
    public boolean undo(boolean applyPhysics) {
        return undo(applyPhysics ? ModifyType.NORMAL : ModifyType.NO_PHYSICS);
    }

    @Override
    public boolean undo(ModifyType modifyType)
    {
        Block block = getBlock();
        if (block == null)
        {
            return true;
        }

        Chunk chunk = block.getChunk();
        if (!chunk.isLoaded())
        {
            chunk.load();
            return false;
        }

        // Don't undo if not the top of the stack
        // Otherwise, state will be pushed up in unlink
        if (nextState == null && isDifferent(block))
        {
            modify(block, modifyType);
        }
        unlink();

        return true;
    }

    @Override
    public void commit()
    {
        updateFrom(getBlock());
        if (nextState != null) {
            nextState.setPriorState(null);
            nextState.updateFrom(getBlock());
            nextState = null;
        }

        if (priorState != null) {
            // Very important for recursion!
            priorState.setNextState(null);

            // Cascade the commit downward, unlinking everything,
            // in case other BlockLists contain these records
            priorState.updateFrom(getBlock());
            priorState.commit();
            priorState = null;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public String toString() {
        return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + worldName + "|" + getMaterial().getId() + ":" + getData();
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

    @Override
    public com.elmakers.mine.bukkit.api.block.BlockData getNextState() {
        return nextState;
    }

    @Override
    public void setNextState(com.elmakers.mine.bukkit.api.block.BlockData next) {
        nextState = next;
    }

    @Override
    public com.elmakers.mine.bukkit.api.block.BlockData getPriorState() {
        return priorState;
    }

    @Override
    public void setPriorState(com.elmakers.mine.bukkit.api.block.BlockData prior) {
        priorState = prior;
    }

    @Override
    public void restore() {
        restore(false);
    }

    @Override
    public void restore(boolean applyPhysics) {
        modify(getBlock(), applyPhysics);
    }

    @Override
    public String getWorldName() {
        return worldName;
    }

    @Override
    public BlockVector getPosition()
    {
        return location;
    }

    @Override
    public World getWorld()
    {
        if (worldName == null || worldName.length() == 0) return null;
        return Bukkit.getWorld(worldName);
    }

    @Override
    public Block getBlock()
    {
        Block block = null;
        if (location != null)
        {
            World world = getWorld();
            if (world != null) {
                block = world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            }
        }
        return block;
    }

    @Override
    public boolean isDifferent()
    {
        return isDifferent(getBlock());
    }

    @Override
    public UndoList getUndoList() {
        return undoList != null ? undoList.get() : null;
    }

    @Override
    public void setUndoList(UndoList list) {
        if (list == null) {
            undoList = null;
            return;
        }
        undoList = new WeakReference<>(list);
    }

    @Override
    public BlockVector getLocation() {
        return location;
    }

    @Override
    @Deprecated
    public boolean containsAny(Set<Material> materials) {
        if (materials.contains(material)) {
            return true;
        } else if (priorState != null) {
            return priorState.containsAny(materials);
        }

        return false;
    }

    @Override
    public boolean containsAny(MaterialSet materials) {
        if (materials.testMaterialAndData(this)) {
            return true;
        } else if (priorState != null) {
            return priorState.containsAny(materials);
        }

        return false;
    }
}
