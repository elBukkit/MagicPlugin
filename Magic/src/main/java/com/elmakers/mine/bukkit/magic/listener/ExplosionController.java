package com.elmakers.mine.bukkit.magic.listener;

import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;

import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class ExplosionController implements Listener {
    private final MagicController controller;
    private int	maxTNTPerChunk = 0;
    private int autoRollbackDuration = 10000;
    private double autoRollbackSpeed = 2;
    private Set<EntityType> rollbackExplosions = new HashSet<>();

    public ExplosionController(MagicController controller) {
        this.controller = controller;
    }

    public void loadProperties(ConfigurationSection properties) {
        maxTNTPerChunk = properties.getInt("max_tnt_per_chunk", 0);
        autoRollbackDuration = properties.getInt("auto_rollback_duration", 10000);
        autoRollbackSpeed = properties.getDouble("auto_rollback_speed", 2);
        rollbackExplosions.clear();
        Collection<String> typeNames = properties.getStringList("auto_rollback_explosions");
        if (typeNames != null) {
            for (String typeName : typeNames) {
                try {
                    EntityType entityType = EntityType.valueOf(typeName.toUpperCase());
                    rollbackExplosions.add(entityType);
                } catch (Exception ex) {
                    controller.getLogger().warning("Failed to parse entity type: " + typeName + " in auto_rollback_explosions");
                }
            }
        }
    }

    protected UndoList getExplosionUndo(Entity explodingEntity) {
        UndoList blockList = controller.getEntityUndo(explodingEntity);
        if (blockList == null && autoRollbackDuration > 0 && rollbackExplosions.contains(explodingEntity.getType())) {
            Mage mage = controller.getMage(explodingEntity);
            blockList = new com.elmakers.mine.bukkit.block.UndoList(mage, "Explosion (" + explodingEntity.getType().name() + ")");
            blockList.setScheduleUndo(autoRollbackDuration);
            blockList.setUndoSpeed(autoRollbackSpeed);
            mage.prepareForUndo(blockList);
        }
        return blockList;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity explodingEntity = event.getEntity();
        if (explodingEntity == null) return;

        UndoList blockList = getExplosionUndo(explodingEntity);
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
            blockList.getOwner().registerForUndo(blockList);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityFinalizeExplode(EntityExplodeEvent event) {
        Entity explodingEntity = event.getEntity();
        if (explodingEntity == null) return;

        UndoList blockList = getExplosionUndo(explodingEntity);
        if (blockList == null) return;

        if (event.isCancelled()) {
            blockList.cancelExplosion(explodingEntity);
        } else {
            controller.disableItemSpawn();
            try {
                blockList.finalizeExplosion(explodingEntity, event.blockList());
            } catch (Exception ex) {
                controller.getLogger().log(Level.WARNING, "Error finalizing explosion", ex);
            }
            controller.enableItemSpawn();
        }
    }
}
