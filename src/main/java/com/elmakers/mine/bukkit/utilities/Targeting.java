package com.elmakers.mine.bukkit.utilities;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * An entity and block targeting system
 * 
 * @author NathanWolf
 */
public class Targeting
{
    /**
     * A helper function to go change a given direction to the direction
     * "to the right".
     * 
     * There's probably some better matrix-y, math-y way to do this. It'd be
     * nice if this was in BlockFace.
     * 
     * @param direction
     *            The current direction
     * @return The direction to the left
     */
    public BlockFace goLeft(BlockFace direction)
    {
        switch (direction)
        {
            case EAST:
                return BlockFace.NORTH;
            case NORTH:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.EAST;
        }
        return direction;
    }

    /**
     * A helper function to go change a given direction to the direction
     * "to the right".
     * 
     * There's probably some better matrix-y, math-y way to do this. It'd be
     * nice if this was in BlockFace.
     * 
     * @param direction
     *            The current direction
     * @return The direction to the right
     */
    public BlockFace goRight(BlockFace direction)
    {
        switch (direction)
        {
            case EAST:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.NORTH;
            case NORTH:
                return BlockFace.EAST;
        }
        return direction;
    }

    private boolean                          allowMaxRange          = false;

    private double                           hLength                = 0;

    private int                              lastX                  = 0;

    private int                              lastY                  = 0;

    private int                              lastZ                  = 0;

    private double                           length                 = 0;

    private final Player                     player;

    private Location                         playerLocation;

    private int                              range                  = 200;

    private boolean                          reverseTargeting       = false;

    private final double                     step                   = 0.2;

    private int                              targetHeightRequired   = 1;

    private boolean                          targetingComplete      = false;

    private final HashMap<Material, Boolean> targetThroughMaterials = new HashMap<Material, Boolean>();

    private int                              targetX                = 0;

    private int                              targetY                = 0;

    private int                              targetZ                = 0;

    private final double                     viewHeight             = 1.65;

    private double                           xOffset                = 0;

    private double                           xRotation              = 0;

    /*
     * HitBlox-ported code
     */

    private double                           yOffset                = 0;

    private double                           yRotation              = 0;

    private double                           zOffset                = 0;

    public Targeting(Player player)
    {
        this.player = player;
        reset();
    }

    public Location findPlaceToStand(Location playerLoc, boolean goUp)
    {
        int step;
        if (goUp)
        {
            step = 1;
        }
        else
        {
            step = -1;
        }

        // get player position
        int x = (int) Math.round(playerLoc.getX() - 0.5);
        int y = (int) Math.round(playerLoc.getY() + step + step);
        int z = (int) Math.round(playerLoc.getZ() - 0.5);

        World world = player.getWorld();

        // search for a spot to stand
        while (4 < y && y < 125)
        {
            Block block = world.getBlockAt(x, y, z);
            Block blockOneUp = world.getBlockAt(x, y + 1, z);
            Block blockTwoUp = world.getBlockAt(x, y + 2, z);
            if (isOkToStandOn(block.getType()) && isOkToStandIn(blockOneUp.getType()) && isOkToStandIn(blockTwoUp.getType()))
            {
                // spot found - return location
                return new Location(world, x + 0.5, (double) y + 1, z + 0.5, playerLoc.getYaw(), playerLoc.getPitch());
            }
            y += step;
        }

        // no spot found
        return null;
    }

    protected void findTargetBlock()
    {
        if (targetingComplete)
        {
            return;
        }

        while (getNextBlock() != null)
        {
            Block block = getCurBlock();
            if (isTargetable(block.getType()))
            {
                boolean enoughSpace = true;
                for (int i = 1; i < targetHeightRequired; i++)
                {
                    block = block.getFace(BlockFace.UP);
                    if (!isTargetable(block.getType()))
                    {
                        enoughSpace = false;
                        break;
                    }
                }
                if (enoughSpace)
                {
                    break;
                }
            }
        }
        targetingComplete = true;
    }

