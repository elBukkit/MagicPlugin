package com.elmakers.mine.bukkit.magic.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ItemUtils;
import com.elmakers.mine.bukkit.wand.Wand;

public class AnvilController implements Listener {
    private final MagicController controller;
    private boolean bindingEnabled = false;
    private boolean combiningEnabled = false;
    private boolean wandBookCombiningEnabled = true;
    private boolean bookCombiningDisabled = false;
    private boolean organizingEnabled = false;
    private boolean clearDescriptionOnRename = false;
    private boolean enableRenaming = true;
    private boolean disableAnvil = false;

    public AnvilController(MagicController controller) {
        this.controller = controller;
    }

    public void load(ConfigurationSection properties) {
        disableAnvil = properties.getBoolean("disable_anvil", false);
        bookCombiningDisabled = properties.getBoolean("disable_book_combining", bookCombiningDisabled);
        enableRenaming = properties.getBoolean("enable_wand_renaming", true);
        bindingEnabled = properties.getBoolean("enable_anvil_binding", bindingEnabled);
        combiningEnabled = properties.getBoolean("enable_combining", combiningEnabled);
        wandBookCombiningEnabled = properties.getBoolean("enable_wand_book_combining", wandBookCombiningEnabled);
        organizingEnabled = properties.getBoolean("enable_organizing", organizingEnabled);
        clearDescriptionOnRename = properties.getBoolean("anvil_rename_clears_description", clearDescriptionOnRename);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getType() != InventoryType.ANVIL)  return;
        if (!event.getInventorySlots().contains(0)) return;

        // Unfortunately this event gives us a shallow copy of the item so we need to dig a little bit.
        ItemStack oldCursor = event.getOldCursor();
        oldCursor = oldCursor.hasItemMeta() ? ItemUtils.makeReal(oldCursor) : oldCursor;

