package com.elmakers.mine.bukkit.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import com.elmakers.mine.bukkit.spell.UndoableSpell;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
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

    protected Set<UUID> 		   			entities;
    protected List<Runnable>				runnables;
    protected HashMap<UUID, EntityData> 	modifiedEntities;

    protected final Mage			owner;
    protected final Plugin		   	plugin;

    protected int                  	passesRemaining  = 1;
    protected int                  	timeToLive       = 0;

    protected boolean				bypass		 	= false;
    protected final long			createdTime;
    protected long					modifiedTime;
    protected long                  scheduledTime;

    // Doubly-linked list
    protected final UndoableSpell   spell;
    protected UndoQueue             undoQueue;
    protected UndoList              next;
    protected UndoList              previous;

    protected String				name;

    public UndoList(Mage mage, UndoableSpell spell, String name)
    {
        this(mage, spell);
        this.name = name;
    }

    public UndoList(Mage mage, UndoableSpell spell)
    {
        setUndoQueue(mage.getUndoQueue());
        this.owner = mage;
        this.spell = spell;
        this.plugin = owner.getController().getPlugin();
        createdTime = System.currentTimeMillis();
        modifiedTime = createdTime;
    }

    public UndoList(UndoList other)
    {
        super(other);
        this.worldName = other.worldName;
        this.owner = other.owner;
        this.name = other.name;
        this.plugin = other.plugin;
        this.undoQueue = other.undoQueue;
        this.spell = other.spell;
        timeToLive = other.timeToLive;
        passesRemaining = other.passesRemaining;
        createdTime = other.createdTime;
        modifiedTime = other.modifiedTime;
        scheduledTime = other.scheduledTime;
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

    public void setRepetitions(int repeat)
    {
        passesRemaining = repeat;
    }

    public boolean isComplete()
    {
        return passesRemaining <= 0;
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
        if (!super.add(blockData)) return false;
        modifiedTime = System.currentTimeMillis();
        if (bypass) return true;

        BlockData priorState = modified.get(blockData.getId());
        if (priorState != null)
        {
            priorState.setNextState(blockData);
            blockData.setPriorState(priorState);
        }

        modified.put(blockData.getId(), blockData);
        return true;
    }

    public void commit()
    {
        unlink();
        if (blockList == null) return;

        for (BlockData block : blockList)
        {
            BlockData currentState = modified.get(block.getId());
            if (currentState == block)
            {
                modified.remove(block.getId());
            }

            block.commit();
        }
    }

    @Override
    public boolean remove(Object o)
    {
        if (o instanceof BlockData)
        {
            BlockData block = (BlockData)o;
            removeFromModified(block);
        }

        return super.remove(o);
    }

    protected static void removeFromModified(BlockData block)
    {
        BlockData currentState = modified.get(block.getId());
        if (currentState == block)
        {
            BlockData priorState = block.getPriorState();
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

    public static boolean undo(BlockData undoBlock)
    {
        if (undoBlock.undo()) {
            removeFromModified(undoBlock);
            return true;
        }

        return false;
    }


    public void undo()
    {
        undo(true);
    }

    public void undo(boolean undoEntityEffects)
    {
        unlink();
        if (isComplete()) return;

        if (spell != null)
        {
            spell.cancelEffects();
        }

        // This part doesn't happen in a batch, and may lag on large lists
        if (entities != null || modifiedEntities != null) {
            Map<UUID, Entity> currentEntities = new HashMap<UUID, Entity>();
            World world = worldName != null && worldName.length() > 0 ? Bukkit.getWorld(worldName) : null;
            if (world != null) {
                List<Entity> entities = world.getEntities();
                for (Entity entity : entities) {
                    currentEntities.put(entity.getUniqueId(), entity);
                }
            }
            if (entities != null) {
                for (UUID entityID : entities) {
                    Entity entity = currentEntities.get(entityID);
                    if (entity != null && entity.isValid()) {
                        entity.remove();
                    }
                }
                entities = null;
            }
            if (undoEntityEffects && modifiedEntities != null) {
                for (Entry<UUID, EntityData> entry : modifiedEntities.entrySet()) {
                    Entity entity = currentEntities.get(entry.getKey());
                    if (entity == null) {
                        entry.getValue().spawn();
                    } else {
                        entry.getValue().modify(entity);
                    }
                }
                modifiedEntities = null;
            }
        }

        if (runnables != null) {
            for (Runnable runnable : runnables) {
                runnable.run();
            }
            runnables = null;
        }

        if (blockList == null) return;

        // Block changes will be performed in a batch
        UndoBatch batch = new UndoBatch(this);
        owner.addUndoBatch(batch);
        passesRemaining--;
        if (isComplete()) {
            blockList = null;
        }
    }

    @Override
    public void load(ConfigurationSection node)
    {
        super.load(node);
        timeToLive = node.getInt("time_to_live", timeToLive);
        passesRemaining = node.getInt("passes_remaining", passesRemaining);
        name = node.getString("name", name);
    }

    @Override
    public void save(ConfigurationSection node)
    {
        super.save(node);
        node.set("time_to_live", (Integer)timeToLive);
        node.set("passes_remaining", (Integer) passesRemaining);
        node.set("name", name);
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
        if (entities == null) entities = new HashSet<UUID>();
        if (worldName != null && !entity.getWorld().getName().equals(worldName)) return;

        entities.add(entity.getUniqueId());
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
        if (entities != null && entities.contains(entity) && !entity.isValid()) {
            entities.remove(entity);
        } else {
            if (modifiedEntities == null) modifiedEntities = new HashMap<UUID, EntityData>();
            entityData = modifiedEntities.get(entity);
            if (entityData == null) {
                entityData = new EntityData(entity);
                modifiedEntities.put(entity.getUniqueId(), entityData);
            }
        }
        modifiedTime = System.currentTimeMillis();

        return entityData;
    }

    public void move(Entity entity)
    {
        EntityData entityData = modify(entity);
        if (entityData != null) {
            entityData.setHasMoved(true);
        }
    }

    public void modifyVelocity(Entity entity)
    {
        EntityData entityData = modify(entity);
        if (entityData != null) {
            entityData.setHasVelocity(true);
        }
    }

    public void addPotionEffects(Entity entity)
    {
        EntityData entityData = modify(entity);
        if (entityData != null) {
            entityData.setHasPotionEffects(true);
        }
    }

    public void remove(Entity entity)
    {
        if (entities != null && entities.contains(entity)) {
            entities.remove(entity);
        }
        if (modifiedEntities != null && modifiedEntities.containsKey(entity)) {
            entities.remove(entity);
        }
        modifiedTime = System.currentTimeMillis();
    }

    public void convert(Entity fallingBlock, Block block)
    {
        if (entities != null) {
            entities.remove(fallingBlock);
        }
        add(block);
        modifiedTime = System.currentTimeMillis();
    }

    public void fall(Entity fallingBlock, Block block)
    {
        add(fallingBlock);
        add(block);
        modifiedTime = System.currentTimeMillis();
    }

    public void explode(Entity explodingEntity, List<Block> blocks)
    {
        if (entities != null) {
            entities.remove(explodingEntity);
        }
        for (Block block : blocks) {
            add(block);
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
                removeFromModified(block);
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
}
