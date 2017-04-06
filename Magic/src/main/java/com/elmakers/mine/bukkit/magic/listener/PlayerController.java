package com.elmakers.mine.bukkit.magic.listener;

import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.magic.DropActionTask;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.wand.Wand;

import com.elmakers.mine.bukkit.wand.WandAction;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerController implements Listener {
    private final MagicController controller;
    private int clickCooldown = 150;
    private boolean enableCreativeModeEjecting = true;
    private MaterialAndData enchantBlockMaterial;
    private String enchantClickSpell = "spellshop";
    private String enchantSneakClickSpell = "upgrades";
    private boolean openOnSneakDrop;
    private boolean cancelInteractOnCast = true;
    private boolean allowOffhandCasting = true;
    private long lastDropWarn = 0;

    public PlayerController(MagicController controller) {
        this.controller = controller;
    }

    public void loadProperties(ConfigurationSection properties) {
        clickCooldown = properties.getInt("click_cooldown", 0);
        enableCreativeModeEjecting = properties.getBoolean("enable_creative_mode_ejecting", false);
        enchantBlockMaterial = new MaterialAndData(properties.getString("enchant_block", "enchantment_table"));
        enchantClickSpell = properties.getString("enchant_click");
        enchantSneakClickSpell = properties.getString("enchant_sneak_click");
        openOnSneakDrop = properties.getBoolean("open_wand_on_sneak_drop");
        cancelInteractOnCast = properties.getBoolean("cancel_interact_on_cast", true);
        allowOffhandCasting = properties.getBoolean("allow_offhand_casting", true);
    }

    @EventHandler
    public void onPlayerEquip(PlayerItemHeldEvent event)
    {
        if (!controller.isLoaded()) return;

        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack next = inventory.getItem(event.getNewSlot());

        Mage mage = controller.getMage(player);
        if (mage == null) return;

        // Check for self-destructing and temporary items
        if (Wand.isSelfDestructWand(next)) {
            mage.sendMessageKey("wand.self_destruct");
            inventory.setItem(event.getNewSlot(), null);
            mage.checkWand();
            return;
        }

        if (NMSUtils.isTemporary(next)) {
            inventory.setItem(event.getNewSlot(), null);
            mage.checkWand();
            return;
        }

        Wand activeWand = mage.getActiveWand();
        boolean isSkill = Wand.isSkill(next);
        if (isSkill)
        {
            mage.useSkill(next);
            event.setCancelled(true);
            return;
        }

        boolean isQuickCast = activeWand != null && activeWand.isQuickCast() && activeWand.isInventoryOpen();
        if (isQuickCast)
        {
            Spell spell = mage.getSpell(Wand.getSpell(next));
            if (spell != null) {
                activeWand.cast(spell);
            }
            event.setCancelled(true);
            return;
        }

        // Check for active Wand
        boolean isWand = Wand.isWand(next);
        if (activeWand != null && activeWand.isInventoryOpen()) {
            // If the wand inventory is open, we're going to let them select a spell or material
            if (!isWand) {
                mage.activateIcon(activeWand, next);

                // Make sure we have the current wand item in the player's inventory so the
                // item text gets updated on hotbar item selection "bounce"
                int previousSlot = event.getPreviousSlot();
                ItemStack previous = inventory.getItem(previousSlot);
                if (previous != null && previous.equals(activeWand.getItem())) {
                    player.getInventory().setItem(previousSlot, activeWand.getItem());
                }
            }

            event.setCancelled(true);
        } else if (isWand || activeWand != null) {
            // Check to see if we've switched to/from a wand
            mage.checkWandNextTick();
        }

        // Check for map selection if no wand is active
        activeWand = mage.getActiveWand();
        if (activeWand == null && next != null) {
            if (next.getType() == Material.MAP) {
                mage.setLastHeldMapId(next.getDurability());
            }
        }
    }
    
    @EventHandler
    public void onPlayerSwapItem(PlayerSwapHandItemsEvent event) {

        final Player player = event.getPlayer();
        Mage mage = controller.getRegisteredMage(player);
        if (mage == null) return;
        
        final com.elmakers.mine.bukkit.api.wand.Wand apiWand = mage.getActiveWand();
        final com.elmakers.mine.bukkit.api.wand.Wand apiOffhandWand = mage.getOffhandWand();
        Wand activeWand = (apiWand instanceof Wand) ? (Wand)apiWand : null;
        Wand offhandWand = (apiOffhandWand instanceof Wand) ? (Wand)apiOffhandWand : null;

        if (activeWand == null && offhandWand == null) return;

        if (activeWand != null && activeWand.performAction(activeWand.getSwapAction())) {
            event.setCancelled(true);
        } else if (activeWand != null && activeWand.isInventoryOpen()) {
            activeWand.closeInventory();
            event.setCancelled(true);
        } else if (activeWand != null || offhandWand != null || Wand.isWand(event.getMainHandItem()) || Wand.isWand(event.getOffHandItem())){
            mage.checkWandNextTick();
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event)
    {
        final Player player = event.getPlayer();
        Mage mage = controller.getRegisteredMage(player);
        if (mage == null) return;

        // Catch lag-related glitches dropping items from GUIs
        if (mage.getActiveGUI() != null) {
            event.setCancelled(true);
            return;
        }

        final Wand activeWand = mage.getActiveWand();
        final ItemStack droppedItem = event.getItemDrop().getItemStack();

        boolean cancelEvent = false;
        ItemStack activeItem = activeWand == null ? null : activeWand.getItem();
        // It seems like Spigot sets the original item to air before dropping
        // We will be satisfied to only compare the metadata.
        ItemMeta activeMeta = activeItem == null ? null : activeItem.getItemMeta();
        ItemMeta droppedMeta = droppedItem.getItemMeta();
        boolean droppedWand = droppedMeta != null && activeMeta != null && activeItem.getItemMeta().equals(droppedItem.getItemMeta());
        if (droppedWand && activeWand.isUndroppable()) {
            // Postpone cycling until after this event unwinds
            Bukkit.getScheduler().scheduleSyncDelayedTask(controller.getPlugin(), new DropActionTask(activeWand));
            cancelEvent = true;
        } else if (activeWand != null) {
            if (droppedWand) {
                activeWand.deactivate();
                ItemStack restoredItem = player.getInventory().getItemInMainHand();
                ItemMeta restoredMeta = restoredItem == null ? null : restoredItem.getItemMeta();
                activeMeta = activeWand.getItem().getItemMeta();
                // Might have just saved some changes
                droppedItem.setItemMeta(activeMeta);
                // Clear after inventory restore (potentially with deactivate), since that will put the wand back
                if (Wand.hasActiveWand(player) && restoredItem.getType() != Material.AIR &&
                    restoredMeta != null && activeMeta.equals(restoredMeta)) {
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR, 1));
                }
            } else if (activeWand.isInventoryOpen()) {
                if (!controller.isSpellDroppingEnabled()) {
                    cancelEvent = true;
                    // This will happen if you graph an item, change pages, and then drop the item- it would disappear
                    // from the inventory until reload.
                    // Check for this state and prevent it.
                    boolean isInventoryFull = true;
                    PlayerInventory playerInventory = player.getInventory();
                    for (int i = 0; i < Wand.PLAYER_INVENTORY_SIZE; i++) {
                        ItemStack item = playerInventory.getItem(i);
                        if (item == null || item.getType() == Material.AIR) {
                            isInventoryFull = false;
                            break;
                        }
                    }
                    if (isInventoryFull) {
                        activeWand.addToInventory(droppedItem);
                    }
                } else {
                    // The item is already removed from the wand's inventory, but that should be ok
                    controller.removeItemFromWand(activeWand, droppedItem);
                }
            }
        } else if (openOnSneakDrop && !player.isSneaking() && event.getPlayer().getItemOnCursor().getType() == Material.AIR) {
            PlayerInventory inventory = player.getInventory();

            // Find a wand on the hotbar to open
            for (int i = 0; i < 9; i++) {
                ItemStack item = inventory.getItem(i);

                if (item != null && Wand.isWand(item)) {
                    final int previouslySelected = inventory
                            .getHeldItemSlot();
                    inventory.setHeldItemSlot(i);

                    final Wand newWand = mage.checkWand();

                    // Restore if not activated
                    if (null == newWand) {
                        inventory.setHeldItemSlot(previouslySelected);
                    } else {
                        // Using a runnable here as workaround for bukkit bug
                        // that uses inventory.addItem when drop event is cancelled
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                newWand.openInventory();
                                newWand.setHeldSlot(previouslySelected);
                            }
                        }.runTaskLater(mage.getController().getPlugin(), 0);
                        break;
                    }
                }
            }
            cancelEvent = true;
        } 
        
        if (!cancelEvent && Wand.Undroppable && Wand.isWand(droppedItem) && !player.hasPermission("Magic.wand.override_drop")) {
            Wand wand = controller.getWand(droppedItem);
            if (wand.isUndroppable() && wand.isBound()) {
                cancelEvent = true;
            }
        }
        if (cancelEvent) {
            if (droppedWand) {
                activeWand.setItem(droppedItem);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Mage mage = controller.getRegisteredMage(event.getPlayer());
        if (mage == null) return;
        mage.restoreRespawnInventories();
    }

    @EventHandler(priority=EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractArmorStand(PlayerArmorStandManipulateEvent event)
    {
        Player player = event.getPlayer();
        Mage mage = controller.getRegisteredMage(player);
        if (mage == null) return;
        com.elmakers.mine.bukkit.api.wand.Wand wand = mage.checkWand();
        if (wand != null) {
            if (wand.isUndroppable()) {
                event.setCancelled(true);
                return;
            } else {
                wand.deactivate();
            }
        }
    }

    @EventHandler(priority=EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Mage mage = controller.getRegisteredMage(player);
        if (mage == null) return;
        Wand wand = mage.checkWand();

        // Check for a player placing a wand in an item frame
        Entity clickedEntity = event.getRightClicked();

        // Don't think this ever fires for ArmorStand - see above
        boolean isPlaceable = clickedEntity instanceof ItemFrame || clickedEntity instanceof ArmorStand;
        if (wand != null && isPlaceable) {
            if (wand.isUndroppable()) {
                event.setCancelled(true);
                return;
            } else {
                wand.deactivate();
            }
        }

        // Check for clicking on a Citizens NPC, in case
        // this hasn't been cancelled yet
        if (controller.isNPC(event.getRightClicked())) {
            if (wand != null) {
                wand.closeInventory();
            }

            // Don't let it re-open right away
            mage.checkLastClick(0);
        } else {
            // Don't allow interacting while holding spells, brushes or upgrades
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (Wand.isSpell(itemInHand) || Wand.isBrush(itemInHand) || Wand.isUpgrade(itemInHand)) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerAnimate(PlayerAnimationEvent event)
    {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.ADVENTURE || event.getAnimationType() != PlayerAnimationType.ARM_SWING)
        {
            return;
        }

        Mage mage = controller.getMage(player);
        if (mage == null) return;

        Wand wand = mage.checkWand();
        if (wand == null) return;
        
        Messages messages = controller.getMessages();
        if (!controller.hasWandPermission(player))
        {
            return;
        }
        
        // Check for region or wand-specific permissions
        if (!controller.hasWandPermission(player, wand))
        {
            wand.deactivate();
            mage.sendMessage(messages.get("wand.no_permission").replace("$wand", wand.getName()));
            return;
        }
        
        if (!mage.checkLastClick(clickCooldown)) {
            return;
        }

        if (wand.isUpgrade()) return;

        wand.playEffects("swing");

        wand.performAction(wand.getLeftClickAction());
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (!controller.isLoaded()) return;
        // Note that an interact on air event will arrive pre-cancelled
        // So this is kind of useless. :\
        //if (event.isCancelled()) return;

        // Block block = event.getClickedBlock();
        // controller.getLogger().info("INTERACT: " + event.getAction() + " on " + (block == null ? "NOTHING" : block.getType()));

        Player player = event.getPlayer();
        Action action = event.getAction();
        boolean isLeftClick = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
        
        // GM2 left-click interaction is handled by the animation event, for now.
        // Might make sense to move all left-click handling there eventually?
        if (player.getGameMode() == GameMode.ADVENTURE && isLeftClick) return;
        
        // Don't allow interacting while holding spells, brushes or upgrades
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (Wand.isSpell(itemInHand) || Wand.isBrush(itemInHand) || Wand.isUpgrade(itemInHand)) {
            event.setCancelled(true);
            return;
        }
        
        boolean isRightClick = (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK);
        if (isRightClick) {
            ItemStack itemInOffhand = player.getInventory().getItemInOffHand();
            if (Wand.isSpell(itemInOffhand) || Wand.isBrush(itemInOffhand) || Wand.isUpgrade(itemInOffhand)) {
                event.setCancelled(true);
                return;
            }
        }

        Mage mage = controller.getMage(player);
        if (mage == null) return;

        Wand wand = mage.checkWand();
        if (action == Action.RIGHT_CLICK_BLOCK) {
            Material material = event.getClickedBlock().getType();
            isRightClick = !controller.isInteractable(event.getClickedBlock());

            // This is to prevent Essentials signs from giving you an item in your wand inventory.
            if (wand != null && (material== Material.SIGN_POST || material == Material.WALL_SIGN)) {
                wand.closeInventory();
            }
        }
        if (!mage.checkLastClick(clickCooldown)) {
            // We need to be careful about cancelling right-click events since this interferes with offhand
            // item use and particularly blocking
            if (wand != null && wand.getRightClickAction() != WandAction.NONE) {
                event.setCancelled(true);
            } else if (wand != null && wand.getLeftClickAction() != WandAction.NONE && isLeftClick) {
                event.setCancelled(true);
            }
            return;
        }
        
        // Check for offhand casting
        if (isRightClick)
        {
            if (allowOffhandCasting && mage.offhandCast()) {
                if (cancelInteractOnCast) {
                    event.setCancelled(true);
                }
                return;
            }
        }

        // Check for wearing via right-click
        if (itemInHand != null && isRightClick && controller.isWearable(itemInHand))
        {
            if (wand != null)
            {
                wand.deactivate();
            }
            controller.onArmorUpdated(mage);
            return;
        }

        if (wand == null) return;

        Messages messages = controller.getMessages();
        if (!controller.hasWandPermission(player))
        {
            return;
        }

        // Check for region or wand-specific permissions
        if (!controller.hasWandPermission(player, wand))
        {
            wand.deactivate();
            mage.sendMessage(messages.get("wand.no_permission").replace("$wand", wand.getName()));
            return;
        }

        // Check for enchantment table click
        Block clickedBlock = event.getClickedBlock();
        if (wand.hasSpellProgression() && clickedBlock != null && clickedBlock.getType() != Material.AIR && enchantBlockMaterial != null && enchantBlockMaterial.is(clickedBlock))
        {
            Spell spell = null;
            if (player.isSneaking())
            {
                spell = enchantSneakClickSpell != null ? mage.getSpell(enchantSneakClickSpell) : null;
            }
            else
            {
                spell = enchantClickSpell != null ? mage.getSpell(enchantClickSpell) : null;
            }
            if (spell != null)
            {
                spell.cast();
                event.setCancelled(true);
                return;
            }
        }
        
        if (isLeftClick) {
            wand.playEffects("swing");
        }

        if (isLeftClick && !wand.isUpgrade())
        {
            if (wand.performAction(wand.getLeftClickAction()) && cancelInteractOnCast) {
                event.setCancelled(true);
            }
            return;
        }

        if (isRightClick && wand.performAction(wand.getRightClickAction()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        // Automatically re-activate mages.
        controller.getMage(event.getPlayer());
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerKick(PlayerKickEvent event)
    {
        handlePlayerQuitEvent(event);
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        handlePlayerQuitEvent(event);
    }

    protected void handlePlayerQuitEvent(PlayerEvent event) {
        Player player = event.getPlayer();
        Mage mage = controller.getRegisteredMage(player);
        if (mage != null) {
            mage.onPlayerQuit(event);
            controller.playerQuit(mage);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        Mage mage = controller.getMage(player);
        mage.onChangeWorld();
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event)
    {
        if (event.getNewGameMode() == GameMode.CREATIVE) {
            Player player = event.getPlayer();
            Mage mage = controller.getMage(player);
            com.elmakers.mine.bukkit.api.wand.Wand activeWand = mage.getActiveWand();
            if (activeWand != null) {
                activeWand.closeInventory();
            }

            if (enableCreativeModeEjecting) {
                boolean ejected = false;
                if (activeWand != null) {
                    activeWand.deactivate();
                }
                Inventory inventory = player.getInventory();
                ItemStack[] contents = inventory.getContents();
                for (int i = 0; i < contents.length; i++) {
                    ItemStack item = contents[i];
                    if (Wand.isWand(item)) {
                        ejected = true;
                        inventory.setItem(i, null);
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                    }
                }
                if (ejected) {
                    mage.sendMessage("Ejecting wands, creative mode will destroy them!");
                }
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event)
    {
        Player player = event.getPlayer();
        Mage mage = controller.getMage(player);

        if (mage == null) return;

        // If a wand's inventory is active, add the item there
        if (mage.hasStoredInventory()) {
            event.setCancelled(true);
            if (mage.addToStoredInventory(event.getItem().getItemStack())) {
                event.getItem().remove();
            }
        }
    }

    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerPrePickupItem(PlayerPickupItemEvent event)
    {
        Item item = event.getItem();
        ItemStack pickup = item.getItemStack();
        if (NMSUtils.isTemporary(pickup) || item.hasMetadata("temporary"))
        {
            item.remove();
            event.setCancelled(true);
            return;
        }

        boolean isWand = Wand.isWand(pickup);

        // Creative mode inventory hacky work-around :\
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE && isWand && enableCreativeModeEjecting) {
            event.setCancelled(true);
            return;
        }

        // Check to see if this is an item we might undo, and remove it from undo
        UndoList undoList = controller.getEntityUndo(item);
        if (undoList != null) {
            undoList.remove(item);
        }
        
        Player player = event.getPlayer();
        Mage mage = controller.getMage(player);
        // Remove lost wands from records
        Messages messages = controller.getMessages();
        if (isWand) {
            Wand wand = controller.getWand(pickup);
            if (!wand.canUse(player)) {
                if (mage != null && (lastDropWarn == 0 || System.currentTimeMillis() - lastDropWarn > 10000)) {
                    mage.sendMessage(messages.get("wand.bound").replace("$name", wand.getOwner()));
                }
                lastDropWarn = System.currentTimeMillis();
                event.setCancelled(true);
                return;
            }

            controller.removeLostWand(wand.getId());
        }

        if (mage == null) return;

        // Wands will absorb spells and upgrade items
        Wand activeWand = mage.getActiveWand();
        if (activeWand != null
                && activeWand.isModifiable()
                && (Wand.isSpell(pickup) || Wand.isBrush(pickup) || Wand.isUpgrade(pickup) || Wand.isSP(pickup))
                && activeWand.addItem(pickup)) {
            event.getItem().remove();
            event.setCancelled(true);
            return;
        }

        if (!mage.hasStoredInventory() && isWand) {
            mage.checkWandNextTick();
        }
    }
}