        if (Wand.isWand(oldCursor)) {
            ItemStack item = event.getNewItems().get(0);
            if (item != null && item.hasItemMeta()) {
                item = ItemUtils.makeReal(item);
                if (Wand.isWand(item)) {
                    Wand wand = controller.getWand(item);
                    wand.updateName(false, false);
                    final Inventory inventory = event.getInventory();
                    final ItemStack finalItem = item;

                    // Changes made during the drag event do nothing.
                    Bukkit.getScheduler().runTaskLater(controller.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            // Try to prevent quick-clicking dupe exploits
                            ItemStack item = inventory.getItem(0);
                            if (item != null && item.hasItemMeta()) {
                                inventory.setItem(0, finalItem);
                            }
                        }
                    }, 1);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (disableAnvil && event.getInventory().getType().equals(InventoryType.ANVIL)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        InventoryType inventoryType = event.getInventory().getType();
        SlotType slotType = event.getSlotType();
        Player player = (Player)event.getWhoClicked();
        Mage mage = controller.getMage(player);

        if (inventoryType == InventoryType.ANVIL)
        {
            ItemStack cursor = event.getCursor();
            ItemStack current = event.getCurrentItem();
            Inventory anvilInventory = event.getInventory();
            InventoryAction action = event.getAction();
            ItemStack firstItem = anvilInventory.getItem(0);
            ItemStack secondItem = anvilInventory.getItem(1);

            mage.sendDebugMessage(ChatColor.AQUA + "ANVIL CLICK: "
                + ChatColor.WHITE + "cursor wand? " + ChatColor.GOLD + Wand.isWand(cursor)
                + ChatColor.WHITE + " current wand?" + ChatColor.GOLD + Wand.isWand(current)
                + ChatColor.WHITE + " action: " + ChatColor.GOLD + action
                + ChatColor.WHITE + " slot: " + ChatColor.GOLD + slotType
                + ChatColor.WHITE + " first: " + ChatColor.YELLOW + (firstItem == null ? "null" : firstItem.getType().name())
                + ChatColor.WHITE + ", second: " + ChatColor.YELLOW + (secondItem == null ? "null" : secondItem.getType().name()),
                80
            );

            // Handle direct movement into the anvil inventory
            if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY)
            {
                if (!Wand.isWand(current)) return;

                // Moving from anvil back to inventory
                if (slotType == SlotType.CRAFTING) {
                    Wand wand = controller.getWand(current);
                    wand.updateName(true);
                    return;
                } else if (slotType != SlotType.RESULT) {
                    // Moving from inventory to anvil
                    Wand wand = controller.getWand(current);
                    wand.updateName(false, true);
                    return;
                }
            }

            // Set/unset active names when starting to craft
            if (slotType == SlotType.CRAFTING) {
                // Putting a wand into the anvil's crafting slot
                if (Wand.isWand(cursor)) {
                    Wand wand = controller.getWand(cursor);
                    wand.updateName(false, true);
                }

                // Taking a wand out of the anvil's crafting slot
                if (Wand.isWand(current)) {
                    Wand wand = controller.getWand(current);
                    if (clearDescriptionOnRename) {
                        wand.setDescription("");
                    }
                    wand.updateName(true);
                    if (event.getWhoClicked() instanceof Player && bindingEnabled) {
                        wand.tryToOwn((Player)event.getWhoClicked());
                    }
                }

                // Check for combining a wand with something else
                // Behave as if the item has already been placed
                // This doesn't seem to do anything, I don't think you can set the result slot :\
                /*
                if (event.getSlot() == 0) {
                    firstItem = cursor;
                } else if (event.getSlot() == 1) {
                    secondItem = cursor;
                }
                if (firstItem != null && secondItem != null && (Wand.isWand(firstItem) || Wand.isWand(secondItem))) {
                    if (!combiningEnabled && !bookCombiningEnabled) {
                        anvilInventory.setItem(2, null);
                    } else if (Wand.isWand(firstItem)) {
                        if (!combiningEnabled && Wand.isWand(secondItem)) {
                            anvilInventory.setItem(2, null);
                            return;
                        }
                        Wand wand = controller.getWand(firstItem);
                        if (!wand.isEnchantable() || !wand.getEnchantments().isEmpty()) {
                            anvilInventory.setItem(2, null);
                            return;
                        }
                        ItemMeta meta = secondItem.getItemMeta();
                        if (meta != null && meta instanceof EnchantmentStorageMeta) {
                            EnchantmentStorageMeta enchantmentStorage = (EnchantmentStorageMeta)meta;
                            ItemStack result = InventoryUtils.getCopy(firstItem);
                            Wand resultWand = controller.getWand(result);
                            resultWand.setEnchantments(enchantmentStorage.getStoredEnchants());
                            anvilInventory.setItem(2, result);
                        } else if (!combiningEnabled) {
                            anvilInventory.setItem(2, null);
                        }

                    } else if (Wand.isWand(secondItem)) {
                        if (!combiningEnabled && Wand.isWand(firstItem)) {
                            anvilInventory.setItem(2, null);
                            return;
                        }
                        Wand wand = controller.getWand(secondItem);
                        if (!wand.isEnchantable() || !wand.getEnchantments().isEmpty()) {
                            anvilInventory.setItem(2, null);
                            return;
                        }
                        ItemMeta meta = firstItem.getItemMeta();
                        if (meta != null && meta instanceof EnchantmentStorageMeta) {
                            EnchantmentStorageMeta enchantmentStorage = (EnchantmentStorageMeta)meta;
                            ItemStack result = InventoryUtils.getCopy(secondItem);
                            Wand resultWand = controller.getWand(result);
                            resultWand.setEnchantments(enchantmentStorage.getStoredEnchants());
                            anvilInventory.setItem(2, result);
                        } else if (!combiningEnabled) {
                            anvilInventory.setItem(2, null);
                        }
                    }
                }
                */

                return;
            }

            // Rename wand when taking from result slot
            if (slotType == SlotType.RESULT && Wand.isWand(current)) {
                if (!combiningEnabled && !wandBookCombiningEnabled) {
                    if (firstItem != null && secondItem != null) {
                        event.setCancelled(true);
                        return;
                    }
                }
                ItemMeta meta = current.getItemMeta();
                String newName = meta.getDisplayName();

                Wand wand = controller.getWand(current);
                if (!wand.canUse(player)) {
                    event.setCancelled(true);
                    mage.sendMessage(controller.getMessages().get("wand.bound").replace("$name", wand.getOwner()));
                    return;
                }
                boolean combinedWandAndBook = false;
                if (wandBookCombiningEnabled && secondItem != null) {
                    ItemMeta secondMeta = secondItem.getItemMeta();
                    if (secondMeta != null && secondMeta instanceof EnchantmentStorageMeta) {
                        if (!wand.isEnchantable()) {
                            event.setCancelled(true);
                            return;
                        }
                        EnchantmentStorageMeta enchantmentStorage = (EnchantmentStorageMeta)secondMeta;
                        if (!wand.addEnchantments(enchantmentStorage.getStoredEnchants())) {
                            event.setCancelled(true);
                            return;
                        }

                        combinedWandAndBook = true;
                    }
                }
                if (!combinedWandAndBook && bookCombiningDisabled && secondItem != null) {
                    ItemMeta secondMeta = secondItem.getItemMeta();
                    if (secondMeta != null && secondMeta instanceof EnchantmentStorageMeta) {
                        event.setCancelled(true);
                        return;
                    }

                }
                if (enableRenaming) {
                    wand.setName(newName);
                }
                if (organizingEnabled) {
                    wand.organizeInventory(controller.getMage(player));
                }
                if (bindingEnabled) {
                    wand.tryToOwn(player);
                }
                wand.saveState();
                return;
            }

            if (combiningEnabled && slotType == SlotType.RESULT) {
                // Check for wands in both slots
                // ...... arg. So close.. and yet, not.
                // I guess I need to wait for the long-awaited anvil API?
                if (Wand.isWand(firstItem) && Wand.isWand(secondItem))
                {
                    Wand firstWand = controller.getWand(firstItem);
                    Wand secondWand = controller.getWand(secondItem);
                    if (!firstWand.isModifiable() || !secondWand.isModifiable()) {
                        mage.sendMessage("One of your wands can not be combined");
                        return;
                    }
                    if (!firstWand.canUse(player) || !secondWand.canUse(player)) {
                        mage.sendMessage("One of those wands is not bound to you");
                        return;
                    }

                    if (!firstWand.add(secondWand)) {
                        mage.sendMessage("These wands can not be combined with each other");
                        return;
                    }
                    anvilInventory.setItem(0,  null);
                    anvilInventory.setItem(1,  null);
                    cursor.setType(Material.AIR);

                    if (organizingEnabled) {
                        firstWand.organizeInventory(mage);
                    }
                    if (bindingEnabled) {
                        firstWand.tryToOwn(player);
                    }
                    player.getInventory().addItem(firstWand.getItem());
                    mage.sendMessage("Your wands have been combined!");

                    // This seems to work in the debugger, but.. doesn't do anything.
                    // InventoryUtils.setInventoryResults(anvilInventory, newWand.getItem());
                } else if (organizingEnabled && Wand.isWand(firstItem)) {
                    Wand firstWand = controller.getWand(firstItem);
                    // TODO: Can't get the anvil's text from here.
                    anvilInventory.setItem(0,  null);
                    anvilInventory.setItem(1,  null);
                    cursor.setType(Material.AIR);
                    firstWand.organizeInventory(mage);
                    if (bindingEnabled) {
                        firstWand.tryToOwn(player);
                    }
                    player.getInventory().addItem(firstWand.getItem());
                    mage.sendMessage("Your wand has been organized!");
                }

                return;
            }
        }
    }

    public boolean isCombiningEnabled()
    {
        return combiningEnabled;
    }

    public boolean isOrganizingEnabled()
    {
        return organizingEnabled;
    }
}
