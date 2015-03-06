package com.elmakers.mine.bukkit.block;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.elmakers.mine.bukkit.spell.UndoableSpell;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.block.BlockData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.block.batch.UndoBatch;
import com.elmakers.mine.bukkit.entity.EntityData;

/**
 * Implements a Collection of Blocks, for quick getting/putting while iterating
 * over a set or area of blocks.
 * 
 * This stores BlockData objects, which are hashable via their Persisted
 * inheritance, and their LocationData id (which itself has a hash function
 * based on world name and BlockVector's hash function)
 * 
 */
public class UndoList extends BlockList implements com.elmakers.mine.bukkit.api.block.UndoList
{
    protected static Map<Long, BlockData> modified = new HashMap<Long, BlockData>();

    protected List<WeakReference<Entity>> 	entities;
    protected List<Runnable>				runnables;
    protected HashMap<UUID, EntityData> 	modifiedEntities;

    protected final Mage			owner;
    protected final Plugin		   	plugin;

    protected boolean               undone              = false;
    protected int                  	timeToLive          = 0;
    protected boolean               applyPhysics        = false;

    protected boolean				bypass		 	    = false;
    protected final long			createdTime;
    protected long					modifiedTime;
    protected long                  scheduledTime;

    // Doubly-linked list
    protected final UndoableSpell   spell;
    protected UndoQueue             undoQueue;
    protected UndoList              next;
    protected UndoList              previous;

    protected String				name;

    private boolean                 undoEntityEffects = true;

    public UndoList(Mage mage, UndoableSpell spell, String name)
    {
        this(mage, spell);
        this.name = name;
    }

    public UndoList(Mage mage, UndoableSpell spell)
    {
        this.owner = mage;
        this.spell = spell;
        this.plugin = owner.getController().getPlugin();
        createdTime = System.currentTimeMillis();
        modifiedTime = createdTime;
    }

    @Override
    public int size()
    {
        return (
                (blockList == null ? 0 :blockList.size())
            + 	(entities == null ? 0 : entities.size()))
            + 	(runnables == null ? 0 : runnables.size());
    }

    @Override
    public boolean isEmpty()
    {
        return (
            (blockList == null || blockList.isEmpty())
        && 	(entities == null || entities.isEmpty())
        && 	(runnables == null || runnables.isEmpty()));
    }

    public boolean isComplete()
    {
        return undone;
    }

    public void setScheduleUndo(int ttl)
    {
        timeToLive = ttl;
        scheduledTime = System.currentTimeMillis() + timeToLive;
    }

    public int getScheduledUndo()
    {
        return timeToLive;
    }

    @Override
    public boolean add(BlockData blockData)
    {
        if (!super.add(blockData)) {
            return false;
        }
        modifiedTime = System.currentTimeMillis();
        if (bypass) return true;

        register(blockData);
        blockData.setUndoList(this);
        return true;
    }

    public static BlockData register(Block block)
    {
        BlockData blockData = new com.elmakers.mine.bukkit.block.BlockData(block);
        register(blockData);
        return blockData;
    }

    public static void register(BlockData blockData)
    {
        BlockData priorState = modified.get(blockData.getId());
        if (priorState != null)
        {
            priorState.setNextState(blockData);
            blockData.setPriorState(priorState);
        }

        modified.put(blockData.getId(), blockData);
    }

    public void commit()
    {
        unlink();
        if (blockList == null) return;

        for (BlockData block : blockList)
        {
            commit(block);
        }
    }

    public void commit(BlockData block)
    {
        modified.remove(block.getId());
        BlockData currentState = modified.get(block.getId());
        if (currentState == block)
        {
            modified.remove(block.getId());
        }
        block.commit();
    }

    @Override
    public boolean remove(Object o)
    {
        if (o instanceof BlockData)
        {
            BlockData block = (BlockData)o;
            removeFromModified(block, block.getPriorState());
        }

        return super.remove(o);
    }

    protected static void removeFromModified(BlockData block, BlockData priorState)
    {
        BlockData currentState = modified.get(block.getId());
        if (currentState == block)
        {
            if (priorState == null)
            {
                modified.remove(block.getId());
            }
            else
            {
                modified.put(block.getId(), priorState);
            }
        }
    }

    public static boolean undo(BlockData undoBlock, boolean applyPhysics)
    {
        BlockData priorState = undoBlock.getPriorState();
        if (undoBlock.undo(applyPhysics)) {
            removeFromModified(undoBlock, priorState);
            return true;
        }

        return false;
    }

    public void undoEntityEffects()
    {
        // This part doesn't happen in a batch, and may lag on large lists
        if (entities != null || modifiedEntities != null) {
            if (entities != null) {
                for (WeakReference<Entity> entityReference : entities) {
                    Entity entity = entityReference.get();
                    if (entity != null && entity.isValid()) {
                        entity.remove();
                    }
                }
                entities = null;
            }
            if (modifiedEntities != null) {
                for (EntityData data : modifiedEntities.values()) {
                    if (!undoEntityEffects && !data.isHanging() && !data.isProjectile()) continue;
                    data.undo();
                }
                modifiedEntities = null;
            }
        }
    }

