package com.elmakers.mine.bukkit.api.block;

import java.util.List;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.Mage;

public interface UndoList extends BlockList, Comparable<UndoList> {
    public void commit();
    public void undo();
    public void undo(boolean undoEntityChanges);

    public void setScheduleUndo(int ttl);
    public int getScheduledUndo();
    public boolean bypass();
    public long getCreatedTime();
    public long getModifiedTime();
    public long getScheduledTime();
    public boolean isScheduled();

    public void prune();

    public void add(Entity entity);
    public void remove(Entity entity);
    public EntityData modify(Entity entity);
    public void add(Runnable runnable);

    public void convert(Entity entity, Block block);
    public void fall(Entity entity, Block block);
    public void explode(Entity entity, List<Block> explodedBlocks);
    public void cancelExplosion(Entity entity);

    public boolean contains(Location location, int threshold);

    public String getName();
    public Mage getOwner();
}
