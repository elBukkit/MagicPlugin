package com.elmakers.mine.bukkit.api.block;

import java.util.List;
import java.util.Set;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.spell.Spell;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.Mage;
import org.bukkit.entity.EntityType;

public interface UndoList extends BlockList, Comparable<UndoList> {
    public void commit();
    public void undo();
    public void undo(boolean blocking);
    public void undoScheduled();
    public void undoScheduled(boolean blocking);

    public void setEntityUndo(boolean undoEntityEffects);
    public void setEntityUndoTypes(Set<EntityType> undoTypes);

    public void setScheduleUndo(int ttl);
    public int getScheduledUndo();
    public void updateScheduledUndo();
    public boolean bypass();
    public void setApplyPhysics(boolean physics);
    public long getCreatedTime();
    public long getModifiedTime();
    public long getScheduledTime();
    public boolean isScheduled();

    public void setUndoBreakable(boolean breakable);
    public void setUndoReflective(boolean reflective);

    public void prune();

    public void add(Entity entity);
    public void remove(Entity entity);
    public EntityData damage(Entity entity, double damage);
    public EntityData modify(Entity entity);
    public void add(Runnable runnable);
    public void move(Entity entity);
    public void modifyVelocity(Entity entity);
    public void addPotionEffects(Entity entity);

    public void convert(Entity entity, Block block);
    public void fall(Entity entity, Block block);
    public void explode(Entity entity, List<Block> explodedBlocks);
    public void finalizeExplosion(Entity entity, List<Block> explodedBlocks);
    public void cancelExplosion(Entity entity);
    public void setBatch(Batch batch);
    public void setSpell(Spell spell);

    public boolean contains(Location location, int threshold);

    public String getName();
    public Mage getOwner();
    public CastContext getContext();
}