    public void undo()
    {
        undo(false);
    }

    public void undo(boolean blocking)
    {
        undo(blocking, true);
    }

    public void undoScheduled(boolean blocking)
    {
        undo(blocking, false);
    }

    public void undoScheduled()
    {
        undoScheduled(false);
    }

    public void undo(boolean blocking, boolean undoEntities)
    {
        undoEntityEffects = undoEntityEffects || undoEntities;
        unlink();
        if (isComplete()) return;
        undone = true;

        if (spell != null)
        {
            spell.cancelEffects();
        }

        if (runnables != null) {
            for (Runnable runnable : runnables) {
                runnable.run();
            }
            runnables = null;
        }

        if (blockList == null) {
            undoEntityEffects();
            return;
        }

        // Block changes will be performed in a batch
        UndoBatch batch = new UndoBatch(this);
        if (blocking) {
            while (!batch.isFinished()) {
                batch.process(1000);
            }
        } else {
            owner.addUndoBatch(batch);
        }
        blockList = null;
    }

    @Override
    public void load(ConfigurationSection node)
    {
        super.load(node);
        timeToLive = node.getInt("time_to_live", timeToLive);
        name = node.getString("name", name);
        applyPhysics = node.getBoolean("apply_physics", applyPhysics);
    }

    @Override
    public void save(ConfigurationSection node)
    {
        super.save(node);
        node.set("time_to_live", (Integer)timeToLive);
        node.set("name", name);
        node.set("apply_physics", applyPhysics);
    }

    public void watch(Entity entity)
    {
        if (entity == null) return;
        if (worldName != null && !entity.getWorld().getName().equals(worldName)) return;
        if (worldName == null) worldName = entity.getWorld().getName();

        entity.setMetadata("MagicBlockList", new FixedMetadataValue(plugin, this));
        modifiedTime = System.currentTimeMillis();
    }

    public void add(Entity entity)
    {
        if (entity == null) return;
        if (entities == null) entities = new ArrayList<WeakReference<Entity>>();
        if (worldName != null && !entity.getWorld().getName().equals(worldName)) return;

        entities.add(new WeakReference<Entity>(entity));
        if (this.isScheduled()) {
            entity.setMetadata("temporary", new FixedMetadataValue(plugin, true));
        }
        watch(entity);
        contain(entity.getLocation().toVector());
        modifiedTime = System.currentTimeMillis();
    }

    public void add(Runnable runnable)
    {
        if (runnable == null) return;
        if (runnables == null) runnables = new LinkedList<Runnable>();
        runnables.add(runnable);
        modifiedTime = System.currentTimeMillis();
    }

    public EntityData modify(Entity entity)
    {
        EntityData entityData = null;
        if (entity == null) return entityData;
        if (worldName != null && !entity.getWorld().getName().equals(worldName)) return entityData;
        if (worldName == null) worldName = entity.getWorld().getName();

        // Check to see if this is something we spawned, and has now been destroyed
        UUID entityId = entity.getUniqueId();
        if (entities != null && entities.contains(entityId) && !entity.isValid()) {
            entities.remove(entityId);
        } else {
            if (modifiedEntities == null) modifiedEntities = new HashMap<UUID, EntityData>();
            entityData = modifiedEntities.get(entityId);
            if (entityData == null) {
                entityData = new EntityData(entity);
                modifiedEntities.put(entityId, entityData);
            }
        }
        modifiedTime = System.currentTimeMillis();

        return entityData;
    }

    @Override
    public void move(Entity entity)
    {
        EntityData entityData = modify(entity);
        if (entityData != null) {
            entityData.setHasMoved(true);
        }
    }

    @Override
    public void modifyVelocity(Entity entity)
    {
        EntityData entityData = modify(entity);
        if (entityData != null) {
            entityData.setHasVelocity(true);
        }
    }

    @Override
    public void addPotionEffects(Entity entity)
    {
        EntityData entityData = modify(entity);
        if (entityData != null) {
            entityData.setHasPotionEffects(true);
        }
    }

    public void remove(Entity entity)
    {
        UUID entityId = entity.getUniqueId();
        if (entities != null && entities.contains(entityId)) {
            entities.remove(entityId);
        }
        if (modifiedEntities != null && modifiedEntities.containsKey(entityId)) {
            entities.remove(entityId);
        }
        modifiedTime = System.currentTimeMillis();
    }

    public void convert(Entity fallingBlock, Block block)
    {
        if (entities != null) {
            entities.remove(fallingBlock);
        }
        add(block, true);
        modifiedTime = System.currentTimeMillis();
    }

