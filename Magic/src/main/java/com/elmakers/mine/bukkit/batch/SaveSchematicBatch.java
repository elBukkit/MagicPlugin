package com.elmakers.mine.bukkit.batch;

import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class SaveSchematicBatch implements Batch, com.elmakers.mine.bukkit.api.batch.SpellBatch {
    private final World world;
    private final TargetingSpell spell;
    private final CastContext context;
    private final MaterialSet ignore;
    private boolean finished = false;
    private String[][][] blockData;
    private String filename;

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

    public SaveSchematicBatch(TargetingSpell spell, Location p1, Location p2, MaterialSet ignore) {
        this.spell = spell;
        this.context = spell.getCurrentCast();
        this.ignore = ignore;
        this.world = p1.getWorld();

        int deltax = p2.getBlockX() - p1.getBlockX();
        int deltay = p2.getBlockY() - p1.getBlockY();
        int deltaz = p2.getBlockZ() - p1.getBlockZ();

        absx = Math.abs(deltax) + 1;
        absy = Math.abs(deltay) + 1;
        absz = Math.abs(deltaz) + 1;

        blockData = new String[absx][absy][absz];

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

    @Override
    @SuppressWarnings("deprecation")
    public int process(int maxWork) {
        int workPerformed = 0;

        while (workPerformed <= maxWork && ix < absx) {
            Location location = new Location(world, x + ix * dx, y + iy * dy, z + iz * dz);
            if (!CompatibilityLib.getCompatibilityUtils().checkChunk(location)) {
                return workPerformed + 20;
            }
            Block block = location.getBlock();
            String data = "minecraft:air";
            if (ignore == null || !ignore.testBlock(block)) {
                data = CompatibilityLib.getCompatibilityUtils().getBlockData(block);
            }
            blockData[ix][iy][iz] = data;
            context.addResult(SpellResult.CAST);
            workPerformed += 10;

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

        if (ix >= absx) {
            if (!saveToFile()) {
                context.addResult(SpellResult.FAIL);
            }
            finish();
        }

        return workPerformed;
    }

    private boolean saveToFile() {
        File schematicFolder = new File(context.getPlugin().getDataFolder(), "schematics");
        if (!schematicFolder.exists()) {
            if (!schematicFolder.mkdirs()) {
                context.getLogger().warning("Could not create schematics folder: " + schematicFolder.getAbsolutePath());
                return false;
            }
        }

        // TODO: Find new filename
        filename = context.getMage().getName().toLowerCase() + "1.schem";
        File targetFile = new File(schematicFolder, filename);
        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            return CompatibilityLib.getSchematicUtils().saveSchematic(outputStream, blockData);
        } catch (Exception ex) {
            context.getLogger().log(Level.WARNING, "Failed to write to file: " + targetFile.getAbsolutePath());
            return false;
        }
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public void finish() {
        if (!finished) {
            finished = true;
        }
    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public Spell getSpell() {
        return spell;
    }

    @Override
    public String getName() {
        return spell.getName();
    }

    @Override
    public UndoList getUndoList() {
        return null;
    }
}
