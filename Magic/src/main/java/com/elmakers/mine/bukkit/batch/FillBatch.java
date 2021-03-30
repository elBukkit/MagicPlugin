package com.elmakers.mine.bukkit.batch;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.FallingBlock;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.BoundingBox;
import com.elmakers.mine.bukkit.spell.BrushSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;

public class FillBatch extends BrushBatch {
    private final MaterialBrush brush;
    private final World world;

    private final int absx;
    private final int absy;
    private final int absz;
    private final int dx;
    private final int dy;
    private final int dz;
    private final int x;
    private final int y;
    private final int z;
    private int ix = 0;
    private int iy = 0;
    private int iz = 0;

    private boolean consume = false;
    private boolean consumeVariants = true;

    private final BoundingBox bounds;

    private boolean spawnFallingBlocks = false;

    public FillBatch(BrushSpell spell, Location p1, Location p2, MaterialBrush brush) {
        super(spell);
        this.bounds = new BoundingBox(p1.toVector(), p2.toVector());
        this.brush = brush;
        this.world = p1.getWorld();

        int deltax = p2.getBlockX() - p1.getBlockX();
        int deltay = p2.getBlockY() - p1.getBlockY();
        int deltaz = p2.getBlockZ() - p1.getBlockZ();

        absx = Math.abs(deltax) + 1;
        absy = Math.abs(deltay) + 1;
        absz = Math.abs(deltaz) + 1;

        dx = (int)Math.signum(deltax);
        dy = (int)Math.signum(deltay);
        dz = (int)Math.signum(deltaz);

        x = p1.getBlockX();
        y = p1.getBlockY();
        z = p1.getBlockZ();
    }

    @Override
    public int size() {
        return absx * absy * absz;
    }

    @Override
    public int remaining() {
        return (absx - ix) * (absy - iy) * (absz - iz);
    }

    public boolean checkDimension(int maxDimension) {
        return !(maxDimension > 0 && (absx > maxDimension || absy > maxDimension || absz > maxDimension));
    }

    public boolean checkVolume(int maxVolume) {
        return !(maxVolume > 0 && absx * absy * absz > maxVolume);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int process(int maxWork) {
        int workPerformed = 0;

        while (workPerformed <= maxWork && ix < absx) {
            Location location = new Location(world, x + ix * dx, y + iy * dy, z + iz * dz);
            if (!CompatibilityUtils.checkChunk(location)) {
                return workPerformed + 20;
            }
            Block block = location.getBlock();
            brush.update(mage, block.getLocation());
            if (!brush.isReady()) {
                brush.prepare();
                return workPerformed + 10;
            }
            workPerformed += 10;

            touch(block);
            boolean hasPermission = brush.isErase() ? spell.hasBreakPermission(block) : spell.hasBuildPermission(block);
            if (hasPermission && !spell.isIndestructible(block) && spell.isDestructible(block)) {
                Material previousMaterial = block.getType();
                byte previousData = block.getData();

                if (brush.isDifferent(block)) {
                    if (consume && !context.isConsumeFree() && brush.getMaterial() != Material.AIR) {
                        if (!mage.consumeBlock(brush, consumeVariants)) {
                            String requiresMessage = context.getMessage("insufficient_resources");
                            context.addResult(SpellResult.INSUFFICIENT_RESOURCES);
                            context.sendMessageKey("insufficient_resources", requiresMessage.replace("$cost", brush.getName()));
                            finish();
                            return workPerformed;
                        }
                    }

                    BlockState prior = block.getState();
                    registerForUndo(block);
                    brush.modify(block);
                    if (!undoList.isScheduled()) {
                        controller.logBlockChange(spell.getMage(), prior, block.getState());
                    }

                    if (spawnFallingBlocks) {
                        FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation(), previousMaterial, previousData);
                        falling.setDropItem(false);
                    }
                    context.addResult(SpellResult.CAST);
                }
            }

            iy++;
            if (iy >= absy) {
                iy = 0;
                iz++;
                if (iz >= absz) {
                    iz = 0;
                    ix++;
                }
            }
        }

        if (ix >= absx)
        {
            finish();
        }

        return workPerformed;
    }

    public int getXSize() {
        return absx;
    }

    public int getYSize() {
        return absy;
    }

    public int getZSize() {
        return absz;
    }

    public void setConsume(boolean consume) {
        this.consume = consume;
    }
    public void setConsumeVariants(boolean variants) {
        this.consumeVariants = variants;
    }

    @Override
    protected boolean contains(Location location) {
        return bounds.contains(location.toVector());
    }
}
