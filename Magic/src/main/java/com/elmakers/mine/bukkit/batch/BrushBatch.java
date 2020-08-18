package com.elmakers.mine.bukkit.batch;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.spell.BrushSpell;

public abstract class BrushBatch extends SpellBatch {

    public BrushBatch(BrushSpell spell) {
        super(spell);
    }

    protected abstract boolean contains(Location location);

    @Override
    public void finish() {
        if (!finished) {
            MaterialBrush brush = spell.getBrush();
            if (brush != null && brush.hasEntities()) {
                // Copy over new entities
                Collection<EntityData> entities = brush.getEntities();
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
                            undoList.add(entity.spawn(controller));
                        }
                    }
                }
            }
            super.finish();
        }
    }
}
