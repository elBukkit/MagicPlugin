package com.elmakers.mine.bukkit.block;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.block.ModifyType;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Hanging;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.block.BlockData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.batch.UndoBatch;
import com.elmakers.mine.bukkit.entity.EntityData;
import com.elmakers.mine.bukkit.magic.MaterialSets;

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
    public static @Nonnull MaterialSet attachables          = MaterialSets.empty();
    public static @Nonnull MaterialSet attachablesWall      = MaterialSets.empty();
    public static @Nonnull MaterialSet attachablesDouble    = MaterialSets.empty();

    protected static final UndoRegistry     registry = new UndoRegistry();
    protected static BlockComparator        blockComparator = new BlockComparator();

    protected Map<Long, BlockData>          watching;
    private boolean                         loading = false;

    protected Set<Entity> 	                entities;
    protected List<Runnable>				runnables;
    protected HashMap<UUID, EntityData> 	modifiedEntities;

    protected WeakReference<CastContext>    context;

    protected Mage			        owner;
    protected Plugin		   	    plugin;

    protected boolean               undone              = false;
    protected int                  	timeToLive          = 0;
    protected ModifyType            modifyType           = ModifyType.NO_PHYSICS;

    protected boolean				bypass		 	    = false;
    protected boolean				hasBeenScheduled    = false;
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

    private boolean                 consumed = false;
    private boolean                 undoEntityEffects = true;
    private Set<EntityType>         undoEntityTypes = null;
    protected boolean               undoBreakable = false;
    protected boolean               undoReflective = false;
    protected boolean               undoBreaking = false;

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

    @Override
    public void setBatch(Batch batch)
    {
        this.batch = batch;
    }

    @Override
    public void setSpell(Spell spell)
    {
        this.spell = spell;
        this.context = spell == null ? null : new WeakReference<>(spell.getCurrentCast());
    }

    @Override
    public boolean isEmpty()
    {
        return (
            (blockList == null || blockList.isEmpty())
        && 	(entities == null || entities.isEmpty())
        && 	(runnables == null || runnables.isEmpty()));
    }

    @Override
    public void setScheduleUndo(int ttl)
    {
        timeToLive = ttl;
        updateScheduledUndo();
    }

    @Override
    public void updateScheduledUndo() {
        if (timeToLive > 0) {
            scheduledTime = System.currentTimeMillis() + timeToLive;
        }
    }

    @Override
    public int getScheduledUndo()
    {
        return timeToLive;
    }

    @Override
    public boolean hasChanges() {
        return size() > 0 || (runnables != null && !runnables.isEmpty()) || (entities != null && !entities.isEmpty()) || (undoEntityEffects && modifiedEntities != null && !modifiedEntities.isEmpty());
    }

    @Override
    public void clearAttachables(Block block)
    {
        clearAttachables(block, BlockFace.NORTH, attachablesWall);
        clearAttachables(block, BlockFace.SOUTH, attachablesWall);
        clearAttachables(block, BlockFace.EAST, attachablesWall);
        clearAttachables(block, BlockFace.WEST, attachablesWall);
        clearAttachables(block, BlockFace.UP, attachables);
        clearAttachables(block, BlockFace.DOWN, attachables);
    }

    protected boolean clearAttachables(Block block, BlockFace direction, @Nonnull MaterialSet materials)
    {
        Block testBlock = block.getRelative(direction);
        long blockId = com.elmakers.mine.bukkit.block.BlockData.getBlockId(testBlock);

        if (!materials.testBlock(testBlock))
        {
            return false;
        }

        // Don't clear it if we've already modified it
        if (blockIdMap != null && blockIdMap.contains(blockId))
        {
            return false;
        }
        
        add(testBlock);
        MaterialAndData.clearItems(testBlock.getState());
        DeprecatedUtils.setTypeIdAndData(testBlock, DeprecatedUtils.getId(Material.AIR), (byte)0, false);
        
        return true;
    }
    
    @Override
    public boolean add(BlockData blockData)
    {
        if (bypass) return true;
        if (!super.add(blockData)) {
            return false;
        }
        modifiedTime = System.currentTimeMillis();
        if (watching != null) {
            BlockData attachedBlock = watching.remove(blockData.getId());
            if (attachedBlock != null) {
                removeFromWatched(attachedBlock);
            }
        }
        register(blockData);
        blockData.setUndoList(this);

        if (loading) return true;

        addAttachable(blockData, BlockFace.NORTH, attachablesWall);
        addAttachable(blockData, BlockFace.SOUTH, attachablesWall);
        addAttachable(blockData, BlockFace.EAST, attachablesWall);
        addAttachable(blockData, BlockFace.WEST, attachablesWall);
        addAttachable(blockData, BlockFace.UP, attachables);
        addAttachable(blockData, BlockFace.DOWN, attachables);

        return true;
    }

    protected boolean addAttachable(BlockData block, BlockFace direction, @Nonnull MaterialSet materials)
    {
        Block testBlock = block.getBlock().getRelative(direction);
        Long blockId = com.elmakers.mine.bukkit.block.BlockData.getBlockId(testBlock);

        // This gets called recursively, so don't re-process anything
        if (blockIdMap != null && blockIdMap.contains(blockId))
        {
            return false;
        }
        if (watching != null && watching.containsKey(blockId))
        {
            return false;
        }

        if (materials.testBlock(testBlock))
        {
            BlockData newBlock = new com.elmakers.mine.bukkit.block.BlockData(testBlock);
            if (contain(newBlock))
            {
                registerWatched(newBlock);
                newBlock.setUndoList(this);
                if (attachablesDouble.testBlock(testBlock))
                {
                    if (direction != BlockFace.UP)
                    {
                        add(newBlock);
                        addAttachable(newBlock, BlockFace.DOWN, materials);
                    }
                    else if (direction != BlockFace.DOWN)
                    {
                        add(newBlock);
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
        registry.registerModified(blockData);
        return blockData;
    }

    public static void register(BlockData blockData)
    {
        registry.registerModified(blockData);
    }

    public void registerWatched(BlockData blockData)
    {
        registry.registerWatched(blockData);
        if (watching == null)
        {
            watching = new HashMap<>();
        }
        watching.put(blockData.getId(), blockData);
    }

    @Override
    public void commit()
    {
        unlink();
        unregisterWatched();
        if (blockList == null) return;

        for (BlockData block : blockList)
        {
            commit(block);
        }
        clear();
    }

    public static void commitAll()
    {
        registry.commitAll();
    }

    public static void commit(com.elmakers.mine.bukkit.api.block.BlockData block)
    {
        registry.commit(block);
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

    protected static void removeFromModified(BlockData block) {
        registry.removeFromModified(block);
    }

    protected static void removeFromModified(BlockData block, BlockData priorState)
    {
        registry.removeFromModified(block, priorState);
    }

    protected static void removeFromWatched(BlockData block) {
        registry.removeFromWatched(block);
    }

    @Override
    public BlockData undoNext(boolean applyPhysics)
    {
        if (blockList.size() == 0) {
            return null;
        }
        BlockData blockData = blockList.removeFirst();
        BlockState currentState = blockData.getBlock().getState();
        if (undo(blockData, applyPhysics)) {
            blockIdMap.remove(blockData.getId());
            if (consumed && currentState.getType() != Material.AIR && owner != null) {
                owner.giveItem(new ItemStack(currentState.getType(), 1, DeprecatedUtils.getRawData(currentState)));
            }
            return blockData;
        }
        blockList.addFirst(blockData);

        return null;
    }

    private boolean undo(BlockData undoBlock, boolean applyPhysics)
    {
        BlockData priorState = undoBlock.getPriorState();

        // Remove any tagged metadata
        if (undoBreakable) {
            registry.removeBreakable(undoBlock);
        }
        if (undoReflective) {
            registry.removeReflective(undoBlock);
        }

        boolean isTopOfQueue = undoBlock.getNextState() == null;
        if (undoBlock.undo(applyPhysics ? ModifyType.NORMAL : modifyType)) {
            removeFromModified(undoBlock, priorState);
            // Continue watching this block until we completely finish the undo process
            registerWatched(undoBlock);

            // Undo breaking state only if this was the top of the queue
            if (undoBreaking && isTopOfQueue) {
                // This may have been unregistered already, if the block was broken for instance.
                if (registry.removeBreaking(undoBlock) != null) {
                    CompatibilityUtils.clearBreaking(undoBlock.getBlock());
                }
            }
            return true;
        }

        return false;
    }

    public void undoEntityEffects()
    {
        // This part doesn't happen in a batch, and may lag on large lists
        if (entities != null || modifiedEntities != null) {
            if (entities != null) {
                for (Entity entity : entities) {
                    if (entity != null) {
                        if (!entity.isValid()) {
                            if (!entity.getLocation().getChunk().isLoaded()) {
                                entity.getLocation().getChunk().load();
                            }
                            entity = NMSUtils.getEntity(entity.getWorld(), entity.getUniqueId());
                        }
                        if (entity != null && entity.isValid()) {
                            entity.remove();
                        }
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

    @Override
    public void undo()
    {
        undo(false);
    }

    @Override
    public void undo(boolean blocking)
    {
        if (spell != null)
        {
            spell.cancel();
        }
        undo(blocking, true);
    }

    @Override
    public void undoScheduled(boolean blocking)
    {
        undo(blocking, false);

        if (isScheduled())
        {
            owner.getController().cancelScheduledUndo(this);
        }
    }

    @Override
    public void undoScheduled()
    {
        undo(false, false);
    }

    public void unregisterWatched()
    {
        if (watching != null) {
            for (BlockData block : watching.values()) {
                removeFromWatched(block);
            }
            watching = null;
        }
    }

    public void undo(boolean blocking, boolean undoEntities)
    {
        if (undone) return;
        undone = true;

        if (batch != null && !batch.isFinished())
        {
            batch.finish();
        }

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
        String typeName = node.getString("mode", "");
        if (!typeName.isEmpty()) {
            try {
                modifyType = ModifyType.valueOf(typeName);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        consumed = node.getBoolean("consumed", consumed);
    }

    @Override
    public void save(ConfigurationSection node)
    {
        super.save(node);
        node.set("time_to_live", timeToLive);
        node.set("name", name);
        if (modifyType != ModifyType.NORMAL) {
            node.set("mode", modifyType.name());
        }
        if (consumed) node.set("consumed", true);
    }

    public void watch(Entity entity)
    {
        if (entity == null) return;
        if (worldName != null && !entity.getWorld().getName().equals(worldName)) return;
        if (worldName == null) worldName = entity.getWorld().getName();

        if (!entity.hasMetadata("MagicBlockList")) {
            entity.setMetadata("MagicBlockList", new FixedMetadataValue(plugin, this));
        }
        modifiedTime = System.currentTimeMillis();
    }

    @Override
    public void add(Entity entity)
    {
        if (entity == null) return;
        if (entities == null) entities = new HashSet<>();
        if (worldName != null && !entity.getWorld().getName().equals(worldName)) return;

        entities.add(entity);
        if (this.isScheduled()) {
            entity.setMetadata("temporary", new FixedMetadataValue(plugin, true));
        }
        watch(entity);
        contain(entity.getLocation().toVector());
        modifiedTime = System.currentTimeMillis();
    }

    @Override
    public void add(Runnable runnable)
    {
        if (runnable == null) return;
        if (runnables == null) runnables = new LinkedList<>();
        runnables.add(runnable);
        modifiedTime = System.currentTimeMillis();
    }

    @Override
    public EntityData modify(Entity entity)
    {
        EntityData entityData = null;
        if (entity == null || entity.hasMetadata("notarget")) return entityData;
        if (worldName != null && !entity.getWorld().getName().equals(worldName)) return entityData;
        if (worldName == null) worldName = entity.getWorld().getName();

        // Check to see if this is something we spawned, and has now been destroyed
        if (entities != null && entities.contains(entity) && !entity.isValid()) {
            entities.remove(entity);
        } else if (entity.isValid()) {
            if (modifiedEntities == null) modifiedEntities = new HashMap<>();
            UUID entityId = entity.getUniqueId();
            entityData = modifiedEntities.get(entityId);
            if (entityData == null) {
                entityData = new EntityData(entity);
                modifiedEntities.put(entityId, entityData);
                watch(entity);
            }
        }
        modifiedTime = System.currentTimeMillis();

        return entityData;
    }

    @Override
    public EntityData damage(Entity entity) {
        EntityData data = modify(entity);
        // Kind of a hack to prevent dropping hanging entities that we're going to undo later
        if (undoEntityTypes != null && undoEntityTypes.contains(entity.getType()))
        {
            if (data != null)
            {
                data.removed(entity);
            }
            // Hacks upon hacks, this prevents item dupe exploits with shooting items out of item
            // frames.
            if (entity instanceof Hanging)
            {
                entity.remove();
            }
        }
        if (data != null)
        {
            data.setRespawn(true);
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

    @Override
    public void remove(Entity entity)
    {
        entity.removeMetadata("MagicBlockList", plugin);
        if (entities != null) {
            entities.remove(entity);
        }
        UUID entityId = entity.getUniqueId();
        if (modifiedEntities != null) {
            modifiedEntities.remove(entityId);
        }
        modifiedTime = System.currentTimeMillis();
    }

    @Override
    public void convert(Entity fallingBlock, Block block)
    {
        remove(fallingBlock);
        add(block);
    }

    @Override
    public void fall(Entity fallingBlock, Block block)
    {
        if (isScheduled() && fallingBlock instanceof FallingBlock) {
            ((FallingBlock)fallingBlock).setDropItem(false);
        }
        add(fallingBlock);
        add(block);
        modifiedTime = System.currentTimeMillis();
    }

    @Override
    public void explode(Entity explodingEntity, List<Block> blocks)
    {
        for (Block block : blocks) {
            add(block);
        }
        modifiedTime = System.currentTimeMillis();
    }

    @Override
    public void finalizeExplosion(Entity explodingEntity, List<Block> blocks)
    {
        remove(explodingEntity);
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

    @Override
    public void cancelExplosion(Entity explodingEntity)
    {
        // Stop tracking this entity
        remove(explodingEntity);
    }

    @Override
    public boolean bypass()
    {
        return bypass;
    }

    @Override
    public void setBypass(boolean bypass)
    {
        this.bypass = bypass;
    }

    @Override
    public long getCreatedTime()
    {
        return this.createdTime;
    }

    @Override
    public long getModifiedTime()
    {
        return this.modifiedTime;
    }

    @Override
    public boolean contains(Location location, int threshold)
    {
        if (location == null || area == null || worldName == null) return false;
        if (!location.getWorld().getName().equals(worldName)) return false;

        return area.contains(location.toVector(), threshold);
    }

    @Override
    public void prune()
    {
        if (blockList == null) return;

        Iterator<BlockData> iterator = iterator();
        while (iterator.hasNext()) {
            BlockData block = iterator.next();
            if (!block.isDifferent()) {
                removeFromMap(block);
                iterator.remove();
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
    public boolean hasBeenScheduled() {
        return hasBeenScheduled;
    }

    @Override
    public void setHasBeenScheduled() {
        hasBeenScheduled = true;
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
        if (applyPhysics) {
            this.modifyType = ModifyType.NORMAL;
        } else if (this.modifyType != ModifyType.FAST) {
            this.modifyType = ModifyType.NO_PHYSICS;
        }
    }

    @Override
    public boolean getApplyPhysics() {
        return modifyType == ModifyType.NORMAL;
    }

    @Override
    public void setModifyType(ModifyType modifyType) {
        this.modifyType = modifyType;
    }

    @Override
    public ModifyType getModifyType() {
        return modifyType;
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
        } else if (entity != null && entity instanceof FallingBlock) {
            // Falling blocks need to check their location to handle chain reaction effects
            Location entityLocation = entity.getLocation();
            blockList = getUndoList(entityLocation);
            if (blockList == null) {
                // Check one block down as well, in case a spell removed the block underneath a falling block
                entityLocation.setY(entityLocation.getY() - 1);
                blockList = getUndoList(entityLocation);
            }
        }

        return blockList;
    }

    public static BlockData getBlockData(Location location) {
        return registry.getBlockData(location);
    }

    public static com.elmakers.mine.bukkit.api.block.UndoList getUndoList(Location location) {
        BlockData blockData = getBlockData(location);
        return blockData == null ? null : blockData.getUndoList();
    }

    public static UndoRegistry getRegistry() {
        return registry;
    }

    @Override
    public void setUndoBreakable(boolean breakable) {
        this.undoBreakable = breakable;
    }

    @Override
    public void setUndoReflective(boolean reflective) {
        this.undoReflective = reflective;
    }

    @Override
    public void setUndoBreaking(boolean breaking) {
        this.undoBreaking = breaking;
    }

    @Override
    public Collection<Entity> getAllEntities() {
        ArrayList<Entity> entities = new ArrayList<>();
        if (this.entities != null)
        {
            for (Entity entity : this.entities)
            {
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

    public void sort(MaterialSet attachables) {
        if (blockList == null) return;

        Collections.reverse(blockList);
        if (attachables == null) {
            return;
        }
        blockComparator.setAttachables(attachables);
        Collections.sort(blockList, blockComparator);
    }

    public double getUndoSpeed() {
        return speed;
    }

    @Override
    public void setUndoSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public boolean isConsumed() {
        return consumed;
    }

    @Override
    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    public void removeFromMap(BlockData blockData) {
        removeFromModified(blockData);
        blockIdMap.remove(blockData.getId());
    }
}
