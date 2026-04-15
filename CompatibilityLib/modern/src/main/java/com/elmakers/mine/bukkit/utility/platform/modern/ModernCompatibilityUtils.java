package com.elmakers.mine.bukkit.utility.platform.modern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;

import com.elmakers.mine.bukkit.utility.BoundingBox;
import com.elmakers.mine.bukkit.utility.platform.Platform;

public class ModernCompatibilityUtils extends com.elmakers.mine.bukkit.utility.platform.v1_16.CompatibilityUtils {

    public ModernCompatibilityUtils(Platform platform) {
        super(platform);
    }

    @Override
    public Collection<BoundingBox> getBoundingBoxes(Block block) {
        VoxelShape voxelShape = block.getCollisionShape();
        Collection<org.bukkit.util.BoundingBox> boxes = voxelShape.getBoundingBoxes();
        if (boxes.isEmpty()) {
            return super.getBoundingBoxes(block);
        }
        List<BoundingBox> converted = new ArrayList<>(boxes.size());
        Vector center = block.getLocation().toVector();

        for (org.bukkit.util.BoundingBox bukkitBB : boxes) {
            converted.add(new BoundingBox(center,
                    bukkitBB.getMinX(), bukkitBB.getMaxX(),
                    bukkitBB.getMinY(), bukkitBB.getMaxY(),
                    bukkitBB.getMinZ(), bukkitBB.getMaxZ())
            );
        }
        return converted;
    }

    @Override
    public Runnable getTaskRunnable(BukkitTask task) {
        return null;
    }
}
