package com.elmakers.mine.bukkit.magic.listener;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.magic.MagicController;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class ExplosionController implements Listener {
    private final MagicController controller;
    private int	maxTNTPerChunk = 0;

    public ExplosionController(MagicController controller) {
        this.controller = controller;
    }

    public void setMaxTNTPerChunk(int max) {
        this.maxTNTPerChunk = max;
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        Entity explodingEntity = event.getEntity();
        ActionHandler.runActions(explodingEntity, explodingEntity.getLocation(), null);
        ActionHandler.runEffects(explodingEntity);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity explodingEntity = event.getEntity();
        if (explodingEntity == null) return;

        UndoList blockList = controller.getEntityUndo(explodingEntity);
        boolean cancel = event.isCancelled();
        cancel = cancel || explodingEntity.hasMetadata("cancel_explosion");
        if (blockList != null)
        {
            com.elmakers.mine.bukkit.api.action.CastContext context = blockList.getContext();
            if (!cancel && context != null && !context.hasBreakPermission(explodingEntity.getLocation().getBlock())) {
                cancel = true;
            }
        }
        if (cancel) {
            event.setCancelled(true);
        }
        else if (maxTNTPerChunk > 0 && explodingEntity.getType() == EntityType.PRIMED_TNT) {
            Chunk chunk = explodingEntity.getLocation().getChunk();
            if (chunk == null || !chunk.isLoaded()) return;

            int tntCount = 0;
            Entity[] entities = chunk.getEntities();
            for (Entity entity : entities) {
                if (entity != null && entity.getType() == EntityType.PRIMED_TNT) {
                    tntCount++;
                }
            }
            if (tntCount > maxTNTPerChunk) {
                event.setCancelled(true);
            } else {
                if (blockList != null) {
                    blockList.explode(explodingEntity, event.blockList());
                }
            }
        }
        else if (blockList != null) {
            blockList.explode(explodingEntity, event.blockList());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityFinalizeExplode(EntityExplodeEvent event) {
        Entity explodingEntity = event.getEntity();
        if (explodingEntity == null) return;

        UndoList blockList = controller.getEntityUndo(explodingEntity);
        if (blockList == null) return;

        if (event.isCancelled()) {
            blockList.cancelExplosion(explodingEntity);
        } else {
            blockList.finalizeExplosion(explodingEntity, event.blockList());
        }
    }
}
