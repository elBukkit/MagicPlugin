package com.elmakers.mine.bukkit.magic.listener;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.wand.Wand;
import com.elmakers.mine.bukkit.wand.WandUpgradePath;

public class EnchantingController implements Listener {
    private final MagicController controller;
    private boolean enchantingEnabled = false;

    public EnchantingController(MagicController controller) {
        this.controller = controller;
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        Wand wand = controller.getIfWand(event.getItem());
        if (wand == null) return;
        if (wand.isEnchantable()) {
            Player player = event.getEnchanter();
            if (player == null || !controller.hasPermission(player, "Magic.wand.enchant_vanilla")) {
                event.setCancelled(true);
                return;
            }
        } else if (enchantingEnabled) {
            Player player = event.getEnchanter();
            if (player == null || !controller.hasPermission(player, "Magic.wand.enchant")) {
                event.setCancelled(true);
                return;
            }

            event.getEnchantsToAdd().clear();
            int level = event.getExpLevelCost();
            if (wand.enchant(level, controller.getMage(event.getEnchanter())) <= 0) {
                event.setCancelled(true);
            } else {
                event.setCancelled(false);

                // This is necessary due to a special-case check Bukkit added in
                // https://github.com/Bukkit/CraftBukkit/commit/ac1a2d0233eff169efcc7c807cbf799b57bf2306
                // This will skip deducting XP costs (!!) if you don't return something to add to the item
                // Unfortunately, adding an enchant to the item is going to blow away its data, soooo...
                //
                // This is kind of an "FU" to this particular commit, in that it will trigger an NPE
                // in NMS code that will get silently eaten, but avoid modifying the resultant ItemStack.
                // :P
                event.getEnchantsToAdd().put(null, 0);
            }
            wand.makeEnchantable(true);
        }
    }

    @EventHandler
    public void onPrepareEnchantItem(PrepareItemEnchantEvent event) {
        Wand wand = controller.getIfWand(event.getItem());
        if (wand == null) return;

        // In this context we do not want to do anything special for enchantable wands
        // The non-enchantable ones might go through the old enchanting progression system though.
        if (wand.isEnchantable()) {
            Player player = event.getEnchanter();
            if (player == null || !controller.hasPermission(player, "Magic.wand.enchant_vanilla")) {
                event.setCancelled(true);
                return;
            }
        } else {
            if (!enchantingEnabled) {
                event.setCancelled(true);
                return;
            }
            Player player = event.getEnchanter();
            if (player == null || !controller.hasPermission(player, "Magic.wand.enchant")) {
                event.setCancelled(true);
                return;
            }
            if (!wand.isModifiable() && wand.getPath() == null) {
                event.setCancelled(true);
                return;
            }

            if (controller.isSPEnabled() && wand.hasSpellProgression()) {
                event.setCancelled(true);
                return;
            }

            if (!wand.canUse(player)) {
                event.setCancelled(true);
                return;
            }
            wand.makeEnchantable(true);
            WandUpgradePath path = wand.getPath();
            if (path == null) {
                event.setCancelled(true);
                return;
            }
            int minLevel = path.getMinLevel();
            int maxLevel = path.getMaxLevel();
            int levelRange = maxLevel - minLevel;
            int[] offered = DeprecatedUtils.getExpLevelCostsOffered(event);

            float bonusLevelMultiplier = path.getBonusLevelMultiplier();
            int bonusLevels = event.getEnchantmentBonus();

            for (int i = 0; i < offered.length; i++)
            {
                int level = minLevel + (int)((float)i * levelRange / offered.length);
                if (bonusLevels > 0 && bonusLevelMultiplier > 0)
                {
                    level = (int)(level + bonusLevels * bonusLevelMultiplier);
                }
                offered[i] = level;
            }
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        InventoryType inventoryType = event.getInventory().getType();
        if (inventoryType != InventoryType.ENCHANTING) return;

        SlotType slotType = event.getSlotType();
        ItemStack current = event.getCurrentItem();
        Wand currentWand = controller.getIfWand(current);
        if (currentWand != null && currentWand.isEnchantable()) {
            // When taking the wand out, reconfigured the wand's properties with the new item
            // enchantments.
            currentWand.setEnchantments(current.getItemMeta().getEnchants());
            return;
        }

        if (enchantingEnabled)
        {
            if (slotType == SlotType.CRAFTING) {
                HumanEntity clicker = event.getWhoClicked();
                Player player = clicker instanceof Player ? (Player)clicker : null;
                if (player == null || !controller.hasPermission(player, "Magic.wand.enchant")) {
                    return;
                }

                // Make wands into an enchantable item when placing
                ItemStack cursor = event.getCursor();
                Wand cursorWand = controller.getIfWand(cursor);
                if (cursorWand != null && cursorWand.isModifiable()) {
                    cursorWand.makeEnchantable(true);
                }
                // And turn them back when taking out
                if (currentWand != null && currentWand.isModifiable()) {
                    currentWand.makeEnchantable(false);
                }
            }
        }
    }

    public boolean isEnabled()
    {
        return enchantingEnabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enchantingEnabled = enabled;
    }
}
