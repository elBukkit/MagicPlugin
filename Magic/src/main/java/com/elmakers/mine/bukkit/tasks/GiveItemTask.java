package com.elmakers.mine.bukkit.tasks;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveItemTask implements Runnable {
    private final Player player;
    private final ItemStack item;
    private final boolean offhand;

    public GiveItemTask(Player player, ItemStack item, boolean offhand) {
        this.player = player;
        this.item = item;
        this.offhand = offhand;
    }

    @Override
    public void run() {
        if (offhand) {
            player.getInventory().setItemInOffHand(item);
        } else {
            player.getInventory().setItemInMainHand(item);
        }
    }
}