    /**
     * Get a Vector reprsenting the current aim direction
     * 
     * @return The player's aim vector
     */
    public Vector getAimVector()
    {
        return new Vector((0 - Math.sin(Math.toRadians(playerLocation.getYaw()))), (0 - Math.sin(Math.toRadians(playerLocation.getPitch()))), Math.cos(Math.toRadians(playerLocation.getYaw())));
    }

    /**
     * Returns the block at the specified location
     * 
     * Just a wrapper for world.getBlock at this point.
     * 
     * @param x
     * @param y
     * @param z
     * @return block The block at the specified coordinates
     */
    public Block getBlockAt(int x, int y, int z)
    {
        World world = player.getWorld();
        return world.getBlockAt(x, y, z);
    }

    /**
     * Returns the current block along the line of vision
     * 
     * @return The block
     */
    public Block getCurBlock()
    {
        if (length > range && !allowMaxRange)
        {
            return null;
        }
        else
        {
            return getBlockAt(targetX, targetY, targetZ);
        }
    }

    public double getDistance(Location source, Location target)
    {
        return Math.sqrt(Math.pow(source.getX() - target.getX(), 2) + Math.pow(source.getY() - target.getY(), 2) + Math.pow(source.getZ() - target.getZ(), 2));
    }

    public double getDistance(Player player, Block target)
    {
        Location loc = player.getLocation();
        return Math.sqrt(Math.pow(loc.getX() - target.getX(), 2) + Math.pow(loc.getY() - target.getY(), 2) + Math.pow(loc.getZ() - target.getZ(), 2));
    }

