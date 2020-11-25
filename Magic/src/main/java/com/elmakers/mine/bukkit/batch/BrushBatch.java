package com.elmakers.mine.bukkit.batch;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.spell.BrushSpell;

public abstract class BrushBatch extends SpellBatch {
    private Set<Chunk> affectedChunks = new HashSet<>();

    public BrushBatch(BrushSpell spell) {
        super(spell);
    }

    protected abstract boolean contains(Location location);

    protected void touch(Block block) {
        Chunk chunk = block.getChunk();
        if (affectedChunks.add(chunk)) {
            controller.lockChunk(chunk);
        }
    }

    @Override
    public void finish() {
        if (!finished) {
            MaterialBrush brush = spell.getBrush();
            if (brush != null && brush.hasEntities()) {
                // Copy over new entities
                Collection<EntityData> entities = brush.getEntities(affectedChunks);
                // Delete any entities already in the area, add them to the undo list.
                Collection<Entity> targetEntities = brush.getTargetEntities();

                if (targetEntities != null) {
                    for (Entity entity : targetEntities) {
                        if (contains(entity.getLocation())) {
                            undoList.modify(entity);
                            entity.remove();
                        }
                    }
                }

                if (entities != null) {
                    for (EntityData entity : entities) {
                        if (contains(entity.getLocation())) {
                            undoList.add(entity.spawn());
                        }
                    }
                }
            }
            for (Chunk chunk : affectedChunks) {
                controller.unlockChunk(chunk);
            }
            affectedChunks.clear();
            super.finish();
        }
    }
}