    public void fall(Entity fallingBlock, Block block)
    {
        add(fallingBlock);
        add(block, true);
        modifiedTime = System.currentTimeMillis();
    }

    public void explode(Entity explodingEntity, List<Block> blocks)
    {
        for (Block block : blocks) {
            add(block, true);
        }
        modifiedTime = System.currentTimeMillis();
    }

    public void finalizeExplosion(Entity explodingEntity, List<Block> blocks)
    {
        if (entities != null) {
            entities.remove(explodingEntity);
        }
        // Prevent dropping items if this is going to auto-undo
        if (isScheduled()) {
            for (Block block : blocks) {
                block.setType(Material.AIR);
            }
        }
        modifiedTime = System.currentTimeMillis();
    }

    public void cancelExplosion(Entity explodingEntity)
    {
        if (entities != null) {
            entities.remove(explodingEntity);
            modifiedTime = System.currentTimeMillis();
        }
    }

    public boolean bypass()
    {
        return bypass;
    }

    public void setBypass(boolean bypass)
    {
        this.bypass = bypass;
    }

    public long getCreatedTime()
    {
        return this.createdTime;
    }

    public long getModifiedTime()
    {
        return this.modifiedTime;
    }

    public boolean contains(Location location, int threshold)
    {
        if (location == null || area == null || worldName == null) return false;
        if (!location.getWorld().getName().equals(worldName)) return false;

        return area.contains(location.toVector(), threshold);
    }

    public void prune()
    {
        if (blockList == null) return;

        List<BlockData> current = new ArrayList<BlockData>(blockList);

        blockList = null;
        blockIdMap = null;
        for (BlockData block : current)
        {
            if (block.isDifferent()) {
                super.add(block);
            } else {
                removeFromModified(block, block.getPriorState());
                block.unlink();
            }
        }

        modifiedTime = System.currentTimeMillis();
    }

    @Override
    public String getName()
    {
        return name;
    }

    public UndoableSpell getSpell()
    {
        return spell;
    }

    @Override
    public Mage getOwner()
    {
        return owner;
    }

    @Override
    public long getScheduledTime() {
        return scheduledTime;
    }

    @Override
    public boolean isScheduled() {
        return timeToLive > 0;
    }

    @Override
    public int compareTo(com.elmakers.mine.bukkit.api.block.UndoList o) {
        return (int)(scheduledTime - o.getScheduledTime());
    }

    public void setEntityUndo(boolean undoEntityEffects) {
        this.undoEntityEffects = undoEntityEffects;
    }

    public void setNext(UndoList next) {
        this.next = next;
    }

    public void setPrevious(UndoList previous) {
        this.previous = previous;
    }

    public void setUndoQueue(com.elmakers.mine.bukkit.api.block.UndoQueue undoQueue) {
        if (undoQueue != null && undoQueue instanceof UndoQueue) {
            this.undoQueue = (UndoQueue)undoQueue;
        }
    }

    public boolean hasUndoQueue() {
        return this.undoQueue != null;
    }

    public void unlink() {
        if (undoQueue != null) {
            undoQueue.removed(this);
            undoQueue = null;
        }
        if (this.next != null) {
            this.next.previous = previous;
        }
        if (this.previous != null) {
            this.previous.next = next;
        }

        this.previous = null;
        this.next = null;
    }

    public UndoList getNext()
    {
        return next;
    }

    public UndoList getPrevious()
    {
        return previous;
    }

    public void setApplyPhysics(boolean applyPhysics) {
        this.applyPhysics = applyPhysics;
    }

    public boolean getApplyPhysics() {
        return applyPhysics;
    }

    public static com.elmakers.mine.bukkit.api.block.UndoList getUndoList(Entity entity) {
        com.elmakers.mine.bukkit.api.block.UndoList blockList = null;
        if (entity != null && entity.hasMetadata("MagicBlockList")) {
            List<MetadataValue> values = entity.getMetadata("MagicBlockList");
            for (MetadataValue metadataValue : values) {
                Object value = metadataValue.value();
                if (value instanceof com.elmakers.mine.bukkit.api.block.UndoList) {
                    blockList = (com.elmakers.mine.bukkit.api.block.UndoList)value;
                }
            }
        }

        return blockList;
    }

    public static BlockData getBlockData(Location location) {
        return modified.get(com.elmakers.mine.bukkit.block.BlockData.getBlockId(location.getBlock()));
    }

    public static com.elmakers.mine.bukkit.api.block.UndoList getUndoList(Location location) {
        com.elmakers.mine.bukkit.api.block.UndoList blockList = null;
        BlockData modifiedBlock = modified.get(com.elmakers.mine.bukkit.block.BlockData.getBlockId(location.getBlock()));
        if (modifiedBlock != null) {
            blockList = modifiedBlock.getUndoList();
        }
        return blockList;
    }

    public static Map<Long, BlockData> getModified() {
        return modified;
    }
}
