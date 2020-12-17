package com.elmakers.mine.bukkit.block;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.block.BlockData;
import com.elmakers.mine.bukkit.api.block.ModifyType;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.batch.UndoBatch;
import com.elmakers.mine.bukkit.entity.EntityData;
import com.elmakers.mine.bukkit.entity.SpawnedEntity;
import com.elmakers.mine.bukkit.materials.MaterialSets;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.EntityMetadataUtils;
import com.google.common.base.Preconditions;

/**
 * Implements a Collection of Blocks, for quick getting/putting while iterating
 * over a set or area of blocks.
 *
 * <p>This stores BlockData objects, which are hashable via their Persisted
 * inheritance, and their LocationData id (which itself has a hash function
 * based on world name and BlockVector's hash function)
 */
public class UndoList extends BlockList implements com.elmakers.mine.bukkit.api.block.UndoList
{
    public static @Nonnull MaterialSet attachables          = MaterialSets.empty();
    public static @Nonnull MaterialSet attachablesWall      = MaterialSets.empty();
    public static @Nonnull MaterialSet attachablesDouble    = MaterialSets.empty();

    protected static final UndoRegistry             registry = new UndoRegistry();
    protected static final Map<Entity, com.elmakers.mine.bukkit.api.block.UndoList>    watchedEntities = new WeakHashMap<>();
    protected static BlockComparator                blockComparator = new BlockComparator();

    protected Map<Long, BlockData>          watching;
    private Set<String>                     worlds = new HashSet<>();
    private boolean                         loading = false;

    protected Deque<Runnable>                 runnables;
    protected Map<UUID, SpawnedEntity>      spawnedEntities;
    protected Map<UUID, EntityData>         modifiedEntities;

    protected WeakReference<CastContext>    context;

    protected WeakReference<Mage>     owner;
    protected MageController          controller;
    protected Plugin                   plugin;

    protected boolean               undone              = false;
    protected boolean               finished              = false;
    protected int                      timeToLive          = 0;
    protected ModifyType            modifyType           = ModifyType.NO_PHYSICS;
    protected boolean               lockChunks = false;

    protected boolean                bypass                 = false;
    protected boolean                hasBeenScheduled    = false;
    protected final long            createdTime;
    protected long                    modifiedTime;
    protected long                  scheduledTime;
    protected double                speed = 0;

    protected WeakReference<Spell>  spell;
    protected WeakReference<Batch>  batch;
    protected UndoQueue             undoQueue;

    // Doubly-linked list
    protected UndoList              next;
    protected UndoList              previous;

    protected String                name;

    private boolean                 consumed = false;
    private boolean                 undoEntityEffects = true;
    private Set<EntityType>         undoEntityTypes = null;
    protected boolean               undoBreakable = false;
    protected boolean               undoReflective = false;
    protected boolean               sorted = true;
    protected boolean               reverse = true;
    protected boolean               unbreakable = false;

    public UndoList(Mage mage, String name)
    {
        this(mage);
        this.name = name;
    }

    public UndoList(@Nonnull MageController controller)
    {
        this.controller = controller;
        this.plugin = controller.getPlugin();
        createdTime = System.currentTimeMillis();
        modifiedTime = createdTime;
    }

    public UndoList(Mage mage)
    {
        setMage(mage);
        createdTime = System.currentTimeMillis();
        modifiedTime = createdTime;
    }

    public void setMage(Mage mage) {
        this.owner = mage == null ? null : new WeakReference<>(mage);
        if (mage != null) {
            this.plugin = mage.getController().getPlugin();
            this.controller = mage.getController();
        }
    }

    @Override
    public void setBatch(Batch batch)
    {
        this.batch = batch == null ? null : new WeakReference<>(batch);
    }

    @Override
    public void setSpell(Spell spell)
    {
        this.spell = spell == null ? null : new WeakReference<>(spell);
        this.context = spell == null ? null : new WeakReference<>(spell.getCurrentCast());
    }

    @Override
    public boolean isEmpty()
    {
        return (
            (blockList == null || blockList.isEmpty())
        &&     (spawnedEntities == null || spawnedEntities.isEmpty())
        &&     (runnables == null || runnables.isEmpty()));
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
        return size() > 0 || (runnables != null && !runnables.isEmpty()) || (spawnedEntities != null && !spawnedEntities.isEmpty()) || (undoEntityEffects && modifiedEntities != null && !modifiedEntities.isEmpty());
    }

