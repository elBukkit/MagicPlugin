package com.elmakers.mine.bukkit.block;

import org.bukkit.block.Block;

/**
 * Represents the face of a block
 *
 * <p>This was copied from the Bukkit API to add
 * vertical diagonals.
 */
public enum BlockFace {
    NORTH(0, 0, -1),
    EAST(1, 0, 0),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    UP(0, 1, 0),
    DOWN(0, -1, 0),
    NORTH_EAST(NORTH, EAST),
    NORTH_WEST(NORTH, WEST),
    SOUTH_EAST(SOUTH, EAST),
    SOUTH_WEST(SOUTH, WEST),
    WEST_NORTH_WEST(WEST, NORTH_WEST),
    NORTH_NORTH_WEST(NORTH, NORTH_WEST),
    NORTH_NORTH_EAST(NORTH, NORTH_EAST),
    EAST_NORTH_EAST(EAST, NORTH_EAST),
    EAST_SOUTH_EAST(EAST, SOUTH_EAST),
    SOUTH_SOUTH_EAST(SOUTH, SOUTH_EAST),
    SOUTH_SOUTH_WEST(SOUTH, SOUTH_WEST),
    WEST_SOUTH_WEST(WEST, SOUTH_WEST),
    SELF(0, 0, 0),

    UP_NORTH(UP, NORTH),
    UP_EAST(UP, EAST),
    UP_SOUTH(UP, SOUTH),
    UP_WEST(UP, WEST),

    UP_NORTH_EAST(UP, NORTH_EAST),
    UP_NORTH_WEST(UP, NORTH_WEST),
    UP_SOUTH_EAST(UP, SOUTH_EAST),
    UP_SOUTH_WEST(UP, SOUTH_WEST),

    DOWN_NORTH(DOWN, NORTH),
    DOWN_EAST(DOWN, EAST),
    DOWN_SOUTH(DOWN, SOUTH),
    DOWN_WEST(DOWN, WEST),

    DOWN_NORTH_EAST(DOWN, NORTH_EAST),
    DOWN_NORTH_WEST(DOWN, NORTH_WEST),
    DOWN_SOUTH_EAST(DOWN, SOUTH_EAST),
    DOWN_SOUTH_WEST(DOWN, SOUTH_WEST)
    ;

    private final int modX;
    private final int modY;
    private final int modZ;

    private BlockFace(final int modX, final int modY, final int modZ) {
        this.modX = modX;
        this.modY = modY;
        this.modZ = modZ;
    }

    private BlockFace(final BlockFace face1, final BlockFace face2) {
        this.modX = face1.getModX() + face2.getModX();
        this.modY = face1.getModY() + face2.getModY();
        this.modZ = face1.getModZ() + face2.getModZ();
    }

    /**
     * Get the amount of X-coordinates to modify to get the represented block
     *
     * @return Amount of X-coordinates to modify
     */
    public int getModX() {
        return modX;
    }

    /**
     * Get the amount of Y-coordinates to modify to get the represented block
     *
     * @return Amount of Y-coordinates to modify
     */
    public int getModY() {
        return modY;
    }

    /**
     * Get the amount of Z-coordinates to modify to get the represented block
     *
     * @return Amount of Z-coordinates to modify
     */
    public int getModZ() {
        return modZ;
    }

    public Block getRelative(Block block) {
        return block.getWorld().getBlockAt(block.getX() + modX, block.getY() + modY, block.getZ() + modZ);
    }
}
