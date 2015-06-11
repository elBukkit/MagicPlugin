package com.elmakers.mine.bukkit.magic.listener;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.wand.Wand;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class BlockController implements Listener {
    private final MagicController controller;
    private boolean undoOnWorldSave = false;

    public BlockController(MagicController controller) {
        this.controller = controller;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event)
    {
        Block block = event.getBlock();
        if (controller.areLocksProtected() && controller.isContainer(block) && !event.getPlayer().hasPermission("Magic.bypass")) {
            String lockKey = CompatibilityUtils.getLock(block);
            if (lockKey != null && !lockKey.isEmpty()) {
                Player player = event.getPlayer();
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
            event.setCancelled(true);
            block.setType(Material.AIR);
            com.elmakers.mine.bukkit.block.UndoList.commit(modifiedBlock);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItemInHand();

        if (NMSUtils.isTemporary(itemStack)) {
            event.setCancelled(true);
            player.setItemInHand(null);
            return;
        }

        if (NMSUtils.isUnplaceable(itemStack) || Wand.isWand(itemStack) || Wand.isSpell(itemStack) || Wand.isBrush(itemStack)) {
            event.setCancelled(true);
            return;
        }

        Mage apiMage = controller.getMage(player);

        if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
        com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;

        if (mage.hasStoredInventory() || mage.getBlockPlaceTimeout() > System.currentTimeMillis()) {
            event.setCancelled(true);
        }

        if (Wand.isWand(itemStack) || Wand.isBrush(itemStack) || Wand.isSpell(itemStack) || Wand.isUpgrade(itemStack)) {
            event.setCancelled(true);
        }

        if (!event.isCancelled()) {
            Block block = event.getBlock();
            com.elmakers.mine.bukkit.api.block.BlockData modifiedBlock = com.elmakers.mine.bukkit.block.UndoList.getBlockData(block.getLocation());
            if (modifiedBlock != null) {
                com.elmakers.mine.bukkit.block.UndoList.commit(modifiedBlock);
            }
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
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        Block targetBlock = event.getBlock();
        UndoList undoList = controller.getPendingUndo(targetBlock.getLocation());
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
            ActionHandler.runActions(entity, entity.getLocation(), null);
            ActionHandler.runEffects(entity);
            UndoList blockList = com.elmakers.mine.bukkit.block.UndoList.getUndoList(entity);
            if (blockList != null) {
                com.elmakers.mine.bukkit.api.action.CastContext context = blockList.getContext();
                if (context != null && !context.hasBuildPermission(entity.getLocation().getBlock())) {
                    event.setCancelled(true);
                } else {
                    blockList.convert(entity, event.getBlock());
                }
            } else {
                controller.registerFallingBlock(entity, event.getBlock());
            }
        }
    }

    @EventHandler
    public void onWorldSaveEvent(WorldSaveEvent event) {
        World world = event.getWorld();
        Collection<? extends Player> players = controller.getPlugin().getServer().getOnlinePlayers();
        for (Player player : players) {
            if (world.equals(player.getWorld()) && controller.isMage(player)) {
                Mage mage = controller.getMage(player);
                controller.saveMage(mage, true);

                if (undoOnWorldSave) {
                    com.elmakers.mine.bukkit.api.block.UndoQueue queue = mage.getUndoQueue();
                    if (queue != null) {
                        int undone = queue.undoScheduled();
                        if (undone > 0) {
                            controller.info("Undid " + undone + " spells for " + player.getName() + "prior to save of world " + world.getName());
                        }
                    }
                }
            }
        }
    }

    public void setUndoOnWorldSave(boolean undo) {
        this.undoOnWorldSave = undo;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        // Check for any blocks we need to toggle.
        controller.triggerBlockToggle(e.getChunk());
    }
}
