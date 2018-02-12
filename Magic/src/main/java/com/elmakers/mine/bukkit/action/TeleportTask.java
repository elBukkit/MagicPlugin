package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.magic.MageController;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class TeleportTask implements Runnable {
    protected final static int TELEPORT_RETRY_COUNT = 8;
    protected final static int TELEPORT_RETRY_INTERVAL = 1;

    private final CastContext context;
    private final Entity entity;
    private final Location location;
    private final int verticalSearchDistance;
    private final MageController controller;
    private final boolean preventFall;
    private final boolean safe;
    private int retryCount;

    public TeleportTask(MageController controller, final Entity entity, final Location location, final int verticalSearchDistance, final boolean preventFall, final boolean safe, CastContext context) {
        this.context = context;
        this.entity = entity;
        this.location = location;
        this.verticalSearchDistance = verticalSearchDistance;
        this.controller = controller;
        this.retryCount = TELEPORT_RETRY_COUNT;
        this.preventFall = preventFall;
        this.safe = safe;
    }

    @Override
    public void run() {
        Chunk chunk = location.getBlock().getChunk();
        if (!chunk.isLoaded()) {
            chunk.load(true);
            if (retryCount > 0) {
                retryCount--;
                Plugin plugin = controller.getPlugin();
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, TELEPORT_RETRY_INTERVAL);
            }
            return;
        }

        Location targetLocation = location;
        if (context != null) {
            targetLocation = context.findPlaceToStand(location, verticalSearchDistance);
        }
        if (targetLocation == null && !preventFall) {
            Block block = location.getBlock();
            Block blockOneUp = block.getRelative(BlockFace.UP);
            if (!safe || (context.isOkToStandIn(blockOneUp) && context.isOkToStandIn(block)))
            {
                targetLocation = location;
            }
        }
        if (targetLocation != null) {
            targetLocation.setX(location.getX() - location.getBlockX() + targetLocation.getBlockX());
            targetLocation.setZ(location.getZ() - location.getBlockZ() + targetLocation.getBlockZ());
            if (context != null) {
                context.registerMoved(entity);
            }

            // Hacky double-teleport to work-around vanilla suffocation checks
            boolean isWorldChange = !targetLocation.getWorld().equals(entity.getWorld());
            entity.teleport(targetLocation);
            if (isWorldChange) {
                entity.teleport(targetLocation);
            }
            if (context != null) {
                context.setTargetLocation(targetLocation);
                context.sendMessageKey("teleport");
                context.playEffects("teleport");
            }
        } else if (context != null) {
            context.sendMessageKey("teleport_failed");
            context.playEffects("teleport_failed");
        }
    }
}