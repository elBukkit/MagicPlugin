package com.elmakers.mine.bukkit.batch;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.block.ConstructionType;
import com.elmakers.mine.bukkit.spell.BrushSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

/**
 * TODO: Abstract a lot of this into a common class for ConstructBatch
 */
public class ShapeBatch extends BrushBatch {
    private final Location center;
    private Vector orient = null;
    private final int radius;
    private final int thickness;
    private final ConstructionType type;
    private Integer maxOrientDimension = null;
    private Integer minOrientDimension = null;

    private int x = 0;
    private int y = 0;
    private int z = 0;
    private int r = 0;

    private boolean limitYAxis = false;
    // TODO.. min X, Z, etc

    public ShapeBatch(BrushSpell spell, Location center, ConstructionType type, int radius, int thickness, Location orientToLocation) {
        super(spell);
        this.center = center;
        this.radius = radius;
        this.thickness = thickness;
        this.type = type;
        if (orientToLocation != null) {
            Vector orientTo = orientToLocation.toVector().subtract(center.toVector());
            orientTo.setX(Math.abs(orientTo.getX()));
            orientTo.setY(Math.abs(orientTo.getY()));
            orientTo.setZ(Math.abs(orientTo.getZ()));
            if (orientTo.getX() < orientTo.getZ() && orientTo.getX() < orientTo.getY()) {
                orient = new Vector(1, 0, 0);
            } else if (orientTo.getZ() < orientTo.getX() && orientTo.getZ() < orientTo.getY()) {
                orient = new Vector(0, 0, 1);
            } else {
                orient = new Vector(0, 1, 0);
            }
        } else {
            orient = new Vector(0, 1, 0);
        }
    }

    public void setOrientDimensionMax(int maxDim) {
        this.maxOrientDimension = maxDim;
    }

    public void setOrientDimensionMin(int minDim) {
        this.minOrientDimension = minDim;
    }

    @Override
    public int size() {
        return radius * radius * radius * 8;
    }

    @Override
    public int remaining() {
        if (r >= radius) return 0;
        return (radius - r) * (radius - r) * (radius - r) * 8;
    }

    @Override
    public int process(int maxBlocks) {
        int processedBlocks = 0;

        int yBounds = radius;
        if ((maxOrientDimension != null || minOrientDimension != null) && orient.getBlockY() > 0) {
            limitYAxis = true;
            yBounds = Math.max(minOrientDimension == null ? radius : minOrientDimension, maxOrientDimension == null ? radius : maxOrientDimension);
        }
        yBounds = Math.min(yBounds, 255);

        while (processedBlocks <= maxBlocks && !finished) {
            if (!createSymmetricalBlock(x, y, z)) {
                return processedBlocks;
            }

            int xBounds = r;
            int zBounds = r;
            if ((maxOrientDimension != null || minOrientDimension != null) && orient.getBlockX() > 0) {
                xBounds = Math.max(minOrientDimension == null ? r : minOrientDimension, maxOrientDimension == null ? r : maxOrientDimension);
            }

            if ((maxOrientDimension != null || minOrientDimension != null) && orient.getBlockZ() > 0) {
                zBounds = Math.max(minOrientDimension == null ? r : minOrientDimension, maxOrientDimension == null ? r : maxOrientDimension);
            }

            y++;
            if (y > yBounds) {
                y = 0;
                if (x < xBounds) {
                    x++;
                } else {
                    z--;
                    if (z < 0) {
                        r++;
                        zBounds = r;
                        if ((maxOrientDimension != null || minOrientDimension != null) && orient.getBlockZ() > 0) {
                            zBounds = Math.max(minOrientDimension == null ? r : minOrientDimension, maxOrientDimension == null ? r : maxOrientDimension);
                        }
                        z = zBounds;
                        x = 0;
                    }
                }
            }

            if (r > radius)
            {
                finish();
                break;
            }
            processedBlocks++;
        }

        return processedBlocks;
    }

    public boolean createSymmetricalBlock(int x, int y, int z)
    {
        boolean fillBlock = false;
        switch (type) {
            case SPHERE:
                int maxDistanceSquared = radius * radius;
                float mx = x - 0.5f;
                float my = y - 0.5f;
                float mz = z - 0.5f;

                int distanceSquared = (int)((mx * mx) + (my * my) + (mz * mz));
                if (thickness == 0)
                {
                    fillBlock = distanceSquared <= maxDistanceSquared;
                }
                else
                {
                    mx++;
                    my++;
                    mz++;
                    int outerDistanceSquared = (int)((mx * mx) + (my * my) + (mz * mz));
                    fillBlock = maxDistanceSquared >= distanceSquared - thickness && maxDistanceSquared <= outerDistanceSquared;
                }
                //spells.getLog().info("(" + x + "," + y + "," + z + ") : " + fillBlock + " = " + distanceSquared + " : " + maxDistanceSquared);
                break;
            case PYRAMID:
                int elevation = radius - y;
                if (thickness == 0) {
                    fillBlock = (x <= elevation) && (z <= elevation);
                } else {
                    fillBlock = (x <= elevation && x >= elevation - thickness && z <= elevation)
                             || (z <= elevation && z >= elevation - thickness && x <= elevation);
                }
                break;
            default:
                fillBlock = thickness == 0 ? true : (x >= radius - thickness || y >= radius - thickness || z >= radius - thickness);
                break;
        }
        boolean success = true;
        if (fillBlock)
        {
            if (y != 0) {
                success = success && createBlock(x, -y, z);
                if (x != 0) success = success && createBlock(-x, -y, z);
                if (z != 0) success = success && createBlock(x, -y, -z);
                if (x != 0 && z != 0) success = success && createBlock(-x, -y, -z);
            }
            success = success && createBlock(x, y, z);
            if (x != 0) success = success && createBlock(-x, y, z);
            if (z != 0) success = success && createBlock(x, y, -z);
            if (z != 0 && x != 0) success = success && createBlock(-x, y, -z);
        }
        return success;
    }

    public boolean createBlock(int dx, int dy, int dz)
    {
        // Special-case hackiness..
        if (limitYAxis && minOrientDimension != null && dy < -minOrientDimension) return true;
        if (limitYAxis && maxOrientDimension != null && dy > maxOrientDimension) return true;

        // Initial range checks, we skip everything if this is not sane.
        int x = center.getBlockX() + dx;
        int y = center.getBlockY() + dy;
        int z = center.getBlockZ() + dz;

        if (y < 0 || y > center.getWorld().getMaxHeight()) return true;

        // Make sure the block is loaded.
        Location location = new Location(center.getWorld(), x, y, z);
        if (!CompatibilityLib.getCompatibilityUtils().checkChunk(location)) {
            return false;
        }
        Block block = location.getBlock();

        touch(block);
        // Prepare material brush, it may update
        // given the current target (clone, replicate)
        MaterialBrush brush = spell.getBrush();
        brush.update(mage, block.getLocation());

        // Make sure the brush is ready, it may need to load chunks.
        if (!brush.isReady()) {
            brush.prepare();
            return false;
        }

        if (!spell.hasBuildPermission(block))
        {
            return true;
        }

        Location loc = center.clone();
        Vector direction = block.getLocation().toVector().subtract(center.toVector()).normalize();
        loc.setDirection(direction);
        Minecart minecart = CompatibilityLib.getCompatibilityUtils().spawnCustomMinecart(loc, brush.getMaterial(), brush.getData(), radius * 16);
        registerForUndo(minecart);
        return true;
    }

    @Override
    protected boolean contains(Location location) {
        return false;
    }
}
