package com.elmakers.mine.bukkit.magic.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Painting;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;

public class HangingController implements Listener {
    private final MagicController controller;
    private final List<Location> checkBlocks = new ArrayList<>();

    public HangingController(MagicController controller) {
        this.controller = controller;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        final Hanging entity = event.getEntity();
        if (!entity.isValid()) return;
        checkBlocks.clear();
        try {
            final BlockFace attachedFace = entity.getAttachedFace();
            if (entity instanceof Painting) {
                Location location = CompatibilityUtils.getHangingLocation(entity);
                Location attachedLocation = location.getBlock().getRelative(attachedFace).getLocation();
                Painting painting = (Painting)entity;
                int width = painting.getArt().getBlockWidth();
                int height = painting.getArt().getBlockHeight();
                BlockFace sideways = CompatibilityUtils.getCCW(painting.getFacing());
                Block attached = attachedLocation.getBlock();
                Block block = location.getBlock();

                for (int x = 0; x < width; ++x) {
                    for (int y = 0; y < height; ++y) {
                        int widthMid = (width - 1) / -2;
                        int heightMid = (height - 1) / -2;

                        Block neighbor = attached.getRelative(BlockFace.UP, y + heightMid);
                        neighbor = neighbor.getRelative(sideways, x + widthMid);
                        checkBlocks.add(neighbor.getLocation());

                        neighbor = block.getRelative(BlockFace.UP, y + heightMid);
                        neighbor = neighbor.getRelative(sideways, x + widthMid);
                        checkBlocks.add(neighbor.getLocation());
                    }
                }
            } else {
                Location location = entity.getLocation();
                Location attachedLocation = location.getBlock().getRelative(attachedFace).getLocation();
                checkBlocks.add(location);
                checkBlocks.add(attachedLocation);
            }

            UndoList undoList = null;
            for (Location checkBlock : checkBlocks) {
                undoList = controller.getPendingUndo(checkBlock);
                if (undoList != null) {
                    event.setCancelled(true);
                    undoList.damage(entity);
                    break;
                }
            }
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Failed to handle HangingBreakEvent", ex);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        Entity breakingEntity = event.getRemover();
        if (breakingEntity == null) return;

        Hanging entity = event.getEntity();
        UndoList undoList = controller.getEntityUndo(breakingEntity);
        if (undoList != null && undoList.isScheduled())
        {
            undoList.damage(entity);

            // Prevent item drops, but still remove it
            // Else it'll probably just break again.
            event.setCancelled(true);
        }
    }
}
