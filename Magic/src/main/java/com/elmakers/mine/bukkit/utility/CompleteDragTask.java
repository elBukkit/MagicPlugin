package com.elmakers.mine.bukkit.utility;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;

public class CompleteDragTask extends BukkitRunnable {
    private final Player player;
    private final int itemSlot;
    private final InventoryView view;

    public CompleteDragTask(Player player, InventoryView view, int slot) {
        this.player = player;
        this.itemSlot = slot;
        this.view = view;
    }

    @Override
    public void run() {
        ItemStack heldItem = player.getItemOnCursor();
        view.setItem(itemSlot, heldItem);
        player.setItemOnCursor(null);
        DeprecatedUtils.updateInventory(player);
    }
}