    @Override
    public void clearAttachables(Block block)
    {
        clearAttachables(block, BlockFace.NORTH, attachablesWall);
        clearAttachables(block, BlockFace.SOUTH, attachablesWall);
        clearAttachables(block, BlockFace.EAST, attachablesWall);
        clearAttachables(block, BlockFace.WEST, attachablesWall);
        clearAttachables(block, BlockFace.UP, attachables);
        clearAttachables(block, BlockFace.DOWN, attachablesDouble);
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
        if (blockIdMap != null && blockIdMap.containsKey(blockId))
        {
            return false;
        }

        add(testBlock);
        MaterialAndData.clearItems(testBlock.getState());
        DeprecatedUtils.setTypeAndData(testBlock, Material.AIR, (byte)0, false);

        return true;
    }

    @Override
    public boolean add(BlockData blockData)
    {
        if (finished) {
            controller.getLogger().warning("Trying to add to a finished UndoList, this may result in blocks that don't get cleaned up: " + name);
            Thread.dumpStack();
        }
        if (bypass) return true;
        if (!controller.isUndoable(blockData.getMaterial())) {
            return false;
        }
        if (!super.add(blockData)) {
            return false;
        }
        worlds.add(blockData.getWorldName());
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
        addAttachable(blockData, BlockFace.DOWN, attachablesDouble);

        return true;
    }

    @Override
    public void add(Entity entity)
    {
        if (entity == null) return;
        if (spawnedEntities == null) spawnedEntities = new HashMap<>();
        if (worldName != null && !entity.getWorld().getName().equals(worldName)) return;

        spawnedEntities.put(entity.getUniqueId(), new SpawnedEntity(entity));
        if (this.isScheduled()) {
            EntityMetadataUtils.instance().setBoolean(entity, "magicspawned", true);
        }
        watch(entity);
        contain(entity.getLocation().toVector());
        modifiedTime = System.currentTimeMillis();
    }

    @Override
    public void add(Runnable runnable)
    {
        if (runnable == null) return;
        if (runnables == null) runnables = new ArrayDeque<>();
        runnables.add(runnable);
        modifiedTime = System.currentTimeMillis();
    }

