package com.elmakers.mine.bukkit.tasks;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.wand.Wand;

public class RestoreSpellIconTask implements Runnable {
    private final Player player;
    private final int slot;
    private final ItemStack icon;
    private final Wand wand;

    public RestoreSpellIconTask(Player player, int slot, ItemStack icon, Wand wand) {
        this.player = player;
        this.slot = slot;
        this.icon = icon;
        this.wand = wand;
    }

    @Override
    public void run() {
        if (wand.isInventoryOpen()) {
            player.getInventory().setItem(slot, icon);
        }
    }
}
