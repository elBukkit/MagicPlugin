package com.elmakers.mine.bukkit.magic.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.batch.SpellBatch;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.wand.Wand;

public class BlockController implements Listener {
    private final MagicController controller;
    private boolean undoOnWorldSave = false;
    private int creativeBreakFrequency = 0;
    private boolean dropOriginalBlock = true;

    // This is used only for the BlockBurn event, in other cases we get a source block to check.
    static final List<BlockFace> blockBurnDirections = Arrays.asList(
            BlockFace.NORTH, BlockFace.SOUTH,
            BlockFace.EAST, BlockFace.WEST,
            BlockFace.UP, BlockFace.DOWN
    );

    public BlockController(MagicController controller) {
        this.controller = controller;
    }

    public void loadProperties(ConfigurationSection properties) {
        undoOnWorldSave = properties.getBoolean("undo_on_world_save", false);
        creativeBreakFrequency = properties.getInt("prevent_creative_breaking", 0);
        dropOriginalBlock = properties.getBoolean("drop_original_block", true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event)
    {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (creativeBreakFrequency > 0 && player.getGameMode() == GameMode.CREATIVE) {
            com.elmakers.mine.bukkit.magic.Mage mage = controller.getMage(event.getPlayer());
            if (mage.checkLastClick(creativeBreakFrequency)) {
                event.setCancelled(true);
                return;
            }
        }
        if (controller.areLocksProtected() && controller.isContainer(block) && !event.getPlayer().hasPermission("Magic.bypass")) {
            String lockKey = CompatibilityUtils.getLock(block);
            if (lockKey != null && !lockKey.isEmpty()) {
                Inventory inventory = player.getInventory();
                Mage mage = controller.getRegisteredMage(event.getPlayer());
                if (mage != null) {
                    inventory = mage.getInventory();
                }
                if (!InventoryUtils.hasItem(inventory, lockKey)) {
                    String message = controller.getMessages().get("general.locked_chest");
                    if (mage != null) {
                        mage.sendMessage(message);
                    } else {
                        player.sendMessage(message);
                    }
                    event.setCancelled(true);
                    return;
                }
            }
        }
        com.elmakers.mine.bukkit.api.block.BlockData modifiedBlock = com.elmakers.mine.bukkit.block.UndoList.getBlockData(block.getLocation());
        if (modifiedBlock != null) {
            UndoList undoList = modifiedBlock.getUndoList();
            if (undoList != null) {
                if (!undoList.isConsumed()) {
                    event.setCancelled(true);
                    Collection<ItemStack> items = null;
                    if (dropOriginalBlock) {
                        while (modifiedBlock.getPriorState() != null) {
                            modifiedBlock = modifiedBlock.getPriorState();
                        }
                        modifiedBlock.modify(block);
                        items = block.getDrops();
                    }
                    block.setType(Material.AIR);
                    if (items != null) {
                        Location location = block.getLocation();
                        for (ItemStack item : items) {
                            location.getWorld().dropItemNaturally(location, item);
                        }
                    }
                }
                com.elmakers.mine.bukkit.block.UndoList.commit(modifiedBlock);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItemInHand();

        if (NMSUtils.isTemporary(itemStack)) {
            event.setCancelled(true);
            player.getInventory().setItemInMainHand(null);
            return;
        }

        if (NMSUtils.isUnplaceable(itemStack) || Wand.isSpecial(itemStack)) {
            event.setCancelled(true);
            return;
        }

        com.elmakers.mine.bukkit.magic.Mage mage = controller.getMage(player);

        if (mage.getBlockPlaceTimeout() > System.currentTimeMillis()) {
            event.setCancelled(true);
        }

        if (Wand.isSpecial(itemStack)) {
            event.setCancelled(true);
        }

        if (!event.isCancelled()) {
            Block block = event.getBlock();
            com.elmakers.mine.bukkit.api.block.BlockData modifiedBlock = com.elmakers.mine.bukkit.block.UndoList.getBlockData(block.getLocation());
            if (modifiedBlock != null) {
                com.elmakers.mine.bukkit.block.UndoList.commit(modifiedBlock);
            }
            if (DefaultMaterials.isMobSpawner(block.getType()) && event.getItemInHand() != null && DefaultMaterials.isMobSpawner(event.getItemInHand().getType()) && player.hasPermission("Magic.spawners")) {
                CompatibilityUtils.applyItemData(event.getItemInHand(), block);
            }
        }
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        Block block = event.getBlock();
        UndoList undoList = controller.getPendingUndo(block.getLocation());
        if (undoList != null)
        {
            undoList.add(block);
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        Block piston = event.getBlock();
        Block block = piston.getRelative(event.getDirection());
        UndoList undoList = controller.getPendingUndo(block.getLocation());
        if (undoList != null)
        {
            undoList.add(block);
            undoList.add(piston);
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        Block targetBlock = event.getToBlock();
        Block sourceBlock = event.getBlock();

        UndoList undoList = controller.getPendingUndo(sourceBlock.getLocation());
        if (undoList != null)
        {
            undoList.add(targetBlock);
        }
        else
        {
            undoList = controller.getPendingUndo(targetBlock.getLocation());
            if (undoList != null)
            {
                undoList.add(targetBlock);
            }
        }
        if (undoList != null && undoList.isScheduled())
        {
            // Avoid dropping broken items!
            MaterialSet doubles = com.elmakers.mine.bukkit.block.UndoList.attachablesDouble;
            if (doubles.testBlock(targetBlock))
            {
                Block upBlock = targetBlock.getRelative(BlockFace.UP);
                while (doubles.testBlock(upBlock)) {
                    undoList.add(upBlock);
                    DeprecatedUtils.setTypeAndData(upBlock, Material.AIR, (byte) 0, false);
                    upBlock = upBlock.getRelative(BlockFace.UP);
                }

                Block downBlock = targetBlock.getRelative(BlockFace.DOWN);
                while (doubles.testBlock(downBlock)) {
                    undoList.add(downBlock);
                    DeprecatedUtils.setTypeAndData(downBlock, Material.AIR, (byte) 0, false);
                    downBlock = downBlock.getRelative(BlockFace.DOWN);
                }
            }
            DeprecatedUtils.setTypeAndData(targetBlock, Material.AIR, (byte) 0, false);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        Block targetBlock = event.getBlock();
        UndoList undoList = controller.getPendingUndo(targetBlock.getLocation());
        if (undoList == null)
        {
            // TODO: Use BlockBurnEvent.getIgnitingBlock to avoid this mess, 1.11 and up

            // This extra check is necessary to prevent a very specific condition that is not necessarily unique to
            // burning, but happens much more frequently. This may be a sign that the attachment-watching code in UndoList
            // needs review, and perhaps in general instead of watching neighbor blocks we should check all neighbor blocks
            // for block flow and attachable break events.
            //
            // The specific problem here has to do with overlapping spells with paced undo. Take 2 Fire casts as an example:
            // 1) Fire A casts and begins to burn some blocks, which turn to air
            // 2) Fire B casts and registers nearby combustible blocks.
            // 3) Some blocks already burnt by Fire A are adjacent to blocks burning from Fire B, and are not registered
            //    for watching because they are currently air and non-combustible.
            // 4) Fire A rolls back, restoring blocks that were air when Fire B cast to something combustible
            // 5) Rolled back blocks from Fire A start to burn, but are not tracked since they were registered for
            //    watching by Fire B.
            // 6) Fire B rolls back, leaving burning blocks that continue to spread.
            for (BlockFace face : blockBurnDirections) {
                Block sourceBlock = targetBlock.getRelative(face);
                if (sourceBlock.getType() != Material.FIRE) continue;
                undoList = controller.getPendingUndo(sourceBlock.getLocation());
                if (undoList != null) {
                    break;
                }
            }
        }
        if (undoList != null)
        {
            undoList.add(targetBlock);
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        BlockIgniteEvent.IgniteCause cause = event.getCause();
        if (cause == BlockIgniteEvent.IgniteCause.ENDER_CRYSTAL || cause == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL)
        {
            return;
        }

        Entity entity = event.getIgnitingEntity();
        UndoList entityList = controller.getEntityUndo(entity);
        if (entityList != null)
        {
            entityList.add(event.getBlock());
            return;
        }

        Block ignitingBlock = event.getIgnitingBlock();
        Block targetBlock = event.getBlock();
        if (ignitingBlock != null)
        {
            UndoList undoList = controller.getPendingUndo(ignitingBlock.getLocation());
            if (undoList != null)
            {
                undoList.add(event.getBlock());
                return;
            }
        }

        UndoList undoList = controller.getPendingUndo(targetBlock.getLocation());
        if (undoList != null)
        {
            undoList.add(targetBlock);
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Player damager = event.getPlayer();
        Mage damagerMage = controller.getRegisteredMage(damager);
        if (damagerMage != null) {
            com.elmakers.mine.bukkit.api.wand.Wand activeWand = damagerMage.getActiveWand();
            if (activeWand != null) {
                activeWand.playEffects("hit_block");
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof FallingBlock) {
            if (event.getTo() == Material.AIR) {
                // Block is falling, register it
                controller.registerFallingBlock(entity, event.getBlock());
            } else {
                // Block is landing, convert it
                UndoList blockList = com.elmakers.mine.bukkit.block.UndoList.getUndoList(entity);
                if (blockList != null) {
                    com.elmakers.mine.bukkit.api.action.CastContext context = blockList.getContext();
                    if (context != null && !context.hasBuildPermission(entity.getLocation().getBlock())) {
                        event.setCancelled(true);
                    } else {
                        Block block = event.getBlock();
                        blockList.convert(entity, block);
                        if (!blockList.getApplyPhysics()) {
                            FallingBlock falling = (FallingBlock)entity;
                            DeprecatedUtils.setTypeAndData(block, falling.getMaterial(), DeprecatedUtils.getBlockData(falling), false);
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    private void undoPending(World world, String logType) {
        Collection<Mage> mages = controller.getMages();
        for (Mage mage : mages) {
            Collection<Batch> pending = new ArrayList<>(mage.getPendingBatches());
            int cancelled = 0;
            for (Batch batch : pending) {
                if (batch instanceof SpellBatch) {
                    SpellBatch spellBatch = (SpellBatch)batch;
                    UndoList undoList = spellBatch.getUndoList();
                    if (undoList != null && undoList.isScheduled() && undoList.affectsWorld(world)) {
                        spellBatch.getSpell().cancel();
                        batch.finish();
                        cancelled++;
                    }
                }
            }
            if (cancelled > 0) {
                controller.info("Cancelled " + cancelled + " pending spells for " + mage.getName() + " prior to " + logType + " of world " + world.getName());
            }
        }
        Collection<UndoList> pending = new ArrayList<>(controller.getPendingUndo());
        int undone = 0;
        for (UndoList list : pending) {
            if (list.isScheduled() && list.affectsWorld(world)) {
                list.undoScheduled(true);
                undone++;
            }
        }
        if (undone > 0) {
            controller.info("Undid " + undone + " spells prior to " + logType + " of world " + world.getName());
        }
    }

    @EventHandler
    public void onWorldSaveEvent(WorldSaveEvent event) {
        World world = event.getWorld();

        if (undoOnWorldSave) {
            undoPending(world, "save");
        }

        Collection<Player> players = world.getPlayers();
        for (Player player : players) {
            Mage mage = controller.getRegisteredMage(player);
            if (mage != null) {
                controller.saveMage(mage, true);
            }
        }
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        World world = event.getWorld();
        undoPending(world, "unload");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        controller.resumeAutomata(e.getChunk());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        controller.pauseAutomata(e.getChunk());
    }
}