    protected boolean addAttachable(BlockData block, BlockFace direction, @Nonnull MaterialSet materials)
    {
        Preconditions.checkNotNull(block, "block");
        Preconditions.checkNotNull(direction, "direction");

        Block baseBlock = block.getBlock();
        if (baseBlock == null) {
            // World unloaded.
            // TODO: Move this check somewhere else?
            return false;
        }

        Block testBlock = baseBlock.getRelative(direction);
        Long blockId = com.elmakers.mine.bukkit.block.BlockData.getBlockId(testBlock);

        // This gets called recursively, so don't re-process anything
        if (blockIdMap != null && blockIdMap.containsKey(blockId))
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

    public static void commit(com.elmakers.mine.bukkit.api.block.BlockData block) {
        registry.commit(block);
    }

    public static void commitAll()
    {
        registry.commitAll();
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

    @Override
    public void remove(Entity entity) {
        watchedEntities.remove(entity);
        UUID entityId = entity.getUniqueId();
        if (spawnedEntities != null) {
            spawnedEntities.remove(entityId);
        }
        if (modifiedEntities != null) {
            modifiedEntities.remove(entityId);
        }
        modifiedTime = System.currentTimeMillis();
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

    @Nullable
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
            Mage owner = getOwner();
            if (consumed && !isScheduled() && currentState.getType() != Material.AIR && owner != null) {
                owner.giveItem(new ItemStack(currentState.getType(), 1, DeprecatedUtils.getRawData(currentState)));
            }

            CastContext context = getContext();
            if (context != null && context.hasEffects("undo_block")) {
                Block block = blockData.getBlock();
                if (block.getType() != currentState.getType()) {
                    context.playEffects("undo_block", 1.0f, null, null, block.getLocation(), null, block);
                }
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

        if (undoBlock.undo(applyPhysics ? ModifyType.NORMAL : modifyType)) {
            removeFromModified(undoBlock, priorState);
            // Continue watching this block until we completely finish the undo process
            registerWatched(undoBlock);

            Double remainingDamage = registry.removeDamage(undoBlock);
            if (remainingDamage != null) {
                if (remainingDamage <= 0) {
                    CompatibilityUtils.clearBreaking(undoBlock.getBlock());
                } else {
                    CompatibilityUtils.setBreaking(undoBlock.getBlock(), remainingDamage);
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public void undo()
    {
        undo(false);
    }

    @Override
    public void undo(boolean blocking)
    {
        Spell spell = getSpell();
        if (spell != null)
        {
            spell.cancel();
        }
        undo(blocking, true);
    }

    public void undo(boolean blocking, boolean undoEntities)
    {
        if (undone) return;
        undone = true;

        Batch batch = getBatch();
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

        // This will happen again when the batch is finished, but doing it first as well prevents
        // hanging entities from breaking
        undoEntityEffects();

        // Block changes will be performed in a batch
        UndoBatch undoBatch = new UndoBatch(this);
        Mage owner = getOwner();
        if (blocking || owner == null) {
            while (!undoBatch.isFinished()) {
                undoBatch.process(1000);
            }
        } else {
            owner.addUndoBatch(undoBatch);
        }
    }

    public void undoEntityEffects()
    {
        // This part doesn't happen in a batch, and may lag on large lists
        if (spawnedEntities != null || modifiedEntities != null) {
            if (spawnedEntities != null) {
                for (SpawnedEntity entity : spawnedEntities.values()) {
                    entity.despawn(controller, getContext());
                }
                spawnedEntities = null;
            }
            if (modifiedEntities != null) {
                for (EntityData data : modifiedEntities.values()) {
                    Entity entity = data.getEntity();
                    if (entity != null) {
                        setUndoList(entity, null);
                    }
                    if (!undoEntityEffects && undoEntityTypes != null && !undoEntityTypes.contains(data.getType())) continue;

                    CastContext context = getContext();
                    if (context != null && entity != null && context.hasEffects("undo_entity")) {
                        context.playEffects("undo_entity", 1.0f, null, null, entity.getLocation(), entity, null);
                    }

                    try {
                        data.undo();
                    } catch (Exception ex) {
                        if (context != null) {
                            context.getLogger().log(Level.WARNING, "Error restoring entity on undo", ex);
                        } else {
                            ex.printStackTrace();
                        }
                    }

                    // Check for playing effects on resurrected entities
                    if (entity == null && context != null) {
                        entity = data.getEntity();
                        if (entity != null && context.hasEffects("undo_entity")) {
                            context.playEffects("undo_entity", 1.0f, null, null, entity.getLocation(), entity, null);
                        }
                    }
                }
                modifiedEntities = null;
            }
        }
    }

    @Override
    public boolean isUndone()
    {
        return undone;
    }

    @Override
    public boolean isUnbreakable() {
        return unbreakable;
    }

    @Override
    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    @Override
    public void undoScheduled(boolean blocking)
    {
        undo(blocking, false);

        Mage owner = getOwner();
        if (isScheduled() && owner != null)
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

    public void finish() {
        finished = true;
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

        setUndoList(entity, this);
        modifiedTime = System.currentTimeMillis();
    }

    @Nullable
    @Override
    public EntityData modify(Entity entity) {
        EntityData entityData = null;
        if (entity == null || EntityMetadataUtils.instance().getBoolean(entity, "notarget")) return entityData;
        if (worldName != null && !entity.getWorld().getName().equals(worldName)) return entityData;
        if (worldName == null) worldName = entity.getWorld().getName();

        // Check to see if this is something we spawned, and has now been destroyed
        UUID entityId = entity.getUniqueId();
        if (spawnedEntities != null && spawnedEntities.containsKey(entityId) && !entity.isValid()) {
            spawnedEntities.remove(entityId);
        } else if (entity.isValid()) {
            if (modifiedEntities == null) modifiedEntities = new HashMap<>();
            entityData = modifiedEntities.get(entityId);
            if (entityData == null) {
                entityData = new EntityData(controller, entity);
                modifiedEntities.put(entityId, entityData);
                watch(entity);
            }
        }
        modifiedTime = System.currentTimeMillis();

        return entityData;
    }

    @Nullable
    @Override
    public EntityData damage(Entity entity) {
        EntityData data = modify(entity);
        // Kind of a hack to prevent dropping hanging entities that we're going to undo later
        if (undoEntityTypes != null && undoEntityTypes.contains(entity.getType()))
        {
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
            data.setDamaged(true);
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
    public void addPotionEffectForRemoval(Entity entity, PotionEffectType potionEffectType)
    {
        EntityData entityData = modify(entity);
        if (entityData != null) {
            entityData.addPotionEffectForRemoval(potionEffectType);
        }
    }

    @Override
    public void addModifier(Entity entity, com.elmakers.mine.bukkit.api.magic.MageModifier modifier) {
        EntityData entityData = modify(entity);
        if (entityData != null) {
            entityData.addModifier(modifier);
        }
    }

    @Override
    public void addModifierForRemoval(Entity entity, String modifierKey) {
        EntityData entityData = modify(entity);
        if (entityData != null) {
            entityData.addModifierForRemoval(modifierKey);
        }
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
    public boolean isBypass() {
        return this.bypass;
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

    @Nullable
    public Spell getSpell()
    {
        return spell == null ? null : spell.get();
    }

    @Nullable
    public Batch getBatch()
    {
        return batch == null ? null : batch.get();
    }

    @Override
    @Nullable
    public Mage getOwner()
    {
        return owner == null ? null : owner.get();
    }

    public MageController getController() {
        return controller;
    }

    @Nullable
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
        return Long.compare(scheduledTime, o.getScheduledTime());
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
    public void setSorted(boolean sorted) {
        this.sorted = sorted;
    }

    @Override
    public void setReversed(boolean reverse) {
        this.reverse = reverse;
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
    public void setLockChunks(boolean lockChunks) {
        this.lockChunks = lockChunks;
    }

    public boolean getLockChunks() {
        return lockChunks;
    }

    @Override
    public void setModifyType(ModifyType modifyType) {
        this.modifyType = modifyType;
    }

    @Override
    public ModifyType getModifyType() {
        return modifyType;
    }

    @Nullable
    public static com.elmakers.mine.bukkit.api.block.UndoList getUndoList(Entity entity) {
        com.elmakers.mine.bukkit.api.block.UndoList blockList = entity != null ? watchedEntities.get(entity) : null;
        if (entity != null && blockList == null && entity instanceof FallingBlock) {
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

    @Nullable
    public static com.elmakers.mine.bukkit.api.block.UndoList getUndoList(Location location) {
        BlockData blockData = getBlockData(location);
        return blockData == null ? null : blockData.getUndoList();
    }

    public static void setUndoList(Entity entity, com.elmakers.mine.bukkit.api.block.UndoList list) {
        if (entity != null) {
            if (list != null) {
                watchedEntities.put(entity, list);
            } else {
                watchedEntities.remove(entity);
            }
        }
    }

    @Override
    public EntityData getEntityData(Entity entity) {
        return modifiedEntities.get(entity.getUniqueId());
    }

    @Nullable
    public static BlockData getBlockData(Location location) {
        return registry.getBlockData(location);
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
    @Deprecated
    public void setUndoBreaking(boolean breaking) {
    }

    @Override
    public void addDamage(Block block, double damage) {
        BlockData blockData = get(block);
        if (blockData != null) {
            blockData.addDamage(damage);
        }
    }

    @Override
    public void registerFakeBlock(Block block, Collection<WeakReference<Player>> players) {
        if (contains(block)) return;
        BlockData blockData = get(block);
        if (blockData != null) {
            blockData.setFake(players);
        }
    }


    @Override
    public Collection<Entity> getAllEntities() {
        ArrayList<Entity> entities = new ArrayList<>();
        if (this.spawnedEntities != null)
        {
            for (SpawnedEntity spawnedEntity : this.spawnedEntities.values())
            {
                Entity entity = spawnedEntity.getEntity();
                if (entity != null) {
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

        List<BlockData> sortedList = new ArrayList<>(blockList);
        blockList.clear();
        if (reverse) {
            Collections.reverse(sortedList);
        }
        if (attachables != null && sorted) {
            blockComparator.setAttachables(attachables);
            Collections.sort(sortedList, blockComparator);
        }
        blockList.addAll(sortedList);
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

    @Override
    public int getRunnableCount() {
        return runnables == null ? 0 : runnables.size();
    }

    @Nullable
    @Override
    public Runnable undoNextRunnable() {
        Runnable undone = null;
        if (runnables != null && !runnables.isEmpty()) {
            undone = runnables.pop();
            undone.run();
        }
        return undone;
    }

    @Override
    public boolean isUndoType(EntityType entityType) {
        return undoEntityTypes != null && undoEntityTypes.contains(entityType);
    }

    @Override
    public boolean affectsWorld(@Nonnull World world) {
        Preconditions.checkNotNull(world);
        return worlds.contains(world.getName());
    }
}