    /**
     * Returns the block attached to the face at the cursor, or null if out of
     * range
     * 
     * @return The face block
     */
    public Block getFaceBlock()
    {
        findTargetBlock();
        if (getCurBlock() != null)
        {
            return getLastBlock();
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns the previous block along the line of vision
     * 
     * @return The block
     */
    public Block getLastBlock()
    {
        return getBlockAt(lastX, lastY, lastZ);
    }

    /**
     * Move "steps" forward along line of vision and returns the block there
     * 
     * @return The block at the new location
     */
    public Block getNextBlock()
    {
        lastX = targetX;
        lastY = targetY;
        lastZ = targetZ;

        do
        {
            length += step;

            hLength = length * Math.cos(Math.toRadians(yRotation));
            yOffset = length * Math.sin(Math.toRadians(yRotation));
            xOffset = hLength * Math.cos(Math.toRadians(xRotation));
            zOffset = hLength * Math.sin(Math.toRadians(xRotation));

            targetX = (int) Math.floor(xOffset + playerLocation.getX());
            targetY = (int) Math.floor(yOffset + playerLocation.getY() + viewHeight);
            targetZ = (int) Math.floor(zOffset + playerLocation.getZ());

        }
        while (length <= range && targetX == lastX && targetY == lastY && targetZ == lastZ);

        if (length > range)
        {
            if (allowMaxRange)
            {
                return getBlockAt(targetX, targetY, targetZ);
            }
            else
            {
                return null;
            }
        }

        return getBlockAt(targetX, targetY, targetZ);
    }

    /**
     * Get the block the player is standing on.
     * 
     * @return The Block the player is standing on
     */
    public Block getPlayerBlock()
    {
        Block playerBlock = null;
        Location playerLoc = player.getLocation();
        int x = (int) Math.round(playerLoc.getX() - 0.5);
        int y = (int) Math.round(playerLoc.getY() - 0.5);
        int z = (int) Math.round(playerLoc.getZ() - 0.5);
        int dy = 0;
        while (dy > -3 && (playerBlock == null || isOkToStandIn(playerBlock.getType())))
        {
            playerBlock = player.getWorld().getBlockAt(x, y + dy, z);
            dy--;
        }
        return playerBlock;
    }

    /**
     * Get the direction the player is facing as a BlockFace.
     * 
     * @return a BlockFace representing the direction the player is facing
     */
    public BlockFace getPlayerFacing()
    {
        float playerRot = getPlayerRotation();

        BlockFace direction = BlockFace.NORTH;
        if (playerRot <= 45 || playerRot > 315)
        {
            direction = BlockFace.WEST;
        }
        else if (playerRot > 45 && playerRot <= 135)
        {
            direction = BlockFace.NORTH;
        }
        else if (playerRot > 135 && playerRot <= 225)
        {
            direction = BlockFace.EAST;
        }
        else if (playerRot > 225 && playerRot <= 315)
        {
            direction = BlockFace.SOUTH;
        }

        return direction;
    }

    /**
     * Gets the normal player rotation.
     * 
     * This differs from xRotation by 90 degrees. xRotation is ported from
     * HitBlox, I really need to get rid of or refactor all that code, but it
     * may be worth just waiting for the Bukkit targeting implementation at this
     * point.
     * 
     * @return The player X-rotation (yaw)
     */
    public float getPlayerRotation()
    {
        float playerRot = player.getLocation().getYaw();
        while (playerRot < 0)
        {
            playerRot += 360;
        }
        while (playerRot > 360)
        {
            playerRot -= 360;
        }
        return playerRot;
    }

    /**
     * Find a good location to spawn a projectile, such as a fireball.
     * 
     * @return The projectile spawn location
     */
    protected Location getProjectileSpawnLocation()
    {
        Block spawnBlock = getPlayerBlock();

        int height = 2;
        double hLength = 2;
        double xOffset = hLength * Math.cos(Math.toRadians(xRotation));
        double zOffset = hLength * Math.sin(Math.toRadians(xRotation));

        Vector aimVector = new Vector(xOffset + 0.5, height + 0.5, zOffset + 0.5);

        Location location = new Location(player.getWorld(), spawnBlock.getX() + aimVector.getX(), spawnBlock.getY() + aimVector.getY(), spawnBlock.getZ() + aimVector.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());

        return location;
    }

    /**
     * Returns the block at the cursor, or null if out of range
     * 
     * @return The target block
     */
    public Block getTargetBlock()
    {
        findTargetBlock();
        return getCurBlock();
    }

    public int getTargetHeightRequired()
    {
        return targetHeightRequired;
    }

    /**
     * Get the (simplified) player yaw.
     * 
     * @return Player X-axis rotation (yaw)
     */
    public double getXRotation()
    {
        return xRotation;
    }

    /**
     * Get the (simplified) player pitch.
     * 
     * @return Player Y-axis rotation (pitch)
     */
    public double getYRotation()
    {
        return yRotation;
    }

    /*
     * Ground / location search and test function functions
     */
    public boolean isOkToStandIn(Material mat)
    {
        return mat == Material.AIR || mat == Material.WATER || mat == Material.STATIONARY_WATER;
    }

    public boolean isOkToStandOn(Material mat)
    {
        return mat != Material.AIR && mat != Material.LAVA && mat != Material.STATIONARY_LAVA;
    }

    public boolean isReverseTargeting()
    {
        return reverseTargeting;
    }

    public boolean isTargetable(Material mat)
    {
        Boolean checkMat = targetThroughMaterials.get(mat);
        if (reverseTargeting)
        {
            return checkMat != null && checkMat;
        }
        return checkMat == null || !checkMat;
    }

    public void noTargetThrough(Material mat)
    {
        targetThroughMaterials.put(mat, false);
    }

    public void reset()
    {
        playerLocation = player.getLocation();
        length = 0;
        targetHeightRequired = 1;
        xRotation = (playerLocation.getYaw() + 90) % 360;
        yRotation = playerLocation.getPitch() * -1;
        reverseTargeting = false;

        targetX = (int) Math.floor(playerLocation.getX());
        targetY = (int) Math.floor(playerLocation.getY() + viewHeight);
        targetZ = (int) Math.floor(playerLocation.getZ());
        lastX = targetX;
        lastY = targetY;
        lastZ = targetZ;
        targetingComplete = false;
    }

    public void setMaxRange(int range, boolean allow)
    {
        this.range = range;
        this.allowMaxRange = allow;
    }

    public void setReverseTargeting(boolean reverse)
    {
        reverseTargeting = reverse;
    }

    public void setTargetHeightRequired(int height)
    {
        targetHeightRequired = height;
    }

    public void targetThrough(Material mat)
    {
        targetThroughMaterials.put(mat, true);
    }

}
