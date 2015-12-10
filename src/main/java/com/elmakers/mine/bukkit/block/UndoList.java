package com.elmakers.mine.bukkit.block;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.spell.Spell;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.block.BlockData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.batch.UndoBatch;
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
    public static Set<Material>         attachables;
    public static Set<Material>         attachablesWall;
    public static Set<Material>         attachablesDouble;

    protected static Map<Long, BlockData> modified = new HashMap<Long, BlockData>();
    protected static Map<Long, Double> reflective = new HashMap<Long, Double>();
    protected static Map<Long, Double> breakable = new HashMap<Long, Double>();
    protected static BlockComparator blockComparator = new BlockComparator();

    protected Map<Long, BlockData>  attached;
    private boolean                 loading = false;

    protected List<WeakReference<Entity>> 	entities;
    protected List<Runnable>				runnables;
    protected HashMap<UUID, EntityData> 	modifiedEntities;

    protected WeakReference<CastContext>    context;

    protected Mage			        owner;
    protected Plugin		   	    plugin;

    protected boolean               undone              = false;
    protected int                  	timeToLive          = 0;
    protected boolean               applyPhysics        = false;

    protected boolean				bypass		 	    = false;
    protected final long			createdTime;
    protected long					modifiedTime;
    protected long                  scheduledTime;
    protected double                speed = 0;

    protected Spell                 spell;
    protected Batch                 batch;
    protected UndoQueue             undoQueue;

    // Doubly-linked list
    protected UndoList              next;
    protected UndoList              previous;

    protected String				name;

    private boolean                 undoEntityEffects = true;
    private Set<EntityType>         undoEntityTypes = null;
    protected boolean               undoBreakable = false;
    protected boolean               undoReflective = false;

    public UndoList(Mage mage, String name)
    {
        this(mage);
        this.name = name;
    }

    public UndoList(Mage mage)
    {
        setMage(mage);
        createdTime = System.currentTimeMillis();
        modifiedTime = createdTime;
    }

    public void setMage(Mage mage) {
        this.owner = mage;
        if (mage != null) {
            this.plugin = mage.getController().getPlugin();
        }
    }

    public void setBatch(Batch batch)
    {
        this.batch = batch;
    }

    @Override
    public void setSpell(Spell spell)
    {
        this.spell = spell;
        this.context = spell == null ? null : new WeakReference<CastContext>(spell.getCurrentCast());
    }

    @Override
    public boolean isEmpty()
    {
        return (
            (blockList == null || blockList.isEmpty())
        && 	(entities == null || entities.isEmpty())
        && 	(runnables == null || runnables.isEmpty()));
    }

    public void setScheduleUndo(int ttl)
    {
        timeToLive = ttl;
        updateScheduledUndo();
    }

    public void updateScheduledUndo() {
        if (timeToLive > 0) {
            scheduledTime = System.currentTimeMillis() + timeToLive;
        }
    }

    public int getScheduledUndo()
    {
        return timeToLive;
    }

    @Override
    public boolean contains(Block block)
    {
        if (blockIdMap == null) return false;
        Long blockId = com.elmakers.mine.bukkit.block.BlockData.getBlockId(block);
        if (attached != null && attached.containsKey(blockId)) return false;
        return blockIdMap.contains(blockId);
    }

    @Override
    public boolean contains(BlockData blockData)
    {
        if (blockIdMap == null || blockData == null)
        {
            return false;
        }
        Long blockId = blockData.getId();
        if (attached != null && attached.containsKey(blockId)) return false;
        return blockIdMap.contains(blockData.getId());
    }

    @Override
    public boolean add(BlockData blockData)
    {
        if (bypass) return true;
        if (!super.add(blockData)) {
            return false;
        }
        modifiedTime = System.currentTimeMillis();

        register(blockData);
        blockData.setUndoList(this);

        if (loading) return true;

        if (attached != null) {
            attached.remove(blockData.getId());
        }

        addAttachable(blockData, BlockFace.NORTH, attachablesWall);
        addAttachable(blockData, BlockFace.SOUTH, attachablesWall);
        addAttachable(blockData, BlockFace.EAST, attachablesWall);
        addAttachable(blockData, BlockFace.WEST, attachablesWall);
        addAttachable(blockData, BlockFace.UP, attachables);
        addAttachable(blockData, BlockFace.DOWN, attachables);

        return true;
    }

    protected boolean addAttachable(BlockData block, BlockFace direction, Set<Material> materials)
    {
        Block testBlock = block.getBlock().getRelative(direction);
        Long blockId = com.elmakers.mine.bukkit.block.BlockData.getBlockId(testBlock);

        // This gets called recursively, so don't re-process anything
        if (blockIdMap != null && blockIdMap.contains(blockId))
        {
            return false;
        }
        if (attached != null && attached.containsKey(blockId))
        {
            return false;
        }
        Material material = testBlock.getType();
        if (material.isBurnable() || (materials != null && materials.contains(material)))
        {
            BlockData newBlock = new com.elmakers.mine.bukkit.block.BlockData(testBlock);
            if (super.add(newBlock))
            {
                register(newBlock);
                newBlock.setUndoList(this);
                if (attached == null)
                {
                    attached = new HashMap<Long, BlockData>();
                }
                attached.put(blockId, newBlock);
                if (attachablesDouble != null && attachablesDouble.contains(material))
                {
                    if (direction != BlockFace.UP)
                    {
                        addAttachable(newBlock, BlockFace.DOWN, materials);
                    }
                    else if (direction != BlockFace.DOWN)
                    {
                        addAttachable(newBlock, BlockFace.UP, materials);
                    }
                }
                return true;
            }
        }
        return false;
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

    public static void commitAll()
    {
        Collection<BlockData> blocks = modified.values();
        modified.clear();
        for (BlockData block : blocks) {
            block.commit();
        }
    }

    public static void commit(com.elmakers.mine.bukkit.api.block.BlockData block)
    {
        modified.remove(block.getId());
        BlockData currentState = modified.get(block.getId());
        if (currentState == block)
        {
            modified.remove(block.getId());
        }
        block.commit();
        reflective.remove(block.getId());
        breakable.remove(block.getId());
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

    public BlockData undoNext(boolean applyPhysics)
    {
        if (blockList.size() == 0) {
            return null;
        }
        BlockData blockData = blockList.removeFirst();
        if (undo(blockData, applyPhysics)) {
            return blockData;
        }
        blockList.addFirst(blockData);

        return null;
    }

    public boolean undo(BlockData undoBlock, boolean applyPhysics)
    {
        BlockData priorState = undoBlock.getPriorState();

        // Remove any tagged metadata
        if (undoBreakable) {
            breakable.remove(undoBlock.getId());
        }
        if (undoReflective) {
            reflective.remove(undoBlock.getId());
        }

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
                    if (!undoEntityEffects && undoEntityTypes != null && !undoEntityTypes.contains(data.getType())) continue;
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
        if (spell != null)
        {
            spell.cancel();
        }
        if (batch != null && !batch.isFinished())
        {
            batch.finish();
        }
        undo(blocking, true);
        if (isScheduled())
        {
            owner.getController().cancelScheduledUndo(this);
        }
    }

    public void undoScheduled(boolean blocking)
    {
        undo(blocking, false);
    }

    public void undoScheduled()
    {
        undoScheduled(false);
    }

    public void unregisterAttached()
    {
        if (attached != null) {
            for (BlockData block : attached.values()) {
                removeFromModified(block, block.getPriorState());
                block.unlink();
            }
            attached = null;
        }
    }

    public void undo(boolean blocking, boolean undoEntities)
    {
        if (undone) return;
        undone = true;

        // This is a hack to make forced-undo happen instantly
        if (undoEntities) {
            this.speed = 0;
        }

        undoEntityEffects = undoEntityEffects || undoEntities;
        unlink();

        CastContext context = getContext();
        if (context != null)
        {
            context.cancelEffects();
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
    }

    @Override
    public void load(ConfigurationSection node)
    {
        loading = true;
        super.load(node);
        loading = false;
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
        if (entity == null || entity.hasMetadata("notarget")) return entityData;
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
    public EntityData damage(Entity entity, double damage) {
        EntityData data = modify(entity);
        // Kind of a hack to prevent dropping things we're going to undo later
        if (undoEntityTypes != null && undoEntityTypes.contains(entity.getType()))
        {
            data.removed(entity);
            entity.remove();
        }
        return data;
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
        for (Block block : blocks) {
            add(block);
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
                BlockState state = block.getState();
                if (state instanceof InventoryHolder) {
                    InventoryHolder holder = (InventoryHolder)state;
                    holder.getInventory().clear();
                    state.update();
                }
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

    @Override
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

    public Spell getSpell()
    {
        return spell;
    }

    @Override
    public Mage getOwner()
    {
        return owner;
    }

    @Override
    public CastContext getContext() {
        return context == null ? null : context.get();
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

    @Override
    public void setEntityUndo(boolean undoEntityEffects) {
        this.undoEntityEffects = undoEntityEffects;
    }

    @Override
    public void setEntityUndoTypes(Set<EntityType> undoTypes) {
        this.undoEntityTypes = undoTypes;
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

    @Override
    public void setApplyPhysics(boolean applyPhysics) {
        this.applyPhysics = applyPhysics;
    }

    @Override
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

    public static Map<Long, Double> getReflective() {
        return reflective;
    }

    public static Map<Long, Double> getBreakable() {
        return breakable;
    }

    public void setUndoBreakable(boolean breakable) {
        this.undoBreakable = breakable;
    }

    public void setUndoReflective(boolean reflective) {
        this.undoReflective = reflective;
    }

    @Override
    public Collection<Entity> getAllEntities() {
        ArrayList<Entity> entities = new ArrayList<Entity>();
        if (this.entities != null)
        {
            for (WeakReference<Entity> entityReference : this.entities)
            {
                Entity entity = entityReference.get();
                if (entity != null)
                {
                    entities.add(entity);
                }
            }
        }
        if (this.modifiedEntities != null)
        {
            for (EntityData entityData : this.modifiedEntities.values())
            {
                Entity entity = entityData.getEntity();
                if (entity != null)
                {
                    entities.add(entity);
                }
            }
        }
        return entities;
    }

    public void sort(Set<Material> attachables) {
        if (blockList == null) return;

        Collections.reverse(blockList);
        if (attachables == null) {
            return;
        }
        blockComparator.setAttachables(attachables);
        Collections.sort(blockList, blockComparator);
    }

    public static boolean isReflective(Block block) {
        return block != null && reflective.containsKey(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block));
    }

    public static boolean isBreakable(Block block) {
        return block != null && breakable.containsKey(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block));
    }

    public static Double getReflective(Block block) {
        return block == null ? null : reflective.get(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block));
    }

    public static Double getBreakable(Block block) {
        return block == null ? null : breakable.get(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block));
    }

    public static void registerReflective(Block block, double amount) {
        if (block == null) return;
        reflective.put(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block), amount);
    }

    public static void registerBreakable(Block block, double amount) {
        if (block == null) return;
        breakable.put(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block), amount);
    }

    public static void unregisterBreakable(Block block) {
        if (block == null) return;
        breakable.remove(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block));
    }

    public static void unregisterReflective(Block block) {
        if (block == null) return;
        reflective.remove(com.elmakers.mine.bukkit.block.BlockData.getBlockId(block));
    }

    public double getUndoSpeed() {
        return speed;
    }

    public void setUndoSpeed(double speed) {
        this.speed = speed;
    }
}
