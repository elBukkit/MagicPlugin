package com.elmakers.mine.bukkit.magic.listener;

import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompleteDragTask;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.wand.Wand;
import com.elmakers.mine.bukkit.wand.WandMode;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class InventoryController implements Listener {
    private final MagicController controller;
    private boolean	enableItemHacks	= true;
    private boolean dropChangesPages = false;

    public InventoryController(MagicController controller) {
        this.controller = controller;
    }

    public void setEnableItemHacks(boolean hack) {
        enableItemHacks = hack;
    }
    public void setDropChangesPages(boolean drop) {
        dropChangesPages = drop;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        Mage mage = controller.getMage(event.getWhoClicked());
        GUIAction activeGUI = mage == null ? null : mage.getActiveGUI();
        if (activeGUI != null) {
            activeGUI.dragged(event);
            return;
        }

        if (!enableItemHacks) return;

        // this is a huge hack! :\
        // I apologize for any weird behavior this causes.
        // Bukkit, unfortunately, will blow away NBT data for anything you drag
        // Which will nuke a wand or spell.
        // To make matters worse, Bukkit passes a copy of the item in the event, so we can't
        // even check for metadata and only cancel the event if it involves one of our special items.
        // The best I can do is look for metadata at all, since Bukkit will retain the name and lore.

        // I have now decided to copy over the CB default handler for this, and cancel the event.
        // The only change I have made is that *real* ItemStack copies are made, instead of shallow Bukkit ones.
        ItemStack oldStack = event.getOldCursor();
        HumanEntity entity = event.getWhoClicked();
        if (oldStack != null && oldStack.hasItemMeta() && entity instanceof Player) {
            // Only do this if we're only dragging one item, since I don't
            // really know what happens or how you would drag more than one.
            Map<Integer, ItemStack> draggedSlots = event.getNewItems();
            if (draggedSlots.size() != 1) return;

            event.setCancelled(true);

            // Cancelling the event will put the item back on the cursor,
            // and skip updating the inventory.

            // So we will wait one tick and then fix this up using the original item.
            InventoryView view = event.getView();
            for (Integer dslot : draggedSlots.keySet()) {
                CompleteDragTask completeDrag = new CompleteDragTask((Player)entity, view, dslot);
                completeDrag.runTaskLater(controller.getPlugin(), 1);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        //controller.getLogger().info("CLICK: " + event.getAction() + ", " + event.getClick() + " on " + event.getSlotType() + " in "+ event.getInventory().getType() + " slots: " + event.getSlot() + ":" + event.getRawSlot());

        if (event.isCancelled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player)event.getWhoClicked();
        Mage apiMage = controller.getMage(player);

        if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
        final com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;

        GUIAction gui = mage.getActiveGUI();
        if (gui != null)
        {
            gui.clicked(event);
            return;
        }

        // Check for temporary items and skill items
        InventoryAction action = event.getAction();
        InventoryType inventoryType = event.getInventory().getType();
        ItemStack clickedItem = event.getCurrentItem();

        boolean isDrop = event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP;
        boolean isSkill = clickedItem != null && Wand.isSkill(clickedItem);

        // Preventing putting skills in containers
        if (isSkill && inventoryType != InventoryType.CRAFTING) {
            if (!isDrop) {
                event.setCancelled(true);
            }
            return;
        }

        // Check for right-click-to-use
        if (isSkill && action == InventoryAction.PICKUP_HALF)
        {
            Spell spell = mage.getSpell(Wand.getSpell(clickedItem));
            if (spell != null) {
                spell.cast();
            }
            player.closeInventory();
            event.setCancelled(true);
            return;
        }

        if (clickedItem != null && NMSUtils.isTemporary(clickedItem)) {
            String message = NMSUtils.getTemporaryMessage(clickedItem);
            if (message != null && message.length() > 1) {
                mage.sendMessage(message);
            }
            ItemStack replacement = NMSUtils.getReplacement(clickedItem);
            event.setCurrentItem(replacement);
            event.setCancelled(true);
            return;
        }

        // Check for wearing spells
        ItemStack heldItem = event.getCursor();
        if (event.getSlotType() == InventoryType.SlotType.ARMOR)
        {
            if (Wand.isSpell(heldItem)) {
                event.setCancelled(true);
                return;
            }
            if (Wand.isWand(clickedItem) || Wand.isWand(heldItem)) {
                controller.onArmorUpdated(mage);
            }
        }
        boolean isHotbar = event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD;
        if (isHotbar && event.getSlotType() == InventoryType.SlotType.ARMOR)
        {
            int slot = event.getHotbarButton();
            ItemStack item =  mage.getPlayer().getInventory().getItem(slot);
            if (item != null && Wand.isSpell(item))
            {
                event.setCancelled(true);
                return;
            }
            controller.onArmorUpdated(mage);
        }

        Wand activeWand = mage.getActiveWand();

        boolean isChest = inventoryType == InventoryType.CHEST || inventoryType == InventoryType.HOPPER || inventoryType == InventoryType.DISPENSER || inventoryType == InventoryType.DROPPER;
        boolean clickedWand = Wand.isWand(clickedItem);
        boolean isContainerSlot = event.getSlot() == event.getRawSlot();

        if (activeWand != null && activeWand.isInventoryOpen())
        {
            // Don't allow the offhand slot to be messed with while the spell inventory is open
            if (event.getRawSlot() == 45)
            {
                event.setCancelled(true);
                return;
            }
            
            if (Wand.isSpell(clickedItem) && clickedItem.getAmount() != 1)
            {
                clickedItem.setAmount(1);
            }
            if (clickedWand)
            {
                event.setCancelled(true);
                if (dropChangesPages) {
                    activeWand.cycleInventory();
                } else {
                    activeWand.cycleHotbar(1);
                }
                return;
            }

            // So many ways to try and move the wand around, that we have to watch for!
            if (isHotbar && Wand.isWand(player.getInventory().getItem(event.getHotbarButton())))
            {
                event.setCancelled(true);
                return;
            }

            // Can't wear spells
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && clickedItem != null)
            {
                if (controller.isWearable(clickedItem))
                {
                    event.setCancelled(true);
                    return;
                }
            }

            // Safety check for something that ought not to be possible
            // but perhaps happens with lag?
            if (Wand.isWand(event.getCursor()))
            {
                activeWand.closeInventory();
                event.setCursor(null);
                event.setCancelled(true);
                return;
            }
        } else if (activeWand != null) {
            // Check for changes that could have been made to the active wand
            int activeSlot = player.getInventory().getHeldItemSlot();
            if (event.getSlot() == activeSlot || 
                    (event.getAction() == InventoryAction.HOTBAR_SWAP && event.getHotbarButton() == activeSlot))
            {
                mage.checkWand();
                activeWand = mage.getActiveWand();
            }
        } 
            
        if (clickedWand && Wand.Undroppable && !player.hasPermission("Magic.wand.override_drop") && isChest && !isContainerSlot) {
            Wand wand = controller.getWand(clickedItem);
            if (wand.isUndroppable()) {
                event.setCancelled(true);
                return;
            }
        }
        
        // Check for armor changing
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && clickedItem != null)
        {
            if (controller.isWearable(clickedItem)) {
                controller.onArmorUpdated(mage);
            }
        }

        // Check for dropping items out of a wand's inventory
        // or dropping undroppable wands
        if (isDrop) {
            if (clickedWand) {
                Wand wand = controller.getWand(clickedItem);
                if (wand.isUndroppable()) {
                    event.setCancelled(true);
                    if (activeWand != null) {
                        if (activeWand.getHotbarCount() > 1) {
                            activeWand.cycleHotbar(1);
                        } else {
                            activeWand.closeInventory();
                        }
                    }
                    return;
                }
            }
            if (activeWand != null && activeWand.isInventoryOpen()) {

                ItemStack droppedItem = clickedItem;

                if (!Wand.isSpell(droppedItem)) {
                    mage.giveItem(droppedItem);
                    event.setCurrentItem(null);
                    event.setCancelled(true);
                    return;
                }

                // This is a hack to deal with spells on cooldown disappearing,
                // Since the event handler doesn't match the zero-count itemstacks
                Integer slot = event.getSlot();
                int heldSlot = player.getInventory().getHeldItemSlot();
                Inventory hotbar = activeWand.getHotbar();
                if (hotbar != null && slot >= 0 && slot <= hotbar.getSize() && slot != heldSlot && activeWand.getMode() == WandMode.INVENTORY)
                {
                    if (slot > heldSlot) slot--;
                    if (slot < hotbar.getSize())
                    {
                        droppedItem = hotbar.getItem(slot);
                    }
                    else
                    {
                        slot = null;
                    }
                }
                else
                {
                    slot = null;
                }

                if (!controller.isSpellDroppingEnabled()) {
                    player.closeInventory();
                    String spellName = Wand.getSpell(droppedItem);
                    if (spellName != null && !activeWand.isManualQuickCastDisabled()) {
                        Spell spell = mage.getSpell(spellName);
                        if (spell != null) {
                            activeWand.cast(spell);
                            // Just in case a spell has levelled up... jeez!
                            if (hotbar != null && slot != null)
                            {
                                droppedItem = hotbar.getItem(slot);
                            }
                        }
                    }
                    event.setCancelled(true);

                    // This is needed to avoid spells on cooldown disappearing from the hotbar
                    if (hotbar != null && slot != null && mage.getActiveGUI() == null)
                    {
                        player.getInventory().setItem(event.getSlot(), droppedItem);
                        DeprecatedUtils.updateInventory(player);
                    }

                    return;
                }
                ItemStack newDrop = controller.removeItemFromWand(activeWand, droppedItem);

                if (newDrop != null) {
                    Location location = player.getLocation();
                    Item item = location.getWorld().dropItem(location, newDrop);
                    item.setVelocity(location.getDirection().normalize());
                } else {
                    event.setCancelled(true);
                }
            }
            return;
        }

        // Check for wand cycling with active inventory
        if (activeWand != null) {
            WandMode wandMode = activeWand.getMode();
            if ((wandMode == WandMode.INVENTORY && inventoryType == InventoryType.CRAFTING) ||
                    (wandMode == WandMode.CHEST && inventoryType == InventoryType.CHEST)) {
                if (activeWand.isInventoryOpen()) {
                    if (event.getAction() == InventoryAction.NOTHING) {
                        int direction = event.getClick() == ClickType.LEFT ? 1 : -1;
                        activeWand.cycleInventory(direction);
                        event.setCancelled(true);
                        return;
                    }

                    if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                        event.setCancelled(true);
                        return;
                    }

                    // Chest mode falls back to selection from here.
                    if (event.getAction() == InventoryAction.PICKUP_HALF || wandMode == WandMode.CHEST) {
                        controller.onPlayerActivateIcon(mage, activeWand, clickedItem);
                        player.closeInventory();
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClosed(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        // Update the active wand, it may have changed around
        Player player = (Player)event.getPlayer();
        Mage apiMage = controller.getRegisteredMage(player);

        if (apiMage == null || !(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
        com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;

        GUIAction gui = mage.getActiveGUI();
        if (gui != null)
        {
            mage.onGUIDeactivate();
        }

        Wand previousWand = mage.getActiveWand();

        // Save the inventory state of the current wand if its spell inventory is open
        // This is just to make sure we don't lose changes made to the inventory
        if (previousWand != null && previousWand.isInventoryOpen()) {
            if (previousWand.getMode() == WandMode.INVENTORY) {
                previousWand.saveInventory();
                // Update hotbar names
                previousWand.updateHotbar();
            } else if (previousWand.getMode() == WandMode.CHEST) {
                // Check for chest inventory mode, we may just be closing a display inventory.
                // In theory you can't re-arrange items in here.
                previousWand.closeInventory();
            }
        } else {
            mage.checkWand();
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player)event.getPlayer();
        Mage apiMage = controller.getRegisteredMage(player);

        if (apiMage == null || !(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
        com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;

        Wand wand = mage.getActiveWand();
        GUIAction gui = mage.getActiveGUI();
        if (wand != null && gui == null) {
            // NOTE: The type will never actually be CRAFTING, at least for now.
            // But we can hope for server-side player inventory open notification one day, right?
            // Anyway, check for opening another inventory and close the wand.
            if (event.getView().getType() != InventoryType.CRAFTING) {
                if (wand.getMode() == WandMode.INVENTORY || !wand.isInventoryOpen()) {
                    wand.deactivate();
                }
            }
        }
    }
}
