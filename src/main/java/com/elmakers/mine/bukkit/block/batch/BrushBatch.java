package com.elmakers.mine.bukkit.block.batch;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.spell.BrushSpell;

public abstract class BrushBatch extends UndoableBatch {
	protected final BrushSpell spell;
	
	public BrushBatch(BrushSpell spell) {
		super(spell.getMage(), spell.getUndoList());
		this.spell = spell;
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
							entity.remove();
							undoList.addRemoved(entity);
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

			spell.sendMessage(spell.getMessage("cast_finish"));
			super.finish();
		}
	}
}
